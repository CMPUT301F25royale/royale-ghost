package com.example.project_part_3.Notification;

import com.example.project_part_3.Users.Organizer;

import java.sql.Timestamp;

public class Notification_Organizer {
    private String title;
    private Organizer organizer;
    private String message;
    private Timestamp time_sent;

    public Notification_Organizer(String title, Organizer organizer, String message, Timestamp time_sent) {
        this.title = title;
        this.organizer = organizer;
        this.message = message;
        this.time_sent = time_sent;
    }
    public String getTitle() {
        return title;
    }
    public Organizer getOrganizer() {
        return organizer;
    }
    public String getMessage() {
        return message;
    }
    public Timestamp getTime_sent() {
        return time_sent;
    }

}
