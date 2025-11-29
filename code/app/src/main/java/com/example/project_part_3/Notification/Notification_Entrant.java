package com.example.project_part_3.Notification;

import com.google.firebase.Timestamp;

public class Notification_Entrant {
    private String title;
    private String entrantEmail;
    private String message;
    private Timestamp time_sent;
    private String eventId;
    private String eventTitle;
    private String type; // "lottery_won" or "lottery_not_won"

    // Required empty constructor for Firestore
    public Notification_Entrant() {}

    public Notification_Entrant(String title,
                                String entrantEmail,
                                String message,
                                Timestamp time_sent,
                                String eventId,
                                String eventTitle,
                                String type) {
        this.title = title;
        this.entrantEmail = entrantEmail;
        this.message = message;
        this.time_sent = time_sent;
        this.eventId = eventId;
        this.eventTitle = eventTitle;
        this.type = type;
    }

    // Convenience constructor (matches your current usage)
    public Notification_Entrant(String title,
                                String entrantEmail,
                                String message,
                                Timestamp time_sent) {
        this(title, entrantEmail, message, time_sent, null, null, null);
    }

    public String getTitle() {
        return title;
    }

    public String getEntrantEmail() {
        return entrantEmail;
    }

    public String getMessage() {
        return message;
    }

    public Timestamp getTime_sent() {
        return time_sent;
    }

    public String getEventId() {
        return eventId;
    }

    public String getEventTitle() {
        return eventTitle;
    }

    public String getType() {
        return type;
    }

    // ---- Setters ----
    public void setTitle(String title) {
        this.title = title;
    }

    public void setEntrantEmail(String entrantEmail) {
        this.entrantEmail = entrantEmail;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setTime_sent(Timestamp time_sent) {
        this.time_sent = time_sent;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public void setEventTitle(String eventTitle) {
        this.eventTitle = eventTitle;
    }

    public void setType(String type) {
        this.type = type;
    }
}
