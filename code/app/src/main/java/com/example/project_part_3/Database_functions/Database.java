package com.example.project_part_3.Database_functions;

import static java.util.UUID.randomUUID;

import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.project_part_3.Events.Event;
import com.example.project_part_3.Image.Image_datamap;
import com.example.project_part_3.Users.Admin;
import com.example.project_part_3.Users.Entrant;
import com.example.project_part_3.Users.Organizer;
import com.example.project_part_3.Users.User;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.WriteBatch;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The {@code Database} class provides a higher-level API for interacting with
 * Firebase Firestore and Firebase Storage, mainly around {@link User},
 * {@link Event}, and {@link Image_datamap} objects.
 * <p>
 * Most methods return a {@link Task} that must be handled asynchronously via
 * {@code addOnSuccessListener(...)} / {@code addOnFailureListener(...)}.
 * Example usage:
 * <pre>{@code
 * FirebaseFirestore ff = FirebaseFirestore.getInstance();
 * Database db = new Database(ff);
 *
 * String email = "example@gmail.com";
 * db.fetchUser(email).addOnSuccessListener(user -> {
 *     if (user != null) {
 *         user.setName("Dion");
 *     }
 * });
 *
 * db.fetchUser(email).addOnFailureListener(e -> {
 *     Log.e("fetchUser", "Failed to fetch user", e);
 * });
 *
 * db.addUser(new Entrant(...)).addOnSuccessListener(success -> {
 *     // success = true if user was added
 *     // success = false if user already exists
 * });
 * }</pre>
 */
public class Database {
    private final FirebaseFirestore db;
    private final FirebaseStorage storage;
    private static final String USERS_COLLECTION = "users";
    private static final String EVENTS_SUBCOLLECTION = "organized_events";
    private static final String IMAGES_COLLECTION = "images";

    /**
     * Returns the underlying {@link FirebaseFirestore} instance.
     *
     * @return the Firestore instance used by this {@code Database}
     */
    public FirebaseFirestore getDb() {
        return this.db;
    }

    /**
     * Creates a new {@code Database} wrapper using the given
     * {@link FirebaseFirestore} instance and a default {@link FirebaseStorage}.
     *
     * @param db the Firestore instance to use
     */
    public Database(FirebaseFirestore db) {
        this.db = db;
        this.storage = FirebaseStorage.getInstance();
    }

    /**
     * Registers a realtime listener for all documents in the {@code users} collection.
     *
     * @param listener the {@link EventListener} to receive snapshot updates
     * @return a {@link ListenerRegistration} that can be used to remove the listener
     */
    public ListenerRegistration listenForUsers(EventListener<QuerySnapshot> listener) {
        return db.collection(USERS_COLLECTION).addSnapshotListener(listener);
    }

    /**
     * Registers a realtime listener for a single user document identified by email.
     *
     * @param email    the user email / document ID
     * @param listener the {@link EventListener} to receive snapshot updates
     * @return a {@link ListenerRegistration} that can be used to remove the listener
     */
    public ListenerRegistration listenForSingleUser(String email, EventListener<DocumentSnapshot> listener) {
        return db.collection(USERS_COLLECTION).document(email).addSnapshotListener(listener);
    }

    /**
     * Registers a realtime listener for all {@link Event} documents in the
     * {@code organized_events} collection group.
     *
     * @param listener the {@link EventListener} to receive snapshot updates
     * @return a {@link ListenerRegistration} that can be used to remove the listener
     */
    public ListenerRegistration listenForEvents(EventListener<QuerySnapshot> listener) {
        return db.collectionGroup(EVENTS_SUBCOLLECTION).addSnapshotListener(listener);
    }

    /**
     * Registers a realtime listener for a single {@link Event} identified by ID
     * using the {@code organized_events} collection group.
     *
     * @param eventId  the ID of the event to listen to
     * @param listener the {@link EventListener} to receive snapshot updates
     * @return a {@link ListenerRegistration} that can be used to remove the listener
     */
    public ListenerRegistration listenForSingleEvent(String eventId, EventListener<QuerySnapshot> listener) {
        return db.collectionGroup(EVENTS_SUBCOLLECTION).whereEqualTo("id", eventId).limit(1).addSnapshotListener(listener);
    }

    /**
     * Add a {@link User} to the database if they do not already exist.
     * Internally calls {@link #doesUserExist(User)}.
     *
     * @param user the {@link User} to add to the database
     * @return a {@link Task} that resolves to {@code true} if the user was added,
     *         or {@code false} if the user already exists
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
     * Add an interest string to the {@code interests} array of the specified user.
     *
     * @param email       the email of the user (document ID in {@code users})
     * @param newInterest the interest to add
     * @return a {@link Task} resolving to {@code true} if the update succeeded
     */
    public Task<Boolean> addInterest(String email, String newInterest) {
        return db.collection(USERS_COLLECTION)
                .document(email)
                .update("interests", FieldValue.arrayUnion(newInterest))
                .continueWith(task -> {
                    if (!task.isSuccessful()) {
                        throw task.getException();
                    }
                    return true;
                });
    }

    /**
     * Fetches the {@code interests} field for the user with the given email.
     *
     * @param email the email of the user whose interests are requested
     * @return a {@link Task} resolving to an {@link ArrayList} of interests
     */
    public Task<ArrayList<String>> getInterests(String email) {
        return db.collection(USERS_COLLECTION)
                .document(email)
                .get()
                .continueWith(task -> {
                    if (!task.isSuccessful()) {
                        throw task.getException();
                    }
                    List<String> interests = (List<String>) task.getResult().get("interests");

                    if (interests == null) {
                        return new ArrayList<>();    // Field not yet created → return empty list
                    }

                    return new ArrayList<>(interests);  // Return copy as ArrayList
                });
    }

    /**
     * Removes an interest string from the {@code interests} array of the specified user.
     *
     * @param email       the email of the user (document ID in {@code users})
     * @param oldInterest the interest to remove
     * @return a {@link Task} resolving to {@code true} if the update succeeded
     */
    public Task<Boolean> deleteInterest(String email, String oldInterest) {
        return db.collection(USERS_COLLECTION)
                .document(email)
                .update("interests", FieldValue.arrayRemove(oldInterest))
                .continueWith(task -> {
                    if (!task.isSuccessful()) {
                        throw task.getException();
                    }
                    return true;
                });
    }

    /**
     * Check if a user exists and validate their password.
     * <p>
     * This method calls {@link #fetchUser(String)} and compares the stored password.
     *
     * @param email    the email of the user to check
     * @param password the password of the user to check
     * @return a {@link Task} that resolves to the {@link User} on success;
     *         throws if the user is not found or the password is incorrect
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
     * Fetch a {@link User} from the database by email, resolving to the appropriate subtype.
     * May return {@link Entrant}, {@link Organizer}, {@link Admin}, or plain {@link User}.
     *
     * @param email the email of the user to fetch (document ID in {@code users})
     * @return a {@link Task} resolving to a {@link User} instance, or {@code null} if not found
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
                return doc.toObject(Entrant.class);
            } else if ("Organizer".equals(userType)) {
                return doc.toObject(Organizer.class);
            } else if ("Admin".equals(userType)) {
                return doc.toObject(Admin.class);
            } else {
                Log.e("Database", "Unknown user type: " + userType);
                return doc.toObject(User.class);
            }
        });
    }

    /**
     * Overwrites a {@link User} document in the {@code users} collection.
     * This is a direct setter and does not return a {@link Task}.
     *
     * @param user the {@link User} to persist
     */
    public void setUser(User user) {
        db.collection(USERS_COLLECTION).document(user.getEmail()).set(user);
    }

    /**
     * Get a list of all {@link User} documents from the database.
     *
     * @return a {@link Task} resolving to a {@link List} of {@link User} objects
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

    /**
     * Check if a given {@link User} already exists in the database by email.
     * This method delegates to {@link #fetchUser(String)}.
     *
     * @param user the {@link User} whose existence is being checked
     * @return a {@link Task} resolving to {@code true} if the user exists, {@code false} otherwise
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
     * Add an {@link Event} to the database under its organizer's {@code organized_events}
     * subcollection. If the organizer document does not exist yet, a minimal one is created.
     *
     * @param event the {@link Event} to add; must have a non-null {@code organizerId}
     * @return a {@link Task} resolving to {@code true} when the event is created successfully
     */
    public Task<Boolean> addEvent(@NonNull Event event) {
        if (event.getOrganizerId() == null) {
            return Tasks.forException(new IllegalArgumentException("Event must have an organizer ID."));
        }

        DocumentReference OrganizerRef = db.collection(USERS_COLLECTION).document(event.getOrganizerId());
        DocumentReference eventDocRef = OrganizerRef.collection(EVENTS_SUBCOLLECTION).document();
        event.setId(eventDocRef.getId());
        event.setLotteryDone(false);
        return db.runTransaction(transaction -> {
            DocumentSnapshot organizerSnapshot = transaction.get(OrganizerRef);
            if (!organizerSnapshot.exists()) {
                Log.w("addEvent", "Organizer document " + event.getOrganizerId() + " did not exist. Creating it now.");
                Map<String, Object> newOrganizerData = new HashMap<>();
                newOrganizerData.put("email", event.getOrganizerId());
                newOrganizerData.put("userType", "Organizer");
                transaction.set(OrganizerRef, newOrganizerData);
            }
            transaction.set(eventDocRef, event);
            return true;
        });
    }

    /**
     * Fetch an {@link Event} by its ID using the {@code organized_events} collection group.
     *
     * @param eventId the ID of the event to fetch
     * @return a {@link Task} resolving to the {@link Event} if found; throws otherwise
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

    /**
     * Get a list of all {@link Event} documents from the {@code organized_events} collection group.
     *
     * @return a {@link Task} resolving to a {@link List} of {@link Event} objects
     */
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
     * Get all events organized by a specific organizer.
     *
     * @param email the email of the organizer (document ID in {@code users})
     * @return a {@link Task} resolving to a {@link List} of {@link Event} objects
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
     * Delete a {@link User}, their organized events, and associated images (profile picture
     * and event posters). This method uses a {@link WriteBatch} to ensure consistency.
     *
     * @param email the email of the user to delete
     * @return a {@link Task} that completes when all deletions are committed
     */
    public Task<Void> deleteUser(String email) {
        DocumentReference userDocRef = db.collection(USERS_COLLECTION).document(email);
        return getEventsByOrganizer(email).continueWithTask(eventsTask -> {
            WriteBatch batch = db.batch();
            batch.delete(userDocRef);
            if (eventsTask.isSuccessful()) {
                for (Event event : eventsTask.getResult()) {
                    if (event.getImageInfo() != null && event.getImageInfo().getPath() != null && event.getImageInfo().getId() != null) {
                        try {
                            String imagePath = event.getImageInfo().getPath();
                            String imageId = event.getImageInfo().getId();
                            storage.getReference().child(imagePath).delete();
                            DocumentReference imageDoc = db.collection(IMAGES_COLLECTION).document(imageId);
                            batch.delete(imageDoc);
                        } catch (Exception e) {
                            Log.w("DeleteUser", "Could not delete event poster: " + event.getImageInfo().getPath(), e);
                        }
                    }
                    DocumentReference eventRef = db.collection(USERS_COLLECTION)
                            .document(email)
                            .collection(EVENTS_SUBCOLLECTION)
                            .document(event.getId());
                    batch.delete(eventRef);
                }
            } else {
                Log.w("DeleteUser", "Could not fetch events for user, they will not be deleted.", eventsTask.getException());
            }

            return userDocRef.get().continueWithTask(userTask -> {
                if (userTask.isSuccessful() && userTask.getResult().exists()) {
                    Image_datamap profilePicMeta = userTask.getResult().get("imageInfo", Image_datamap.class);
                    if (profilePicMeta != null && profilePicMeta.getPath() != null && profilePicMeta.getId() != null) {
                        try {
                            String imagePath = profilePicMeta.getPath();
                            String imageId = profilePicMeta.getId();
                            storage.getReference().child(imagePath).delete();
                            DocumentReference imageDoc = db.collection(IMAGES_COLLECTION).document(imageId);
                            batch.delete(imageDoc);
                        } catch (Exception e) {
                            Log.w("DeleteUser", "Could not delete profile pic: " + profilePicMeta.getPath(), e);
                        }
                    }
                }
                return batch.commit();
            });
        });
    }

    /**
     * Delete an {@link Event} and its associated top-level image document (if any).
     *
     * @param event the {@link Event} to delete
     * @return a {@link Task} resolving to {@code true} once deletion is complete
     */
    public Task<Boolean> deleteEvent(Event event) {
        String eventId = event.getId();
        String organizerId = event.getOrganizerId();

        if (eventId == null || organizerId == null) {
            return Tasks.forException(new IllegalArgumentException("Event ID or Organizer ID is null"));
        }

        DocumentReference eventDocRef = db.collection(USERS_COLLECTION)
                .document(organizerId)
                .collection(EVENTS_SUBCOLLECTION)
                .document(eventId);

        return db.runTransaction(transaction -> {
            DocumentSnapshot eventSnapshot = transaction.get(eventDocRef);
            if (!eventSnapshot.exists()) {
                try {
                    throw new Exception("Event does not exist!");
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
            Event currentEvent = eventSnapshot.toObject(Event.class);
            if (currentEvent != null && currentEvent.getImageInfo() != null && currentEvent.getImageInfo().getPath() != null && currentEvent.getImageInfo().getId() != null) {
                String imageId = currentEvent.getImageInfo().getId();
                String imagePath = currentEvent.getImageInfo().getPath();
                storage.getReference().child(imagePath).delete();
                DocumentReference imageTopLevelRef = db.collection(IMAGES_COLLECTION).document(imageId);
                transaction.delete(imageTopLevelRef);
            }

            transaction.delete(eventDocRef);
            return true;
        });
    }

    /**
     * Update an {@link Event} document using {@link Event#toMap()}.
     *
     * @param event the {@link Event} to update (must have non-null ID and organizer ID)
     * @return a {@link Task} resolving to {@code true} when the update completes
     */
    public Task<Boolean> updateEvent(Event event) {
        if (event == null || event.getOrganizerId() == null || event.getId() == null) {
            return Tasks.forException(new IllegalArgumentException("Invalid event object for update."));
        }

        DocumentReference docRef = db.collection(USERS_COLLECTION)
                .document(event.getOrganizerId())
                .collection(EVENTS_SUBCOLLECTION)
                .document(event.getId());

        return db.runTransaction(transaction -> {
            DocumentSnapshot snapshot = transaction.get(docRef);
            if (!snapshot.exists()) {
                throw new RuntimeException("Event " + event.getId() + " not found, cannot update.");
            }
            Map<String, Object> eventData = event.toMap();
            transaction.update(docRef, eventData);
            return true;
        });
    }

    /**
     * Resolve the single {@code organized_events} document for a given eventId.
     * Used by methods such as {@link #addUserToWaitlistById(String, String)}.
     *
     * @param eventId the ID of the event
     * @return a {@link Task} resolving to the {@link DocumentReference} for that event
     */
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
     * Add a user to the {@code waitlistUserIds} array for an event located via
     * {@link #findEventDocById(String)}.
     *
     * @param eventId the ID of the event
     * @param userId  the ID/email of the user to add to the waitlist
     * @return a {@link Task} that completes when the user is added
     */
    public Task<Void> addUserToWaitlistById(@NonNull String eventId, @NonNull String userId) {
        return findEventDocById(eventId)
                .continueWithTask(t -> t.getResult()
                        .update("waitlistUserIds", FieldValue.arrayUnion(userId)));
    }

    /**
     * Remove a user from the {@code waitlistUserIds} array for an event located via
     * {@link #findEventDocById(String)}.
     *
     * @param eventId the ID of the event
     * @param userId  the ID/email of the user to remove from the waitlist
     * @return a {@link Task} that completes when the user is removed
     */
    public Task<Void> removeUserFromWaitlistById(@NonNull String eventId, @NonNull String userId) {
        return findEventDocById(eventId)
                .continueWithTask(t -> t.getResult()
                        .update("waitlistUserIds", FieldValue.arrayRemove(userId)));
    }

    /**
     * Check whether a given user is on the {@code waitlistUserIds} array for an event
     * found via {@link #findEventDocById(String)}.
     *
     * @param eventId the ID of the event to check
     * @param userId  the ID/email of the user to check
     * @return a {@link Task} resolving to {@code true} if the user is on the waitlist
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
     * Get a list of all {@link Event} objects where a given user ID appears in
     * {@code waitlistUserIds}.
     *
     * @param userId the ID/email of the user to check
     * @return a {@link Task} resolving to a {@link List} of {@link Event} objects
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

    /**
     * Helper to locate an event doc by its ID in users' {@code organized_events}.
     * Similar to {@link #findEventDocById(String)} but used by other waitlist helpers.
     *
     * @param eventId the ID of the event
     * @return a {@link Task} resolving to the {@link DocumentReference} tied to the given event ID
     */
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
     * Add a user email to the {@code waitlistUserIds} array for an event.
     *
     * @param eventId   the ID of the event
     * @param userEmail the user email to add to the waitlist
     * @return a {@link Task} that completes when the user is added
     */
    public Task<Void> addUserToWaitlist(@NonNull String eventId, @NonNull String userEmail) {
        return findEventDocRefById(eventId)
                .onSuccessTask(ref -> ref.update("waitlistUserIds", FieldValue.arrayUnion(userEmail)));
    }

    /**
     * Remove a user email from the {@code waitlistUserIds} array for an event, and
     * also remove that event from the user's {@code eventsAppliedFor} list by calling
     * {@link #removeEventFromUser(String, String)}.
     *
     * @param eventId   the ID of the event
     * @param userEmail the user email to remove from the waitlist
     * @return a {@link Task} that completes when all updates are applied
     */
    public Task<Void> removeUserFromWaitlist(@NonNull String eventId, @NonNull String userEmail) {
        removeEventFromUser(userEmail, eventId);
        return findEventDocRefById(eventId)
                .onSuccessTask(ref -> ref.update("waitlistUserIds", FieldValue.arrayRemove(userEmail)));
    }

    /**
     * Check whether a user is on the {@code waitlistUserIds} array for a given event.
     *
     * @param eventId   the ID of the event to check
     * @param userEmail the email of the user to check
     * @return a {@link Task} resolving to {@code true} if the user is on the waitlist
     */
    public Task<Boolean> isUserOnWaitlist(@NonNull String eventId, @NonNull String userEmail) {
        return findEventDocRefById(eventId)
                .onSuccessTask(ref -> ref.get())
                .continueWith(task -> {
                    if (!task.isSuccessful() || task.getResult() == null) return false;
                    Object waitlistObj = task.getResult().get("waitlistUserIds");
                    if (waitlistObj instanceof List) {
                        List<?> waitlist = (List<?>) waitlistObj;
                        return waitlist.contains(userEmail);
                    }
                    return false;
                });
    }

    /**
     * Get the list of event IDs that a user has applied for, stored in
     * {@code eventsAppliedFor} on the {@link User} document.
     *
     * @param userId the ID/email of the user to check
     * @return a {@link Task} resolving to a {@link List} of event ID strings
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
                    if (snap.contains("eventsAppliedFor")) {
                        Object eventsObj = snap.get("eventsAppliedFor");
                        if (eventsObj instanceof List) {
                            List<?> rawList = (List<?>) eventsObj;
                            List<String> events = new ArrayList<>();
                            for (Object item : rawList) {
                                if (item instanceof String) {
                                    events.add((String) item);
                                }
                            }
                            return events;
                        }
                    }
                    return new ArrayList<String>();
                });
    }

    /**
     * Adds an event ID to a user's {@code eventsAppliedFor} array.
     *
     * @param userId  the user ID/email
     * @param eventId the event ID to add
     * @return a {@link Task} that completes when the update finishes
     */
    public Task<Void> addEventToUser(@NonNull String userId, @NonNull String eventId) {
        return db.collection(USERS_COLLECTION).document(userId)
                .update("eventsAppliedFor", FieldValue.arrayUnion(eventId));
    }

    /**
     * Removes an event ID from a user's {@code eventsAppliedFor} array.
     *
     * @param userId  the user ID/email
     * @param eventId the event ID to remove
     * @return a {@link Task} that completes when the update finishes
     */
    public Task<Void> removeEventFromUser(@NonNull String userId, @NonNull String eventId) {
        return db.collection(USERS_COLLECTION).document(userId).update("eventsAppliedFor", FieldValue.arrayRemove(eventId));
    }

    /**
     * Get a list of all {@link Entrant} objects who have accepted an invitation to a given event.
     * Uses {@link Event#getAttendant_list()} and {@link #fetchUser(String)} under the hood.
     *
     * @param event the {@link Event} to get entrants for
     * @return a {@link Task} resolving to a {@link List} of {@link Entrant} objects
     */
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
            List<Entrant> entrants = new ArrayList<>();
            if (task.isSuccessful() && task.getResult() != null) {
                for (Object result : task.getResult()) {
                    if (result instanceof Entrant) {
                        Entrant entrant = (Entrant) result;
                        if (entrant.getUserType().equals("Entrant")) {
                            entrants.add(entrant);
                        }
                    }
                }
            }
            return entrants;
        });
    }

    /**
     * Get a list of {@link Entrant} objects who are on the waitlist for an event,
     * using {@link Event#getWaitlistUserIds()} and {@link #fetchUser(String)}.
     *
     * @param event the {@link Event} to get entrants for
     * @return a {@link Task} resolving to a {@link List} of {@link Entrant} objects
     */
    public Task<List<Entrant>> getAllEntrantsByEvent(@NonNull Event event) {
        ArrayList<String> entrantIDs = new ArrayList<>();
        if (event.getWaitlistUserIds() != null) {
            entrantIDs.addAll(event.getWaitlistUserIds());
        }

        if (entrantIDs.isEmpty()) {
            return Tasks.forResult(new ArrayList<>());
        }

        List<Task<User>> tasks = new ArrayList<>();

        for (String id : entrantIDs) {
            tasks.add(fetchUser(id));
        }

        return Tasks.whenAllSuccess(tasks).continueWith(task -> {
            List<Entrant> entrants = new ArrayList<>();
            if (task.isSuccessful() && task.getResult() != null) {
                for (Object result : task.getResult()) {
                    if (result instanceof Entrant) {
                        Entrant entrant = (Entrant) result;
                        if (entrant.getUserType().equals("Entrant")) {
                            entrants.add(entrant);
                        }
                    }
                }
            }
            return entrants;
        });
    }

    /**
     * Accepts an {@link Entrant} into an {@link Event} by calling
     * {@link Event#acceptAttendant(String)} and then {@link #updateEvent(Event)}.
     *
     * @param event   the {@link Event} whose status will change
     * @param entrant the {@link Entrant} to accept
     * @return a {@link Task} resolving to {@code true} if the update succeeds
     */
    public Task<Boolean> acceptEntrant(Event event, Entrant entrant) {
        event.acceptAttendant(entrant.getEmail());
        return updateEvent(event);
    }

    /**
     * Accepts an entrant (by email) into an {@link Event} by calling
     * {@link Event#acceptAttendant(String)} and then {@link #updateEvent(Event)}.
     *
     * @param event        the {@link Event} whose status will change
     * @param entrantEmail the email of the entrant to accept
     * @return a {@link Task} resolving to {@code true} if the update succeeds
     */
    public Task<Boolean> acceptEntrant(Event event, String entrantEmail) {
        event.acceptAttendant(entrantEmail);
        return updateEvent(event);
    }

    /**
     * Declines an {@link Entrant} from an {@link Event} by calling
     * {@link Event#declineAttendant(String)} and then {@link #updateEvent(Event)}.
     *
     * @param event   the {@link Event} to decline the entrant from
     * @param entrant the {@link Entrant} to decline
     * @return a {@link Task} resolving to {@code true} if the update succeeds
     */
    public Task<Boolean> declineEntrant(Event event, Entrant entrant) {
        event.declineAttendant(entrant.getEmail());
        return updateEvent(event);
    }

    /**
     * Declines an entrant (by email) from an {@link Event} by calling
     * {@link Event#declineAttendant(String)} and then {@link #updateEvent(Event)}.
     *
     * @param event        the {@link Event} to decline the entrant from
     * @param entrantEmail the email of the entrant to decline
     * @return a {@link Task} resolving to {@code true} if the update succeeds
     */
    public Task<Boolean> declineEntrant(Event event, String entrantEmail) {
        event.declineAttendant(entrantEmail);
        return updateEvent(event);
    }

    /**
     * Uploads an image to Firebase Storage, replaces any existing image of the same type
     * for the given user/event, and updates both the nested metadata document and the
     * top-level {@code images} collection with an {@link Image_datamap}.
     *
     * @param imageUri        the URI of the image file to upload
     * @param imageType       the logical type/category of the image (e.g., {@code "profile_pic"}, {@code "event_poster"})
     * @param description     a descriptive text associated with the image
     * @param associated_user the user ID/email to whom this image belongs
     * @param eventId         optional event ID if the image is associated with a specific event;
     *                        may be {@code null}
     * @return a {@link Task} that resolves to an {@link Image_datamap} containing the
     *         uploaded image’s metadata once all upload and Firestore writes complete
     * @see #deleteImage(String, String, String)
     * @see #updateImageMetadataInDocument(Image_datamap, String, String, String)
     */
    public Task<Image_datamap> uploadImage(@NonNull Uri imageUri, @NonNull String imageType, @NonNull String description, @NonNull String associated_user, @Nullable String eventId) {
        Task<Void> deleteOldImageTask = deleteOldImageIfExists(this, associated_user, eventId, imageType);

        return deleteOldImageTask.continueWithTask(deleteTask -> {
            if (!deleteTask.isSuccessful()) {
                Log.w("uploadImage", "Could not delete previous image, but proceeding with upload.", deleteTask.getException());
            }
            String newImageId = randomUUID().toString();
            String imagePath = imageType + "/" + newImageId + ".jpg";
            StorageReference storageRef = storage.getReference().child(imagePath);

            return storageRef.putFile(imageUri).continueWithTask(uploadTask -> {
                if (!uploadTask.isSuccessful()) {
                    throw uploadTask.getException();
                }
                return storageRef.getDownloadUrl();
            }).continueWithTask(uriTask -> {
                if (!uriTask.isSuccessful()) {
                    throw uriTask.getException();
                }
                String downloadUrl = uriTask.getResult().toString();
                Image_datamap metadata = new Image_datamap(newImageId, downloadUrl, imagePath, imageType, description, eventId, associated_user);

                Task<Void> updateNestedDocTask = updateImageMetadataInDocument(metadata, associated_user, eventId, imageType);
                Task<Void> createTopLevelDocTask = db.collection(IMAGES_COLLECTION).document(newImageId).set(metadata);

                return Tasks.whenAll(updateNestedDocTask, createTopLevelDocTask).continueWith(updateTask -> {
                    if (!updateTask.isSuccessful()) {
                        throw updateTask.getException();
                    }
                    return metadata;
                });
            });
        });
    }

    /**
     * Delete an image in Firebase Storage by its public download URL.
     *
     * @param imageUrl the Storage URL of the image to delete
     * @return a {@link Task} that completes when the image is deleted
     */
    public Task<Void> deleteImageByUrl(@NonNull String imageUrl) {
        StorageReference photoRef = storage.getReferenceFromUrl(imageUrl);
        return photoRef.delete();
    }

    /**
     * If applicable, deletes any previous image of the given type for the owner/event by
     * delegating to {@link #deleteImage(String, String, String)}.
     *
     * @param db       the {@link Database} instance
     * @param ownerId  the owner user ID/email
     * @param eventId  the event ID (for event posters), may be {@code null}
     * @param imageType the type of image (e.g., {@code "profile_pic"}, {@code "event_poster"})
     * @return a {@link Task} that completes when any old image is deleted
     */
    private Task<Void> deleteOldImageIfExists(Database db, String ownerId, @Nullable String eventId, String imageType) {
        if ("event_poster".equals(imageType) && eventId == null) {
            return Tasks.forResult(null);
        }
        return db.deleteImage(ownerId, eventId, imageType);
    }

    /**
     * Updates the metadata fields ({@code profilePicUrl}/{@code posterImageUrl} and {@code imageInfo})
     * for either a user document or a nested event document.
     *
     * @param metadata the {@link Image_datamap} to write, or {@code null} to clear
     * @param ownerId  the owner user ID/email
     * @param eventId  the event ID (for event posters), may be {@code null}
     * @param imageType the type of image (e.g., {@code "profile_pic"}, {@code "event_poster"})
     * @return a {@link Task} that completes when the Firestore update finishes
     */
    private Task<Void> updateImageMetadataInDocument(Image_datamap metadata, String ownerId, @Nullable String eventId, String imageType) {
        DocumentReference docRef;
        String urlField, infoField;

        if ("profile_pic".equals(imageType)) {
            docRef = db.collection(USERS_COLLECTION).document(ownerId);
            urlField = "profilePicUrl";
            infoField = "imageInfo";
        } else if ("event_poster".equals(imageType) && eventId != null) {
            docRef = db.collection(USERS_COLLECTION).document(ownerId).collection(EVENTS_SUBCOLLECTION).document(eventId);
            urlField = "posterImageUrl";
            infoField = "imageInfo";
        } else {
            return Tasks.forException(new IllegalArgumentException("Invalid image type or missing eventId for poster."));
        }

        Map<String, Object> updates = new HashMap<>();
        updates.put(urlField, metadata != null ? metadata.getUrl() : null);
        updates.put(infoField, metadata);

        return docRef.update(updates);
    }

    /**
     * Deletes an image associated with a user or a specific event, including its metadata
     * in Firestore and the actual file in Firebase Storage. The method determines the
     * correct document to inspect based on the image type and optional event ID.
     *
     * @param associatedUser the ID or email of the user who owns the image
     * @param eventId        the event ID if the image belongs to a specific event; may be {@code null}
     * @param imageType      the category of the image, such as {@code "profile_pic"} or {@code "event_poster"}
     * @return a {@link Task} that completes when all deletion operations (Firestore and Storage)
     *         have finished; resolves to {@code null} on success
     * @see #uploadImage(Uri, String, String, String, String)
     */
    public Task<Void> deleteImage(@NonNull String associatedUser, @Nullable String eventId, @NonNull String imageType) {
        DocumentReference docRef;
        if ("profile_pic".equals(imageType)) {
            docRef = db.collection(USERS_COLLECTION).document(associatedUser);
        } else if ("event_poster".equals(imageType) && eventId != null) {
            docRef = db.collection(USERS_COLLECTION).document(associatedUser).collection(EVENTS_SUBCOLLECTION).document(eventId);
        } else {
            return Tasks.forException(new IllegalArgumentException("Invalid image type or missing eventId."));
        }

        return docRef.get().continueWithTask(task -> {
            if (!task.isSuccessful() || !task.getResult().exists()) {
                Log.w("DeleteImage", "Document not found. Nothing to delete.");
                return Tasks.forResult(null);
            }
            Image_datamap metadata = task.getResult().get("imageInfo", Image_datamap.class);
            List<Task<Void>> deletionTasks = new ArrayList<>();

            if (metadata != null && metadata.getPath() != null && metadata.getId() != null) {
                try {
                    String imagePath = metadata.getPath();
                    String imageId = metadata.getId();
                    StorageReference storageRef = storage.getReference().child(imagePath);
                    deletionTasks.add(storageRef.delete());
                    deletionTasks.add(db.collection(IMAGES_COLLECTION).document(imageId).delete());

                } catch (IllegalArgumentException e) {
                    Log.w("DeleteImage", "Path in metadata was not a valid storage path: " + metadata.getPath(), e);
                }
            }

            if (deletionTasks.isEmpty()) {
                return Tasks.forResult(null);
            }
            return Tasks.whenAll(deletionTasks).onSuccessTask(aVoid -> {
                return updateImageMetadataInDocument(null, associatedUser, eventId, imageType);
            });
        });
    }

    /**
     * Retrieves the stored image metadata for a user or event, depending on the image type.
     *
     * @param ownerId   the ID/email of the user who owns the image
     * @param eventId   the event ID if the image belongs to a specific event; may be {@code null}
     * @param imageType the type of image to fetch (e.g., {@code "profile_pic"}, {@code "event_poster"})
     * @return a {@link Task} resolving to an {@link Image_datamap} if found,
     *         or {@code null} if no image metadata exists
     */
    public Task<Image_datamap> fetchImage(@NonNull String ownerId, @Nullable String eventId, @NonNull String imageType) {
        DocumentReference docRef;
        if ("profile_pic".equals(imageType)) {
            docRef = db.collection(USERS_COLLECTION).document(ownerId);
        } else if ("event_poster".equals(imageType) && eventId != null) {
            docRef = db.collection(USERS_COLLECTION).document(ownerId).collection(EVENTS_SUBCOLLECTION).document(eventId);
        } else {
            return Tasks.forException(new IllegalArgumentException("Invalid type or missing ID."));
        }

        return docRef.get().continueWith(task -> {
            if (task.isSuccessful() && task.getResult() != null && task.getResult().exists()) {
                return task.getResult().get("imageInfo", Image_datamap.class);
            }
            return null;
        });
    }

    /**
     * Updates the description field of an existing image’s metadata document.
     *
     * @param newDescription the new description text to write
     * @param ownerId        the ID/email of the user who owns the image
     * @param eventId        the event ID if the image belongs to a specific event; may be {@code null}
     * @param imageType      the type of the image to update
     * @return a {@link Task} that completes when the Firestore update finishes
     */
    public Task<Void> updateImage(@NonNull String newDescription, @NonNull String ownerId, @Nullable String eventId, @NonNull String imageType) {
        DocumentReference docRef;
        if ("profile_pic".equals(imageType)) {
            docRef = db.collection(USERS_COLLECTION).document(ownerId);
        } else if ("event_poster".equals(imageType) && eventId != null) {
            docRef = db.collection(USERS_COLLECTION).document(ownerId).collection(EVENTS_SUBCOLLECTION).document(eventId);
        } else {
            return Tasks.forException(new IllegalArgumentException("Invalid type or missing ID."));
        }
        return docRef.update("imageInfo.description", newDescription);
    }

    /**
     * Registers a realtime Firestore listener for image metadata changes on a user or event document.
     *
     * @param ownerId   the ID/email of the user who owns the image
     * @param eventId   the event ID if the image belongs to a specific event; may be {@code null}
     * @param imageType the type of image to listen for
     * @param listener  the callback invoked when metadata changes or errors occur
     * @return a {@link ListenerRegistration} used to stop listening to document updates
     */
    public ListenerRegistration listenToImageChanges(@NonNull String ownerId, @Nullable String eventId, @NonNull String imageType, @NonNull EventListener<Image_datamap> listener) {
        DocumentReference docRef;
        if ("profile_pic".equals(imageType)) {
            docRef = db.collection(USERS_COLLECTION).document(ownerId);
        } else if ("event_poster".equals(imageType) && eventId != null) {
            docRef = db.collection(USERS_COLLECTION).document(ownerId).collection(EVENTS_SUBCOLLECTION).document(eventId);
        } else {
            throw new IllegalArgumentException("Invalid type or missing ID for listener.");
        }

        return docRef.addSnapshotListener((snapshot, e) -> {
            if (e != null) {
                Log.w("ListenToImage", "Listen failed.", e);
                listener.onEvent(null, e);
                return;
            }

            if (snapshot != null && snapshot.exists()) {
                Image_datamap metadata = snapshot.get("imageInfo", Image_datamap.class);
                listener.onEvent(metadata, null);
            } else {
                listener.onEvent(null, null);
            }
        });
    }

    /**
     * Deletes an image file in Firebase Storage and its corresponding Firestore metadata document,
     * then clears the image metadata from the owner’s nested user/event document.
     *
     * @param metadata the {@link Image_datamap} describing the image to delete, including ID and storage path
     * @return a {@link Task} that completes once both the file and metadata have been deleted
     * @see #updateImageMetadataInDocument(Image_datamap, String, String, String)
     */
    public Task<Void> deleteImageFromMetadata(Image_datamap metadata) {
        if (metadata == null || metadata.getPath() == null || metadata.getId() == null) {
            return Tasks.forException(new IllegalArgumentException("Metadata is incomplete for deletion."));
        }

        String imagePath = metadata.getPath();
        String imageId = metadata.getId();
        String imageType = metadata.getType();
        String associatedUser = metadata.getAssociated_user();
        String eventId = metadata.getOwner();

        StorageReference storageRef = storage.getReference().child(imagePath);
        DocumentReference imageDocRef = db.collection(IMAGES_COLLECTION).document(imageId);

        List<Task<Void>> tasks = new ArrayList<>();
        tasks.add(storageRef.delete());
        tasks.add(imageDocRef.delete());

        return Tasks.whenAll(tasks).onSuccessTask(aVoid -> {
            return updateImageMetadataInDocument(null, associatedUser, eventId, imageType);
        });
    }

    /**
     * Marks a user as confirmed for an event lottery by moving them from
     * {@code selectedUserIds} to {@code confirmedUserIds} and removing them
     * from the other participation lists.
     *
     * @param eventId   the ID of the event the user was selected for
     * @param userEmail the email of the user accepting the lottery selection
     * @return a {@link Task} that completes when Firestore updates are applied
     * @see #declineLotterySelection(String, String)
     */
    public Task<Void> acceptLotterySelection(@NonNull String eventId, @NonNull String userEmail) {
        return findEventDocRefById(eventId)
                .onSuccessTask(ref -> ref.update(
                        "confirmedUserIds", FieldValue.arrayUnion(userEmail),
                        "selectedUserIds", FieldValue.arrayRemove(userEmail),
                        "waitlistUserIds", FieldValue.arrayRemove(userEmail),
                        "alternatesUserIds", FieldValue.arrayRemove(userEmail)
                ));
    }

    /**
     * Marks a user as declined for an event lottery by adding them to
     * {@code declinedUserIds} and removing them from all other participation lists.
     *
     * @param eventId   the ID of the event the user was selected for
     * @param userEmail the email of the user declining the lottery selection
     * @return a {@link Task} that completes when Firestore updates are applied
     * @see #acceptLotterySelection(String, String)
     */
    public Task<Void> declineLotterySelection(@NonNull String eventId, @NonNull String userEmail) {
        return findEventDocRefById(eventId)
                .onSuccessTask(ref -> ref.update(
                        "declinedUserIds", FieldValue.arrayUnion(userEmail),
                        "selectedUserIds", FieldValue.arrayRemove(userEmail),
                        "waitlistUserIds", FieldValue.arrayRemove(userEmail),
                        "alternatesUserIds", FieldValue.arrayRemove(userEmail)
                ));
    }

}
