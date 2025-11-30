package com.example.project_part_3.Users.Organizer_UI.Organizer_event;

import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.bumptech.glide.Glide;
import com.example.project_part_3.Database_functions.Database;
import com.example.project_part_3.Events.Event;
import com.example.project_part_3.R;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;

/**
 * Concrete fragment for editing an existing event in the Organizer UI.
 * Extends the {@link Organizer_create_edit_event_template} to handle event editing for organizers
 */
public class Organizer_edit_event extends Organizer_create_edit_event_template {

    @Override
    protected void populateFields(Event event) {
        // set up delete button
        Button deleteEventButton = getView().findViewById(R.id.organizer_delete_event_button);
        deleteEventButton.setVisibility(View.VISIBLE);
        deleteEventButton.setOnClickListener(l -> {
            handleEventDelete(event);
        });


        // if views weren't found, don't try to set them
        if (titleEditText == null || event == null || getView() == null) return;

        TextView pageName = getView().findViewById(R.id.create_event_title);
        pageName.setText(String.format("Editing: %s", event.getTitle()));

        titleEditText.setText(event.getTitle());
        descriptionEditText.setText(event.getDescription());
        locationEditText.setText(event.getLocation());


        // Use DateTimeInstance to show both date and time
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy\nHH:mm a");

        if (event.getDate_open() != null) {
            registrationOpenDate = event.getDate_open();
            if (openDateButton != null) {
                openDateButton.setText(sdf.format(event.getDate_open()));
            }
        }

        if (event.getDate_close() != null) {
            registrationCloseDate = event.getDate_close();
            if (closeDateButton != null) {
                closeDateButton.setText(sdf.format(event.getDate_close()));
            }
        }

        if (event.getEventStartAt() != null) {
            eventStartDate = event.getEventStartAt();
            if (startDateButton != null) {
                startDateButton.setText(sdf.format(event.getEventStartAt()));
            }
        }

        if (event.getEventEndAt() != null) {
            eventEndDate = event.getEventEndAt();
            if (endDateButton != null) {
                endDateButton.setText(sdf.format(event.getEventEndAt()));
            }
        }

        if (event.getCapacity() != null) {
            capacityEditText.setText(String.valueOf(event.getCapacity()));
        }

        if (event.getPrice() != null) {
            priceEditText.setText(String.format("%.2f", event.getPrice()));
        }

        if (event.getPosterImageUrl() != null) {
            imageURL = event.getPosterImageUrl();
            EventImageView.setVisibility(View.VISIBLE);
            Glide.with(requireContext())
                    .load(event.getPosterImageUrl())
                    .placeholder(R.drawable.ic_launcher_foreground)
                    .error(R.drawable.ic_launcher_foreground)
                    .into(EventImageView);

            if (geolocationSwitch != null) {
                geolocationSwitch.setChecked(event.getGeolocationEnabled());
            }
        }
    }
    private void handleEventDelete(Event event) {
        Database db = new Database(FirebaseFirestore.getInstance());
        db.deleteEvent(event).addOnSuccessListener(success -> {
            if (success) {
                Toast.makeText(getContext(), "Event deleted successfully!", Toast.LENGTH_SHORT).show();
                NavController navBack = NavHostFragment.findNavController(this);
                navBack.navigate(R.id.action_organizer_edit_event_to_organizerEventsFragment);
            } else {
                Toast.makeText(getContext(), "Failed to delete event.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void setupDateButtons(@NonNull View view) {
        // Use the helper from the parent class to enable Date + Time picking
        setupDateTimePicker(openDateButton, date -> registrationOpenDate = date);
        setupDateTimePicker(closeDateButton, date -> registrationCloseDate = date);
        setupDateTimePicker(startDateButton, date -> eventStartDate = date);
        setupDateTimePicker(endDateButton, date -> eventEndDate = date);
    }

    @Override
    protected void setupBackButton(@NonNull View view) {
        ImageButton back = view.findViewById(R.id.organizer_create_edit_event_back);
        back.setOnClickListener(v -> {
            NavController navBack = NavHostFragment.findNavController(this);
            navBack.navigate(R.id.action_organizer_edit_event_to_organizerEventsFragment);
        });
    }

    //TODO: KANAJENIJNFI
    /**
     * @param db    The database to push to
     * @param event The event to push
     */
    @Override
    protected void pushEventToDatabase(Database db, Event event) {

    }
}
