package com.example.project_part_3;

import static org.junit.Assert.*;

import com.example.project_part_3.Events.Event;

import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;

public class EventCapacityLogicTest {

    @Test
    public void remainingCapacity_whenNoConfirmed_equalsCapacity() {
        Event e = new Event();
        e.setCapacity(10);
        e.setConfirmedUserIds(Collections.emptyList());

        int remaining = e.getRemainingCapacity();

        assertEquals(10, remaining);
        assertFalse(e.isFull());
    }

    @Test
    public void remainingCapacity_whenPartiallyFull_isCapacityMinusConfirmed() {
        Event e = new Event();
        e.setCapacity(10);
        e.setConfirmedUserIds(Arrays.asList("a@example.com", "b@example.com", "c@example.com"));

        int remaining = e.getRemainingCapacity();

        assertEquals(7, remaining);
        assertFalse(e.isFull());
    }

    @Test
    public void remainingCapacity_neverNegative_evenIfConfirmedExceedsCapacity() {
        Event e = new Event();
        e.setCapacity(2);
        e.setConfirmedUserIds(Arrays.asList("a@example.com", "b@example.com", "c@example.com"));

        int remaining = e.getRemainingCapacity();

        assertEquals(0, remaining);
        assertTrue(e.isFull());
    }

    @Test
    public void isFull_falseWhenCapacityZeroAndNoConfirmed() {
        Event e = new Event();
        e.setCapacity(0);
        e.setConfirmedUserIds(Collections.emptyList());

        assertTrue("Capacity 0 event should be considered full/closed", e.isFull());
        assertEquals(0, e.getRemainingCapacity());
    }

    @Test
    public void isFull_trueWhenConfirmedEqualsCapacity() {
        Event e = new Event();
        e.setCapacity(3);
        e.setConfirmedUserIds(Arrays.asList("a", "b", "c"));

        assertTrue(e.isFull());
    }

    @Test
    public void isFull_trueWhenConfirmedGreaterThanCapacity() {
        Event e = new Event();
        e.setCapacity(3);
        e.setConfirmedUserIds(Arrays.asList("a", "b", "c", "d"));

        assertTrue(e.isFull());
    }
}
