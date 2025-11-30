package com.example.project_part_3.Users.Entrant_UI.Entrant_profile;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.Toast;

import com.example.project_part_3.Database_functions.Database;
import com.example.project_part_3.Users.Entrant;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.Fragment;

import com.example.project_part_3.R;
import com.example.project_part_3.Users.Organizer_UI.Organizer_profile.Organizer_profile_view;

public class Entrant_profile_view extends Organizer_profile_view {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.entrant_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        SwitchCompat notificationsSwitch = view.findViewById(R.id.entrant_notifications_switch);
        // local default = true if we don't have anything stored yet
        boolean localPref = prefs.getBoolean("receiveNotifications", true);
        notificationsSwitch.setChecked(localPref);
        notificationsSwitch.setEnabled(false); // disable until we finish syncing so we dont get that stupid toggle behavior


        // unhide the interests list
        TextView interestsTitle = view.findViewById(R.id.Interests);
        interestsTitle.setVisibility(View.VISIBLE);

        ListView interestsListView = view.findViewById(R.id.InterestsListView);
        interestsListView.setVisibility(View.VISIBLE);

        // ... get interests and do stuff


        // Notifications
        if (username == null || username.isEmpty()) {
            notificationsSwitch.setEnabled(false);
        } else {
            db.db.collection("users")
                    .document(username)
                    .get()
                    .addOnSuccessListener(doc -> {
                        Boolean remotePref = doc.getBoolean("receiveNotifications");
                        boolean remoteValue = (remotePref == null) ? true : remotePref;

                        // Only update UI & local if remote differs
                        if (remoteValue != localPref) {
                            notificationsSwitch.setChecked(remoteValue);

                            SharedPreferences.Editor editor = prefs.edit();
                            editor.putBoolean("receiveNotifications", remoteValue);
                            editor.apply();
                        }

                        // Attach listener after initial state is correct
                        attachNotificationListener(notificationsSwitch, ff, username, prefs);

                        notificationsSwitch.setEnabled(true);
                    })
                    .addOnFailureListener(e -> {
                        attachNotificationListener(notificationsSwitch, ff, username, prefs);
                        notificationsSwitch.setEnabled(true);
                    });
        }

    }

    private void attachNotificationListener(SwitchCompat notificationsSwitch,
                                            FirebaseFirestore ff,
                                            String username,
                                            SharedPreferences prefs) {
        notificationsSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            // Update Firestore
            ff.collection("users")
                    .document(username)
                    .update("receiveNotifications", isChecked)
                    .addOnSuccessListener(unused -> {
                        // Also update local SharedPreferences so next load is instant
                        SharedPreferences.Editor editor = prefs.edit();
                        editor.putBoolean("receiveNotifications", isChecked);
                        editor.apply();
                    })
                    .addOnFailureListener(e -> {
                        android.util.Log.e("EntrantProfile", "Failed to update receiveNotifications", e);
                        android.widget.Toast.makeText(getContext(),
                                "Failed to update notification setting",
                                android.widget.Toast.LENGTH_SHORT).show();
                        // revert UI if update failed
                        buttonView.setChecked(!isChecked);
                    });
        });
    }
}

