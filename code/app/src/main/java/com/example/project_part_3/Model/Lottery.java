package com.example.project_part_3.Model;

import android.util.ArraySet;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

public class Lottery {

    public static class LotteryResult {
        public final List<String> winners;
        public final List<String> alternates;

        public LotteryResult(List<String> winners, List<String> alternates) {
            this.winners = winners;
            this.alternates = alternates;
        }
    }
    // Pass in all entrants by their ID's (this probably wont be string but ill leave it as that for now)
    // Also the ineligibleUserid's. These are people who clicked decline. This is kinda a design decision we can talk
    // ineligibleUserid's can be derived before each call to LotteryResult based on what we defined as ineligibility criteria
    // About later but the alternative would be to just remove people who clicked decline/those who have already accepted
    // from the allEntrantIds before Passing it in. Im going on the assumption that we dont do that.
    // This assumes our lottery is "dumb" and doesn't know about events or firebase or anything.
    // This is good in terms of "separation of concerns".
    public LotteryResult runLottery(
            List<String> allEntrantIds,
            int capacityRemaining,
            List<String> ineligibleUserIds,
            Long seedOrNull
    ) {
        // Dont crash in this case. I could probably throw an exception instead but ill get back to this
        if (allEntrantIds == null) {
            allEntrantIds = new ArrayList<>();
        }
        if (ineligibleUserIds == null) {
            ineligibleUserIds = new ArrayList<String>();
        }

        // Filter eligible entrants as (allEntrantIds - ineligibleUserIds)
        List<String> eligiblePool = new ArrayList<>();
        for (String id : allEntrantIds) {
            if (!ineligibleUserIds.contains(id)) {
                eligiblePool.add(id);
            }
        }

        // Shuffle with a seed, either passed by user (if we want to test) else we make one
        Random rng = new Random();
        if (seedOrNull != null)
        {
            rng = new Random(seedOrNull);
        }

        // This Collections method takes in an ArrayList and will randomly permutate it. This is random,
        // so if we select the first (capacity) members from the shuffled eligiblePool as those selected,
        // This is a valid lottery.
        Collections.shuffle(eligiblePool, rng);

        // Split into winners and alternates
        List<String> winners = new ArrayList<>();
        List<String> alternates = new ArrayList<>();

        for (int i = 0; i < eligiblePool.size(); i++) {
            String userId = eligiblePool.get(i);

            if (i < capacityRemaining) {
                winners.add(userId);
            } else {
                alternates.add(userId);
            }
        }

        return new LotteryResult(winners, alternates);
    }

    // alternatesInOrder must be stored per event. It is whatever the last returned value of alternates from
    // the runLottery function was for a given event. That way we only need one lottery, and we can just select alternates
    // based on their position in the alternatesInOrder list (which has a random ordering)
    public String drawNextAlternate(
            List<String> alternatesInOrder,
            List<String> nowIneligible
    ) {
        if (alternatesInOrder == null) {
            return null;
        }
        if (nowIneligible == null) {
            nowIneligible = new ArrayList<String>();
        }

        for (String candidate : alternatesInOrder) {
            if (!nowIneligible.contains(candidate)) {
                return candidate;
            }
        }

        return null; // No backups left
    }
}
