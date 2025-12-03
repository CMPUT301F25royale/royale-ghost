package com.example.project_part_3.Notification;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.project_part_3.R;
import com.google.firebase.Timestamp;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Adapter for admin notification log – shows ALL entrant notifications.
 */
public class Notification_admin_adapter extends ArrayAdapter<Notification_Entrant> {

    private final LayoutInflater inflater;
    private final SimpleDateFormat dateFormat =
            new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());

    private static final int ITEM_LAYOUT = R.layout.admin_notifications_element;

    public Notification_admin_adapter(@NonNull Context context,
                                      @NonNull List<Notification_Entrant> notifications) {
        super(context, 0, notifications);
        inflater = LayoutInflater.from(context);
    }

    @NonNull
    @Override
    public View getView(int position,
                        @Nullable View convertView,
                        @NonNull ViewGroup parent) {

        ViewHolder holder;

        if (convertView == null) {
            convertView = inflater.inflate(ITEM_LAYOUT, parent, false);
            holder = new ViewHolder();

            holder.bell = convertView.findViewById(R.id.admin_notification_bell);
            holder.name = convertView.findViewById(R.id.admin_notifications_organizer_name);
            holder.date = convertView.findViewById(R.id.admin_notifications_date_sent);
            holder.eventTitle = convertView.findViewById(R.id.admin_notification_event_title);
            holder.message = convertView.findViewById(R.id.admin_notifications_event_message);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        Notification_Entrant notif = getItem(position);
        if (notif != null) {
            // Who did this go to?
            String recipient = notif.getEntrantEmail();
            if (recipient == null || recipient.trim().isEmpty()) {
                recipient = "(unknown recipient)";
            }
            holder.name.setText(recipient);

            // Event title (fallback to notification title if needed)
            String title = notif.getEventTitle();
            if (title == null || title.trim().isEmpty()) {
                title = notif.getTitle();
            }
            holder.eventTitle.setText(title != null ? title : "Event update");

            // Message
            holder.message.setText(
                    notif.getMessage() != null ? notif.getMessage() : ""
            );

            // Date
            Timestamp ts = notif.getTime_sent();
            if (ts != null) {
                Date d = ts.toDate();
                holder.date.setText(dateFormat.format(d));
            } else {
                holder.date.setText("");
            }
        }

        return convertView;
    }

    private static class ViewHolder {
        ImageButton bell;
        TextView name;
        TextView date;
        TextView eventTitle;
        TextView message;
    }
}
