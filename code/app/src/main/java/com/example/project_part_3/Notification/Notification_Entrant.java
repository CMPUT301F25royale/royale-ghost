package com.example.project_part_3.Notification;

import com.google.firebase.Timestamp;

public class Notification_Entrant {
    private String id;
    private String title;
    private String userEmail; // entrant's Email
    private String message;
    private Timestamp createdAt;
    private String eventId;
    private String eventTitle;
    private String type; // "lottery_won" or "lottery_not_won"
    private boolean read;


    // Required empty constructor for Firestore
    public Notification_Entrant() {}

    public Notification_Entrant(String title,
                                String userEmail,
                                String message,
                                Timestamp createdAt,
                                String eventId,
                                String eventTitle,
                                String type) {
        this.title = title;
        this.userEmail = userEmail;
        this.message = message;
        this.createdAt = createdAt;
        this.eventId = eventId;
        this.eventTitle = eventTitle;
        this.type = type;
        this.read = false;
    }

    // Convenience constructor (matches your current usage)
    public Notification_Entrant(String title,
                                String userEmail,
                                String message,
                                Timestamp createdAt) {
        this(title, userEmail, message, createdAt, null, null, null);
        this.read = false;
    }

    // ---- Getters ----

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public String getMessage() {
        return message;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
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

    public boolean isRead() { return read; }


    // ---- Setters ----
    public void setTitle(String title) {
        this.title = title;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
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

    public void setRead(boolean read) { this.read = read; }

    public void setId(String id) {
        this.id = id;
    }
}
