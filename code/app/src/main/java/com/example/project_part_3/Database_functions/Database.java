package com.example.project_part_3.Database_functions;
import android.util.Log;

import androidx.annotation.NonNull;

import com.example.project_part_3.Users.User;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class Database {
    FirebaseFirestore db;
    public Database(FirebaseFirestore db) {
        this.db = db;
    }

    /**
     * Adds a user to the Firebase database
     *
     * @param user the user you would like to add to the database
     */
    public void addUser(@NonNull User user) {
        // users are stored based on their emails (only unique identifier)
        DocumentReference docRef = db.collection("users").document(user.getEmail());

        Map<String, Object> data = new HashMap<>();
        data.put("userData", user);
        docRef.set(data)
                .addOnSuccessListener(l -> {
                    Log.d("Database", "User successfully written to database");
                })
                .addOnFailureListener(l -> {
                    Log.d("Database", "User failed to be written to database");
                });
    }

    /**
     * Fetches the user based on their email
     *
     * @param email The email of the user you would like to fetch
     * @return a task, which is an asynchronous promise to get the requested user sometime
     * in the future
     * <pre> {@code
     * Task<User> userTask = database.getUser(email);
     *
     * userTask.setOnSuccessListener(user -> {
     *  // use the user here
     * });
     * }</pre>
     */
    public Task<User> fetchUser(String email) {
        DocumentReference docRef = db.collection("users").document(email);
        Task<DocumentSnapshot> getTask = docRef.get();

        return getTask.continueWith(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot doc = task.getResult();;
                if (doc.exists()) {
                    return doc.toObject(User.class);
                } else {
                    return null;
                }
            } else {
                Log.d("fetchUser", "Could not find document");
                throw task.getException();
            }
        });
    }
}
