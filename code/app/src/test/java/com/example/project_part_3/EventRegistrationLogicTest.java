package com.example.project_part_3.Events;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.*;

public class EventRegistrationLogicTest {

    @Test
    public void registrationOpen_returnsTrue_whenNowBetweenOpenAndClose() {
        Event e = new Event();
        Date now = new Date();

        // opened 1 hour ago, closes in 1 hour
        e.setDate_open(new Date(now.getTime() - 60 * 60 * 1000));
        e.setDate_close(new Date(now.getTime() + 60 * 60 * 1000));

        assertTrue("Registration should be open when now is between open and close",
                e.registrationOpen());
    }

    @Test
    public void registrationOpen_returnsFalse_whenBeforeOpen() {
        Event e = new Event();
        Date now = new Date();

        // opens in 1 hour, closes in 2 hours
        e.setDate_open(new Date(now.getTime() + 60 * 60 * 1000));
        e.setDate_close(new Date(now.getTime() + 2 * 60 * 60 * 1000));

        assertFalse("Registration should be closed before open date",
                e.registrationOpen());
    }

    @Test
    public void registrationOpen_returnsFalse_whenAfterClose() {
        Event e = new Event();
        Date now = new Date();

        // opened 2 hours ago, closed 1 hour ago
        e.setDate_open(new Date(now.getTime() - 2 * 60 * 60 * 1000));
        e.setDate_close(new Date(now.getTime() - 60 * 60 * 1000));

        assertFalse("Registration should be closed after close date",
                e.registrationOpen());
    }

    @Test
    public void getRemainingCapacity_whenNoConfirmed() {
        Event e = new Event();
        e.setCapacity(10);

        // confirmedUserIds is empty by default
        int remaining = e.getRemainingCapacity();
        assertEquals(10, remaining);
    }

    @Test
    public void getRemainingCapacity_whenSomeConfirmed() {
        Event e = new Event();
        e.setCapacity(10);

        List<String> confirmed = e.getConfirmedUserIds();
        confirmed.add("a@example.com");
        confirmed.add("b@example.com");
        confirmed.add("c@example.com");

        int remaining = e.getRemainingCapacity();
        assertEquals(7, remaining);
    }

    @Test
    public void getRemainingCapacity_neverReturnsNegative() {
        Event e = new Event();
        e.setCapacity(2);

        List<String> confirmed = e.getConfirmedUserIds();
        confirmed.add("a@example.com");
        confirmed.add("b@example.com");
        confirmed.add("c@example.com"); // over capacity

        int remaining = e.getRemainingCapacity();
        assertEquals("Remaining capacity should never be negative", 0, remaining);
    }
}
