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

    /**
     * This is a function that checks if there is a {@code EventDatabase} object in use and if there is, it
     * uses it, if not it makes a new one
     * @return the instance to use
     */

    public static synchronized EventDatabase getInstance() {
        if (instance == null) {
            instance = new EventDatabase();
        }
        return instance;
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
        // Initialize the low-level Database class
        this.db = new Database(FirebaseFirestore.getInstance());
        // Immediately start listening for changes to all events
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


    /**
     * Returns a LiveData object containing a list of all events currently
     * loaded by the ViewModel. This LiveData is typically updated by one
     * of the event listeners registered elsewhere in the application.
     *
     * @return a MutableLiveData holding a list of Event objects
     */
    public MutableLiveData<List<Event>> getAllEvents() {
        return allEvents;
    }

    /**
     * Registers a realtime listener for a single event identified by its ID.
     * If an existing listener is active, it is removed before the new one
     * is created. When Firestore sends an update, the corresponding Event
     * object is posted into the provided LiveData. If the document does not
     * exist or an error occurs, the LiveData is updated with null.
     *
     * @param eventId the unique ID of the event to listen for
     * @param eventLiveData the LiveData that will receive updates to the event
     */
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

    /**
     * Deletes the specified event from the database and notifies the provided
     * listener of the operation result. The deletion process includes removing
     * the event document and any associated resources handled within the
     * database layer. If the deletion succeeds, {@code onSuccess()} is invoked;
     * otherwise, {@code onFailure(String)} is called with an error message.
     *
     * @param event    the Event object to be deleted
     * @param listener a callback interface that reports success or failure
     */
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

    /**
     * Adds a new event to the database and notifies the provided listener of the
     * operation result. Before submission, the event is initialized to indicate
     * that its lottery has not yet been processed. If the database insertion
     * succeeds, {@code onSuccess()} is invoked on the listener; otherwise,
     * {@code onFailure(String)} is called with an error message.
     *
     * @param event    the Event object to be added
     * @param listener a callback interface that reports success or failure
     */
    public void addEvent(Event event, OnEventAddListener listener) {
        event.setLotteryDone(false);
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
