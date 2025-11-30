package com.example.project_part_3.Users.Organizer_UI.Organizer_event;

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
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.bumptech.glide.Glide;
import com.example.project_part_3.Events.Event;
import com.example.project_part_3.R;
import com.example.project_part_3.Users.Organizer_UI.OrganizerSharedViewModel;

import java.util.ArrayList;
import java.text.SimpleDateFormat;
import java.util.Locale;

/**
 * Adapter for displaying a list of events in the Organizer UI.
 * Each list item displays event details including title, location, start date, registration status,
 * capacity, and buttons for editing, viewing entrants, and showing QR code.
 */
public class Organizer_event_adapter extends ArrayAdapter<Event> {
    private final int resourceLayout;

    public interface onEventClickListener {
        void onEditClick(Event event);
        void onSeeEntrantsClick(Event event);
        void onQrClick(Event event);
    }

    private final onEventClickListener listener;

    public Organizer_event_adapter(Context context, int resource, ArrayList<Event> events, onEventClickListener listener) {
        super(context, resource, events);
        this.resourceLayout = resource;
        this.listener = listener;
    }



    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        Organizer_event_adapter.ViewHolder holder;

        if (view == null) {
            view = LayoutInflater.from(getContext()).inflate(resourceLayout, parent, false);
            holder = new Organizer_event_adapter.ViewHolder();
            holder.eventName = view.findViewById(R.id.organizer_event_name);
            holder.eventLocation = view.findViewById(R.id.organizer_event_location);
            holder.eventDate = view.findViewById(R.id.organizer_event_date);
            holder.eventRegistrationStatus = view.findViewById(R.id.organizer_event_registration_status);
            holder.eventCapacity = view.findViewById(R.id.organizer_event_capacity);
            holder.seeEntrantsButton = view.findViewById(R.id.organizer_event_view_entrants_button);
            holder.editEventButton = view.findViewById(R.id.organizer_event_edit_event_button);
            holder.qrButton = view.findViewById(R.id.organizer_event_qr_button);
            holder.eventImage = view.findViewById(R.id.organizer_event_image);
            view.setTag(holder);
        } else {
            holder = (Organizer_event_adapter.ViewHolder) view.getTag();
        }

        Event event = getItem(position);

        if (event != null) {
            holder.eventName.setText(event.getTitle());
            holder.eventLocation.setText(event.getLocation());

            SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
            holder.eventDate.setText("Starts: " + sdf.format(event.getEventStartAt()));

            holder.eventRegistrationStatus.setText(event.registrationStatus());

            if (event.getCapacity() == null || event.getCapacity() == 0) {
                holder.eventCapacity.setText("Capacity: None");
            } else {
                holder.eventCapacity.setText(String.valueOf("Capacity: " + event.getCapacity()));
            }
            // Build event capacity and attendees string
            String eventCapacityAndAttendees = buildEntrantAndCapacityString(event.getWaitlistUserIds().size(), event.getCapacity());
            holder.eventCapacity.setText(eventCapacityAndAttendees);

            holder.editEventButton.setOnClickListener(v -> {
                if (listener != null) listener.onEditClick(event);
            });
            holder.seeEntrantsButton.setOnClickListener(v -> {
                if (listener != null) listener.onSeeEntrantsClick(event);
            });
            holder.qrButton.setOnClickListener(v -> {
                if (listener != null) listener.onQrClick(event);
            });

            if (event.getPosterImageUrl() != null) {
                Glide.with(getContext()).load(event.getPosterImageUrl()).into(holder.eventImage);
            }
        }
        return view;
    }

    private String buildEntrantAndCapacityString(int numEntrants, @Nullable Integer capacity) {
        String s = "Entrants: ";

        s += Integer.toString(numEntrants);

        s += " | Capacity: ";

        if (capacity == null) {
            s += "None";
        } else {
            s += Integer.toString(capacity);
        }

        return s;
    }
    public static class ViewHolder {
        ImageView eventImage;
        TextView eventName;
        TextView eventLocation;
        TextView eventDate;
        TextView eventRegistrationStatus;
        TextView eventCapacity;
        Button seeEntrantsButton;
        Button editEventButton;
        ImageButton qrButton;
    }
}
