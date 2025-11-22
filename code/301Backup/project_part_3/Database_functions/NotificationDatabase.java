package com.example.project_part_3.Database_functions;

import android.app.Notification;

import com.example.project_part_3.Notification.Notification_Organizer;
import com.example.project_part_3.Users.Organizer;
import java.util.Date;
import java.sql.Timestamp;

import java.util.ArrayList;

public class NotificationDatabase {
    private static NotificationDatabase instance;
    private ArrayList<Notification_Organizer> database;
    private NotificationDatabase() {
        database = new ArrayList<>();
        Organizer organizer1 = new Organizer("John Doe", "john.mclean@examplepetstore.com", "1234567890");
        Organizer organizer2 = new Organizer("Jane Smith", "william.henry.harrison@example-pet-store.com", "0987654321");

        database.add(new Notification_Organizer(
                "Annual Tech Conference",
                organizer1,
                "The annual tech conference has been scheduled for December 15th. We need to book the venue.",
                new Timestamp(new Date().getTime() - 86400000)
        ));

        database.add(new Notification_Organizer(
                "Charity Run Logistics",
                organizer2,
                "We have surpassed our sign-up goal for the charity run! Please review the updated logistics plan.",
                new Timestamp(new Date().getTime() - 172800000)
        ));

        database.add(new Notification_Organizer(
                "New Year's Gala Update",
                organizer1,
                "A reminder that all vendor contracts for the New Year's Gala must be finalized by the end of this week.",
                new Timestamp(new Date().getTime())
        ));
    }

    public static synchronized NotificationDatabase getInstance() {
        if (instance == null) {
            instance = new NotificationDatabase();
        }
        return instance;
    }

    public Boolean addNotification(Notification_Organizer notification) {
        if (!database.contains(notification)) {
            database.add(notification);
            return true;
        }
        return false;
    }

    public Boolean removeNotification(Notification_Organizer notification) {
        return database.remove(notification);
    }

    public ArrayList<Notification_Organizer> getAllNotifications() {
        return new ArrayList<>(database);
    }
}
