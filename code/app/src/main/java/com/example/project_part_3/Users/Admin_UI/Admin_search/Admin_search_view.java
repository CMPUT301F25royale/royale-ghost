package com.example.project_part_3.Users.Admin_UI.Admin_search;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.project_part_3.Database_functions.Database;
import com.example.project_part_3.Database_functions.ImageDatabase;
import com.example.project_part_3.Events.Event;
import com.example.project_part_3.Image.Image_holder;
import com.example.project_part_3.R;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class Admin_search_view extends Fragment {

    private Database db;
    private ImageDatabase imagedb;
    private ArrayList<Object> objectList;
    private Event_and_image_array_adapter adapter;

    private ListView listView;
    private TextInputEditText searchInput;
    private ImageButton searchButton;

    private List<Event> allEvents = new ArrayList<>();
    private List<Image_holder> allImages = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.admin_search, container, false);
        db = new Database(FirebaseFirestore.getInstance());
        imagedb = ImageDatabase.getInstance();
        listView = view.findViewById(R.id.admin_search_list);
        searchButton = view.findViewById(R.id.search_button);
        TextInputLayout searchInputLayout = view.findViewById(R.id.Search_input_admin);

        if (searchInputLayout != null) {
            searchInput = (TextInputEditText) searchInputLayout.getEditText();
        }

        objectList = new ArrayList<>();
        adapter = new Event_and_image_array_adapter(getContext(), objectList);
        listView.setAdapter(adapter);

        loadInitialData();

        searchButton.setOnClickListener(v -> performSearch());

        listView.setOnItemClickListener((parent, view1, position, id) -> {
            Object selectedItem = adapter.getItem(position);
            if (selectedItem instanceof Event) {
                Toast.makeText(getContext(), "Clicked on event: " + ((Event) selectedItem).getTitle(), Toast.LENGTH_SHORT).show();
            } else if (selectedItem instanceof Image_holder) {
                Toast.makeText(getContext(), "Clicked on an image.", Toast.LENGTH_SHORT).show();
            }
        });

        return view;
    }

    private void loadInitialData() {
        allEvents.clear();
        allImages.clear();
        db.getAllEvents().addOnSuccessListener(events -> {
            allEvents.addAll(events);
            updateAdapterWithAllData();
        }).addOnFailureListener(e -> {
            Log.e("AdminSearch", "Error fetching events", e);
            Toast.makeText(getContext(), "Failed to load events.", Toast.LENGTH_SHORT).show();
        });

        allImages.addAll(imagedb.getAllImages());
        updateAdapterWithAllData();
    }

    private void updateAdapterWithAllData() {
        objectList.clear();
        objectList.addAll(allEvents);
        objectList.addAll(allImages);
        adapter.notifyDataSetChanged();
    }

    private void performSearch() {
        if (searchInput == null || searchInput.getText() == null) {
            return;
        }

        String query = searchInput.getText().toString().toLowerCase().trim();
        if (query.isEmpty()) {
            updateAdapterWithAllData();
            return;
        }

        ArrayList<Object> filteredResults = new ArrayList<>();
        for (Event event : allEvents) {
            if (event.getTitle() != null && event.getTitle().toLowerCase().contains(query)) {
                filteredResults.add(event);
            }
        }
        for (Image_holder image : allImages) {
            if (image.getDescription() != null && image.getDescription().toLowerCase().contains(query)) {
                filteredResults.add(image);
            }
        }

        adapter.clear();
        if (filteredResults.isEmpty()) {
            Toast.makeText(getContext(), "No items found for \"" + query + "\"", Toast.LENGTH_SHORT).show();
        } else {
            adapter.addAll(filteredResults);
        }
        adapter.notifyDataSetChanged();
    }
}


