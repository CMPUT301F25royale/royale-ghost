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
import com.google.firebase.firestore.FieldValue;
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

    /** Resolve the single organized_events doc for a given eventId. */
    private Task<DocumentReference> findEventDocById(@NonNull String eventId) {
        return db.collectionGroup(EVENTS_SUBCOLLECTION)
                .whereEqualTo("id", eventId)
                .limit(1)
                .get()
                .continueWith(task -> {
                    if (!task.isSuccessful()) throw task.getException();
                    if (task.getResult() == null || task.getResult().isEmpty()) {
                        throw new Exception("Event with ID " + eventId + " not found");
                    }
                    return task.getResult().getDocuments().get(0).getReference();
                });
    }

    /** Add a user to the waitlistUserIds array for an event located via collectionGroup(id). */
    public Task<Void> addUserToWaitlistById(@NonNull String eventId, @NonNull String userId) {
        return findEventDocById(eventId)
                .continueWithTask(t -> t.getResult()
                        .update("waitlistUserIds", com.google.firebase.firestore.FieldValue.arrayUnion(userId)));
    }

    /** Remove a user from the waitlistUserIds array for an event located via collectionGroup(id). */
    public Task<Void> removeUserFromWaitlistById(@NonNull String eventId, @NonNull String userId) {
        return findEventDocById(eventId)
                .continueWithTask(t -> t.getResult()
                        .update("waitlistUserIds", com.google.firebase.firestore.FieldValue.arrayRemove(userId)));
    }

    /** Check whether a given user is on the waitlist for an event found via collectionGroup(id). */
    public Task<Boolean> isUserOnWaitlistById(@NonNull String eventId, @NonNull String userId) {
        return findEventDocById(eventId)
                .continueWithTask(t -> t.getResult().get())
                .continueWith(task -> {
                    if (!task.isSuccessful()) throw task.getException();
                    Event ev = task.getResult().toObject(Event.class);
                    if (ev == null || ev.getWaitlistUserIds() == null) return false;
                    return ev.getWaitlistUserIds().contains(userId);
                });
    }

    public Task<List<Event>> getEventsUserWaitlisted(@NonNull String userId) {
        return db.collectionGroup(EVENTS_SUBCOLLECTION)
                .whereArrayContains("waitlistUserIds", userId)
                .get()
                .continueWith(task -> {
                    if (!task.isSuccessful()) throw task.getException();
                    if (task.getResult() == null) return new ArrayList<>();
                    return task.getResult().toObjects(Event.class);
                });
    }

    // Helpers to locate an event doc by its id in users/*/organized_events/*
    private Task<DocumentReference> findEventDocRefById(@NonNull String eventId) {
        return db.collectionGroup(EVENTS_SUBCOLLECTION)
                .whereEqualTo("id", eventId)
                .limit(1)
                .get()
                .continueWith(task -> {
                    if (!task.isSuccessful() || task.getResult() == null || task.getResult().isEmpty()) {
                        throw new Exception("Event with ID " + eventId + " not found");
                    }
                    DocumentSnapshot snap = task.getResult().getDocuments().get(0);
                    return snap.getReference();
                });
    }

    // Waitlist functions
    public Task<Void> addUserToWaitlist(@NonNull String eventId, @NonNull String userEmail) {
        return findEventDocRefById(eventId)
                .onSuccessTask(ref -> ref.update("waitlistUserIds", FieldValue.arrayUnion(userEmail)));
    }

    // TODO: Wire this to fire when user clicks cancel button on the "events youve signed up for" tab
    public Task<Void> removeUserFromWaitlist(@NonNull String eventId, @NonNull String userEmail) {
        return findEventDocRefById(eventId)
                .onSuccessTask(ref -> ref.update("waitlistUserIds", FieldValue.arrayRemove(userEmail)));
    }

    public Task<Boolean> isUserOnWaitlist(@NonNull String eventId, @NonNull String userEmail) {
        return findEventDocRefById(eventId)
                .onSuccessTask(ref -> ref.get())
                .continueWith(task -> {
                    if (!task.isSuccessful() || task.getResult() == null) return false;
                    List<String> waitlist = (List<String>) task.getResult().get("waitlistUserIds");
                    return waitlist != null && waitlist.contains(userEmail);
                });
    }




}