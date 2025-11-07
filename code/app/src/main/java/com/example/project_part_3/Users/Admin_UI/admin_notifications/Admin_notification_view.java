package com.example.project_part_3.Users.Admin_UI.admin_notifications;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.project_part_3.Database_functions.NotificationDatabase;
import com.example.project_part_3.Notification.Notification_Organizer;
import com.example.project_part_3.R;

import java.util.ArrayList;

public class Admin_notification_view extends Fragment {
    ListView notificationList;
    NotificationDatabase notificationDb;
    Admin_notifications_adapter adapter;
    ArrayList<Notification_Organizer> notifications;

    public Admin_notification_view() {

    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.admin_notifications, container, false);

        notificationDb = NotificationDatabase.getInstance();

        notifications = new ArrayList<>(notificationDb.getAllNotifications());
        adapter = new Admin_notifications_adapter(getContext(), R.layout.admin_notifications_element, notifications);


        notificationList = view.findViewById(R.id.admin_notifications_list);
        notificationList.setAdapter(adapter);
        return view;
    }
}
