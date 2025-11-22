package com.example.project_part_3;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;

import com.example.project_part_3.Model.Lottery;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;

/**
 * Unit tests for the {@link Lottery} class.
 * These tests verify the correctness of the lottery selection logic
 */
public class LotteryTest {

    private Lottery lottery;
    private List<String> entrants;
    private Long testSeed = 12345L; // Use a fixed seed for predictable results

    /**
     * Initializes a new Lottery instance and a simulated list of entrants.
     */
    @Before
    public void setUp() {
        lottery = new Lottery();
        entrants = new ArrayList<>(Arrays.asList("user1", "user2", "user3", "user4", "user5"));
    }

    /**
     * Tests a basic lottery draw where the capacity is greater than the number of entrants.
     * All eligible entrants should become winners.
     */
    @Test
    public void testRunLottery_WithSufficientCapacity() {
        Lottery.LotteryResult result = lottery.runLottery(entrants, 10, null, testSeed);

        assertEquals("There should be 5 winners", 5, result.winners.size());
        assertThat("Winners list should contain all entrants", result.winners,
                hasItems("user1", "user2", "user3", "user4", "user5"));
        assertTrue("Alternates list should be empty", result.alternates.isEmpty());
    }

    /**
     * Tests a lottery draw where capacity is limited.
     * The draw should produce the correct number of winners and alternates.
     */
    @Test
    public void testRunLottery_WithLimitedCapacity() {
        Lottery.LotteryResult result = lottery.runLottery(entrants, 3, null, testSeed);

        assertEquals("Should be exactly 3 winners", 3, result.winners.size());
        assertEquals("Should be exactly 2 alternates", 2, result.alternates.size());
    }

    /**
     * Tests that ineligible users are correctly excluded from the lottery draw.
     */
    @Test
    public void testRunLottery_WithIneligibleUsers() {
        List<String> ineligible = Arrays.asList("user2", "user4");
        Lottery.LotteryResult result = lottery.runLottery(entrants, 2, ineligible, testSeed);

        // Eligible pool is user1, user3, user5
        assertEquals("Should be 2 winners from the eligible pool", 2, result.winners.size());
        assertEquals("Should be 1 alternate from the eligible pool", 1, result.alternates.size());

        // Verify that no ineligible user is in winners or alternates
        assertFalse("Ineligible user 'user2' should not be a winner", result.winners.contains("user2"));
        assertFalse("Ineligible user 'user4' should not be a winner", result.winners.contains("user4"));
        assertFalse("Ineligible user 'user2' should not be an alternate", result.alternates.contains("user2"));
        assertFalse("Ineligible user 'user4' should not be an alternate", result.alternates.contains("user4"));
    }

    /**
     * Tests the lottery draw when there is zero remaining capacity.
     * All eligible entrants should be placed on the alternates list.
     */
    @Test
    public void testRunLottery_WithZeroCapacity() {
        Lottery.LotteryResult result = lottery.runLottery(entrants, 0, null, testSeed);

        assertTrue("Winners list should be empty", result.winners.isEmpty());
        assertEquals("All 5 entrants should be alternates", 5, result.alternates.size());
        assertThat("Alternates list should contain all entrants", result.alternates,
                hasItems("user1", "user2", "user3", "user4", "user5"));
    }

    /**
     * Verifies that two lottery draws with the same seed produce the exact same result.
     * This ensures the lottery is deterministic and reproducible.
     */
    @Test
    public void testRunLottery_IsDeterministicWithSeed() {
        // Run lottery first time
        Lottery.LotteryResult result1 = lottery.runLottery(entrants, 3, null, testSeed);
        // Run lottery second time with the same parameters and seed
        Lottery.LotteryResult result2 = lottery.runLottery(entrants, 3, null, testSeed);

        assertEquals("Winner lists should be identical for the same seed", result1.winners, result2.winners);
        assertEquals("Alternate lists should be identical for the same seed", result1.alternates, result2.alternates);
    }

    /**
     * Tests the lottery with a null entrants list to ensure it doesn't crash.
     * The result should be empty lists.
     */
    @Test
    public void testRunLottery_WithNullEntrants() {
        Lottery.LotteryResult result = lottery.runLottery(null, 5, null, testSeed);
        assertNotNull("Result should not be null", result);
        assertTrue("Winners list should be empty for null entrants", result.winners.isEmpty());
        assertTrue("Alternates list should be empty for null entrants", result.alternates.isEmpty());
    }

    /**
     * Tests the drawNextAlternate method to ensure it selects the first eligible alternate.
     */
    @Test
    public void testDrawNextAlternate_SelectsFirstAvailable() {
        List<String> alternates = Arrays.asList("alt1", "alt2", "alt3");
        List<String> ineligible = Arrays.asList("alt1"); // First alternate is now ineligible

        String nextAlternate = lottery.drawNextAlternate(alternates, ineligible);

        assertEquals("Should select 'alt2' as the next available alternate", "alt2", nextAlternate);
    }

    /**
     * Tests the drawNextAlternate method when all alternates are ineligible.
     * It should return null.
     */
    @Test
    public void testDrawNextAlternate_WhenAllAreIneligible() {
        List<String> alternates = Arrays.asList("alt1", "alt2", "alt3");
        List<String> ineligible = Arrays.asList("alt1", "alt2", "alt3");

        String nextAlternate = lottery.drawNextAlternate(alternates, ineligible);

        assertNull("Should return null when no eligible alternates are left", nextAlternate);
    }

    /**
     * Tests the drawNextAlternate method with an empty alternates list.
     * It should return null.
     */
    @Test
    public void testDrawNextAlternate_WithEmptyList() {
        String nextAlternate = lottery.drawNextAlternate(new ArrayList<>(), new ArrayList<>());
        assertNull("Should return null for an empty alternates list", nextAlternate);
    }
}
