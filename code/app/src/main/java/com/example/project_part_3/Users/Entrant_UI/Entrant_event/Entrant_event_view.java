package com.example.project_part_3.Users.Entrant_UI.Entrant_event;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider; // <-- Add this import
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

// Removed direct EventDatabase import as it's no longer needed here
import com.example.project_part_3.R;

public class Entrant_event_view extends Fragment {

    private Entrant_event_model viewModel;
    private entrant_events_adapter adapter;

    public Entrant_event_view() {

    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(this).get(Entrant_event_model.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.entrant_main, container, false);
        RecyclerView rv = root.findViewById(R.id.eventsRecycler);
        rv.setLayoutManager(new LinearLayoutManager(requireContext()));

        String currentUserEmail = getArguments() != null ? getArguments().getString("userEmail") : null;

        adapter = new entrant_events_adapter(currentUserEmail);
        rv.setAdapter(adapter);

        viewModel.getAllEvents().observe(getViewLifecycleOwner(), events -> {
            if (events != null) {
                adapter.setData(events);
                adapter.notifyDataSetChanged();
            }
        });

        return root;
    }
}
