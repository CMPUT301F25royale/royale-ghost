package com.example.project_part_3.Users.Organizer_UI.Organizer_create_event;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import com.example.project_part_3.Database_functions.Database;
import com.example.project_part_3.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.firestore.FirebaseFirestore;

public class Organizer_create_event extends Fragment {
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.organizer_create_event, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        BottomNavigationView bottomNav = view.findViewById(R.id.organizer_bottom_nav);
        NavHostFragment navHostFragment = (NavHostFragment) getChildFragmentManager()
                .findFragmentById(R.id.organizer_nav_host_fragment);

        if (navHostFragment != null) {
            NavController navController = navHostFragment.getNavController();
            NavigationUI.setupWithNavController(bottomNav, navController);
        }

        EditText titleEditText = view.findViewById(R.id.create_event_title_input);
        EditText descriptionEditText = view.findViewById(R.id.create_event_description_input);
        EditText capacityEditText = view.findViewById(R.id.create_event_capacity_input);
        EditText registrationOpenEditText = view.findViewById(R.id.create_event_registration_open_input);
        EditText registrationCloseEditText = view.findViewById(R.id.create_event_registration_close_input);
        EditText locationEditText = view.findViewById(R.id.create_event_location_input);
        EditText priceEditText = view.findViewById(R.id.cretae_event_price_input);



        FirebaseFirestore ff = FirebaseFirestore.getInstance();
        Database db = new Database(ff);
        Button publishButton = view.findViewById(R.id.create_event_publish_button);
        publishButton.setOnClickListener(v -> {
            String title = titleEditText.getText().toString();
            String description = descriptionEditText.getText().toString();
            Integer capacity = Integer.parseInt(capacityEditText.getText().toString());
            String location = locationEditText.getText().toString();
            String registrationOpen = registrationOpenEditText.getText().toString();
            String registrationClose = registrationCloseEditText.getText().toString();
            Float price = Float.parseFloat(priceEditText.getText().toString());


        });
    }
}
