package com.example.project_part_3.Users.Entrant_UI.Entrant_event;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.project_part_3.Database_functions.Database;
import com.example.project_part_3.Database_functions.EventDatabase; // if still using local stub somewhere
import com.example.project_part_3.Events.Event;
import com.example.project_part_3.R;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class Entrant_event_view extends Fragment {

    private RecyclerView rv;
    private entrant_events_adapter adapter;
    private String currentUserEmail;
    private List<Event> pendingList;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.entrant_main, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View root, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(root, savedInstanceState);

        // Read user ID
        currentUserEmail = (getArguments() != null) ? getArguments().getString("userEmail") : null;
        rv = root.findViewById(R.id.eventsRecycler);
        rv.setLayoutManager(new LinearLayoutManager(requireContext()));

        adapter = new entrant_events_adapter(
                new ArrayList<>(),
                currentUserEmail,
                entrant_events_adapter.Mode.MY_EVENTS // Set the mode to determine UI display elements from adapter
        );
        rv.setAdapter(adapter);

        // This was necessary in case data arrived before adapter was ready, would always crash otherwise
        if (pendingList != null) {
            adapter.submitList(pendingList);
            pendingList = null;
        }

        // Adapter is loaded, now we can load events
        loadEventsForUser(currentUserEmail);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Clear references to avoid using a destroyed view
        rv = null;
        adapter = null;
    }

    private void loadEventsForUser(@Nullable String email) {
        // Replace this with your real Firestore query that filters by email membership
        // Example: using a Database helper that returns Task<List<Event>>
        Database db = new Database(FirebaseFirestore.getInstance());
        db.getAllEvents()
                .addOnSuccessListener(events -> {
                    // Only events the user signed up for
                    List<Event> filtered = filterForUser(events, email);
                    submitToAdapter(filtered);
                })
                .addOnFailureListener(e -> {
                });
    }

    private List<Event> filterForUser(List<Event> all, @Nullable String email) {
        if (all == null) return new ArrayList<>();
        if (email == null || email.isEmpty()) return new ArrayList<>();

        List<Event> mine = new ArrayList<>();
        for (Event e : all) {
            try {
                if (e.getAttendant_list() != null && e.getAttendant_list().contains(email)) {
                    mine.add(e);
                }
            } catch (Exception ignored) {}
        }
        return mine;
    }

    private void submitToAdapter(List<Event> list) {
        // Fragment might no longer be attached or view destroyed when callback fires
        if (!isAdded()) {
            // Not attached — store for later in case view re-creates
            pendingList = list;
            return;
        }
        if (adapter == null) {
            // View recreated but adapter not set yet — stash it
            pendingList = list;
            return;
        }
        adapter.submitList(list);
    }
}
