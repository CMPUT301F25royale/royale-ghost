const functions = require("firebase-functions");
const admin = require("firebase-admin");
const { onSchedule } = require("firebase-functions/v2/scheduler");

admin.initializeApp();
const db = admin.firestore();

/** ----------------- Lottery helper ----------------- */
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

/** ----------------- HTTP trigger to run now ----------------- */
exports.runEventLotteriesNow = functions.https.onRequest(async (req, res) => {
  try {
    const processed = await runAllEligibleLotteries();
    res.status(200).send({ processed });
  } catch (e) {
    console.error(e);
    res.status(500).send((e && e.message) || "error");
  }
});

/** ----------------- Scheduled every 5 minutes ----------------- */
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
    ...snapNotDone.docs.map((d) => d.ref),
    ...snapLegacy.docs.map((d) => d.ref),
  ];
  const uniqueRefs = Array.from(new Set(refs.map((r) => r.path))).map((p) => db.doc(p));

  if (!uniqueRefs.length) return 0;

  // 1) Run each lottery transactionally -> return outcome payloads
  const outcomes = await Promise.all(uniqueRefs.map((ref) => processEventLottery(ref)));

  // 2) Send notifications *after* the writes commit
  await Promise.all(
    outcomes
      .filter(Boolean)
      .map((o) => notifyLotteryResults(o.eventRef, o.winners, o.losers, o.eventData)),
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

    const capacity = (typeof data.capacity === "number") ? data.capacity : 0;
    const confirmed = Array.isArray(data.confirmedUserIds) ? data.confirmedUserIds : [];
    const declined = Array.isArray(data.declinedUserIds) ? data.declinedUserIds : [];
    const waitlist = Array.isArray(data.waitlistUserIds) ? data.waitlistUserIds : [];
    const seed = (typeof data.seed === "number") ? data.seed : null;

    const remaining = Math.max(capacity - confirmed.length, 0);

    if (remaining <= 0 || waitlist.length === 0) {
      tx.update(eventRef, {
        lotteryDone: true,
        lastLotteryTs: admin.firestore.FieldValue.serverTimestamp(),
        selectedUserIds: [],
        alternatesUserIds: [],
      });
      outcome = {
        eventRef,
        winners: [],
        losers: [],
        eventData: { id: data.id, title: data.title },
      };
      return;
    }

    const ineligibleIds = [...new Set([...confirmed, ...declined])];
    const { winners, alternates } = runLottery(waitlist, remaining, ineligibleIds, seed);

    // Remove winners from waitlist (optional cleanup)
    const newWaitlist = waitlist.filter((uid) => !winners.includes(uid));

    tx.update(eventRef, {
      lotteryDone: true,
      lastLotteryTs: admin.firestore.FieldValue.serverTimestamp(),
      selectedUserIds: winners,
      alternatesUserIds: alternates,
      waitlistUserIds: newWaitlist,
    });

    // losers = everyone not selected, deduped
    const losers = Array.from(new Set([...newWaitlist, ...alternates]));

    outcome = {
      eventRef,
      winners,
      losers,
      eventData: { id: data.id, title: data.title },
    };
  });

  return outcome;
}

/** ----------------- Notification helpers (outside transaction) ----------------- */
async function notifyLotteryResults(eventRef, winners, losers, eventData) {
  // Avoid duplicate notifications if we rerun quickly
  const snap = await eventRef.get();
  const data = snap.data() || {};
  if (data.winnersNotifiedAt && data.nonWinnersNotifiedAt) return;

  const eventTitle = (eventData && eventData.title) ? eventData.title : "Event";
  const eventId = (eventData && eventData.id) ? eventData.id : "";

  const title = "Results: " + eventTitle; // This is fine (concatenation)

  // CHANGE THESE TO BACKTICKS (`):
  const bodyWin = `You were selected for ${eventTitle}! Open the event to confirm your spot.`;
  const bodyLose = `You weren’t selected for ${eventTitle} this time. We’ll notify you if a spot opens.`;

  // ---------- FCM PUSH (respect receiveNotifications + dedupe tokens) ----------
  let winnerTokens = await tokensForUserIds(winners);
  let loserTokens = await tokensForUserIds(losers);

  const winnerSet = new Set(winnerTokens);
  const loserSet = new Set(loserTokens);

  winnerTokens = Array.from(winnerSet);
  loserTokens = Array.from([...loserSet].filter((t) => !winnerSet.has(t)));

  await sendMulticast(winnerTokens, {
    title,
    body: bodyWin,
    data: { eventId, action: "lottery_won" },
  });

  await sendMulticast(loserTokens, {
    title,
    body: bodyLose,
    data: { eventId, action: "lottery_not_won" },
  });

  // ---------- IN-APP INBOX NOTIFICATIONS ----------
  await createInboxNotifications(winners, {
    title,
    message: bodyWin,
    eventId,
    eventTitle,
    type: "lottery_won",
  });

  await createInboxNotifications(losers, {
    title,
    message: bodyLose,
    eventId,
    eventTitle,
    type: "lottery_not_won",
  });

  // Mark event as notified
  await eventRef.update({
    winnersNotifiedAt: admin.firestore.FieldValue.serverTimestamp(),
    nonWinnersNotifiedAt: admin.firestore.FieldValue.serverTimestamp(),
  });
}

/**
 * Given a list of user emails, return unique FCM tokens
 * for users who have NOT opted out of notifications.
 */
/**
 * Given a list of user emails, return unique FCM tokens
 * for users who have NOT opted out of notifications.
 */
async function tokensForUserIds(userEmails) {
  if (!userEmails || !userEmails.length) return [];

  const uniqueEmails = Array.from(new Set(userEmails));

  const snaps = await Promise.all(
    uniqueEmails.map((email) =>
      db.collection("users").doc(email).get()
    )
  );

  const tokenSet = new Set();

  snaps.forEach((snap, index) => {
    if (!snap.exists) return;

    const data = snap.data() || {};
    const email = uniqueEmails[index];
    const receive = data.receiveNotifications;
    functions.logger.log(`tokensForUserIds: ${email} receiveNotifications=`, receive);
    functions.logger.log(`receive === false: ${receive === false}`);
    // Only skip if explicitly false
    if (receive === false) {
      return; // do not add tokens for this user
    }

    const tokens = data.fcmTokens || [];
    tokens.forEach((t) => tokenSet.add(t));
  });

  return Array.from(tokenSet);
}

/**
 * Create inbox notification docs for a list of user emails.
 * Writes to: users/{email}/notifications/{autoId}
 */
/**
 * Create inbox notification docs for a list of user emails.
 * RESPECTS receiveNotifications setting and uses Batches.
 */
async function createInboxNotifications(userEmails, payload) {
  if (!userEmails || !userEmails.length) return;

  const now = admin.firestore.FieldValue.serverTimestamp();

  // 1. Chunk the users into groups of 500 (Firestore Batch Limit)
  const CHUNK_SIZE = 500;
  const chunks = [];
  for (let i = 0; i < userEmails.length; i += CHUNK_SIZE) {
    chunks.push(userEmails.slice(i, i + CHUNK_SIZE));
  }

  // 2. Process each chunk
  await Promise.all(chunks.map(async (chunkEmails) => {
    const batch = db.batch();

    // Fetch all user docs in this chunk to check settings
    const userRefs = chunkEmails.map(email => db.collection("users").doc(email));
    const userSnaps = await db.getAll(...userRefs);

    let opCount = 0;

    userSnaps.forEach((snap) => {
      if (!snap.exists) return;

      const data = snap.data() || {};
      let receive = data.receiveNotifications;

      // Handle String vs Boolean edge case
      if (receive === "false") receive = false;

      // SKIP if user opted out
      if (receive === false) {
        functions.logger.info(`Skipping Inbox Notification for ${snap.id} (Opt-out)`);
        return;
      }

      // Add to batch
      const notifRef = snap.ref.collection("notifications").doc();
      batch.set(notifRef, {
        ...payload,
        userEmail: snap.id,
        createdAt: now,
        read: false,
      });
      opCount++;
    });

    // Commit only if there are writes in this batch
    if (opCount > 0) {
      await batch.commit();
    }
  }));
}

/** Send in batches of 500 tokens */
async function sendMulticast(tokens, { title, body, data }) {
  if (!tokens || !tokens.length) return;

  const groups = [];
  for (let i = 0; i < tokens.length; i += 500) {
    groups.push(tokens.slice(i, i + 500));
  }

  await Promise.all(
    groups.map((group) =>
      admin.messaging().sendEachForMulticast({
        tokens: group,
        notification: { title, body },
        data,
        android: { notification: { channelId: "lottery", priority: "HIGH" } },
        apns: { payload: { aps: { sound: "default" } } },
      }),
    ),
  );
}
