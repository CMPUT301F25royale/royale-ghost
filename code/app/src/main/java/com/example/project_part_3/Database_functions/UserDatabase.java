package com.example.project_part_3.Database_functions;

import android.util.Log;
import androidx.lifecycle.MutableLiveData;
import com.example.project_part_3.Users.User;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import java.util.ArrayList;
import java.util.List;

/**
 * High-level repository for managing User data.
 * This class abstracts away the Task-based nature of the low-level Database class,
 * exposing LiveData and simple callbacks for use in ViewModels and UI controllers.
 */
public class UserDatabase {

    private final Database db;
    private final MutableLiveData<List<User>> users = new MutableLiveData<>();
    private ListenerRegistration allUsersListener;
    private ListenerRegistration singleUserListener;

    public interface OnUserAddListener {
        void onSuccess();
        void onFailure(String errorMessage);
    }

    public interface OnUserDeleteListener {
        void onSuccess();
        void onFailure(String errorMessage);
    }

    public UserDatabase() {
        this.db = new Database(FirebaseFirestore.getInstance());
        listenForUsers();
    }

    /**
     * Attaches a listener to the entire 'users' collection. The LiveData object
     * will be updated in real-time with any changes.
     */
    private void listenForUsers() {
        if (allUsersListener == null) {
            allUsersListener = db.listenForUsers((querySnapshot, error) -> {
                if (error != null) {
                    Log.e("UserDatabase", "Listen for all users failed", error);
                    return;
                }
                if (querySnapshot != null) {
                    List<User> userList = querySnapshot.toObjects(User.class);
                    users.postValue(userList);
                }
            });
        }
    }

    /**
     * Returns a LiveData object containing the list of all users.
     * Observers of this object will receive real-time updates.
     */
    public MutableLiveData<List<User>> getAllUsers() {
        return users;
    }

    /**
     * Attaches a listener to a single user document to observe real-time changes
     * (e.g., if their profile details are updated elsewhere).
     * @param email The email of the user to observe.
     * @param userLiveData The MutableLiveData object from the ViewModel that will receive updates.
     */
    public void listenForSingleUser(String email, MutableLiveData<User> userLiveData) {
        // Clean up any previous listener for a single user to avoid multiple listeners
        if (singleUserListener != null) {
            singleUserListener.remove();
        }
        singleUserListener = db.listenForSingleUser(email, (documentSnapshot, error) -> {
            if (error != null) {
                Log.e("UserDatabase", "Listen for single user failed", error);
                userLiveData.postValue(null);
                return;
            }
            if (documentSnapshot != null && documentSnapshot.exists()) {
                User fetchedUser = documentSnapshot.toObject(User.class);
                userLiveData.postValue(fetchedUser);
            } else {
                userLiveData.postValue(null);
            }
        });
    }

    /**
     * Deletes a user by their email. This uses the low-level method which also handles
     * deleting their associated events in a transaction.
     * @param email The email of the user to delete.
     * @param listener A callback to notify the caller of success or failure.
     */
    public void deleteUser(String email, OnUserDeleteListener listener) {
        db.deleteUser(email).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                listener.onSuccess();
            } else {
                String errorMessage = (task.getException() != null) ? task.getException().getMessage() : "Deletion failed.";
                listener.onFailure(errorMessage);
            }
        });
    }

    /**
     * Adds a new user to the database.
     * @param user The user object to add.
     * @param listener A callback to notify the caller of success or failure.
     */
    public void addUser(User user, OnUserAddListener listener) {
        db.addUser(user).addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult()) {
                listener.onSuccess();
            } else {
                String errorMessage = (task.getException() != null) ? task.getException().getMessage() : "Addition failed or user already exists.";
                listener.onFailure(errorMessage);
            }
        });
    }

    /**
     * Cleans up all active Firestore listeners managed by this repository.
     * This is crucial to prevent memory leaks and should be called when the
     * repository is no longer needed (e.g., in a ViewModel's onCleared() method).
     */
    public void cleanupListeners() {
        if (allUsersListener != null) {
            allUsersListener.remove();
            allUsersListener = null;
        }
        if (singleUserListener != null) {
            singleUserListener.remove();
            singleUserListener = null;
        }
        Log.d("UserDatabase", "All user listeners have been cleaned up.");
    }
}
