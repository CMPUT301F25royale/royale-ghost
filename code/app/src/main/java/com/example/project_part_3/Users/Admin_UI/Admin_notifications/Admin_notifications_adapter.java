package com.example.project_part_3.Users.Admin_UI.Admin_notifications;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.example.project_part_3.Notification.Notification_Organizer;
import com.example.project_part_3.R;

import java.util.ArrayList;

public class Admin_notifications_adapter extends ArrayAdapter<Notification_Organizer> {
    private final int resourceLayout;
    private final ArrayList<Notification_Organizer> organizers;

    public Admin_notifications_adapter(Context context, int resource, ArrayList<Notification_Organizer> organizers) {
        super(context, 0, organizers);
        this.resourceLayout = resource;
        this.organizers = organizers;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        ViewHolder holder;

        if (view == null) {
            view = LayoutInflater.from(getContext()).inflate(resourceLayout, parent, false);
            holder = new ViewHolder();
            holder.organizerName = view.findViewById(R.id.admin_notifications_organizer_name);
            holder.dateSent = view.findViewById(R.id.admin_notifications_date_sent);
            holder.eventTitle = view.findViewById(R.id.admin_notification_event_title);
            holder.eventMessage = view.findViewById(R.id.admin_notifications_event_message);
            view.setTag(holder);
        } else {
            holder = (ViewHolder) view.getTag();
        }

        Notification_Organizer notification = getItem(position);

        if (notification != null) {
            holder.organizerName.setText(notification.getOrganizer().getName());
            holder.dateSent.setText(notification.getTime_sent().toString());
            holder.eventTitle.setText(notification.getTitle());
            holder.eventMessage.setText(notification.getMessage());
            return view;
        }
        return view;
    }

    private static class ViewHolder {
        TextView organizerName;
        TextView dateSent;
        TextView eventTitle;
        TextView eventMessage;
    }

}
