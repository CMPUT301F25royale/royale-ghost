package com.example.project_part_3.Users.Organizer_UI.Organizer_event;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.example.project_part_3.Events.Event;
import com.example.project_part_3.Events.Event_Organizer;
import com.example.project_part_3.R;

import java.util.ArrayList;

public class Organizer_event_adapter extends ArrayAdapter<Event> {
    private final int resourceLayout;
    private final ArrayList<Event> events;

    public Organizer_event_adapter(Context context, int resource, ArrayList<Event> events) {
        super(context, resource, events);
        this.resourceLayout = resource;
        this.events = events;
    }



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
            view.setTag(holder);
        } else {
            holder = (Organizer_event_adapter.ViewHolder) view.getTag();
        }

        Event event = getItem(position);

        if (event != null) {
            holder.eventName.setText(event.getTitle());
            holder.eventLocation.setText(event.getLocation());
            holder.eventDate.setText(String.format("Date: %s", event.getDate_open().toString()));//TODO: include close date
            holder.eventRegistrationStatus.setText(event.registrationStatus());
            holder.eventCapacity.setText(String.format("Capacity: %s", String.valueOf(event.getCapacity())));
            return view;
        }
        return view;
    }

    private static class ViewHolder {
        TextView eventName;
        TextView eventLocation;
        TextView eventDate;
        TextView eventRegistrationStatus;
        TextView eventCapacity;
    }
}
