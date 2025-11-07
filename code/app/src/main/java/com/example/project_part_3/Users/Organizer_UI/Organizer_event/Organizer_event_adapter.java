package com.example.project_part_3.Users.Organizer_UI.Organizer_event;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.example.project_part_3.Events.Event_Organizer;
import com.example.project_part_3.R;

import java.util.ArrayList;

public class Organizer_event_adapter extends ArrayAdapter<Event_Organizer> {
    private final int resourceLayout;
    private final ArrayList<Event_Organizer> organizers;

    public Organizer_event_adapter(Context context, int resource, ArrayList<Event_Organizer> organizers) {
        super(context, 0, organizers);
        this.resourceLayout = resource;
        this.organizers = organizers;
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

        Event_Organizer event = getItem(position);

        if (event != null) {
            holder.eventName.setText(event.getName());
            holder.eventLocation.setText(event.getLocation());
            holder.eventDate.setText(event.getDate().toString());
            holder.eventRegistrationStatus.setText(event.getRegStatus());
            holder.eventCapacity.setText(event.getCapacity());
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
