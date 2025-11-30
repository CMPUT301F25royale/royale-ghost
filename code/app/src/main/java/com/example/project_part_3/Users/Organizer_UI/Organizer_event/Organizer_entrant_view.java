package com.example.project_part_3.Users.Organizer_UI.Organizer_event;

import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.example.project_part_3.Database_functions.Database;
import com.example.project_part_3.Events.Event;
import com.example.project_part_3.R;
import com.example.project_part_3.Users.Entrant;
import com.example.project_part_3.Users.Organizer_UI.OrganizerSharedViewModel;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

/**
 * Fragment that displays a list of entrants for a selected event in the Organizer UI.
 * Each entrant's name, email, and status is displayed as well as a decline button if applicable
 */
public class Organizer_entrant_view extends Fragment {
    OrganizerSharedViewModel model;
    Database db;
    Organizer_entrant_adapter adapter;
    ArrayList<Pair<Entrant, String>> entrantArrayListAndStatuses;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.organizer_event_entrant_view, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        model = new ViewModelProvider(requireActivity()).get(OrganizerSharedViewModel.class);
        db = new Database(FirebaseFirestore.getInstance());

        setUpBackButton(view);
        model.getSelectedEvent().observe(getViewLifecycleOwner(), event -> {
            if (event != null) {
                populateUI(view, event);
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        model.setSelectedEvent(null);
    }

    public void setUpBackButton(View view) {
        ImageButton back = view.findViewById(R.id.organizer_event_view_back_button);
        if (back != null) {
            back.setOnClickListener(v -> {
                NavController navBack = NavHostFragment.findNavController(this);
                navBack.navigate(R.id.action_organizer_entrant_view_to_organizerEventsFragment);
            });
        }
    }

    public void populateUI(View view, Event event) {
        db.getAllEntrantsByEvent(event).addOnSuccessListener(entrants -> {
            ArrayList<Entrant> entrantArrayList = new ArrayList<>(entrants);
            ArrayList<String> statuses = new ArrayList<>();

            for (Entrant e : entrantArrayList) {
                String status;
                // Check null safety for lists inside Event
                if (event.getConfirmedUserIds() != null && event.getConfirmedUserIds().contains(e.getEmail())) {
                    status = "Accepted";
                } else if (event.getDeclinedUserIds() != null && event.getDeclinedUserIds().contains(e.getEmail())) {
                    status = "Declined";
                } else {
                    status = "Pending";
                }
                statuses.add(status);
            }

            entrantArrayListAndStatuses = new ArrayList<>();
            for (int i = 0; i < entrantArrayList.size(); i++) {
                entrantArrayListAndStatuses.add(new Pair<>(entrantArrayList.get(i), statuses.get(i)));
            }

            adapter = new Organizer_entrant_adapter(getContext(), R.layout.organizer_event_entrant_element, entrantArrayListAndStatuses, new Organizer_entrant_adapter.OnEntrantClickListener() {
                @Override
                public void onDeclineClick(Entrant entrant) {
                    declineEntrant(event, entrant);
                }
            });

            ListView listView = view.findViewById(R.id.organizer_event_entrant_list);
            listView.setAdapter(adapter);
        });
    }

    private void declineEntrant(Event event, Entrant entrant) {
        db.declineEntrant(event, entrant).addOnSuccessListener(success -> {
            if (success) {
                for (int i = 0; i < entrantArrayListAndStatuses.size(); i++) {
                    Entrant currentEntrant = entrantArrayListAndStatuses.get(i).first;

                    if (currentEntrant.getEmail().equals(entrant.getEmail())) {
                        entrantArrayListAndStatuses.set(i, new Pair<>(entrant, "Declined"));
                        break;
                    }
                }

                if (adapter != null) {
                    adapter.notifyDataSetChanged();
                }
            } else {
                Toast.makeText(getContext(), "Failed to decline entrant", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(e -> {
            Log.e("Organizer_entrant_view", "Failed to decline entrant", e);
            Toast.makeText(getContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }
}