package com.example.project_part_3.Users.Organizer_UI.Organizer_event;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.example.project_part_3.Database_functions.Database;
import com.example.project_part_3.Events.Event;
import com.example.project_part_3.R;

/**
 * Concrete fragment for creating a new event in the Organizer UI.
 * Extends the {@link Organizer_create_edit_event_template} to handle event creation for organizers
 */
public class Organizer_create_event extends Organizer_create_edit_event_template {

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        if (model != null) {
            model.setSelectedEvent(null);
        }

        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    protected void setupBackButton(@NonNull View view) {
        ImageButton back = view.findViewById(R.id.organizer_create_edit_event_back);
        back.setOnClickListener(v -> {
            NavController navBack = NavHostFragment.findNavController(this);
            navBack.navigate(R.id.create_to_event);
        });
    }

    //TODO: NAJNAJDNDDD
    /**
     * @param db    The database to push to
     * @param event The event to push
     */
    @Override
    protected void pushEventToDatabase(Database db, Event event) {

    }
}