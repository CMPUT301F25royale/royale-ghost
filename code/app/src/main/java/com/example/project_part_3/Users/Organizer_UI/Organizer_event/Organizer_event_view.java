package com.example.project_part_3.Users.Organizer_UI.Organizer_event;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

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
    Button createEventButton;


    public Organizer_event_view() {

    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.organizer_events, container, false);

        eventDb = EventDatabase.getInstance();

        events = new ArrayList<>(eventDb.getAllEvents());
        adapter = new Organizer_event_adapter(getContext(), R.layout.organizer_event_element, events);

        createEventButton = view.findViewById(R.id.organizer_events_list_create_button);

        createEventButton.setOnClickListener(v -> {

            NavController navController = NavHostFragment.findNavController(this);
            navController.navigate(R.id.action_organizerEventsFragment_to_organizerCreateEventFragment);
        });



        eventList = view.findViewById(R.id.organizer_events_list);
        eventList.setAdapter(adapter);
        return view;
    }
}