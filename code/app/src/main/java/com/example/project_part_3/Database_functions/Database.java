package com.example.project_part_3.Database_functions;
import android.util.Log;

import androidx.annotation.NonNull;

import com.example.project_part_3.Events.Event;
import com.example.project_part_3.Users.User;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class Database {
    FirebaseFirestore db;
    public Database(FirebaseFirestore db) {
        this.db = db;
    }

    /**
     * Adds a user to the Firebase database, but only if a user with that email doesn't already exist.
     *
     * @param user The user you would like to add.
     * @return A Task that completes with a Boolean: `true` if the user was successfully added,
     * `false` if the user already existed.
     */
    public Task<Boolean> addUser(@NonNull User user) {
        // first, check if the user exists.
        return doesUserExist(user).continueWithTask(task -> {
            boolean exists = task.getResult();

            if (exists) {
                // If the user already exists, complete the task immediately with `false`.
                Log.d("addUser", "User already exists. Add operation cancelled.");
                return Tasks.forResult(false);
            } else {
                // If the user does not exist, proceed to add them.
                DocumentReference docRef = db.collection("users").document(user.getEmail());

                return docRef.set(user).continueWith(setTask -> {
                    if (setTask.isSuccessful()) {
                        Log.d("addUser", "User successfully written to database");
                        return true; // The user was added
                    } else {
                        Log.e("addUser", "User failed to be written to database", setTask.getException());
                        return false;
                    }
                });
            }
        });
    }

    public Task<Boolean> addEvent(@NonNull Event event) {
        DocumentReference docRef = db.collection("events").document();
        event.setId(docRef.getId()); // gives the event a unique ID

        return docRef.set(event).continueWith(setTask -> {
            if (setTask.isSuccessful()) {
                Log.d("addEvent", "Event successfully written to database");
                return true;
            } else {
                Log.e("addEvent", "Event failed to be written to database", setTask.getException());
                return false;
            }
        });
    }

    /**
     * Fetches the user based on their email.
     *
     * @param email The email of the user you want to fetch.
     * @return A Task that completes with the User object. Returns null if the user does not exist or an error occurs.
     */
    public Task<User> fetchUser(String email) {
        DocumentReference docRef = db.collection("users").document(email);
        Task<DocumentSnapshot> getTask = docRef.get();

        return getTask.continueWith(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot doc = task.getResult();

                if (doc != null && doc.exists()) {
                    return doc.toObject(User.class);
                } else {
                    Log.d("fetchUser", "User does not exist");
                    return null;
                }
            } else {
                Log.d("fetchUser", "Could not find document", task.getException());
                return null;
            }
        });
    }

    /**
     * Gets all users from the database
     *
     * @return A Task that completes with a List of Users. The Task will fail if a database error occurs.
     */
    public Task<List<User>> getAllUsers() {
        Task<QuerySnapshot> queryTask = db.collection("users").get();

        return queryTask.continueWith(task -> {
            if (task.isSuccessful()) {
                QuerySnapshot query = task.getResult();
                List<User> users = new ArrayList<>();
                if (query != null) {
                    for (DocumentSnapshot doc : query.getDocuments()) {
                        // Add an extra null check for safety
                        if (doc != null && doc.exists()) {
                            // This will also work now because of Fix 2
                            users.add(doc.toObject(User.class));
                        }
                    }
                }
                return users;
            } else {
                return null;
            }
        });
    }

    public Task<Boolean> deleteUser(String email) {
        DocumentReference docRef = db.collection("users").document(email);
        return docRef.delete().continueWith(task -> {
            if (task.isSuccessful()) {
                Log.d("deleteUser", "User successfully deleted");
                return true;
            } else {
                Log.e("deleteUser", "User failed to be deleted", task.getException());
                return false;
            }
        });
    }

    /**
     * Verifies if email and password is correct, and returns the user for which it is correct.
     *
     * @param email
     * @param password
     * @return Task that completes with the User object. Returns null if the user does not exist or an error occurs.
     */
    public Task<User> checkUser(String email, String password) {
        DocumentReference docRef = db.collection("users").document(email);
        Task<DocumentSnapshot> getTask = docRef.get();

        return getTask.continueWith(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot doc = task.getResult();
                if (doc != null && doc.exists()) {
                    if (doc.getString("password").equals(password)) {
                        return doc.toObject(User.class);
                        }
                }
            }
            return null;
        });
    }

    public Task<Boolean> doesUserExist(User user) {
        return fetchUser(user.getEmail()).continueWith(task -> {
            if (!task.isSuccessful()) {
                // Log the error but treat it as "user does not exist"
                Log.e("doesUserExist", "Failed to check if user exists", task.getException());
                return false;
            }

            User existingUser = task.getResult();
            return (existingUser != null); // True if user exists, false otherwise
        });
    }
}