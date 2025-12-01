// Create this new file: .../Organizer_UI/OrganizerSharedViewModel.java

package com.example.project_part_3.Users.Organizer_UI;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.project_part_3.Events.Event;

/**
 * ViewModel class for sharing data between fragments.
 */
public class OrganizerSharedViewModel extends ViewModel {

    // Private data that can be changed only within this ViewModel
    private final MutableLiveData<String> userEmail = new MutableLiveData<>();
    private final MutableLiveData<Event> selectedEvent = new MutableLiveData<>();


    // Public LiveData that other classes can observe but cannot change
    public LiveData<String> getUserEmail() {
        return userEmail;
    }
    public LiveData<Event> getSelectedEvent() {
        return selectedEvent;
    }
    // Method to update the email from the host fragment
    public void setUserEmail(String email) {
        userEmail.setValue(email);
    }
    // Method to update the selected event from the host fragment
    public void setSelectedEvent(Event event) {
        selectedEvent.setValue(event);
    }
}
