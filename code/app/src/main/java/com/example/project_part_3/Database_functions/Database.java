package com.example.project_part_3.Database_functions;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;

import com.example.project_part_3.Events.Event;
import com.example.project_part_3.Users.Admin;
import com.example.project_part_3.Users.Entrant;
import com.example.project_part_3.Users.Organizer;
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
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * The Database class provides methods for interacting with the Firebase Firestore database. Most
 * Database methods are tasks that require onSuccessListeners() or onFailureListeners() to be called.
 * Example usage:
 * <pre>
 *     FirebaseFirestore ff = FirebaseFirestore.getInstance();
 *     Database db = new Database(ff);
 *
 *     String email = "ballsdeep69@gmail.com"
 *     db.fetchUser(email).addOnSuccessListener(user -> {
 *          user.setName("Dion"); // changes user name
 *     }
 *     db.fetchUser(email).addOnFailureListener(e -> {
 *          // e is the exception that was thrown
 *          Log.e("fetchUser", "Failed to fetch user", e);
 *     }
 *     db.addUser(new Entrant(...)).addOnSuccessListener(success -> {
 *          // success = true if user was added
 *          // success = false if user exists already
 *     }
 * </pre>
 *
 */
public class Database {
    private final FirebaseFirestore db;
    private final FirebaseStorage storage;
    private static final String USERS_COLLECTION = "users";
    private static final String EVENTS_SUBCOLLECTION = "organized_events";

    public Database(FirebaseFirestore db) {
        this.db = db;
        this.storage = FirebaseStorage.getInstance();
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


    /**
     * Add a user to the database.
     *
     * @param user The user to add to the database.
     * @return A task that completes when the user is added to the database.
     */

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

    /**
     * Check if a user exists in the database.
     *
     * @param email The email of the user to check.
     * @param password The password of the user to check.
     * @return A task that completes when the user is checked.
     */

    public Task<User> checkUser(String email, String password) {
        return fetchUser(email).continueWith(task -> {
            if (!task.isSuccessful()) {
                throw task.getException();
            }
            User user = task.getResult();
            if (user == null) {
                throw new Exception("User not found");
            }
            if (TextUtils.equals(user.getPassword(), password)) {
                return user;
            } else {
                throw new Exception("Incorrect password");
            }
        });
    }

    /**
     * Fetch a user from the database.
     *
     * @param email The email of the user to fetch.
     * @return A task that completes when the user is fetched.
    */
    public Task<User> fetchUser(String email) {
        DocumentReference docRef = db.collection(USERS_COLLECTION).document(email);

        return docRef.get().continueWith(task -> {
            if (!task.isSuccessful()) {
                throw task.getException();
            }

            DocumentSnapshot doc = task.getResult();

            if (doc == null || !doc.exists()) {
                return null;
            }

            String userType = doc.getString("userType");

            if ("Entrant".equals(userType)) {
                return doc.toObject(Entrant.class); // Returns an Entrant object
            } else if ("Organizer".equals(userType)) {
                return doc.toObject(Organizer.class); // Returns an Organizer object
            } else if ("Admin".equals(userType)) {
                return doc.toObject(Admin.class); // Returns an Organizer object
            } else {
                // fallback: how did we get here?
                Log.e("Database", "Unknown user type: " + userType);
                return doc.toObject(User.class);
            }
        });
    }

    /**
     * Set a user in the database.
     *
     * @param user The user to set in the database.
     */

    public void setUser(User user) {
        db.collection(USERS_COLLECTION).document(user.getEmail()).set(user);
    }

    /** Get a list of all users from the database.
     *
     * @return A task that contains a list of all users in the database.
     */
    public Task<List<User>> getAllUsers() {

        return db.collection(USERS_COLLECTION).get().continueWith(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                return task.getResult().toObjects(User.class);
            } else {
                Log.d("getAllUsers", "Could not get documents", task.getException());
                throw task.getException();
            }
        });
    }

    /** Check if a user exists in the database.
     *
     * @param user The user to check.
     * @return A task that completes when the user is checked.
     */
    public Task<Boolean> doesUserExist(User user) {

        return fetchUser(user.getEmail()).continueWith(task -> {
            if (!task.isSuccessful()) {
                Log.e("Database", "Failed to check if user exists", task.getException());
                return false;
            }
            return task.getResult() != null;
        });
    }

    /**
     * Add an event to the database.
     *
     * @param event The event to add to the database.
     * @return A task that completes when the event is added to the database.
     */
    public Task<Boolean> addEvent(@NonNull Event event) {

        DocumentReference docRef = db.collection(USERS_COLLECTION).document(event.getOrganizerId())
                .collection(EVENTS_SUBCOLLECTION).document();
        event.setId(docRef.getId());
        return docRef.set(event).continueWith(setTask -> setTask.isSuccessful());
    }

    /** Fetch an event from the database.
     *
     * @param eventId The ID of the event to fetch.
     * @return A task that completes when the event is fetched.
     */
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
                Log.d("getAllEvents", "Could not get documents", task.getException());
                throw task.getException();
            }
        });
    }

    /**
     * Get a list of all events organized by an organizer.
     *
     * @param email The email of the organizer.
     * @return A task that contains a list of all events hosted by the organizer.
     */
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

    /**
     * Delete a user from the database.
     *
     * @param email The email of the user to delete.
     * @return A task that completes when the user is deleted.
     */
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

    /** Delete an event from the database.
     *
     * @param event The event to delete.
     * @return A task that completes when the event is deleted.
     */
    public Task<Boolean> deleteEvent(Event event) {

        DocumentReference docRef = db.collection(USERS_COLLECTION).document(event.getOrganizerId())
                .collection(EVENTS_SUBCOLLECTION).document(event.getId());
        return docRef.delete().continueWith(Task::isSuccessful);
    }

    /**
     * Update an event in the database.
     *
     * @param event The event to update.
     * @return A task that completes when the event is updated.
     */
    public Task<Boolean> updateEvent(Event event) {
        DocumentReference docRef = db.collection(USERS_COLLECTION).document(event.getOrganizerId());
        docRef = docRef.collection(EVENTS_SUBCOLLECTION).document(event.getId());
        return docRef.set(event).continueWith(Task::isSuccessful);
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

    /**
     * Add a user to the waitlistUserIds array for an event located via collectionGroup(id).
     *
     * @param eventId The ID of the event to add the user to.
     * @param userId The ID of the user to add to the waitlist.
     * @return A task that completes when the user is added to the waitlist.
     */
    public Task<Void> addUserToWaitlistById(@NonNull String eventId, @NonNull String userId) {
        return findEventDocById(eventId)
                .continueWithTask(t -> t.getResult()
                        .update("waitlistUserIds", FieldValue.arrayUnion(userId)));
    }

    /**
     *
     * Remove a user from the waitlistUserIds array for an event located via collectionGroup(id).
     *
     * @param eventId The ID of the event to remove the user from.
     * @param userId The ID of the user to remove from the waitlist.
     * @return A task that completes when the user is removed from the waitlist.
     */
    public Task<Void> removeUserFromWaitlistById(@NonNull String eventId, @NonNull String userId) {
        return findEventDocById(eventId)
                .continueWithTask(t -> t.getResult()
                        .update("waitlistUserIds", FieldValue.arrayRemove(userId)));
    }

    /**
     * Check whether a given user is on the waitlist for an event found via collectionGroup(id).
     *
     * @param eventId The ID of the event to check.
     * @param userId The ID of the user to check.
     *
     * @return A task that completes when the user is checked.
     */
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

    /**
     * Get a list of all events a user is on the waitlist for.
     *

     * @param userId The ID of the user to check.
     * @return A task that contains a list of events that the specified user has waitlisted.
     */
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

    /**
     * Add a user to the waitlist for an event.
     *
     * @param eventId The ID of the event which contains the waitlist to add the user to.
     * @param userEmail The email of the user to add to the waitlist.
     * @return A task that completes when the user is added to the waitlist.
     */
    public Task<Void> addUserToWaitlist(@NonNull String eventId, @NonNull String userEmail) {
        return findEventDocRefById(eventId)
                .onSuccessTask(ref -> ref.update("waitlistUserIds", FieldValue.arrayUnion(userEmail)));
    }

    /**
     * Remove a user from the waitlist for an event.
     *
     * @param eventId The ID of the event which contains the waitlist to remove the user from.
     * @param userEmail The email of the user to remove from the waitlist.
     * @return A task that completes when the user is removed from the waitlist.
     */
    // TODO: Wire this to fire when user clicks cancel button on the "events youve signed up for" tab
    public Task<Void> removeUserFromWaitlist(@NonNull String eventId, @NonNull String userEmail) {
        removeEventFromUser(userEmail, eventId);
        return findEventDocRefById(eventId)
                .onSuccessTask(ref -> ref.update("waitlistUserIds", FieldValue.arrayRemove(userEmail)));
    }

    /**
     * Check whether a user is on the waitlist for an event.
     *
     * @param eventId The ID of the event to check.
     * @param userEmail The email of the user to check.
     * @return A task that completes when the user is checked.
     */
    public Task<Boolean> isUserOnWaitlist(@NonNull String eventId, @NonNull String userEmail) {
        return findEventDocRefById(eventId)
                .onSuccessTask(ref -> ref.get())
                .continueWith(task -> {
                    if (!task.isSuccessful() || task.getResult() == null) return false;
                    List<String> waitlist = (List<String>) task.getResult().get("waitlistUserIds");
                    return waitlist != null && waitlist.contains(userEmail);
                });
    }

    /**
     * Get a list of all events a user is on the waitlist for.
     *
     * @param userId The ID of the user to check.
     * @return A task that contains a list of events that the specified user has waitlisted.
     */
    public Task<List<String>> getEventsUserSelected(@NonNull String userId) {
        return db.collection(USERS_COLLECTION).whereEqualTo("email", userId)
                .limit(1)
                .get()
                .continueWith(task -> {
                    if (!task.isSuccessful()) {
                        throw task.getException();
                    }
                    QuerySnapshot querySnapshots = task.getResult();
                    if (querySnapshots == null || querySnapshots.isEmpty()) {
                        Log.w("getEventsUserSelected", "User with ID " + userId + " not found");
                        return new ArrayList<String>();
                    }
                    DocumentSnapshot snap = querySnapshots.getDocuments().get(0);
                    if(snap.contains("eventsAppliedFor")){
                        List<String> events = (List<String>) snap.get("eventsAppliedFor");
                        if(events != null) {
                            return events;
                        }
                    }
                    return new ArrayList<String>();
                });
    }

    public Task<Void> addEventToUser(@NonNull String userId, @NonNull String eventId) {
        return db.collection(USERS_COLLECTION).document(userId)
                .update("eventsAppliedFor", FieldValue.arrayUnion(eventId));
    }

    public Task<Void> removeEventFromUser(@NonNull String userId, @NonNull String eventId) {
        return db.collection(USERS_COLLECTION).document(userId).update("eventsAppliedFor", FieldValue.arrayRemove(eventId));
    }
    public Task<List<Entrant>> getAcceptedEntrantsByEvent(@NonNull Event event) {
        ArrayList<String> entrantIDs = event.getAttendant_list();

        if (entrantIDs == null || entrantIDs.isEmpty()) {
            return Tasks.forResult(new ArrayList<>());
        }

        List<Task<User>> tasks = new ArrayList<>();

        for (String id : entrantIDs) {
            tasks.add(fetchUser(id));
        }

        return Tasks.whenAllSuccess(tasks).continueWith(task -> {
            List<Object> results = task.getResult();
            List<Entrant> entrants = new ArrayList<>();

            for (Object result : results) {
                if (result instanceof Entrant) {
                    Entrant entrant = (Entrant) result;

                    if (entrant.getUserType().equals("Entrant")) {
                        entrants.add(entrant);
                    }
                }
            }

            return entrants;
        });
    }

    public Task<List<Entrant>> getAllEntrantsByEvent(@NonNull Event event) {
        ArrayList<String> entrantIDs = new ArrayList<>(event.getWaitlistUserIds());


        if (entrantIDs == null || entrantIDs.isEmpty()) {
            return Tasks.forResult(new ArrayList<>());
        }

        List<Task<User>> tasks = new ArrayList<>();

        for (String id : entrantIDs) {
            tasks.add(fetchUser(id));
        }

        return Tasks.whenAllSuccess(tasks).continueWith(task -> {
            List<Object> results = task.getResult();
            List<Entrant> entrants = new ArrayList<>();
            for (Object result : results) {
                if (result instanceof Entrant) {
                    Entrant entrant = (Entrant) result;
                    if (entrant.getUserType().equals("Entrant")) {
                        entrants.add(entrant);
                    }
                }
            }
            return entrants;
        });
    }

    public Task<Boolean> declineEntrant(Event event, Entrant entrant) {
        event.declineAttendant(entrant.getEmail());
        return updateEvent(event);
    }

    public Task<Uri> uploadImage(@NonNull Uri imageUri, @NonNull String folderPath) {
        String fileName = "IMG_" + System.currentTimeMillis() + ".jpg";
        StorageReference storageRef = storage.getReference().child(folderPath + "/" + fileName);

        UploadTask uploadTask = storageRef.putFile(imageUri);
        return uploadTask.continueWithTask(task -> {
            if (!task.isSuccessful()) {
               throw task.getException();
            }
            return storageRef.getDownloadUrl();
        });
    }

    public Task<Void> deleteImageByUrl(@NonNull String imageUrl) {
        StorageReference photoRef = storage.getReferenceFromUrl(imageUrl);
        return photoRef.delete();
    }
}

