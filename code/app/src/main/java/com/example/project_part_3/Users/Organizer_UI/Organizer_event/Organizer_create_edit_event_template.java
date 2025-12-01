package com.example.project_part_3.Users.Organizer_UI.Organizer_event;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import com.bumptech.glide.Glide;
import com.example.project_part_3.Database_functions.Database;
import com.example.project_part_3.Events.Event;
import com.example.project_part_3.R;
import com.example.project_part_3.Users.Organizer_UI.OrganizerSharedViewModel;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.materialswitch.MaterialSwitch;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public abstract class Organizer_create_edit_event_template extends Fragment {
    protected MaterialSwitch geolocationSwitch;
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
    protected ImageView eventImageView;
    protected Uri imageUri;

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
        setupImageUpload(view);
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
        eventImageView = view.findViewById(R.id.eventImage);
        geolocationSwitch = view.findViewById(R.id.create_event_geolocation_switch);
    }

    private void setupImageUpload(View view) {
        Button posterButton = view.findViewById(R.id.create_event_poster_button);
        posterButton.setOnClickListener(v -> showImagePopup());
    }

    private void showImagePopup() {
        if (!isAdded()) return;
        LayoutInflater inflater = LayoutInflater.from(requireContext());
        View dialogView = inflater.inflate(R.layout.image_popup, null);

        ImageView popupImagePreview = dialogView.findViewById(R.id.popup_image_preview);
        Button changeImageButton = dialogView.findViewById(R.id.popup_change_image_button);

        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setView(dialogView)
                .create();

        if (eventImageView.getDrawable() != null) {
            Glide.with(requireContext())
                    .load(eventImageView.getDrawable())
                    .into(popupImagePreview);
        }

        changeImageButton.setOnClickListener(v -> {
            galleryLauncher.launch("image/*");
            dialog.dismiss();
        });

        dialog.show();
    }

    private final ActivityResultLauncher<String> galleryLauncher = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    imageUri = uri;
                    if (isAdded() && eventImageView != null) {
                        eventImageView.setVisibility(View.VISIBLE);
                        Glide.with(requireContext())
                                .load(imageUri)
                                .placeholder(R.drawable.ic_launcher_foreground)
                                .error(R.drawable.ic_launcher_foreground)
                                .into(eventImageView);
                    }
                }
            });

    @Override
    public void onDestroyView() {
        // Use onDestroyView to clear data related to the view
        model.setSelectedEvent(null);
        super.onDestroyView();
    }

    protected void setupObservers() {
        model.getUserEmail().observe(getViewLifecycleOwner(), email -> organizerEmail = email);

        model.getSelectedEvent().observe(getViewLifecycleOwner(), event -> {
            // Check subclass name to determine if this is "create" or "edit" mode
            if (getClass().getSimpleName().contains("create")) {
                selectedEvent = null;
            } else {
                selectedEvent = event;
                if (event != null) {
                    populateFields(event);
                }
            }
        });
    }

    protected void setupPublishButton(@NonNull View view) {
        Button publishButton = view.findViewById(R.id.create_event_publish_button);
        publishButton.setOnClickListener(v -> {
            Database db = new Database(FirebaseFirestore.getInstance());
            handlePublishClick(db);
        });
    }

    private void handlePublishClick(Database db) {
        String title = titleEditText.getText().toString().trim();
        String description = descriptionEditText.getText().toString().trim();
        String location = locationEditText.getText().toString().trim();
        String capacityStr = capacityEditText.getText().toString().trim();
        String priceStr = priceEditText.getText().toString().trim();
        boolean geolocationEnabled = geolocationSwitch != null && geolocationSwitch.isChecked();

        // --- Validation ---
        if (title.isEmpty() || description.isEmpty() || location.isEmpty()) {
            showToast("Please fill in all mandatory fields");
            return;
        }
        if (registrationOpenDate == null || registrationCloseDate == null || eventStartDate == null || eventEndDate == null) {
            showToast("Please select all event and registration dates");
            return;
        }
        if (registrationOpenDate.after(registrationCloseDate)) {
            showToast("Registration open date must be before the close date");
            return;
        }
        if (eventStartDate.after(eventEndDate)) {
            showToast("Event start date must be before the end date");
            return;
        }

        Integer capacity = null;
        try {
            if (!capacityStr.isEmpty()) capacity = Integer.parseInt(capacityStr);
        } catch (NumberFormatException e) {
            showToast("Invalid number for capacity");
            return;
        }

        Float price = null;
        try {
            if (!priceStr.isEmpty()) price = Float.parseFloat(priceStr);
        } catch (NumberFormatException e) {
            showToast("Invalid number for price");
            return;
        }

        if (selectedEvent == null) {
            createNewEvent(db, title, description, location, geolocationEnabled, capacity, price);
        } else {
            updateExistingEvent(db, title, description, location, geolocationEnabled, capacity, price);
        }
    }

    private void createNewEvent(Database db, String title, String description, String location, boolean geo, Integer capacity, Float price) {
        Event newEvent = new Event(
                organizerEmail, title, description, location, location, null,
                registrationOpenDate.getTime(), registrationCloseDate.getTime(),
                eventStartDate.getTime(), eventEndDate.getTime(),
                capacity, price, geo
        );

        db.addEvent(newEvent).addOnSuccessListener(success -> {
            if (!isAdded()) return; // CRASH FIX
            if (success) {
                if (imageUri != null) {
                    // If there's an image, upload it now that we have the event ID
                    uploadImageAndUpdateEvent(db, newEvent, "Event and poster created successfully!");
                } else {
                    showToast("Event created successfully!");
                    navigateBack();
                }
            } else {
                showToast("Failed to create event. It might already exist.");
            }
        }).addOnFailureListener(e -> {
            Log.e("CreateEvent", "Event creation failed", e);
            showToast("Error creating event: " + e.getMessage());
        });
    }

    private void updateExistingEvent(Database db, String title, String description, String location, boolean geo, Integer capacity, Float price) {
        selectedEvent.setTitle(title);
        selectedEvent.setDescription(description);
        selectedEvent.setLocation(location);
        selectedEvent.setGeolocationEnabled(geo);
        selectedEvent.setCapacity(capacity);
        selectedEvent.setPrice(price);
        selectedEvent.setDate_open(registrationOpenDate);
        selectedEvent.setDate_close(registrationCloseDate);
        selectedEvent.setEventStartAt(eventStartDate);
        selectedEvent.setEventEndAt(eventEndDate);

        if (imageUri != null) {
            // If a new image was selected, upload it
            uploadImageAndUpdateEvent(db, selectedEvent, "Event updated with new poster!");
        } else {
            // If no new image, just update the event data
            db.updateEvent(selectedEvent).addOnSuccessListener(success -> {
                if (!isAdded()) return; // CRASH FIX
                if (success) {
                    showToast("Event updated successfully!");
                    navigateBack();
                } else {
                    showToast("Failed to update event.");
                }
            }).addOnFailureListener(e -> {
                Log.e("UpdateEvent", "Event update failed", e);
                showToast("Error updating event: " + e.getMessage());
            });
        }
    }

    private void uploadImageAndUpdateEvent(Database db, Event event, String successMessage) {
        db.uploadImage(imageUri, "event_poster", event.getDescription(), event.getOrganizerId(), event.getId())
                .addOnSuccessListener(imageMetadata -> {
                    if (!isAdded()) return; // CRASH FIX
                    // Update the event with the new image URL
                    event.setPosterImageUrl(imageMetadata.getUrl());
                    db.updateEvent(event).addOnSuccessListener(s -> {
                        showToast(successMessage);
                        navigateBack();
                    }).addOnFailureListener(e -> {
                        Log.e("UpdateEventWithImage", "Failed to update event with image URL", e);
                        showToast("Image uploaded, but failed to link to event.");
                    });
                })
                .addOnFailureListener(e -> {
                    Log.e("ImageUpload", "Image upload failed", e);
                    showToast("Image upload failed: " + e.getMessage());
                });
    }

    public void navigateBack() {
        if (isAdded()) {
            NavHostFragment.findNavController(this).popBackStack();
        }
    }

    // A safe way to show Toasts
    private void showToast(String message) {
        if (isAdded() && getContext() != null) {
            Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
        }
    }

    protected void setupDateTimePicker(Button button, DateSelectionCallback callback) {
        button.setOnClickListener(v -> {
            if (!isAdded()) return;
            final Calendar calendar = Calendar.getInstance();
            DatePickerDialog datePickerDialog = new DatePickerDialog(getContext(), (view1, year, month, dayOfMonth) -> {
                TimePickerDialog timePickerDialog = new TimePickerDialog(getContext(), (view2, hourOfDay, minute) -> {
                    Calendar resultCalendar = Calendar.getInstance();
                    resultCalendar.set(year, month, dayOfMonth, hourOfDay, minute);
                    Date selectedDate = resultCalendar.getTime();
                    SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy\nHH:mm a", Locale.getDefault());
                    button.setText(sdf.format(selectedDate));
                    callback.onDateSelected(selectedDate);
                }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), false);
                timePickerDialog.show();
            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
            datePickerDialog.show();
        });
    }

    // Abstract methods and other helpers
    protected abstract void setupBackButton(@NonNull View view);

    protected void setupNavigation(@NonNull View view) {
        setupBackButton(view);
        // setupBottomNavigation(view); // This is likely incorrect in a create/edit screen
    }

    protected void setupDateButtons(@NonNull View view) {
        setupDateTimePicker(openDateButton, date -> registrationOpenDate = date);
        setupDateTimePicker(closeDateButton, date -> registrationCloseDate = date);
        setupDateTimePicker(startDateButton, date -> eventStartDate = date);
        setupDateTimePicker(endDateButton, date -> eventEndDate = date);
    }

    /**
     * Hook to populate fields with event data when editing an existing event.
     * Implemented in the concrete subclass.
     * @param event The event to populate fields with.
     */
    protected void populateFields(Event event) {
        // Default implementation can be empty, subclass will override
    }

    protected void setupBottomNavigation(@NonNull View view) {
        /*
        BottomNavigationView bottomNav = view.findViewById(R.id.organizer_bottom_nav);
        if (bottomNav != null) {
             // This logic is complex inside a nested fragment.
             // It's better handled by the parent NavHostFragment.
        }
        */
    }
}
