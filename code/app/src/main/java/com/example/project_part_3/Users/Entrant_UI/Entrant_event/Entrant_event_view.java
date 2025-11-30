package com.example.project_part_3.Users.Entrant_UI.Entrant_event;

import android.os.Build;
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

/**
 * Fragment responsible for displaying the list of events associated with the currently logged-in
 * entrant user.
 */
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
        String currentUserEmail = getArguments() != null ? getArguments().getString("userEmail") : null;

        adapter = new entrant_events_adapter(
                new ArrayList<>(),
                currentUserEmail,
                entrant_events_adapter.Mode.MY_EVENTS
        );
        rv.setAdapter(adapter);

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

    /**
     * Filters for events that involve a specific user and returns the
     * events as a list
     *
     * @param all list of all events to filter from
     * @param email email of the user to filter for
     * @return list of events that involve the user
     */
    private List<Event> filterForUser(List<Event> all, String email) {
        if (all == null || email == null || email.isEmpty()) return new ArrayList<>();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
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
        return all;
    }


}
