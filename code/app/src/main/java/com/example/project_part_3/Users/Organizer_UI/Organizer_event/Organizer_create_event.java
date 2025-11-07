package com.example.project_part_3.Users.Organizer_UI.Organizer_event;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TimePicker;
import android.widget.Toast; // Import this

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import com.example.project_part_3.Database_functions.Database;
// import com.example.project_part_3.DialogFragments.DatePickerDialogFragment; // 1. No longer needed
import com.example.project_part_3.Events.Event;
import com.example.project_part_3.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class Organizer_create_event extends Fragment {

    private Date registrationOpenDate;
    private Date registrationCloseDate;

    private interface DateSetter {
        void setDate(Date date);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.organizer_create_event, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setupNavigation(view);

        EditText titleEditText = view.findViewById(R.id.create_event_title_input);
        EditText descriptionEditText = view.findViewById(R.id.create_event_description_input);
        EditText capacityEditText = view.findViewById(R.id.create_event_capacity_input);
        EditText locationEditText = view.findViewById(R.id.create_event_location_input);
        EditText priceEditText = view.findViewById(R.id.cretae_event_price_input);

        Button registrationOpenButton = view.findViewById(R.id.create_event_registration_open_button);
        Button registrationCloseButton = view.findViewById(R.id.create_event_registration_close_button);
        Button timeButton = view.findViewById(R.id.create_event_time_value);

        setupTimePicker(timeButton);
        setupDatePicker(registrationOpenButton, date -> registrationOpenDate = date);
        setupDatePicker(registrationCloseButton, date -> registrationCloseDate = date);

        FirebaseFirestore ff = FirebaseFirestore.getInstance();
        Database db = new Database(ff);
        Button publishButton = view.findViewById(R.id.create_event_publish_button);

        publishButton.setOnClickListener(v -> {
            publishEvent(titleEditText, descriptionEditText, capacityEditText, locationEditText, priceEditText);
        });
    }

    private void setupNavigation(View view) {
        BottomNavigationView bottomNav = view.findViewById(R.id.organizer_bottom_nav);
        NavHostFragment navHostFragment = (NavHostFragment) getChildFragmentManager()
                .findFragmentById(R.id.organizer_nav_host_fragment);

        if (navHostFragment != null) {
            NavController navController = navHostFragment.getNavController();
            NavigationUI.setupWithNavController(bottomNav, navController);
        }
    }

    private void setupTimePicker(Button timeButton) {
        timeButton.setOnClickListener(v -> {
            final Calendar c = Calendar.getInstance();
            int currentHour = c.get(Calendar.HOUR_OF_DAY);
            int currentMinute = c.get(Calendar.MINUTE);

            TimePickerDialog.OnTimeSetListener timeSetListener = (view3, hourOfDay, minute) -> {
                String timeString = String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minute);
                timeButton.setText(timeString);
            };

            TimePickerDialog timePickerDialog = new TimePickerDialog(
                    getContext(),
                    timeSetListener,
                    currentHour,
                    currentMinute,
                    true
            );

            timePickerDialog.show();
        });
    }

    private void setupDatePicker(Button button, DateSetter dateSetter) {
        button.setOnClickListener(v -> {
            final Calendar c = Calendar.getInstance();
            int year = c.get(Calendar.YEAR);
            int month = c.get(Calendar.MONTH);
            int day = c.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog.OnDateSetListener dateSetListener = (view, year1, month1, dayOfMonth) -> {
                Calendar calendar = Calendar.getInstance();
                calendar.set(year1, month1, dayOfMonth);

                Date selectedDate = calendar.getTime();
                dateSetter.setDate(selectedDate);

                String dateString = DateFormat.getDateInstance().format(selectedDate);
                button.setText(dateString);
            };

            DatePickerDialog datePickerDialog = new DatePickerDialog(getContext(), dateSetListener, year, month, day);
            datePickerDialog.show();
        });
    }

    private void publishEvent(EditText titleEditText, EditText descriptionEditText, EditText capacityEditText, EditText locationEditText, EditText priceEditText) {
        String title = titleEditText.getText().toString();
        String description = descriptionEditText.getText().toString();
        Integer capacity = Integer.parseInt(capacityEditText.getText().toString());
        String location = locationEditText.getText().toString();
        Float price = Float.parseFloat(priceEditText.getText().toString());

        if (registrationOpenDate == null || registrationCloseDate == null) {
            Toast.makeText(getContext(), "Please select both registration dates", Toast.LENGTH_SHORT).show();
        }

        //TODO: Publish Event!!!
    }
}