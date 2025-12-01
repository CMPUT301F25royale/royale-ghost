package com.example.project_part_3.Users.Admin_UI.Admin_notifications;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.project_part_3.Database_functions.NotificationDatabase;
import com.example.project_part_3.Notification.Notification_Organizer;
import com.example.project_part_3.R;

import java.util.ArrayList;

/**
 * Fragment that displays all admin notifications in a list view.
 */
public class Admin_notification_view extends Fragment {

    ListView notificationList;
    NotificationDatabase notificationDb;
    Admin_notification_adapter adapter;
    ArrayList<Notification_Organizer> notifications;

    /**
     * Default constructor for the admin notification view.
     */
    public Admin_notification_view() { }

    /**
     * Creates and returns the fragment's UI, initializing the notification list
     * and adapter with data from the notification database.
     *
     * @param inflater  the layout inflater used to inflate the view
     * @param container the parent view that the fragment UI will attach to
     * @param savedInstanceState saved state bundle, may be null
     * @return the inflated view for this fragment
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.admin_notifications, container, false);

        notificationDb = NotificationDatabase.getInstance();
        notifications = new ArrayList<>(notificationDb.getAllNotifications());
        adapter = new Admin_notification_adapter(getContext(), R.layout.admin_notifications_element, notifications);

        notificationList = view.findViewById(R.id.admin_notifications_list);
        notificationList.setAdapter(adapter);

        return view;
    }
}
