package com.example.project_part_3.Database_functions;

import androidx.annotation.NonNull;

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.firestore.WriteBatch;

import java.util.Collections;

public class NotificationMessagingService extends com.google.firebase.messaging.FirebaseMessagingService {

    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);
        saveTokenToUser(token);
    }

    public static void saveTokenToUser(@NonNull String token) {
        FirebaseUser authUser = com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser();
        if (authUser == null) return;

        String email = authUser.getEmail();
        if (email == null || email.isEmpty()) return;

        saveTokenForEmail(email, token);
    }

    /** Main method to call from login/signup code */
    public static void saveTokenForEmail(@NonNull String email, @NonNull String token) {
        FirebaseFirestore ff = FirebaseFirestore.getInstance();

        ff.collection("users")
                .whereArrayContains("fcmTokens", token)
                .get()
                .addOnSuccessListener(snapshot -> {
                    WriteBatch batch = ff.batch();

                    // remove token from any other user docs (could probably do this on logout but we dont really have logout)
                    for (DocumentSnapshot doc : snapshot) {
                        if (!email.equals(doc.getId())) {
                            batch.update(doc.getReference(),
                                    "fcmTokens",
                                    FieldValue.arrayRemove(token));
                        }
                    }

                    // ensure this user's doc exists and add token
                    batch.set(
                            ff.collection("users").document(email),
                            Collections.singletonMap("updatedAt", FieldValue.serverTimestamp()),
                            SetOptions.merge()
                    );

                    batch.update(
                            ff.collection("users").document(email),
                            "fcmTokens",
                            FieldValue.arrayUnion(token)
                    );

                    batch.commit();
                });
    }
}
