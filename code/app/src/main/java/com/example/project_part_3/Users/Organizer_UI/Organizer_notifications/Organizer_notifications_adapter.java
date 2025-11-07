package com.example.project_part_3.Users.Organizer_UI.Organizer_notifications;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.example.project_part_3.Notification.Notification_Organizer;
import com.example.project_part_3.R;

import java.util.ArrayList;

public class Organizer_notifications_adapter extends ArrayAdapter<Notification_Organizer> {
    private final int resourceLayout;
    private final ArrayList<Notification_Organizer> organizers;

    public Organizer_notifications_adapter(Context context, int resource, ArrayList<Notification_Organizer> organizers) {
        super(context, 0, organizers);
        this.resourceLayout = resource;
        this.organizers = organizers;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        Organizer_notifications_adapter.ViewHolder holder;

        if (view == null) {
            view = LayoutInflater.from(getContext()).inflate(resourceLayout, parent, false);
            holder = new Organizer_notifications_adapter.ViewHolder();
            holder.organizerName = view.findViewById(R.id.organizer_notifications_organizer_name);
            holder.dateSent = view.findViewById(R.id.organizer_notifications_date_sent);
            holder.eventTitle = view.findViewById(R.id.organizer_notification_event_title);
            holder.eventMessage = view.findViewById(R.id.organizer_notifications_event_message);
            view.setTag(holder);
        } else {
            holder = (Organizer_notifications_adapter.ViewHolder) view.getTag();
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
