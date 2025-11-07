package com.example.project_part_3.Users.Entrant_UI;

import android.os.Bundle;
import android.view.*;
import androidx.annotation.*;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.project_part_3.R;
import com.example.project_part_3.Events.Event;

public class Entrant_search extends Fragment {

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.entrant_search, container, false);
    }

    @Override public void onViewCreated(@NonNull View v, @Nullable Bundle s) {
        RecyclerView rv = v.findViewById(R.id.eventsRecycler);
        rv.setLayoutManager(new LinearLayoutManager(requireContext()));
        rv.setAdapter(new SearchResultsAdapter(
                Event.sample(),
                eventId -> Navigation.findNavController(v)
                        .navigate(R.id.action_search_to_details, Event.bundle(eventId))
        ));
    }
}
