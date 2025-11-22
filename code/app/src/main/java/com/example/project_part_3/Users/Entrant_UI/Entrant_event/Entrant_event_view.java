package com.example.project_part_3.Users.Entrant_UI.Entrant_event;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.project_part_3.Events.Event;
import com.example.project_part_3.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Entrant_event_view extends Fragment {

    private Entrant_event_model viewModel;
    private entrant_events_adapter adapter;

    public Entrant_event_view() { }

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

        // email was forwarded from Entrant_main_fragment
        String currentUserEmail = getArguments() != null ? getArguments().getString("userEmail") : null;

        adapter = new entrant_events_adapter(
                new ArrayList<>(),
                currentUserEmail,
                entrant_events_adapter.Mode.MY_EVENTS
        );
        rv.setAdapter(adapter);

        // Observe the shared list and filter to “my events” for display
        viewModel.getAllEvents().observe(getViewLifecycleOwner(), allEvents -> {
            if (allEvents == null) {
                adapter.setData(new ArrayList<>());
                adapter.notifyDataSetChanged();
                return;
            }
            List<Event> mine = filterForUser(allEvents, currentUserEmail);
            adapter.setData(mine);
            adapter.notifyDataSetChanged();
        });

        return root;
    }

    private List<Event> filterForUser(List<Event> all, String email) {
        if (all == null || email == null || email.isEmpty()) return new ArrayList<>();
        return all.stream().filter(e -> {
            List<String> w  = e.getWaitlistUserIds();
            List<String> c  = e.getConfirmedUserIds();
            List<String> s  = e.getSelectedUserIds();
            List<String> d  = e.getDeclinedUserIds();
            List<String> alt= e.getAlternatesUserIds();
            return (w  != null && w.contains(email))
                    || (c  != null && c.contains(email))
                    || (s  != null && s.contains(email))
                    || (d  != null && d.contains(email))
                    || (alt!= null && alt.contains(email));
        }).toList();
    }


}
