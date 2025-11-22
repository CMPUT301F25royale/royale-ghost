package com.example.project_part_3.Database_functions;
import android.util.Log;

import androidx.annotation.NonNull;

import com.example.project_part_3.Events.Event;
import com.example.project_part_3.Users.User;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;


public class Database {
    private final FirebaseFirestore db;
    private static final String USERS_COLLECTION = "users";
    private static final String EVENTS_SUBCOLLECTION = "organized_events";

    public Database(FirebaseFirestore db) {
        this.db = db;
    }

    public ListenerRegistration listenForUsers(EventListener<QuerySnapshot> listener) {
        return db.collection(USERS_COLLECTION).addSnapshotListener(listener);
    }

    public ListenerRegistration listenForSingleUser(String email, EventListener<DocumentSnapshot> listener) {
        return db.collection(USERS_COLLECTION).document(email).addSnapshotListener(listener);
    }

    public ListenerRegistration listenForEvents(EventListener<QuerySnapshot> listener) {
        return db.collectionGroup(EVENTS_SUBCOLLECTION).addSnapshotListener(listener);
    }

    public ListenerRegistration listenForSingleEvent(String eventId, EventListener<QuerySnapshot> listener) {
        return db.collectionGroup(EVENTS_SUBCOLLECTION).whereEqualTo("id", eventId).limit(1).addSnapshotListener(listener);
    }


    public Task<Boolean> addUser(@NonNull User user) {
        return doesUserExist(user).continueWithTask(task -> {
            boolean exists = task.getResult();
            if (exists) {
                Log.d("Database", "addUser: User already exists.");
                return Tasks.forResult(false);
            } else {
                return db.collection(USERS_COLLECTION).document(user.getEmail()).set(user)
                        .continueWith(setTask -> setTask.isSuccessful());
            }
        });
    }

    public Task<User> checkUser(String email, String password) {
        return fetchUser(email).continueWith(task -> {
            if (!task.isSuccessful()) {
                throw task.getException();
            }
            User user = task.getResult();
            if (user == null) {
                throw new Exception("User not found");
            }
            if (android.text.TextUtils.equals(user.getPassword(), password)) {
                return user;
            } else {
                throw new Exception("Incorrect password");
            }
        });
    }

    public Task<User> fetchUser(String email) {

        DocumentReference docRef = db.collection(USERS_COLLECTION).document(email);
        return docRef.get().continueWith(task -> {
            if (!task.isSuccessful()) {
                throw task.getException();
            }
            DocumentSnapshot doc = task.getResult();
            return (doc != null && doc.exists()) ? doc.toObject(User.class) : null;
        });
    }

    public void setUser(User user) {
        db.collection(USERS_COLLECTION).document(user.getEmail()).set(user);
    }

    public Task<List<User>> getAllUsers() {

        return db.collection(USERS_COLLECTION).get().continueWith(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                return task.getResult().toObjects(User.class);
            } else {
                throw task.getException();
            }
        });
    }

    public Task<Boolean> doesUserExist(User user) {

        return fetchUser(user.getEmail()).continueWith(task -> {
            if (!task.isSuccessful()) {
                Log.e("Database", "Failed to check if user exists", task.getException());
                return false;
            }
            return task.getResult() != null;
        });
    }

    public Task<Boolean> addEvent(@NonNull Event event) {

        DocumentReference docRef = db.collection(USERS_COLLECTION).document(event.getOrganizerId())
                .collection(EVENTS_SUBCOLLECTION).document();
        event.setId(docRef.getId());
        return docRef.set(event).continueWith(setTask -> setTask.isSuccessful());
    }

    public Task<Event> fetchEvent(String eventId) {

        return db.collectionGroup(EVENTS_SUBCOLLECTION).whereEqualTo("id", eventId).limit(1).get()
                .continueWith(task -> {
                    if (!task.isSuccessful() || task.getResult() == null || task.getResult().isEmpty()) {
                        throw new Exception("Event with ID " + eventId + " does not exist.");
                    }
                    return task.getResult().getDocuments().get(0).toObject(Event.class);
                });
    }

    public Task<List<Event>> getAllEvents() {

        return db.collectionGroup(EVENTS_SUBCOLLECTION).get().continueWith(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                return task.getResult().toObjects(Event.class);
            } else {
                throw task.getException();
            }
        });
    }

    public Task<List<Event>> getEventsByOrganizer(@NonNull String email) {

        return db.collection(USERS_COLLECTION).document(email).collection(EVENTS_SUBCOLLECTION).get()
                .continueWith(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        return task.getResult().toObjects(Event.class);
                    } else {
                        throw task.getException();
                    }
                });
    }

    public Task<Boolean> deleteUser(String email) {
        DocumentReference userDocRef = db.collection(USERS_COLLECTION).document(email);
        return db.runTransaction(transaction -> {
                    DocumentSnapshot userSnapshot = transaction.get(userDocRef);
                    User user = userSnapshot.toObject(User.class);

                    if (user != null && "Organizer".equals(user.getUserType())) {
                        CollectionReference eventsCollection = userDocRef.collection(EVENTS_SUBCOLLECTION);
                        QuerySnapshot eventsSnapshot = null;
                        try {
                            eventsSnapshot = Tasks.await(eventsCollection.get());
                        } catch (ExecutionException e) {
                            throw new RuntimeException(e);
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                        for (DocumentSnapshot eventDoc : eventsSnapshot.getDocuments()) {
                            transaction.delete(eventDoc.getReference());
                        }
                    }
                    transaction.delete(userDocRef);
                    return true;
                }).addOnSuccessListener(success -> Log.d("deleteUser", "User and associated events deleted successfully"))
                .addOnFailureListener(e -> Log.e("deleteUser", "Failed to delete user", e));
    }

    public Task<Boolean> deleteEvent(Event event) {

        DocumentReference docRef = db.collection(USERS_COLLECTION).document(event.getOrganizerId())
                .collection(EVENTS_SUBCOLLECTION).document(event.getId());
        return docRef.delete().continueWith(task -> task.isSuccessful());
    }
}
