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

public class Notification_entrant_adapter extends ArrayAdapter<Notification_Entrant> {

    private final LayoutInflater inflater;
    private final SimpleDateFormat dateFormat =
            new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());

    // Replace this with the actual item layout file name if it's different
    private static final int ITEM_LAYOUT = R.layout.organizer_notifications_element;
    // ^^^ if your row XML is named differently, change this

    public Notification_entrant_adapter(@NonNull Context context,
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
            holder.bell = convertView.findViewById(R.id.organizer_notification_bell);
            holder.name = convertView.findViewById(R.id.organizer_notifications_organizer_name);
            holder.date = convertView.findViewById(R.id.organizer_notifications_date_sent);
            holder.eventTitle = convertView.findViewById(R.id.organizer_notification_event_title);
            holder.message = convertView.findViewById(R.id.organizer_notifications_event_message);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        Notification_Entrant notif = getItem(position);
        if (notif != null) {
            holder.eventTitle.setText(
                    notif.getEventTitle() != null ? notif.getEventTitle() : "Event update"
            );

            holder.name.setText(
                    notif.getType() != null ? notif.getType() : ""
            );

            holder.message.setText(
                    notif.getMessage() != null ? notif.getMessage() : ""
            );

            Timestamp ts = notif.getTime_sent();
            if (ts != null) {
                Date d = ts.toDate();
                holder.date.setText(dateFormat.format(d));
            } else {
                holder.date.setText("");
            }

        // Optional: bell click behavior (e.g., open event details)
            holder.bell.setOnClickListener(v -> {
                // TODO: navigate to event screen using notif.getEventId(), if you want
            });
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
