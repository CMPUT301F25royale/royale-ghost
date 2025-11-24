package com.example.project_part_3.Users.Organizer_UI.Organizer_event;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import com.example.project_part_3.Database_functions.Database;
import com.example.project_part_3.Events.Event;
import com.example.project_part_3.R;
import com.example.project_part_3.Users.Organizer_UI.OrganizerSharedViewModel;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public abstract class Organizer_create_edit_event_template extends Fragment {

    protected Date registrationOpenDate;
    protected Date registrationCloseDate;
    protected Date eventStartDate;
    protected Date eventEndDate;

    protected String organizerEmail;
    protected Event selectedEvent;

    protected OrganizerSharedViewModel model;

    protected EditText titleEditText;
    protected EditText descriptionEditText;
    protected EditText capacityEditText;
    protected EditText locationEditText;
    protected EditText priceEditText;

    protected Button openDateButton;
    protected Button closeDateButton;
    protected Button startDateButton;
    protected Button endDateButton;

    protected interface DateSelectionCallback {
        void onDateSelected(Date date);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        model = new ViewModelProvider(requireActivity()).get(OrganizerSharedViewModel.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.organizer_create_event, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initializeViews(view);
        setupNavigation(view);
        setupDateButtons(view);
        setupObservers();
        setupPublishButton(view);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // Clear the selected event when the fragment is destroyed
        model.setSelectedEvent(null);
    }

    protected void initializeViews(@NonNull View view) {
        titleEditText = view.findViewById(R.id.create_event_title_input);
        descriptionEditText = view.findViewById(R.id.create_event_description_input);
        capacityEditText = view.findViewById(R.id.create_event_capacity_input);
        locationEditText = view.findViewById(R.id.create_event_location_input);
        priceEditText = view.findViewById(R.id.create_event_price_input);
        openDateButton = view.findViewById(R.id.create_event_registration_open_button);
        closeDateButton = view.findViewById(R.id.create_event_registration_close_button);
        startDateButton = view.findViewById(R.id.create_event_date_time_start_button);
        endDateButton = view.findViewById(R.id.create_event_date_time_end_button);
    }

    protected void setupNavigation(@NonNull View view) {
        setupBackButton(view);
        setupBottomNavigation(view);
    }

    /**
     * Sets up the listeners for all date/time buttons using a helper method.
     */
    protected void setupDateButtons(@NonNull View view) {
        setupDateTimePicker(openDateButton, date -> registrationOpenDate = date);
        setupDateTimePicker(closeDateButton, date -> registrationCloseDate = date);
        setupDateTimePicker(startDateButton, date -> eventStartDate = date);
        setupDateTimePicker(endDateButton, date -> eventEndDate = date);
    }

    protected void setupObservers() {
        model.getUserEmail().observe(getViewLifecycleOwner(), email -> organizerEmail = email);

        model.getSelectedEvent().observe(getViewLifecycleOwner(), event -> {
            selectedEvent = event;
            // Only populate if event exists (Edit Mode)
            if (event != null) {
                populateFields(selectedEvent);
            }
        });
    }

    protected abstract void setupBackButton(@NonNull View view);

    /**
     * Sets up the BottomNavigationView with the nested NavHostFragment.
     * @param view The fragment's root view.
     */
    protected void setupBottomNavigation(@NonNull View view) {
        BottomNavigationView bottomNav = view.findViewById(R.id.organizer_bottom_nav);
        NavHostFragment navHostFragment = (NavHostFragment) getChildFragmentManager()
                .findFragmentById(R.id.organizer_nav_host_fragment);

        if (navHostFragment != null) {
            NavController navController = navHostFragment.getNavController();
            NavigationUI.setupWithNavController(bottomNav, navController);
        }
    }

    /**
     * Sets up the listener for the registration open date button to show a DatePickerDialog.
     * @param view The fragment's root view.
     * @param c The calendar instance used for date selection.
     */
    protected void setupRegistrationOpenButton(@NonNull View view, Calendar c) {
        openDateButton.setOnClickListener(v -> {
            int year = c.get(Calendar.YEAR);
            int month = c.get(Calendar.MONTH);
            int day = c.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePickerDialog = new DatePickerDialog(getContext(), (view1, year1, month1, dayOfMonth) -> {
                Calendar calendar = Calendar.getInstance();
                calendar.set(year1, month1, dayOfMonth);
                registrationOpenDate = calendar.getTime();
                String dateString = DateFormat.getDateInstance().format(registrationOpenDate);
                openDateButton.setText(dateString);
            }, year, month, day);

            datePickerDialog.show();
        });
    }

    /**
     * Sets up the listener for the registration close date button to show a DatePickerDialog.
     * @param view The fragment's root view.
     * @param c The calendar instance used for date selection.
     */
    protected void setupRegistrationCloseButton(@NonNull View view, Calendar c) {
        closeDateButton.setOnClickListener(v -> {
            int year = c.get(Calendar.YEAR);
            int month = c.get(Calendar.MONTH);
            int day = c.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePickerDialog = new DatePickerDialog(getContext(), (view2, year2, month2, dayOfMonth) -> {
                Calendar calendar = Calendar.getInstance();
                calendar.set(year2, month2, dayOfMonth);
                registrationCloseDate = calendar.getTime();
                String dateString = DateFormat.getDateInstance().format(registrationCloseDate);
                closeDateButton.setText(dateString);
            }, year, month, day);

            datePickerDialog.show();
        });
    }

    /**
     * Initializes the views and listener for the publish event button.
     * @param view The fragment's root view.
     */
    protected void setupPublishButton(@NonNull View view) {
        FirebaseFirestore ff = FirebaseFirestore.getInstance();
        Database db = new Database(ff);
        Button publishButton = view.findViewById(R.id.create_event_publish_button);

        publishButton.setOnClickListener(v -> handlePublishClick(db));
    }

    /**
     * Handles the logic for publishing an event, including validation, parsing, and database insertion.
     * @param db Database instance.
     */
    protected void handlePublishClick(Database db) {
        String title = titleEditText.getText().toString().trim();
        String description = descriptionEditText.getText().toString().trim();
        String location = locationEditText.getText().toString().trim();
        String capacityStr = capacityEditText.getText().toString().trim();
        String priceStr = priceEditText.getText().toString().trim();

        // must fill in only mandatory fields
        if (title.isEmpty() || description.isEmpty() || location.isEmpty()) {
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

        if (eventStartDate == null || eventEndDate == null) {
            Toast.makeText(getContext(), "Please select both event dates", Toast.LENGTH_SHORT).show();
            return;
        }

        if (eventStartDate.after(eventEndDate)) {
            Toast.makeText(getContext(), "Event start date must be before the end date", Toast.LENGTH_SHORT).show();
            return;
        }

        // optional fields
        Integer capacity = null;
        Float price = null;

        if (!capacityStr.isEmpty()) {
            try {
                capacity = Integer.parseInt(capacityStr);
            } catch (NumberFormatException e) {
                Toast.makeText(getContext(), "Invalid number for capacity", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        if (!priceStr.isEmpty()) {
            try {
                price = Float.parseFloat(priceStr);
            } catch (NumberFormatException e) {
                Toast.makeText(getContext(), "Invalid number for price", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        // if selectedEvent is null, it's a new event (pass null ID), else use existing ID
        Event newEvent;
        if (selectedEvent != null) {
            newEvent = new Event(
                    selectedEvent.getId(), // use the ID of the selected event
                    organizerEmail,
                    title,
                    description,
                    location,
                    location,
                    null,
                    registrationOpenDate.getTime(),
                    registrationCloseDate.getTime(),
                    eventStartDate.getTime(),
                    eventEndDate.getTime(),
                    capacity, // optional, may be null
                    price // optional, may be null
            );
        } else {
            // else, create a new ID
            newEvent = new Event(
                    organizerEmail,
                    title,
                    description,
                    location,
                    location,
                    null,
                    registrationOpenDate.getTime(),
                    registrationCloseDate.getTime(),
                    eventStartDate.getTime(),
                    eventEndDate.getTime(),
                    capacity, // optional, may be null
                    price // optional, may be null
            );
        }

        pushEventToDatabase(db, newEvent);
    }

    /**
     * Generic helper to show a DatePicker followed immediately by a TimePicker.
     * @param button The button to attach the listener to and update text.
     * @param callback Interface to save the selected Date object to the correct variable.
     */
    protected void setupDateTimePicker(Button button, DateSelectionCallback callback) {
        button.setOnClickListener(v -> {
            final Calendar calendar = Calendar.getInstance();
            int currentYear = calendar.get(Calendar.YEAR);
            int currentMonth = calendar.get(Calendar.MONTH);
            int currentDay = calendar.get(Calendar.DAY_OF_MONTH);
            int currentHour = calendar.get(Calendar.HOUR_OF_DAY);
            int currentMinute = calendar.get(Calendar.MINUTE);

            DatePickerDialog datePickerDialog = new DatePickerDialog(getContext(), (view1, year, month, dayOfMonth) -> {

                TimePickerDialog timePickerDialog = new TimePickerDialog(getContext(), (view2, hourOfDay, minute) -> {

                    Calendar resultCalendar = Calendar.getInstance();
                    resultCalendar.set(year, month, dayOfMonth, hourOfDay, minute);
                    Date selectedDate = resultCalendar.getTime();

                    SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy\nHH:mm a");
                    String formattedDate = sdf.format(selectedDate);

                    button.setText(formattedDate);
                    callback.onDateSelected(selectedDate);

                }, currentHour, currentMinute, false); // false = 12h format, true = 24h

                timePickerDialog.show();

            }, currentYear, currentMonth, currentDay);

            datePickerDialog.show();
        });
    }

    protected abstract void pushEventToDatabase(Database db, Event event);

    protected void populateFields(Event event) {
        // Hook to populate fields with event data (implemented in subclasses)
    }
}