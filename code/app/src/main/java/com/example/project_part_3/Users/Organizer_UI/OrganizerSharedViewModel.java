// Create this new file: .../Organizer_UI/OrganizerSharedViewModel.java

package com.example.project_part_3.Users.Organizer_UI;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;


public class OrganizerSharedViewModel extends ViewModel {

    // Private data that can be changed only within this ViewModel
    private final MutableLiveData<String> userEmail = new MutableLiveData<>();

    // Public LiveData that other classes can observe but cannot change
    public LiveData<String> getUserEmail() {
        return userEmail;
    }

    // Method to update the email from the host fragment
    public void setUserEmail(String email) {
        userEmail.setValue(email);
    }
}
