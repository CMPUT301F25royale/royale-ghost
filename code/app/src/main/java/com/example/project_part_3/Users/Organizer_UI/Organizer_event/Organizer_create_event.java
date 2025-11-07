package com.example.project_part_3.Users.Organizer_UI.Organizer_event;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Toast; // Import this

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import com.example.project_part_3.Database_functions.Database;
// import com.example.project_part_3.DialogFragments.DatePickerDialogFragment; // 1. No longer needed
import com.example.project_part_3.Events.Event;
import com.example.project_part_3.R;
import com.example.project_part_3.Users.Organizer_UI.OrganizerSharedViewModel;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;

// 2. Remove "implements DatePickerDialog.OnDateSetListener"
public class Organizer_create_event extends Fragment {

    // 3. Keep two separate Date objects
    private Date registrationOpenDate;
    private Date registrationCloseDate;
    private OrganizerSharedViewModel model;
    private String organizerEmail;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        model = new ViewModelProvider(requireActivity()).get(OrganizerSharedViewModel.class);
    }
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        model.getUserEmail().observe(getViewLifecycleOwner(), email -> {
            organizerEmail = email;
        });
        return inflater.inflate(R.layout.organizer_create_event, container, false);

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Button back = view.findViewById(R.id.back);
        back.setOnClickListener(v-> {
            NavController navBack = NavHostFragment.findNavController(this);
            navBack.navigate(R.id.create_to_event);
        });

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
        EditText locationEditText = view.findViewById(R.id.create_event_location_input);
        EditText priceEditText = view.findViewById(R.id.cretae_event_price_input);

        Button registrationOpenButton = view.findViewById(R.id.create_event_registration_open_button);
        Button registrationCloseButton = view.findViewById(R.id.create_event_registration_close_button);

        registrationOpenButton.setOnClickListener(v -> {
            final Calendar c = Calendar.getInstance();
            int year = c.get(Calendar.YEAR);
            int month = c.get(Calendar.MONTH);
            int day = c.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog.OnDateSetListener openDateSetListener = (view1, year1, month1, dayOfMonth) -> {
                Calendar calendar = Calendar.getInstance();
                calendar.set(year1, month1, dayOfMonth);

                registrationOpenDate = calendar.getTime();

                String dateString = DateFormat.getDateInstance().format(registrationOpenDate);
                registrationOpenButton.setText(dateString);
            };

            DatePickerDialog datePickerDialog = new DatePickerDialog(getContext(), openDateSetListener, year, month, day);
            datePickerDialog.show();
        });

        registrationCloseButton.setOnClickListener(v -> {
            final Calendar c = Calendar.getInstance();
            int year = c.get(Calendar.YEAR);
            int month = c.get(Calendar.MONTH);
            int day = c.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog.OnDateSetListener closeDateSetListener = (view2, year2, month2, dayOfMonth) -> {
                Calendar calendar = Calendar.getInstance();
                calendar.set(year2, month2, dayOfMonth);

                registrationCloseDate = calendar.getTime();

                String dateString = DateFormat.getDateInstance().format(registrationCloseDate);
                registrationCloseButton.setText(dateString);
            };

            DatePickerDialog datePickerDialog = new DatePickerDialog(getContext(), closeDateSetListener, year, month, day);
            datePickerDialog.show();
        });


        FirebaseFirestore ff = FirebaseFirestore.getInstance();
        Database db = new Database(ff);
        Button publishButton = view.findViewById(R.id.create_event_publish_button);


        publishButton.setOnClickListener(v -> {
            String title = titleEditText.getText().toString().trim();
            String description = descriptionEditText.getText().toString().trim();
            String capacityStr = capacityEditText.getText().toString().trim();
            String location = locationEditText.getText().toString().trim();
            String priceStr = priceEditText.getText().toString().trim();
            if (title.isEmpty() || description.isEmpty() || capacityStr.isEmpty() || location.isEmpty() || priceStr.isEmpty()) {
                Toast.makeText(getContext(), "Please fill in all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            if (registrationOpenDate == null || registrationCloseDate == null) {
                Toast.makeText(getContext(), "Please select both registration dates", Toast.LENGTH_SHORT).show();
                return;
            }

            if (registrationOpenDate.after(registrationCloseDate)) {
                Toast.makeText(getContext(), "Registration open date must be before the close date", Toast.LENGTH_SHORT).show();
                return;
            }
            Integer capacity;
            Float price;
            try {
                capacity = Integer.parseInt(capacityStr);
                price = Float.parseFloat(priceStr);
            } catch (NumberFormatException e) {
                Toast.makeText(getContext(), "Invalid number for capacity or price", Toast.LENGTH_SHORT).show();
                return;
            }

            String organizerId = organizerEmail;
            Event newEvent = new Event(
                    null,
                    organizerId,
                    title,
                    description,
                    location,
                    location,
                    null,
                    registrationOpenDate.getTime(),
                    registrationCloseDate.getTime(),
                    registrationCloseDate.getTime(),
                    null,
                    capacity,
                    price
            );

            db.addEvent(newEvent).addOnSuccessListener(success -> {
                if (success) {
                    Toast.makeText(getContext(), "Event published successfully!", Toast.LENGTH_SHORT).show();
                    NavController navController = NavHostFragment.findNavController(this);
                    navController.navigate(R.id.create_to_event);
                } else {
                    Toast.makeText(getContext(), "Failed to publish event.", Toast.LENGTH_SHORT).show();
                }
            }).addOnFailureListener(e -> {
                Toast.makeText(getContext(), "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
            });
        });
    }
}
