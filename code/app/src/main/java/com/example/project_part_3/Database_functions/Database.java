package com.example.project_part_3.Database_functions;
import static java.util.UUID.randomUUID;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.example.project_part_3.Events.Event;
import com.example.project_part_3.Image.ImageMetadata;
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
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    public Task<Boolean> deleteInterest (String email, String oldInterest) {
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
        event.setLotteryDone(false);
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
    public Task<Void> deleteUser(String email) {
        DocumentReference userDocRef = db.collection(USERS_COLLECTION).document(email);
        return userDocRef.get().continueWithTask(userTask -> {
            if (!userTask.isSuccessful() || !userTask.getResult().exists()) {
                return Tasks.forResult(null);
            }
            WriteBatch batch = db.batch();
            batch.delete(userDocRef);
            ImageMetadata profilePicMeta = userTask.getResult().get("imageInfo", ImageMetadata.class);
            if (profilePicMeta != null && profilePicMeta.getUrl() != null) {
                try {
                    storage.getReferenceFromUrl(profilePicMeta.getUrl()).delete();
                } catch (Exception e) {
                    Log.w("DeleteUser", "Could not delete profile pic from storage.", e);
                }
            }
            User user = userTask.getResult().toObject(User.class);
            if (user != null && "Organizer".equals(user.getUserType())) {
                return getEventsByOrganizer(email).continueWithTask(eventsTask -> {
                    if (eventsTask.isSuccessful()) {
                        for (Event event : eventsTask.getResult()) {
                            DocumentReference eventRef = db.collection(USERS_COLLECTION)
                                    .document(email)
                                    .collection(EVENTS_SUBCOLLECTION)
                                    .document(event.getId());
                            batch.delete(eventRef);
                            if (event.getImageInfo() != null && event.getImageInfo().getUrl() != null) {
                                try {
                                    storage.getReferenceFromUrl(event.getImageInfo().getUrl()).delete();
                                } catch (Exception e) {
                                    Log.w("DeleteUser", "Could not delete event poster from storage.", e);
                                }
                            }
                        }
                    }
                    return batch.commit();
                });
            }
            return batch.commit();
        });
    }
    /** Delete an event from the database.
     *
     * @param event The event to delete.
     * @return A task that completes when the event is deleted.
     */
    public Task<Boolean> deleteEvent(Event event) {
        String eventId = event.getId();
        String organizerId = event.getOrganizerId();

        if (eventId == null || organizerId == null) {            return Tasks.forResult(false);
        }

        return deleteImage(organizerId, eventId, "event_poster").continueWithTask(imageDeleteTask -> {
            if (!imageDeleteTask.isSuccessful()) {
                Log.e("DeleteEvent", "Failed to delete event poster, but proceeding with event deletion.", imageDeleteTask.getException());
            }

            DocumentReference eventDocRef = db.collection(USERS_COLLECTION)
                    .document(organizerId)
                    .collection(EVENTS_SUBCOLLECTION)
                    .document(eventId);
            return eventDocRef.delete().continueWith(Task::isSuccessful);
        });
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
                    Object waitlistObj = task.getResult().get("waitlistUserIds");
                    if (waitlistObj instanceof List) {
                        List<?> waitlist = (List<?>) waitlistObj;
                        return waitlist.contains(userEmail);
                    }
                    return false;
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
                        Object eventsObj = snap.get("eventsAppliedFor");
                        if(eventsObj instanceof List) {
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
     * Adds an event to a user
     *
     * @param userId The ID of the user to add the event to.
     * @param eventId The ID of the event to add to the user.
     * @return A task that completes when the event is added to the user.
     */
    public Task<Void> addEventToUser(@NonNull String userId, @NonNull String eventId) {
        return db.collection(USERS_COLLECTION).document(userId)
                .update("eventsAppliedFor", FieldValue.arrayUnion(eventId));
    }

    /**
     * Remove an event from a user.
     *
     * @param userId The ID of the user to remove the event from.
     * @param eventId The ID of the event to remove from the user.
     * @return A task that completes when the event is removed from the user.
     */
    public Task<Void> removeEventFromUser(@NonNull String userId, @NonNull String eventId) {
        return db.collection(USERS_COLLECTION).document(userId).update("eventsAppliedFor", FieldValue.arrayRemove(eventId));
    }

    /**
     * Get a list of all entrants who have accepted an invitation to a given event.
     *
     * @param event The event to get entrants for.
     * @return A task that contains a list of entrants.
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
     * Get a list of all entrants who are on the waitlist for an event.
     *
     * @param event The event to get entrants for.
     * @return A task that contains a list of entrants.
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
     * Decline an entrant from an event.
     *
     * @param event The event to decline the entrant from.
     * @param entrant The entrant to decline.
     * @return A task that completes when the entrant is declined.
     */
    public Task<Boolean> declineEntrant(Event event, Entrant entrant) {
        event.declineAttendant(entrant.getEmail());
        return updateEvent(event);
    }

    //TODO: Change Params 

    /**
     * Upload an image to Firebase Storage.
     *
     * @param imageUri The URI of the image to upload.
     * @param folderPath The path to the folder to upload the image to.
     * @return A task that completes when the image is uploaded.
     */
    public Task<ImageMetadata> uploadImage(@NonNull Uri imageUri, @NonNull String imageType, @NonNull String description, @NonNull String ownerId, @Nullable String eventId) {

        return deleteImage(ownerId, eventId, imageType).continueWithTask(deleteTask -> {
            if (!deleteTask.isSuccessful()) {
                Log.e("UploadImage", "Failed to delete previous image, but proceeding with upload.", deleteTask.getException());
            }

            String imagePath = imageType + "/" + randomUUID().toString() + ".jpg";
            StorageReference storageRef = storage.getReference().child(imagePath);
            UploadTask uploadTask = storageRef.putFile(imageUri);

            return uploadTask.continueWithTask(task -> {
                if (!task.isSuccessful()) {
                    throw task.getException();
                }
                return storageRef.getDownloadUrl();
            }).continueWithTask(uriTask -> {
                if (!uriTask.isSuccessful()) {
                    throw uriTask.getException();
                }
                String downloadUrl = uriTask.getResult().toString();
                ImageMetadata metadata = new ImageMetadata(downloadUrl, imageType, description, ownerId);

                return updateImageMetadataInDocument(metadata, ownerId, eventId, imageType)
                        .continueWith(updateTask -> {
                            if (!updateTask.isSuccessful()) {
                                throw updateTask.getException();
                            }
                            return metadata;
                        });
            });
        });
    }

    /**
     * Delete an image from Firebase Storage.
     *
     * @param imageUrl The URL of the image to delete.
     * @return A task that completes when the image is deleted.
     */
    public Task<Void> deleteImageByUrl(@NonNull String imageUrl) {
        StorageReference photoRef = storage.getReferenceFromUrl(imageUrl);
        return photoRef.delete();
    private Task<Void> updateImageMetadataInDocument(ImageMetadata metadata, String ownerId, @Nullable String eventId, String imageType) {
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

    public Task<Void> deleteImage(@NonNull String ownerId, @Nullable String eventId, @NonNull String imageType) {
        DocumentReference docRef;
        if ("profile_pic".equals(imageType)) {
            docRef = db.collection(USERS_COLLECTION).document(ownerId);
        } else if ("event_poster".equals(imageType) && eventId != null) {
            docRef = db.collection(USERS_COLLECTION).document(ownerId).collection(EVENTS_SUBCOLLECTION).document(eventId);
        } else {
            return Tasks.forException(new IllegalArgumentException("Invalid image type or missing eventId."));
        }

        return docRef.get().continueWithTask(task -> {
            if (!task.isSuccessful() || !task.getResult().exists()) {
                Log.w("DeleteImage", "Document not found. Nothing to delete.");
                return Tasks.forResult(null);
            }

            ImageMetadata metadata = task.getResult().get("imageInfo", ImageMetadata.class);
            Task<Void> storageDeleteTask = Tasks.forResult(null);

            if (metadata != null && metadata.getUrl() != null && !metadata.getUrl().isEmpty()) {
                try {
                    StorageReference storageRef = storage.getReferenceFromUrl(metadata.getUrl());
                    storageDeleteTask = storageRef.delete();
                } catch (IllegalArgumentException e) {
                    Log.w("DeleteImage", "URL in metadata was not a valid storage URL: " + metadata.getUrl());
                }
            }

            Task<Void> firestoreUpdateTask = updateImageMetadataInDocument(null, ownerId, eventId, imageType);

            return Tasks.whenAll(storageDeleteTask, firestoreUpdateTask);
        });
    }

    public Task<ImageMetadata> fetchImage(@NonNull String ownerId, @Nullable String eventId, @NonNull String imageType) {
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
                return task.getResult().get("imageInfo", ImageMetadata.class);
            }
            return null;
        });
    }

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

    public ListenerRegistration listenToImageChanges(@NonNull String ownerId, @Nullable String eventId, @NonNull String imageType, @NonNull EventListener<ImageMetadata> listener) {
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
                ImageMetadata metadata = snapshot.get("imageInfo", ImageMetadata.class);
                listener.onEvent(metadata, null);
            } else {
                listener.onEvent(null, null);
            }
        });
    }
}