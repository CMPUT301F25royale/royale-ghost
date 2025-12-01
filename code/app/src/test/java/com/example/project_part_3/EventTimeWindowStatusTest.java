package com.example.project_part_3;

import static org.junit.Assert.*;

import com.example.project_part_3.Events.Event;

import org.junit.Test;

import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class EventTimeWindowStatusTest {

    private Date daysFromNow(int daysOffset) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date());
        cal.add(Calendar.DATE, daysOffset);
        return cal.getTime();
    }

    private Date hoursFromNow(int hoursOffset) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date());
        cal.add(Calendar.HOUR_OF_DAY, hoursOffset);
        return cal.getTime();
    }

    @Test
    public void registrationOpen_betweenOpenAndClose_returnsTrue() {
        Event e = new Event();
        e.setDate_open(daysFromNow(-1));  // opened yesterday
        e.setDate_close(daysFromNow(+1)); // closes tomorrow

        assertTrue("Registration should be open",
                e.registrationOpen());
    }

    @Test
    public void registrationOpen_beforeOpen_returnsFalse() {
        Event e = new Event();
        e.setDate_open(daysFromNow(+1));  // opens tomorrow
        e.setDate_close(daysFromNow(+2)); // closes later

        assertFalse("Registration should NOT be open before open date",
                e.registrationOpen());
    }

    @Test
    public void registrationOpen_afterClose_returnsFalse() {
        Event e = new Event();
        e.setDate_open(daysFromNow(-3)); // opened in past
        e.setDate_close(daysFromNow(-1)); // closed yesterday

        assertFalse("Registration should NOT be open after close date",
                e.registrationOpen());
    }

    @Test
    public void daysUntilClose_handlesFutureCloseDate() {
        Event e = new Event();
        // Close date in 3 days
        Date closeIn3Days = daysFromNow(3);
        e.setDate_close(closeIn3Days);

        long days = e.daysUntilClose();

        // Allow small off-by-one differences due to test running time
        assertTrue("daysUntilClose should be between 2 and 3 inclusive",
                days >= 2 && days <= 3);
    }

    @Test
    public void hoursUntilClose_handlesFutureCloseDate() {
        Event e = new Event();
        // Close date in about 5 hours
        Date closeIn5Hours = hoursFromNow(5);
        e.setDate_close(closeIn5Hours);

        long hours = e.hoursUntilClose();

        // Allow a little slack
        assertTrue("hoursUntilClose should be between 4 and 5 inclusive",
                hours >= 4 && hours <= 5);
    }

    @Test
    public void registrationStatus_closedWhenOutsideWindow() {
        Event e = new Event();
        e.setDate_open(daysFromNow(-3));
        e.setDate_close(daysFromNow(-1)); // already closed

        String status = e.registrationStatus();

        assertEquals("Closed", status);
    }

    @Test
    public void registrationStatus_openWithDaysLeft() {
        Event e = new Event();
        e.setDate_open(daysFromNow(-1));  // opened yesterday
        e.setDate_close(daysFromNow(3));  // closes in a few days

        String status = e.registrationStatus();

        assertTrue(
                "Expected 'Open (... days until close)' but got: " + status,
                status.startsWith("Open (") && status.contains("days until close)")
        );
    }

    @Test
    public void registrationStatus_openWithHoursLeft() {
        Event e = new Event();
        e.setDate_open(daysFromNow(-1));
        e.setDate_close(hoursFromNow(2));  // closes in a couple hours

        String status = e.registrationStatus();

        assertTrue(
                "Expected 'Open (closes within ... hours)' but got: " + status,
                status.startsWith("Open (closes within") && status.contains("hours)")
        );
    }
}
