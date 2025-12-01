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
import com.example.project_part_3.Image.Image_datamap;
import com.example.project_part_3.R;
import com.example.project_part_3.Users.Organizer_UI.OrganizerSharedViewModel;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.materialswitch.MaterialSwitch;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import android.widget.Switch;


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
    protected Button imagebutton;
    protected ImageView EventImageView;
    protected Uri ImageUri;
    public String imageURL;
    protected String title;
    protected String description;
    protected String location;
    protected String capacityStr;
    protected String priceStr;

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

    private void setupImageUpload(View view) {
        Button posterbutton = view.findViewById(R.id.create_event_poster_button);
        posterbutton.setOnClickListener(v -> showImagePopup());
    }

    private void showImagePopup() {
        LayoutInflater inflater = LayoutInflater.from(requireContext());
        View dialogView = inflater.inflate(R.layout.image_popup, null);

        ImageView popupImagePreview = dialogView.findViewById(R.id.popup_image_preview);
        Button changeImageButton = dialogView.findViewById(R.id.popup_change_image_button);

        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setView(dialogView)
                .create();

        if (EventImageView.getDrawable() != null) {
            Glide.with(requireContext())
                    .load(EventImageView.getDrawable())
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
                    ImageUri = uri;
                    uploadProfilePicture();
                }
            });

    private void uploadProfilePicture() {
        if (ImageUri != null && EventImageView != null) {
            EventImageView.setVisibility(View.VISIBLE);
            Glide.with(requireContext())
                    .load(ImageUri)
                    .placeholder(R.drawable.ic_launcher_foreground)
                    .error(R.drawable.ic_launcher_foreground)
                    .into(EventImageView);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
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
        imagebutton = view.findViewById(R.id.create_event_poster_button);
        EventImageView = view.findViewById(R.id.eventImage);
        geolocationSwitch = view.findViewById(R.id.create_event_geolocation_switch);

    }

    protected void setupNavigation(@NonNull View view) {
        setupBackButton(view);
        setupBottomNavigation(view);
    }

    protected void setupDateButtons(@NonNull View view) {
        setupDateTimePicker(openDateButton, date -> registrationOpenDate = date);
        setupDateTimePicker(closeDateButton, date -> registrationCloseDate = date);
        setupDateTimePicker(startDateButton, date -> eventStartDate = date);
        setupDateTimePicker(endDateButton, date -> eventEndDate = date);
    }

    protected void setupObservers() {
        model.getUserEmail().observe(getViewLifecycleOwner(), email -> organizerEmail = email);

        model.getSelectedEvent().observe(getViewLifecycleOwner(), event -> {
            if (this.getClass().getSimpleName().equals("Organizer_create_event")) {
                selectedEvent = null;
            } else {
                selectedEvent = event;
                if (event != null) {
                    populateFields(selectedEvent);
                }
            }
        });
    }

    protected abstract void setupBackButton(@NonNull View view);

    protected void setupBottomNavigation(@NonNull View view) {
        BottomNavigationView bottomNav = view.findViewById(R.id.organizer_bottom_nav);
        NavHostFragment navHostFragment = (NavHostFragment) getChildFragmentManager()
                .findFragmentById(R.id.organizer_nav_host_fragment);

        if (navHostFragment != null) {
            NavController navController = navHostFragment.getNavController();
            NavigationUI.setupWithNavController(bottomNav, navController);
        }
    }

    protected void setupPublishButton(@NonNull View view) {
        FirebaseFirestore ff = FirebaseFirestore.getInstance();
        Database db = new Database(ff);
        Button publishButton = view.findViewById(R.id.create_event_publish_button);

        publishButton.setOnClickListener(v -> handlePublishClick(db));
    }

    protected void handlePublishClick(Database db) {
        Integer capacity = null;
        Float price = null;
        title = titleEditText.getText().toString().trim();
        description = descriptionEditText.getText().toString().trim();
        location = locationEditText.getText().toString().trim();
        capacityStr = capacityEditText.getText().toString().trim();
        priceStr = priceEditText.getText().toString().trim();

        boolean geolocationEnabled =
                geolocationSwitch != null && geolocationSwitch.isChecked();

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
        Event newEvent = new Event();

        //update or create event needs different ways to deal with images
        if (selectedEvent != null) {
            Integer finalCapacity = capacity;
            Float finalPrice = price;
            selectedEvent.setTitle(title);
            selectedEvent.setDescription(description);
            selectedEvent.setLocation(location);
            selectedEvent.setCapacity(capacity);
            selectedEvent.setPrice(price);
            selectedEvent.setDate_open(registrationOpenDate);
            selectedEvent.setDate_close(registrationCloseDate);
            selectedEvent.setEventStartAt(eventStartDate);
            selectedEvent.setEventEndAt(eventEndDate);

            if (ImageUri != null) {
                db.uploadImage(ImageUri, "event_poster", description, organizerEmail, selectedEvent.getId())
                        .addOnSuccessListener(imageMetadata -> {
                            selectedEvent.setImageInfo(imageMetadata);
                            selectedEvent.setPosterImageUrl(imageMetadata.getUrl());
                            updateExistingEvent(db,selectedEvent,imageMetadata, finalCapacity, finalPrice);
                        })
                        .addOnFailureListener(e -> Toast.makeText(getContext(), "Image upload failed: " + e.getMessage(), Toast.LENGTH_LONG).show());

            }else{
                updateExistingEvent(db,selectedEvent,null, finalCapacity, finalPrice);
            }
            } else {
                newEvent = new Event(organizerEmail, title, description, location, location, null, registrationOpenDate.getTime(), registrationCloseDate.getTime(), eventStartDate.getTime(), eventEndDate.getTime(), capacity, price, geolocationEnabled);
                if (geolocationEnabled) {
                    newEvent.setGeolocationEnabled(true);
                }
                createNewEvent(db, newEvent, ImageUri);
            }
            android.util.Log.d("GeoDebug", "Saving geolocationEnabled = " + newEvent.getGeolocationEnabled());
        }


    private void updateExistingEvent(Database db,Event selectedEvent ,Image_datamap imageInfo, Integer capacity, Float price) {
        if (imageInfo != null) {
            selectedEvent.setImageInfo(imageInfo);
            selectedEvent.setPosterImageUrl(imageInfo.getUrl());
        }
        db.updateEvent(selectedEvent).addOnSuccessListener(success -> {
            if (success) {
                Toast.makeText(getContext(), "Event updated successfully!", Toast.LENGTH_SHORT).show();
                navigateBack();
            } else {
                Toast.makeText(getContext(), "Failed to update event.", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(e -> {
            Log.e("UpdateEventFailure", "The updateEvent task failed.", e);
            Toast.makeText(getContext(), "Error updating event: " + e.getMessage(), Toast.LENGTH_LONG).show();
        });
    }

    public void createNewEvent(Database db,Event newEvent , Uri ImageUri) {
        db.addEvent(newEvent).addOnSuccessListener(success -> {
            if (success) {
                if (ImageUri != null) {
                    Log.d("CreateEvent", "Event document created. Now uploading image for event ID: " + newEvent.getId());
                    db.uploadImage(ImageUri, "event_poster", description, organizerEmail, newEvent.getId())
                            .addOnSuccessListener(imageMetadata -> {
                                Toast.makeText(getContext(), "Event and poster created successfully!", Toast.LENGTH_SHORT).show();
                                navigateBack();
                            })
                            .addOnFailureListener(e -> {
                                Log.e("CreateEvent", "Event created, but image upload failed.", e);
                                Toast.makeText(getContext(), "Event created, but poster upload failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                                navigateBack(); // Navigate back anyway
                            });
                } else {
                    Toast.makeText(getContext(), "Event created successfully!", Toast.LENGTH_SHORT).show();
                    navigateBack();
                }
            } else {
                Toast.makeText(getContext(), "Failed to create event. A user or event might already exist.", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(e -> {
            Log.e("CreateEvent", "Initial event creation failed.", e);
            Toast.makeText(getContext(), "Error creating event: " + e.getMessage(), Toast.LENGTH_LONG).show();
        });
    }

    public void navigateBack() {
        if (getView() != null) {
            NavHostFragment.findNavController(this).popBackStack();
        }
    }

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

                }, currentHour, currentMinute, false);

                timePickerDialog.show();

            }, currentYear, currentMonth, currentDay);

            datePickerDialog.show();
        });
    }

    protected void populateFields(Event event) {

    }

    }
