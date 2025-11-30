package com.example.project_part_3.Notification;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
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
            new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());

    private static final int ITEM_LAYOUT = R.layout.organizer_notifications_element;

    private OnNotificationClickListener listener;

    public interface OnNotificationClickListener {
        void onAcceptButtonClick(Notification_Entrant notif);
        void onDeclineButtonClick(Notification_Entrant notif);
    }

    public Notification_entrant_adapter(@NonNull Context context,
                                        @NonNull List<Notification_Entrant> notifications, OnNotificationClickListener listener) {
        super(context, 0, notifications);
        inflater = LayoutInflater.from(context);
        this.listener = listener;
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

            // display fields
            holder.bell = convertView.findViewById(R.id.organizer_notification_bell);
            holder.overlay = convertView.findViewById(R.id.organizer_notification_overlay);

            // text fields
            holder.name = convertView.findViewById(R.id.organizer_notifications_organizer_name);
            holder.date = convertView.findViewById(R.id.organizer_notifications_date_sent);
            holder.eventTitle = convertView.findViewById(R.id.organizer_notification_event_title);
            holder.message = convertView.findViewById(R.id.organizer_notifications_event_message);
            holder.acceptButton = convertView.findViewById(R.id.organizer_notification_accept_button);
            holder.declineButton = convertView.findViewById(R.id.organizer_notification_decline_button);

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

            //if (notif.isSeen()) {
                holder.acceptButton.setVisibility(View.GONE);
                holder.declineButton.setVisibility(View.GONE);
                holder.overlay.setVisibility(View.GONE);
            //}

            holder.acceptButton.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onAcceptButtonClick(getItem(position));

                    holder.acceptButton.setVisibility(View.GONE);
                    holder.declineButton.setVisibility(View.GONE);
                    holder.overlay.setVisibility(View.GONE);
                    //notif.setSeen(true);
                }
            });

            holder.declineButton.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onDeclineButtonClick(getItem(position));

                    holder.acceptButton.setVisibility(View.GONE);
                    holder.declineButton.setVisibility(View.GONE);
                    holder.overlay.setVisibility(View.GONE);
                    //notif.setSeen(true);
                }
            });
        }

        return convertView;
    }

    private static class ViewHolder {
        ImageButton bell;
        ImageView overlay;
        TextView name;
        TextView date;
        TextView eventTitle;
        TextView message;

        // Buttons
        Button acceptButton;
        Button declineButton;
    }

    public void setOnNotificationClickListener(OnNotificationClickListener listener) {
        this.listener = listener;
    }
}