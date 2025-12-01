// File: com/example/project_part_3/Users/Admin_UI/Admin_search/Admin_search_view.java

package com.example.project_part_3.Users.Admin_UI.Admin_search;

import android.os.Bundle;
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

import com.example.project_part_3.Events.Event;
import com.example.project_part_3.Image.Image_datamap;
import com.example.project_part_3.R;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;
import java.util.List;

/**
 * Fragment that provides the admin interface for searching through all events and images.
 * Supports keyword filtering and displays combined results in a list.
 */
public class Admin_search_view extends Fragment {

    private Admin_search_model viewModel;
    private Event_and_image_array_adapter adapter;
    private ListView listView;
    private TextInputEditText searchInput;
    private List<Object> fullDataList = new ArrayList<>();

    /**
     * Initializes the ViewModel for this fragment.
     *
     * @param savedInstanceState saved state passed to the fragment
     */
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(this).get(Admin_search_model.class);
    }

    /**
     * Creates and inflates the UI for the admin search screen.
     *
     * @param inflater  the LayoutInflater used to inflate the view
     * @param container the parent container of the fragment
     * @param savedInstanceState saved fragment state
     * @return the inflated view for this fragment
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.admin_search, container, false);

        listView = view.findViewById(R.id.admin_search_list);
        ImageButton searchButton = view.findViewById(R.id.search_button);
        TextInputLayout searchInputLayout = view.findViewById(R.id.Search_input_admin);

        if (searchInputLayout != null) {
            searchInput = (TextInputEditText) searchInputLayout.getEditText();
        }

        adapter = new Event_and_image_array_adapter(getContext(), new ArrayList<>(), viewModel);
        listView.setAdapter(adapter);

        searchButton.setOnClickListener(v -> performSearch());

        return view;
    }

    /**
     * Sets up observers and updates the UI after the view is created.
     *
     * @param view the root view of the fragment
     * @param savedInstanceState saved state passed to the fragment
     */
    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {

        super.onViewCreated(view, savedInstanceState);

        viewModel.getCombinedData().observe(getViewLifecycleOwner(), combinedData -> {
            if (combinedData != null) {
                fullDataList = combinedData;
                performSearch();
            }
        });
    }

    /**
     * Filters the full data list based on the search query and updates the list adapter.
     * Supports searching events by title, location, description, and organizer ID,
     * and images by description.
     */
    private void performSearch() {

        if (searchInput == null || searchInput.getText() == null) {
            adapter.clear();
            adapter.addAll(fullDataList);
            adapter.notifyDataSetChanged();
            return;
        }

        String query = searchInput.getText().toString().toLowerCase().trim();

        if (query.isEmpty()) {
            adapter.clear();
            adapter.addAll(fullDataList);
            adapter.notifyDataSetChanged();
            return;
        }

        ArrayList<Object> filteredResults = new ArrayList<>();

        for (Object item : fullDataList) {
            if (item instanceof Event) {
                Event event = (Event) item;
                if (event.getTitle() != null && event.getTitle().toLowerCase().contains(query)) {
                    filteredResults.add(event);
                } else if (event.getLocationName() != null && event.getLocationName().toLowerCase().contains(query)) {
                    filteredResults.add(event);
                } else if (event.getDescription() != null && event.getDescription().toLowerCase().contains(query)) {
                    filteredResults.add(event);
                } else if (event.getOrganizerId() != null && event.getOrganizerId().toLowerCase().contains(query)) {
                    filteredResults.add(event);
                }
            }
            else if (item instanceof Image_datamap) {
                Image_datamap image = (Image_datamap) item;
                if (image.getDescription() != null &&
                        image.getDescription().toLowerCase().contains(query)) {
                    filteredResults.add(image);
                }
            }
        }

        adapter.clear();

        if (filteredResults.isEmpty()) {
            Toast.makeText(getContext(),
                    "No items found for \"" + query + "\"",
                    Toast.LENGTH_SHORT).show();
        } else {
            adapter.addAll(filteredResults);
        }

        adapter.notifyDataSetChanged();
    }
}
