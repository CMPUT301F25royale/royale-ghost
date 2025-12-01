package com.example.project_part_3;

import static org.junit.Assert.*;

import com.example.project_part_3.Model.Lottery;

import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class LotteryAlternateSelectionTest {

    @Test
    public void drawNextAlternate_returnsFirstNonIneligible() {
        Lottery lottery = new Lottery();

        List<String> alternates = Arrays.asList("userA", "userB", "userC");
        List<String> nowIneligible = Collections.singletonList("userA");

        String result = lottery.drawNextAlternate(alternates, nowIneligible);

        assertEquals("userB", result);
    }

    @Test
    public void drawNextAlternate_skipsMultipleIneligible() {
        Lottery lottery = new Lottery();

        List<String> alternates = Arrays.asList("userA", "userB", "userC", "userD");
        List<String> nowIneligible = Arrays.asList("userA", "userB", "userC");

        String result = lottery.drawNextAlternate(alternates, nowIneligible);

        assertEquals("userD", result);
    }

    @Test
    public void drawNextAlternate_returnsNullWhenAllIneligible() {
        Lottery lottery = new Lottery();

        List<String> alternates = Arrays.asList("userA", "userB");
        List<String> nowIneligible = Arrays.asList("userA", "userB");

        String result = lottery.drawNextAlternate(alternates, nowIneligible);

        assertNull(result);
    }

    @Test
    public void drawNextAlternate_handlesNullAlternates() {
        Lottery lottery = new Lottery();

        String result = lottery.drawNextAlternate(null, Collections.emptyList());

        assertNull(result);
    }

    @Test
    public void drawNextAlternate_handlesNullIneligibleList() {
        Lottery lottery = new Lottery();

        List<String> alternates = Arrays.asList("userX", "userY");
        String result = lottery.drawNextAlternate(alternates, null);

        assertEquals("userX", result);
    }
}
