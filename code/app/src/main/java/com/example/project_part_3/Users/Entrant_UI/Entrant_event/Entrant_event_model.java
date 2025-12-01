// Create this new file in the same package:
// com/example/project_part_3/Users/Entrant_UI/Entrant_event/EntrantEventViewModel.java

package com.example.project_part_3.Users.Entrant_UI.Entrant_event;

import android.util.Log;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;
import com.example.project_part_3.Database_functions.EventDatabase;
import com.example.project_part_3.Events.Event;
import java.util.List;

/**
 * ViewModel responsible for supplying event data to the entrant event UI.
 */
public class Entrant_event_model extends ViewModel {

    private final EventDatabase eventDb;
    private final LiveData<List<Event>> allEvents;

    public Entrant_event_model() {
        eventDb = new EventDatabase(); // This uses our hybrid EventDatabase
        allEvents = eventDb.getAllEvents();
    }

    public LiveData<List<Event>> getAllEvents() {
        return allEvents;
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        // Important: Clean up the listener to prevent memory leaks
        eventDb.cleanupListeners();
        Log.d("EntrantEventViewModel", "ViewModel cleared and listeners cleaned up.");
    }
}
