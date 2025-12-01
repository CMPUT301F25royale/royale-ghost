package com.example.project_part_3.Users.Organizer_UI.Organizer_event;

import android.app.AlertDialog;import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.bumptech.glide.Glide;
import com.example.project_part_3.Database_functions.Database;
import com.example.project_part_3.Events.Event;
import com.example.project_part_3.R;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

/**
 * Concrete fragment for editing an existing event in the Organizer UI.
 * Extends the {@link Organizer_create_edit_event_template} to handle event editing for organizers
 */
public class Organizer_edit_event extends Organizer_create_edit_event_template {

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupDeleteButton(view);
    }

    private void setupDeleteButton(@NonNull View view) {
        Button deleteEventButton = view.findViewById(R.id.organizer_delete_event_button);
        deleteEventButton.setVisibility(View.GONE);
    }

    @Override
    protected void populateFields(Event event) {
        if (!isAdded() || getView() == null || event == null) {
            return;
        }

        Button deleteEventButton = getView().findViewById(R.id.organizer_delete_event_button);
        deleteEventButton.setVisibility(View.VISIBLE);
        deleteEventButton.setOnClickListener(l -> handleEventDelete(event));

        TextView pageName = getView().findViewById(R.id.create_event_title);
        pageName.setText(String.format("Editing: %s", event.getTitle()));

        titleEditText.setText(event.getTitle());
        descriptionEditText.setText(event.getDescription());
        locationEditText.setText(event.getLocation());

        if (event.getCapacity() != null) {
            capacityEditText.setText(String.valueOf(event.getCapacity()));
        }
        if (event.getPrice() != null) {
            priceEditText.setText(String.format(Locale.US, "%.2f", event.getPrice()));
        }

        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy\nHH:mm a", Locale.getDefault());

        if (event.getDate_open() != null) {
            registrationOpenDate = event.getDate_open();
            openDateButton.setText(sdf.format(registrationOpenDate));
        }

        if (event.getDate_close() != null) {
            registrationCloseDate = event.getDate_close();
            closeDateButton.setText(sdf.format(registrationCloseDate));
        }

        if (registrationCloseDate != null && registrationCloseDate.before(Calendar.getInstance().getTime())) {
            openDateButton.setEnabled(false);
            closeDateButton.setEnabled(false);
        }

        if (event.getEventStartAt() != null) {
            eventStartDate = event.getEventStartAt();
            startDateButton.setText(sdf.format(eventStartDate));
        }

        if (event.getEventEndAt() != null) {
            eventEndDate = event.getEventEndAt();
            endDateButton.setText(sdf.format(eventEndDate));
        }

        if (event.getPosterImageUrl() != null) {
            eventImageView.setVisibility(View.VISIBLE);
            if (isAdded()) {
                Glide.with(requireContext())
                        .load(event.getPosterImageUrl())
                        .placeholder(R.drawable.ic_launcher_foreground)
                        .error(R.drawable.ic_launcher_foreground)
                        .into(eventImageView);
            }
        }


        if (geolocationSwitch != null) {
            geolocationSwitch.setChecked(event.getGeolocationEnabled());
        }
    }

    private void handleEventDelete(Event event) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Delete Event")
                .setMessage("Are you sure you want to permanently delete this event?")
                .setPositiveButton("Yes, Delete", (dialog, which) -> {
                    Database db = new Database(FirebaseFirestore.getInstance());
                    db.deleteEvent(event).addOnSuccessListener(success -> {
                         if (!isAdded()) {
                            return;
                        }
                        if (success) {
                            Toast.makeText(getContext(), "Event deleted successfully!", Toast.LENGTH_SHORT).show();
                            NavController navBack = NavHostFragment.findNavController(this);
                            navBack.popBackStack();
                        } else {
                            Toast.makeText(getContext(), "Failed to delete event.", Toast.LENGTH_SHORT).show();
                        }
                    });
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    protected void setupBackButton(@NonNull View view) {
        ImageButton back = view.findViewById(R.id.organizer_create_edit_event_back);
        if (back != null) {
            back.setOnClickListener(v -> {
                if (isAdded()) {
                    NavHostFragment.findNavController(this).popBackStack();
                }
            });
        }
    }

}
