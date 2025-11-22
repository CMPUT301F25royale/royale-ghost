package com.example.project_part_3.Users.Entrant_UI.Entrant_search;

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
import com.example.project_part_3.Events.Event;
import com.example.project_part_3.R;
import com.example.project_part_3.Users.Entrant_UI.Entrant_event.entrant_events_adapter;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class Entrant_search_view extends Fragment {

    private RecyclerView rv;
    private entrant_events_adapter adapter;
    private String currentUserEmail;
    private List<Event> pendingList; // if data arrives before adapter is ready

    public Entrant_search_view() {}

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.entrant_search, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View root, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(root, savedInstanceState);

        // Read forwarded userEmail from Entrant_main_fragment
        currentUserEmail = (getArguments() != null) ? getArguments().getString("userEmail") : null;

        // Start up Recycler + adapter
        rv = root.findViewById(R.id.searchRecycler);
        rv.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new entrant_events_adapter(new ArrayList<>(), currentUserEmail, entrant_events_adapter.Mode.SEARCH);
        rv.setAdapter(adapter);

        if (pendingList != null) {
            adapter.submitList(pendingList);
            pendingList = null;
        }

        // Load all events from Firestore
        Database db = new Database(FirebaseFirestore.getInstance());
        db.getAllEvents()
                .addOnSuccessListener(events -> submitToAdapter(events))
                .addOnFailureListener(e -> {
                    // Maybe we can log later but for now its whatever
                });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        rv = null;
        adapter = null;
    }

    private void submitToAdapter(List<Event> list) {
        if (!isAdded()) { pendingList = list; return; }
        if (adapter == null) { pendingList = list; return; }
        adapter.submitList(list != null ? list : new ArrayList<>());
    }
}
