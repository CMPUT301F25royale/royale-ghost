package com.example.project_part_3.Database_functions;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.messaging.FirebaseMessagingService;

import java.util.HashMap;
import java.util.Map;

public class NotificationMessagingService extends FirebaseMessagingService {

    private static final String TAG = "NotificationMessaging";

    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);
        Log.d(TAG, "onNewToken (no user bound yet): " + token);
    }

    public static void saveTokenForEmail(@NonNull String email, @NonNull String token) {
        FirebaseFirestore ff = FirebaseFirestore.getInstance();

        Map<String, Object> base = new HashMap<>();
        base.put("userID", email); // this is what Cloud Functions query
        base.put("updatedAt", FieldValue.serverTimestamp());

        ff.collection("users").document(email)
                .set(base, SetOptions.merge())
                .addOnSuccessListener(unused -> {
                    ff.collection("users").document(email)
                            .update("fcmTokens", FieldValue.arrayUnion(token))
                            .addOnSuccessListener(v ->
                                    Log.d(TAG, "Token saved for user " + email))
                            .addOnFailureListener(e ->
                                    Log.e(TAG, "Error updating fcmTokens", e));
                })
                .addOnFailureListener(e ->
                        Log.e(TAG, "Error saving base user doc", e));
    }
}
