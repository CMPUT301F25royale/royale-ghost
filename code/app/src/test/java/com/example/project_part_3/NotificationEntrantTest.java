package com.example.project_part_3;

import com.example.project_part_3.Notification.Notification_Entrant;
import com.google.firebase.Timestamp;
import org.junit.Test;

import static org.junit.Assert.*;

public class NotificationEntrantTest {

    @Test
    public void constructorAndGettersWork() {
        String title = "Results: Cool Event";
        String entrantEmail = "user@example.com";
        String message = "You were selected!";
        Timestamp ts = Timestamp.now();
        String eventId = "event123";
        String eventTitle = "Cool Event";
        String type = "lottery_won";

        Notification_Entrant n = new Notification_Entrant(
                title,
                entrantEmail,
                message,
                ts,
                eventId,
                eventTitle,
                type
        );

        assertEquals(title, n.getTitle());
        assertEquals(entrantEmail, n.getEntrantEmail());
        assertEquals(message, n.getMessage());
        assertEquals(ts, n.getTime_sent());
        assertEquals(eventId, n.getEventId());
        assertEquals(eventTitle, n.getEventTitle());
        assertEquals(type, n.getType());
    }

    @Test
    public void settersUpdateFields() {
        Notification_Entrant n = new Notification_Entrant();

        n.setTitle("New title");
        n.setEntrantEmail("new@example.com");
        n.setMessage("Updated message");
        Timestamp ts = Timestamp.now();
        n.setTime_sent(ts);
        n.setEventId("e2");
        n.setEventTitle("Another Event");
        n.setType("lottery_not_won");

        assertEquals("New title", n.getTitle());
        assertEquals("new@example.com", n.getEntrantEmail());
        assertEquals("Updated message", n.getMessage());
        assertEquals(ts, n.getTime_sent());
        assertEquals("e2", n.getEventId());
        assertEquals("Another Event", n.getEventTitle());
        assertEquals("lottery_not_won", n.getType());
    }
}
