package com.example.project_part_3.Users.Admin_UI.Admin_search;

import android.util.Log;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.ViewModel;

import com.example.project_part_3.Database_functions.EventDatabase;
import com.example.project_part_3.Database_functions.ImageDatabase;
import com.example.project_part_3.Events.Event;
import com.example.project_part_3.Image.Image_datamap;

import java.util.ArrayList;
import java.util.List;

/**
 * ViewModel that manages event and image data for the admin search screen.
 * Provides combined aggregated data and supports deleting events and images.
 */
public class Admin_search_model extends ViewModel {

    private final EventDatabase eventDb;
    private final ImageDatabase imageDb;

    private final LiveData<List<Event>> allEvents;
    private final LiveData<List<Image_datamap>> allImages;

    private final MediatorLiveData<List<Object>> combinedData = new MediatorLiveData<>();

    /**
     * Initializes the ViewModel, retrieves data sources, and prepares the combined data stream.
     */
    public Admin_search_model() {
        eventDb = new EventDatabase();
        imageDb = new ImageDatabase();

        allEvents = eventDb.getAllEvents();
        allImages = imageDb.getAllImages();

        combinedData.addSource(allEvents, events -> combineAllData());
        combinedData.addSource(allImages, images -> combineAllData());
    }

    /**
     * Combines all events and images into a single list for UI display.
     */
    private void combineAllData() {
        List<Event> events = allEvents.getValue();
        List<Image_datamap> images = allImages.getValue();

        ArrayList<Object> combinedList = new ArrayList<>();
        if (events != null) {
            combinedList.addAll(events);
        }
        if (images != null) {
            combinedList.addAll(images);
        }
        combinedData.setValue(combinedList);
    }

    /**
     * Returns a LiveData list containing both events and images.
     *
     * @return LiveData list of combined objects
     */
    public LiveData<List<Object>> getCombinedData() {
        return combinedData;
    }

    /**
     * Deletes an event using the EventDatabase.
     *
     * @param event    the event to delete
     * @param listener callback invoked on success or failure
     */
    public void deleteEvent(Event event, EventDatabase.OnEventDeleteListener listener) {
        eventDb.deleteEvent(event, listener);
    }

    /**
     * Deletes an image using the ImageDatabase.
     *
     * @param image    the image to delete
     * @param listener callback invoked on success or failure
     */
    public void deleteImage(Image_datamap image, ImageDatabase.OnImageDeleteListener listener) {
        imageDb.deleteImage(image, listener);
    }

    /**
     * Cleans up listeners when the ViewModel is cleared.
     */
    @Override
    protected void onCleared() {
        super.onCleared();
        eventDb.cleanupListeners();
        imageDb.cleanupListeners();
        Log.d("AdminSearchViewModel", "ViewModel cleared and listeners cleaned up.");
    }
}
