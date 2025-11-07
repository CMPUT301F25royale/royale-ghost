package com.example.project_part_3.Notification;

import com.example.project_part_3.Users.Organizer;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

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



    public static List<Notification_Organizer> sample() {
        List<Notification_Organizer> list = new ArrayList<>();
        Organizer dummyOrg = new Organizer("Demo Organizer", "pass123", "demo@org.com");

        list.add(new Notification_Organizer(
                "Congratulations!",
                dummyOrg,
                "You have been selected for Beginner Swim Lessons hosted by shadowlynx!",
                new Timestamp(System.currentTimeMillis() - 4 * 24 * 60 * 60 * 1000L) // 4 days ago
        ));

        list.add(new Notification_Organizer(
                "Reminder",
                dummyOrg,
                "Your tennis class starts tomorrow at 9 AM!",
                new Timestamp(System.currentTimeMillis() - 1 * 24 * 60 * 60 * 1000L) // 1 day ago
        ));

        return list;
    }


}
