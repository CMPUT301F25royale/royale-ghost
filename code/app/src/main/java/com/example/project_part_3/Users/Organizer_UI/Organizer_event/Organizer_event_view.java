package com.example.project_part_3.Users.Organizer_UI.Organizer_event;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;

import com.example.project_part_3.Database_functions.Database;
import com.example.project_part_3.Database_functions.EventDatabase;
import com.example.project_part_3.Events.Event;
import com.example.project_part_3.R;
import com.example.project_part_3.Users.Organizer_UI.OrganizerSharedViewModel;
import com.google.firebase.firestore.FirebaseFirestore;


import java.util.ArrayList;

/**
 * Fragment that displays a list of events created by the organizer.
 */
public class Organizer_event_view extends Fragment {
    ListView eventList;
    Organizer_event_adapter adapter;
    Button createEventButton;
    OrganizerSharedViewModel model;

    public Organizer_event_view() {

    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        model = new ViewModelProvider(requireActivity()).get(OrganizerSharedViewModel.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.organizer_events, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        eventList = view.findViewById(R.id.organizer_events_list);
        createEventButton = view.findViewById(R.id.organizer_events_list_create_button);

        createEventButton.setOnClickListener(v -> {
            NavController navController = NavHostFragment.findNavController(this);
            navController.navigate(R.id.action_organizerEventsFragment_to_organizerCreateEventFragment);
        });

        model.getUserEmail().observe(getViewLifecycleOwner(), email -> {
            FirebaseFirestore ff = FirebaseFirestore.getInstance();
            Database db = new Database(ff);

            db.getEventsByOrganizer(email).addOnSuccessListener(events -> {
                ArrayList<Event> eventArrayList = new ArrayList<>(events);
                adapter = new Organizer_event_adapter(getContext(), R.layout.organizer_event_element, eventArrayList, new Organizer_event_adapter.onEventClickListener() {
                    @Override
                    public void onEditClick(Event event) {
                        model.setSelectedEvent(event);

                        NavController navController = NavHostFragment.findNavController(Organizer_event_view.this);
                        navController.navigate(R.id.action_organizerEventsFragment_to_organizerEditEventFragment);
                    }

                    @Override
                    public void onSeeEntrantsClick(Event event) {
                        model.setSelectedEvent(event);

                        NavController navController = NavHostFragment.findNavController(Organizer_event_view.this);
                        navController.navigate(R.id.action_organizerEventsFragment_to_organizerEntrantViewFragment);
                    }

                    @Override
                    public void onQrClick(Event event) {
                        model.setSelectedEvent(event);

                        NavController navController = NavHostFragment.findNavController(Organizer_event_view.this);
                        navController.navigate(R.id.action_organizerEventsFragment_to_organizer_event_qrcode_view);
                    }
                });
                eventList.setAdapter(adapter);
            });
        });
    }
}