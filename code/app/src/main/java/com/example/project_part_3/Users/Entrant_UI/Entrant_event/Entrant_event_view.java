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

import com.example.project_part_3.Database_functions.EventDatabase;
import com.example.project_part_3.R;

public class Entrant_event_view extends Fragment {

    public Entrant_event_view() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.entrant_main, container, false);
        RecyclerView rv = root.findViewById(R.id.eventsRecycler);
        rv.setLayoutManager(new LinearLayoutManager(requireContext()));

        // Fetch from session
        String currentUserEmail= getArguments() != null ? getArguments().getString("userEmail") : null;

        entrant_events_adapter adapter = new entrant_events_adapter(EventDatabase.getInstance().getAllEvents(), currentUserEmail);
        rv.setAdapter(adapter);

        // You can drop this in to refresh: (I need to test this, commented)
        // adapter.submitList(EventDatabase.getInstance().getAllEvents());

        return root;
    }
}
