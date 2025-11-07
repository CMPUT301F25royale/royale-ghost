package com.example.project_part_3.Users.Organizer_UI.Organizer_event;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.project_part_3.Database_functions.EventDatabase;
import com.example.project_part_3.Events.Event;
import com.example.project_part_3.Events.Event_Organizer;
import com.example.project_part_3.R;


import java.util.ArrayList;

public class Organizer_event_view extends Fragment{
    ListView eventList;
    EventDatabase eventDb;
    Organizer_event_adapter adapter;
    ArrayList<Event> events;

    public Organizer_event_view() {

    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.organizer_notifications, container, false);

        eventDb = EventDatabase.getInstance();

        events = new ArrayList<>(eventDb.getAllEvents());
        adapter = new Organizer_event_adapter(getContext(), R.layout.organizer_event_element, events);


        eventList = view.findViewById(R.id.organizer_notifications_list);
        eventList.setAdapter(adapter);
        return view;
    }
}