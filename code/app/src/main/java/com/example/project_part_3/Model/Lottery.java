package com.example.project_part_3.Model;

import android.util.ArraySet;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * Class to perform a lottery selection for events. It takes a list of entrants,
 * filters out ineligible participants, and randomly selects winners via shuffle up to a specified capacity.
 * The remaining eligible entrants are designated as alternates.
 */
public class Lottery {

    /**
     * A data class to hold the results of a lottery draw.
     */
    public static class LotteryResult {
        public final List<String> winners;
        public final List<String> alternates;

        /**
         * Constructs a new LotteryResult.
         *
         * @param winners    A list of user IDs for the winners.
         * @param alternates A list of user IDs for the alternates.
         */
        public LotteryResult(List<String> winners, List<String> alternates) {
            this.winners = winners;
            this.alternates = alternates;
        }
    }
    /**
     * Runs a lottery to select winners and alternates from a list of entrants.
     * The method first filters out ineligible users, then shuffles the remaining pool
     * to ensure that selection is random, and finally splits them into winners and alternates based on the available capacity.
     *
     * @param allEntrantIds     A list of user IDs for everyone who entered the lottery.
     * @param capacityRemaining The number of available spots for winners.
     * @param ineligibleUserIds A list of user IDs who are not eligible to win, such as those who declined
     * @param seedOrNull        A seed to allow for reproducible results.
     *                          If null, a new random seed is used.
     * @return A {@link LotteryResult} object containing the lists of winners and alternates.
     */
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

    /**
     * Selects the next available alternate from a previously generated list.
     * It iterates through the ordered list of alternates and returns the first one
     * who is not currently in the list of ineligible users so that full reshuffle not needed.
     *
     * @param alternatesInOrder The list of alternates in the order they were determined by first lottery run
     * @param nowIneligible     A list of user IDs who have become ineligible since the last draw
     * @return The user ID of the next eligible alternate, or null if no eligible alternates are left.
     */
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
