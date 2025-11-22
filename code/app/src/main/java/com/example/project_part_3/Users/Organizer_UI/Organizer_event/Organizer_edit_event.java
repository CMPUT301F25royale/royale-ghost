package com.example.project_part_3.Users.Organizer_UI.Organizer_event;

import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.example.project_part_3.Database_functions.Database;
import com.example.project_part_3.Events.Event;
import com.example.project_part_3.R;

import java.text.DateFormat;
import java.util.Calendar;

public class Organizer_edit_event extends Organizer_create_edit_event_template {

    @Override
    protected void populateFields(Event event) {
        // if views weren't found, don't try to set them
        if (titleEditText == null || event == null || getView() == null) return;

        TextView pageName = getView().findViewById(R.id.create_event_title);
        pageName.setText(String.format("Edit Event: %s", event.getTitle()));

        titleEditText.setText(event.getTitle());
        descriptionEditText.setText(event.getDescription());
        locationEditText.setText(event.getLocation());

        if (event.getDate_open() != null) {
            registrationOpenDate = event.getDate_open();

            Calendar cal = Calendar.getInstance();
            cal.setTime(registrationOpenDate);
            setupRegistrationOpenButton(getView(), cal);

            // Update text
            if (openDateButton != null) {
                openDateButton.setText(DateFormat.getDateInstance().format(event.getDate_open()));
            }
        }

        if (event.getDate_close() != null) {
            registrationCloseDate = event.getDate_close();

            Calendar cal = Calendar.getInstance();
            cal.setTime(registrationCloseDate);
            setupRegistrationCloseButton(getView(), cal);

            if (closeDateButton != null) {
                closeDateButton.setText(DateFormat.getDateInstance().format(event.getDate_close()));
            }
        }

        if (event.getCapacity() != null) {
            capacityEditText.setText(String.valueOf(event.getCapacity()));
        }

        if (event.getPrice() != null) {
            priceEditText.setText(String.format("%.2f", event.getPrice()));
        }
    }
    @Override
    protected void pushEventToDatabase(Database db, Event event) {
        db.updateEvent(event).addOnSuccessListener(success -> {
            if (success) {
                Toast.makeText(getContext(), "Event updated successfully!", Toast.LENGTH_SHORT).show();
                // Navigate back to the event list
                NavController navController = NavHostFragment.findNavController(this);
                navController.navigate(R.id.action_organizer_edit_event_to_organizerEventsFragment);
            } else {
                Toast.makeText(getContext(), "Failed to publish event.", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(e -> {
            Toast.makeText(getContext(), "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        });
    }

    @Override
    public void setupDateButtons(@NonNull View view) {
        Calendar calOpen = Calendar.getInstance();
        Calendar calClosed = Calendar.getInstance();

        // safe check
        if (registrationOpenDate != null) {
            calOpen.setTime(registrationOpenDate);
        }

        if (registrationCloseDate != null) {
            calClosed.setTime(registrationCloseDate);
        }

        setupRegistrationOpenButton(view, calOpen);
        setupRegistrationCloseButton(view, calClosed);
    }

    @Override
    protected void setupBackButton(@NonNull View view) {
        Button back = view.findViewById(R.id.back);
        back.setOnClickListener(v -> {
            NavController navBack = NavHostFragment.findNavController(this);
            navBack.navigate(R.id.action_organizer_edit_event_to_organizerEventsFragment);
        });
    }
}