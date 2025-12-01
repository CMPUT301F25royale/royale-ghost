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

/**
 * Adapter for displaying admin notifications in a list view.
 */
public class Admin_notification_adapter extends ArrayAdapter<Notification_Organizer> {
    private final int resourceLayout;
    private final ArrayList<Notification_Organizer> organizers;

    /**
     * Creates a new adapter for admin notifications.
     *
     * @param context   the current context
     * @param resource  the layout resource ID for each list item
     * @param organizers the list of organizer notifications to display
     */
    public Admin_notification_adapter(Context context, int resource, ArrayList<Notification_Organizer> organizers) {
        super(context, 0, organizers);
        this.resourceLayout = resource;
        this.organizers = organizers;
    }

    /**
     * Returns a populated row View for the given position.
     *
     * @param position     the position of the item in the list
     * @param convertView  an existing view to reuse if possible
     * @param parent       the parent view group
     * @return the populated list item view
     */
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

    /**
     * Holder class to cache view references for performance.
     */
    private static class ViewHolder {
        TextView organizerName;
        TextView dateSent;
        TextView eventTitle;
        TextView eventMessage;
    }
}
