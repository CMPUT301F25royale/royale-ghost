
package com.example.project_part_3.Users.Admin_UI.Admin_profile;

import android.util.Log;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.example.project_part_3.Database_functions.ImageDatabase;
import com.example.project_part_3.Database_functions.UserDatabase;
import com.example.project_part_3.Image.ImageMetadata;
import com.example.project_part_3.Users.User;
import java.util.List;

public class Admin_profile_model extends ViewModel {

    private final UserDatabase userDb;
    private final LiveData<List<User>> userProfiles;

    public Admin_profile_model() {
        userDb = new UserDatabase();
        userProfiles = userDb.getAllUsers();
    }

    public LiveData<List<User>> getUserProfiles() {
        return userProfiles;
    }

    public void deleteUser(String email, UserDatabase.OnUserDeleteListener listener) {
        userDb.deleteUser(email, listener);
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        userDb.cleanupListeners();
        Log.d("ProfileViewModel", "ViewModel cleared and listeners cleaned up.");
    }
}
