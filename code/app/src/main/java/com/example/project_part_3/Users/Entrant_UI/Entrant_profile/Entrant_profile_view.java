package com.example.project_part_3.Users.Entrant_UI.Entrant_profile;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SwitchCompat;

import com.example.project_part_3.MainActivity;
import com.example.project_part_3.R;
import com.example.project_part_3.Users.Organizer_UI.Organizer_profile.Organizer_profile_view;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

public class Entrant_profile_view extends Organizer_profile_view {

    private ArrayList<String> interests = new ArrayList<>();
    private ArrayAdapter<String> adapter;

    private interface InterestDialogCallback {
        void onInputSubmitted(String input);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.entrant_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Now add Entrant-specific logic (Interests & Notification Switch)
        setupEntrantSpecifics(view);
    }

    private void setupEntrantSpecifics(View view) {
        SwitchCompat notificationsSwitch = view.findViewById(R.id.entrant_notifications_switch);
        boolean localPref = prefs.getBoolean("receiveNotifications", true);

        if (notificationsSwitch != null) {
            notificationsSwitch.setChecked(localPref);
            FirebaseFirestore ff = FirebaseFirestore.getInstance();
            ff.collection("users").document(username).get()
                    .addOnSuccessListener(doc -> {
                        if (doc.exists()) {
                            Boolean remotePref = doc.getBoolean("receiveNotifications");
                            boolean remoteValue = (remotePref == null) ? true : remotePref;
                            if (remoteValue != localPref) {
                                notificationsSwitch.setChecked(remoteValue);
                                prefs.edit().putBoolean("receiveNotifications", remoteValue).apply();
                            }
                        }
                        attachNotificationListener(notificationsSwitch, ff);
                    })
                    .addOnFailureListener(e -> attachNotificationListener(notificationsSwitch, ff));
        }

        ListView listOfInterests = view.findViewById(R.id.InterestsListView);
        TextView interestsTitle = view.findViewById(R.id.Interests);
        Button addInterest = view.findViewById(R.id.Add_interest_button);

        if (listOfInterests != null) {
            if (interestsTitle != null) interestsTitle.setVisibility(View.VISIBLE);

            adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_list_item_1, interests);
            listOfInterests.setAdapter(adapter);

            // Load Interests
            db.getInterests(username).addOnSuccessListener(list -> {
                interests.clear();
                interests.addAll(list);
                adapter.notifyDataSetChanged();
            });

            // Delete Interest on Click
            listOfInterests.setOnItemClickListener((parent, view1, position, id) -> {
                String choice = adapter.getItem(position);
                db.deleteInterest(username, choice).addOnSuccessListener(result -> {
                    Toast.makeText(getActivity(), "Interest deleted!", Toast.LENGTH_SHORT).show();
                    interests.remove(position);
                    adapter.notifyDataSetChanged();
                });
            });
        }

        // Add Interest Button
        if (addInterest != null) {
            addInterest.setOnClickListener(v -> {
                InterestDialog((input) -> {
                    if (input == null || input.isEmpty()) return;
                    db.addInterest(username, input).addOnSuccessListener(result -> {
                        Toast.makeText(getActivity(), "Interest added!", Toast.LENGTH_SHORT).show();
                        interests.add(input);
                        adapter.notifyDataSetChanged();
                    });
                });
            });
        }
    }

    private void attachNotificationListener(SwitchCompat notificationsSwitch, FirebaseFirestore ff) {
        notificationsSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            ff.collection("users")
                    .document(username)
                    .update("receiveNotifications", isChecked)
                    .addOnSuccessListener(unused -> {
                        prefs.edit().putBoolean("receiveNotifications", isChecked).apply();
                    })
                    .addOnFailureListener(e -> {
                        Log.e("EntrantProfile", "Failed to update receiveNotifications", e);
                        Toast.makeText(getContext(), "Failed to update setting", Toast.LENGTH_SHORT).show();
                        // Revert switch visually without triggering listener again (simplified)
                        buttonView.setChecked(!isChecked);
                    });
        });
    }

    private void InterestDialog(InterestDialogCallback callback) {
        LayoutInflater inflator = LayoutInflater.from(requireContext());
        View dialogView = inflator.inflate(R.layout.interest_add, null);
        EditText interest = dialogView.findViewById(R.id.interest_add_text);

        new AlertDialog.Builder(requireContext())
                .setTitle("Enter Interest")
                .setView(dialogView)
                .setPositiveButton("OK", (d, which) -> {
                    String input = interest.getText().toString().trim();
                    callback.onInputSubmitted(input);
                })
                .setNegativeButton("Cancel", (d, which) -> d.dismiss())
                .show();
    }
}