package com.example.project_part_3.Users.Admin_UI.Admin_profile;

import android.util.Log;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.example.project_part_3.Database_functions.UserDatabase;
import com.example.project_part_3.Users.User;
import java.util.List;

/**
 * ViewModel for managing and providing admin access to all user profiles.
 */
public class Admin_profile_model extends ViewModel {

    private final UserDatabase userDb;
    private final LiveData<List<User>> userProfiles;

    /**
     * Initializes the model and loads all user profiles from the database.
     */
    public Admin_profile_model() {
        userDb = new UserDatabase();
        userProfiles = userDb.getAllUsers();
    }

    /**
     * Returns the observable list of user profiles.
     *
     * @return LiveData containing a list of users
     */
    public LiveData<List<User>> getUserProfiles() {
        return userProfiles;
    }

    /**
     * Deletes a user by email using the database API.
     *
     * @param email the email of the user to delete
     * @param listener callback that reports success or failure
     */
    public void deleteUser(String email, UserDatabase.OnUserDeleteListener listener) {
        userDb.deleteUser(email, listener);
    }

    /**
     * Cleans up listeners when the ViewModel is destroyed.
     */
    @Override
    protected void onCleared() {
        super.onCleared();
        userDb.cleanupListeners();
        Log.d("ProfileViewModel", "ViewModel cleared and listeners cleaned up.");
    }
}
