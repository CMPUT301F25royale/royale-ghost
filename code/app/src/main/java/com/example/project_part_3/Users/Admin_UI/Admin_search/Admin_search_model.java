// Create this new file in the same package:
// com/example/project_part_3/Users/Admin_UI/Admin_search/AdminSearchViewModel.java

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
 * ViewModel responsible for managing and combining data used in the Admin Search UI.
 * This ViewModel retrieves all events and images from their respective databases and combines it into
 * a single stream containing a combined list of both data types. It uses
 * The class also provides helper methods for deleting events and images, and ensures
 * proper cleanup of database listeners when the ViewModel is cleared.
 */
public class Admin_search_model extends ViewModel {

    private final EventDatabase eventDb;
    private final ImageDatabase imageDb;


    private final LiveData<List<Event>> allEvents;
    private final LiveData<List<Image_datamap>> allImages;

    private final MediatorLiveData<List<Object>> combinedData = new MediatorLiveData<>();

    public Admin_search_model() {
        eventDb = new EventDatabase();
        imageDb = new ImageDatabase();

        allEvents = eventDb.getAllEvents();
        allImages = imageDb.getAllImages();

        combinedData.addSource(allEvents, events -> combineAllData());
        combinedData.addSource(allImages, images -> combineAllData());
    }

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

    public LiveData<List<Object>> getCombinedData() {
        return combinedData;
    }

    public void deleteEvent(Event event, EventDatabase.OnEventDeleteListener listener) {
        eventDb.deleteEvent(event, listener);
    }

    public void deleteImage(Image_datamap image, ImageDatabase.OnImageDeleteListener listener) {
        imageDb.deleteImage(image, listener);
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        eventDb.cleanupListeners();
        imageDb.cleanupListeners();
        Log.d("AdminSearchViewModel", "ViewModel cleared and listeners cleaned up.");
    }
}
