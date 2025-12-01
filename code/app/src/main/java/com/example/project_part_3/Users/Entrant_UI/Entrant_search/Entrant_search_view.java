package com.example.project_part_3.Users.Entrant_UI.Entrant_search;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.content.Context;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.project_part_3.Database_functions.Database;
import com.example.project_part_3.Events.Event;
import com.example.project_part_3.R;
import com.example.project_part_3.Users.Entrant_UI.Entrant_event.Entrant_event_model;
import com.example.project_part_3.Users.Entrant_UI.Entrant_event.entrant_events_adapter;
import com.google.android.material.chip.Chip;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Fragment responsible for displaying the search page for entrants.
 */
public class Entrant_search_view extends Fragment {

    private Entrant_event_model viewModel;
    private entrant_events_adapter adapter;

    private TextInputEditText etSearch;
    private RecyclerView recycler;

    private Chip interestsToggle;

    private final List<Event> allEvents = new ArrayList<>();
    private String currentQuery = "";
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final long DEBOUNCE_MS = 250;

    private final Runnable filterRunnable = this::applyFilter;

    private List<String> userInterests = new ArrayList<>();

    public Entrant_search_view() {}

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(requireActivity()).get(Entrant_event_model.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.entrant_search, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(v, savedInstanceState);

        etSearch = v.findViewById(R.id.etSearch);
        recycler = v.findViewById(R.id.searchRecycler);
        interestsToggle = v.findViewById(R.id.chipInterests);


        recycler.setLayoutManager(new LinearLayoutManager(requireContext()));

        String currentUserEmail = getArguments() != null ? getArguments().getString("userEmail") : null;

        adapter = new entrant_events_adapter(
                new ArrayList<>(),
                currentUserEmail,
                entrant_events_adapter.Mode.SEARCH
        );
        recycler.setAdapter(adapter);

        // Live event list
        viewModel.getAllEvents().observe(getViewLifecycleOwner(), events -> {
            allEvents.clear();
            if (events != null) allEvents.addAll(events);
            applyFilter(); // Rerun query whenever the source list changes
        });

        Database db = new Database(FirebaseFirestore.getInstance());

        if (currentUserEmail != null) {
            db.getInterests(currentUserEmail)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful() && task.getResult() != null) {
                            userInterests = new ArrayList<>(task.getResult());
                        } else {
                            userInterests = new ArrayList<>();
                        }

                        applyFilter();
                    });
        }

        interestsToggle.setOnCheckedChangeListener((button, isChecked) -> {
            handler.removeCallbacks(filterRunnable);
            handler.post(filterRunnable);
        });

        // Search typing, Textwatcher fires on each keystroke
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override public void afterTextChanged(Editable s) {
                currentQuery = s != null ? s.toString().trim() : "";
                handler.removeCallbacks(filterRunnable);
                handler.postDelayed(filterRunnable, DEBOUNCE_MS);
            }
        });

        etSearch.setOnEditorActionListener((tv, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH ||
                    (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_DOWN)) {
                hideKeyboard();
                applyFilter();
                return true;
            }
            return false;
        });
    }

    private void applyFilter() {
        if (!isAdded()) return;

        final String q = currentQuery.toLowerCase(Locale.US);
        final boolean interestsMode = interestsToggle != null && interestsToggle.isChecked();

        // If no text and interests mode is off → show everything
        if (q.isEmpty() && !interestsMode) {
            adapter.setData(allEvents);
            recycler.scrollToPosition(0);
            return;
        }

        List<Event> filtered = new ArrayList<>();

        for (Event e : allEvents) {
            if (e == null) continue;

            String title = safe(e.getTitle());
            String location = safe(e.getLocation());
            String description = safe(e.getDescription());
            String organizer = safe(e.getOrganizerId());

            boolean matchesText;
            if (q.isEmpty()) {
                // No query → don't restrict by text
                matchesText = true;
            } else {
                matchesText =
                        title.contains(q) ||
                                location.contains(q) ||
                                description.contains(q) ||
                                organizer.contains(q);
            }

            boolean matchesInterest;
            if (!interestsMode) {
                // Chip is off → ignore interests
                matchesInterest = true;
            } else if (userInterests == null || userInterests.isEmpty()) {
                // Chip on but user has no interests → don't filter by interests
                matchesInterest = true;
            } else {
                matchesInterest = false;
                for (String interest : userInterests) {
                    String term = safe(interest);
                    if (term.isEmpty()) continue;

                    // Match interest in description OR title
                    if (description.contains(term) || title.contains(term)) {
                        matchesInterest = true;
                        break;
                    }
                }
            }

            if (matchesText && matchesInterest) {
                filtered.add(e);
            }
        }

        adapter.setData(filtered);
        recycler.scrollToPosition(0);
    }

    private String safe(String s) {
        return s == null ? "" : s.toLowerCase(Locale.US);
    }

    private void hideKeyboard() {
        try {
            InputMethodManager imm = (InputMethodManager) requireContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            View view = requireActivity().getCurrentFocus();
            if (imm != null && view != null) {
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            }
        } catch (Exception ignored) {}
    }
}
