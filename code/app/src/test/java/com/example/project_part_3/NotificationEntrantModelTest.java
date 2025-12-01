package com.example.project_part_3;

import static org.junit.Assert.*;

import com.example.project_part_3.Notification.Notification_Entrant;
import com.google.firebase.Timestamp;

import org.junit.Test;

public class NotificationEntrantModelTest {

    @Test
    public void constructor_setsAllFieldsCorrectly() {
        String title = "Results: Cool Event";
        String email = "user@example.com";
        String message = "You were selected!";
        Timestamp ts = Timestamp.now();
        String eventId = "event123";
        String eventTitle = "Cool Event";
        String type = "lottery_won";

        Notification_Entrant n = new Notification_Entrant(
                title,
                email,
                message,
                ts,
                eventId,
                eventTitle,
                type
        );

        assertEquals(title, n.getTitle());
        assertEquals(email, n.getEntrantEmail());
        assertEquals(message, n.getMessage());
        assertEquals(ts, n.getTime_sent());
        assertEquals(eventId, n.getEventId());
        assertEquals(eventTitle, n.getEventTitle());
        assertEquals(type, n.getType());
    }

    @Test
    public void setters_updateFields() {
        Notification_Entrant n = new Notification_Entrant();

        String title = "Updated Title";
        String email = "updated@example.com";
        String message = "Updated message";
        Timestamp ts = Timestamp.now();
        String eventId = "evt999";
        String eventTitle = "Updated Event";
        String type = "lottery_not_won";

        n.setTitle(title);
        n.setEntrantEmail(email);
        n.setMessage(message);
        n.setTime_sent(ts);
        n.setEventId(eventId);
        n.setEventTitle(eventTitle);
        n.setType(type);

        assertEquals(title, n.getTitle());
        assertEquals(email, n.getEntrantEmail());
        assertEquals(message, n.getMessage());
        assertEquals(ts, n.getTime_sent());
        assertEquals(eventId, n.getEventId());
        assertEquals(eventTitle, n.getEventTitle());
        assertEquals(type, n.getType());
    }
}
