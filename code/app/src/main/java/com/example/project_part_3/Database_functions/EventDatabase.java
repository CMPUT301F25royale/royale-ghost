package com.example.project_part_3.Database_functions;

import android.util.Log;
import androidx.lifecycle.MutableLiveData;
import com.example.project_part_3.Events.Event;
import com.example.project_part_3.Users.Organizer;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import java.util.List;
import java.util.Objects;

/**
 * High-level repository for managing Event data.
 * This class abstracts away the Task-based nature of the low-level Database class,
 * exposing LiveData and simple callbacks for use in ViewModels and UI controllers.
 *
 * NOTE: This is a hybrid class containing legacy singleton logic for backward compatibility.
 * This legacy code should be removed once all dependent activities are refactored.
 */
public class EventDatabase {

    private final Database db;
    private final MutableLiveData<List<Event>> allEvents = new MutableLiveData<>();
    private ListenerRegistration allEventsListener;
    private ListenerRegistration singleEventListener;

    // =================================================================================
    // LEGACY CODE - FOR BACKWARD COMPATIBILITY
    // TODO: This entire section should be removed after refactoring dependent classes.
    // =================================================================================
    private static EventDatabase instance;

    public static synchronized EventDatabase getInstance() {
        if (instance == null) {
            instance = new EventDatabase();
        }
        return instance;
    }

    public interface OnEventUpdateListener {
        void onSuccess();
        void onFailure(String errorMessage);
    }

    public void updateEvent(Event event, OnEventUpdateListener listener) {
        db.updateEvent(event)
                .addOnSuccessListener(aVoid -> listener.onSuccess())
                .addOnFailureListener(e -> listener.onFailure(e.getMessage()));
    }

    /**
     * LEGACY METHOD: Synchronously gets an event from the current LiveData value.
     * @param title The title of the event.
     * @param organizer The organizer of the event.
     * @return The Event object if found, otherwise null.
     */
    public Event getEvent(String title, Organizer organizer) {
        List<Event> currentEvents = allEvents.getValue();
        if (currentEvents == null) {
            return null; // Data not loaded yet
        }
        for (Event event : currentEvents) {
            if (event.getTitle().equalsIgnoreCase(title) &&
                    event.getOrganizerId() != null &&
                    organizer.getEmail().equals(event.getOrganizerId())) {
                return event;
            }
        }
        return null;
    }
    // =================================================================================
    // END OF LEGACY CODE
    // =================================================================================


    public interface OnEventAddListener {
        void onSuccess();
        void onFailure(String errorMessage);
    }

    public interface OnEventDeleteListener {
        void onSuccess();
        void onFailure(String errorMessage);
    }

    public EventDatabase() {
        this.db = new Database(FirebaseFirestore.getInstance());
        listenForAllEvents();
    }

    private void listenForAllEvents() {
        if (allEventsListener == null) {
            allEventsListener = db.listenForEvents((querySnapshot, error) -> {
                if (error != null) {
                    Log.e("EventDatabase", "Listen for all events failed", error);
                    return;
                }
                if (querySnapshot != null) {
                    List<Event> eventList = querySnapshot.toObjects(Event.class);
                    allEvents.postValue(eventList);
                }
            });
        }
    }

    public MutableLiveData<List<Event>> getAllEvents() {
        return allEvents;
    }

    public void listenForSingleEvent(String eventId, MutableLiveData<Event> eventLiveData) {
        if (singleEventListener != null) {
            singleEventListener.remove();
        }
        singleEventListener = db.listenForSingleEvent(eventId, (querySnapshot, error) -> {
            if (error != null) {
                Log.e("EventDatabase", "Listen for single event failed", error);
                eventLiveData.postValue(null);
                return;
            }
            if (querySnapshot != null && !querySnapshot.isEmpty()) {
                DocumentSnapshot doc = querySnapshot.getDocuments().get(0);
                eventLiveData.postValue(doc.toObject(Event.class));
            } else {
                eventLiveData.postValue(null);
            }
        });
    }

    public void deleteEvent(Event event, OnEventDeleteListener listener) {
        db.deleteEvent(event).addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult()) {
                listener.onSuccess();
            } else {
                String errorMessage = (task.getException() != null) ? task.getException().getMessage() : "Deletion failed.";
                listener.onFailure(errorMessage);
            }
        });
    }

    public void addEvent(Event event, OnEventAddListener listener) {
        db.addEvent(event).addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult()) {
                listener.onSuccess();
            } else {
                String errorMessage = (task.getException() != null) ? task.getException().getMessage() : "Addition failed.";
                listener.onFailure(errorMessage);
            }
        });
    }

    public void cleanupListeners() {
        if (allEventsListener != null) {
            allEventsListener.remove();
            allEventsListener = null;
        }
        if (singleEventListener != null) {
            singleEventListener.remove();
            singleEventListener = null;
        }
        Log.d("EventDatabase", "All event listeners have been cleaned up.");
    }
}
