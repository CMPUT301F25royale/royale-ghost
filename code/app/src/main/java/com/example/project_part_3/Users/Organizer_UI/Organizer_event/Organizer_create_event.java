package com.example.project_part_3.Users.Organizer_UI.Organizer_event;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
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
import java.util.Calendar;
import java.util.Date;

/**
 * Concrete fragment for creating a new event in the Organizer UI.
 * Extends the {@link Organizer_create_edit_event_template} to handle event creation for organizers
 */
public class Organizer_create_event extends Organizer_create_edit_event_template {


    @Override
    protected void pushEventToDatabase(Database db, Event event) {
        db.addEvent(event).addOnSuccessListener(success -> {
            if (success) {
                Toast.makeText(getContext(), "Event created successfully!", Toast.LENGTH_SHORT).show();
                // Navigate back to the event list
                NavController navController = NavHostFragment.findNavController(this);
                navController.navigate(R.id.create_to_event);
            } else {
                Toast.makeText(getContext(), "Failed to publish event.", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(e -> {
            Toast.makeText(getContext(), "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        });
    }

    @Override
    protected void setupBackButton(@NonNull View view) {
        ImageButton back = view.findViewById(R.id.organizer_create_edit_event_back);
        back.setOnClickListener(v -> {
            NavController navBack = NavHostFragment.findNavController(this);
            navBack.navigate(R.id.create_to_event);
        });
    }
}
