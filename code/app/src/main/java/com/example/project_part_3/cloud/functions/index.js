const functions = require("firebase-functions");
const admin = require("firebase-admin");
const { onSchedule } = require("firebase-functions/v2/scheduler");

admin.initializeApp();
const db = admin.firestore();

/** Seeded lottery helper (unchanged) */
function runLottery(allEntrantIds, capacityRemaining, ineligibleUserIds, seedOrNull) {
  allEntrantIds = allEntrantIds || [];
  ineligibleUserIds = ineligibleUserIds || [];
  const ineligible = new Set(ineligibleUserIds);

  const eligiblePool = allEntrantIds.filter((id) => !ineligible.has(id));

  let rng;
  if (seedOrNull !== undefined && seedOrNull !== null) {
    let seed = seedOrNull >>> 0;
    rng = () => {
      seed = (seed * 1664525 + 1013904223) >>> 0;
      return seed / 4294967296;
    };
  } else {
    rng = Math.random;
  }

  for (let i = eligiblePool.length - 1; i > 0; i--) {
    const j = Math.floor(rng() * (i + 1));
    [eligiblePool[i], eligiblePool[j]] = [eligiblePool[j], eligiblePool[i]];
  }

  const winners = eligiblePool.slice(0, Math.max(capacityRemaining, 0));
  const alternates = eligiblePool.slice(Math.max(capacityRemaining, 0));
  return { winners, alternates };
}

/** ---------- HTTP trigger to run now ---------- */
exports.runEventLotteriesNow = functions.https.onRequest(async (req, res) => {
  try {
    const processed = await runAllEligibleLotteries();
    res.status(200).send({ processed });
  } catch (e) {
    console.error(e);
    res.status(500).send(e?.message || "error");
  }
});

/** ---------- Scheduled every 5 minutes ---------- */
exports.runEventLotteries = onSchedule("every 5 minutes", async () => {
  await runAllEligibleLotteries();
  return null;
});

/** Find closed + not-done (or missing) lotteries via collectionGroup and process them */
async function runAllEligibleLotteries() {
  const now = admin.firestore.Timestamp.now();

  const qNotDone = db.collectionGroup("organized_events")
    .where("date_close", "<=", now)
    .where("lotteryDone", "==", false);

  const qLegacyMissing = db.collectionGroup("organized_events")
    .where("date_close", "<=", now)
    .where("lotteryDone", "==", null);

  const [snapNotDone, snapLegacy] = await Promise.all([qNotDone.get(), qLegacyMissing.get()]);

  const refs = [
    ...snapNotDone.docs.map(d => d.ref),
    ...snapLegacy.docs.map(d => d.ref),
  ];
  const uniqueRefs = Array.from(new Set(refs.map(r => r.path))).map(p => db.doc(p));

  if (!uniqueRefs.length) return 0;

  // 1) Run each lottery transactionally -> return outcome payloads
  const outcomes = await Promise.all(uniqueRefs.map(ref => processEventLottery(ref)));

  // 2) Send notifications *after* the writes commit
  await Promise.all(
    outcomes
      .filter(Boolean)
      .map(o => notifyLotteryResults(o.eventRef, o.winners, o.losers, o.eventData))
  );

  return outcomes.filter(Boolean).length;
}

/**
 * Transaction: compute winners/losers and write results.
 * Returns a small payload so the caller can notify outside the transaction.
 */
async function processEventLottery(eventRef) {
  let outcome = null;

  await db.runTransaction(async (tx) => {
    const snap = await tx.get(eventRef);
    if (!snap.exists) return;

    const data = snap.data();
    // Already done? skip.
    if (data.lotteryDone === true) return;

    const capacity  = (typeof data.capacity === "number") ? data.capacity : 0;
    const confirmed = Array.isArray(data.confirmedUserIds) ? data.confirmedUserIds : [];
    const declined  = Array.isArray(data.declinedUserIds)  ? data.declinedUserIds  : [];
    const waitlist  = Array.isArray(data.waitlistUserIds)  ? data.waitlistUserIds  : [];
    const seed      = (typeof data.seed === "number") ? data.seed : null;

    const remaining = Math.max(capacity - confirmed.length, 0);

    if (remaining <= 0 || waitlist.length === 0) {
      tx.update(eventRef, {
        lotteryDone: true,
        lastLotteryTs: admin.firestore.FieldValue.serverTimestamp(),
        selectedUserIds: [],
        alternatesUserIds: [],
      });
      outcome = { eventRef, winners: [], losers: [], eventData: { id: data.id, title: data.title } };
      return;
    }

    const ineligibleIds = [...new Set([...confirmed, ...declined])];
    const { winners, alternates } = runLottery(waitlist, remaining, ineligibleIds, seed);

    // Remove winners from waitlist (optional cleanup)
    const newWaitlist = waitlist.filter(uid => !winners.includes(uid));

    tx.update(eventRef, {
      lotteryDone: true,
      lastLotteryTs: admin.firestore.FieldValue.serverTimestamp(),
      selectedUserIds: winners,
      alternatesUserIds: alternates,
      waitlistUserIds: newWaitlist,
    });

    // losers = everyone still in waitlist + alternates (not selected this round)
    const losers = [...newWaitlist, ...alternates];

    outcome = { eventRef, winners, losers, eventData: { id: data.id, title: data.title } };
  });

  return outcome;
}

/** ---- Notification helpers (run OUTSIDE the transaction) ---- */
/** ---- Notification helpers (run OUTSIDE the transaction) ---- */
async function notifyLotteryResults(eventRef, winners, losers, eventData) {
  // Avoid duplicate sends if we rerun quickly
  const snap = await eventRef.get();
  const data = snap.data() || {};
  if (data.winnersNotifiedAt && data.nonWinnersNotifiedAt) return;

  let winnerTokens = await tokensForUserIds(winners);
  let loserTokens  = await tokensForUserIds(losers);

  // Deduplicate across groups: no token should get both win + lose
  const winnerSet = new Set(winnerTokens);
  const loserSet  = new Set(loserTokens);

  winnerTokens = Array.from(winnerSet);
  loserTokens  = Array.from([...loserSet].filter(t => !winnerSet.has(t)));

  const title = `Results: ${eventData?.title || "Event"}`;
  const bodyWin  = `You were selected! Open the app to confirm your spot.`;
  const bodyLose = `You weren’t selected this time. We’ll notify you if a spot opens.`;

  const eventId = eventData?.id || "";
  const eventTitle = eventData?.title || "";

  // 1) Send push notifications
  await sendMulticast(winnerTokens, {
    title, body: bodyWin,
    data: { eventId, action: "lottery_won" }
  });
  await sendMulticast(loserTokens, {
    title, body: bodyLose,
    data: { eventId, action: "lottery_not_won" }
  });

  // 2) Write inbox notifications in Firestore for winners & losers
  await createInboxNotifications(winners, {
    title,
    message: bodyWin,
    eventId,
    eventTitle,
    type: "lottery_won"
  });

  await createInboxNotifications(losers, {
    title,
    message: bodyLose,
    eventId,
    eventTitle,
    type: "lottery_not_won"
  });

  // 3) Mark event as notified
  await eventRef.update({
    winnersNotifiedAt: admin.firestore.FieldValue.serverTimestamp(),
    nonWinnersNotifiedAt: admin.firestore.FieldValue.serverTimestamp(),
  });
}



/** Firestore `in` supports up to 10 values; chunk queries and merge tokens. */
async function tokensForUserIds(userIds) {
  if (!userIds?.length) return [];

  const chunks = [];
  for (let i = 0; i < userIds.length; i += 10) {
    chunks.push(userIds.slice(i, i + 10));
  }

  const snaps = await Promise.all(
    chunks.map(chunk =>
      db.collection("users")
        .where("userID", "in", chunk)
        .get()
    )
  );

  const tokensSet = new Set();

  snaps.forEach(snap => {
    snap.forEach(doc => {
      const arr = doc.get("fcmTokens") || [];
      arr.forEach(t => tokensSet.add(t));
    });
  });

  return Array.from(tokensSet);
}


async function createInboxNotifications(userEmails, payload) {
  if (!userEmails || !userEmails.length) return;

  const batch = db.batch();
  const now = admin.firestore.FieldValue.serverTimestamp();

  userEmails.forEach((email) => {
    const userRef = db.collection("users").doc(email);
    const notifRef = userRef.collection("notifications").doc();
    batch.set(notifRef, {
      ...payload,
      userEmail: email,
      createdAt: now,
      read: false,
    });
  });

  await batch.commit();
}


/** Send in batches of 500 tokens */
async function sendMulticast(tokens, { title, body, data }) {
  if (!tokens.length) return;
  const groups = [];
  for (let i = 0; i < tokens.length; i += 500) {
    groups.push(tokens.slice(i, i + 500));
  }
  await Promise.all(
    groups.map(group =>
      admin.messaging().sendEachForMulticast({
        tokens: group,
        notification: { title, body },
        data,
        android: { notification: { channelId: "lottery", priority: "HIGH" } },
        apns: { payload: { aps: { sound: "default" } } }
      })
    )
  );
}
