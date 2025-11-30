package com.example.project_part_3.Events;

import org.junit.Test;

import java.util.Date;

import static org.junit.Assert.*;

public class EventRegistrationStatusTest {

    @Test
    public void registrationStatus_returnsClosed_whenRegistrationNotOpen() {
        Event e = new Event();
        Date now = new Date();

        // event closed 1 hour ago
        e.setDate_open(new Date(now.getTime() - 5 * 60 * 60 * 1000));
        e.setDate_close(new Date(now.getTime() - 60 * 60 * 1000));

        String status = e.registrationStatus();
        assertEquals("Closed", status);
    }

    @Test
    public void registrationStatus_openWithMultipleDaysRemaining() {
        Event e = new Event();
        Date now = new Date();

        // opened yesterday, closes in 3 days
        e.setDate_open(new Date(now.getTime() - 24L * 60 * 60 * 1000));
        e.setDate_close(new Date(now.getTime() + 3L * 24 * 60 * 60 * 1000));

        String status = e.registrationStatus();

        assertTrue("Status should start with 'Open (' when registration is open",
                status.startsWith("Open ("));
        assertTrue("Status should mention 'days until close'",
                status.contains("days until close"));
    }

    @Test
    public void registrationStatus_openWithOneDayRemaining() {
        Event e = new Event();
        Date now = new Date();

        // opened, closes in exactly ~1 day
        e.setDate_open(new Date(now.getTime() - 2L * 24 * 60 * 60 * 1000));
        e.setDate_close(new Date(now.getTime() + 24L * 60 * 60 * 1000));

        String status = e.registrationStatus();

        // allow for rounding in daysUntilClose()
        assertTrue(
                "Status should mention '1 day until close' when there's about one day left",
                status.contains("1 day until close")
        );
    }

    @Test
    public void registrationStatus_openWithLessThanOneDayRemaining() {
        Event e = new Event();
        Date now = new Date();

        // closes in a few hours
        e.setDate_open(new Date(now.getTime() - 24L * 60 * 60 * 1000));
        e.setDate_close(new Date(now.getTime() + 3L * 60 * 60 * 1000));

        String status = e.registrationStatus();

        assertTrue("Status should start with 'Open ('", status.startsWith("Open ("));
        assertTrue("Status should contain 'closes within'",
                status.contains("closes within"));
    }
}
