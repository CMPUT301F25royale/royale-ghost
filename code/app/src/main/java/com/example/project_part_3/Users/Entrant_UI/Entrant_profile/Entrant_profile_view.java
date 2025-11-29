package com.example.project_part_3.Users.Entrant_UI.Entrant_profile;

import android.app.AlertDialog;
import android.content.Context;
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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.Fragment;

import com.example.project_part_3.R;

public class Entrant_profile_view extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.entrant_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        FirebaseFirestore ff = FirebaseFirestore.getInstance();
        Database db = new Database(ff);

        SharedPreferences prefs = requireContext()
                .getSharedPreferences("UserData", Context.MODE_PRIVATE);
        String username = prefs.getString("username", "");

        SwitchCompat notificationsSwitch = view.findViewById(R.id.entrant_notifications_switch);
        // local default = true if we don't have anything stored yet
        boolean localPref = prefs.getBoolean("receiveNotifications", true);
        notificationsSwitch.setChecked(localPref);
        notificationsSwitch.setEnabled(false); // disable until we finish syncing so we dont get that stupid toggle behavior

        if (username == null || username.isEmpty()) {
            notificationsSwitch.setEnabled(false);
        } else {
            ff.collection("users")
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



    Button passwordReset = view.findViewById(R.id.Pass_Reset);
        passwordReset.setOnClickListener(v -> {
            InputDialog((old,_new) -> {
                db.fetchUser(username).addOnSuccessListener(user -> {
                    if(user.getPassword().equals(old)){
                        user.setPassword(_new);
                        db.setUser(user);
                    }
                });
                SharedPreferences.Editor editor = prefs.edit();
                editor.putString("password", _new);
                editor.apply();

            });
        });

        //reset Name
        Button nameRest = view.findViewById(R.id.name_change);
        nameRest.setOnClickListener(v -> {
            InputDialog((old,_new) -> {
                //firebase change
                db.fetchUser(username).addOnSuccessListener(user -> {
                    if(user.getName().equals(old)){
                        user.setName(_new);
                        db.setUser(user);
                    }
                });
            });
        });

        //reset phone
        Button phone = view.findViewById(R.id.number_Change);
        phone.setOnClickListener(v -> {
            InputDialog((old,_new) -> {
                //firebase change
                db.fetchUser(username).addOnSuccessListener(user -> {
                    if(user.getPhone().equals(old)){
                        user.setPhone(_new);
                        db.setUser(user);
                    }
                });
            });
        });

        //reset email
        Button emailReset = view.findViewById(R.id.change_email);
        emailReset.setOnClickListener(v -> {
            InputDialog((old,_new) -> {
                db.fetchUser(username).addOnSuccessListener(user->{
                    String name = user.getName();
                    String password = user.getPassword();
                    String number = user.getPhone();
                    db.deleteUser(username);
                    Entrant new_user = new Entrant(name,password,_new,number);
                    db.addUser(new_user);
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putString("username", _new);
                    editor.apply();
                });
            });
        });

        //delete user
        Button delete = view.findViewById(R.id.pass_Reset);
        delete.setOnClickListener(v -> {
            db.deleteUser(username).addOnSuccessListener(user -> {});
        });

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

    private void InputDialog(InputDialogCallback callback){
        LayoutInflater inflator = LayoutInflater.from(requireContext());
        View dialogView = inflator.inflate(R.layout.profile_popup,null);

        EditText old = dialogView.findViewById(R.id.oldpass);
        EditText _new = dialogView.findViewById(R.id.newpass);

        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setTitle("Enter Old value and New Value")
                .setView(dialogView) // attach your custom layout
                .setPositiveButton("OK", (d, which) -> {
                    String oldtext = old.getText().toString().trim();
                    String newtext = _new.getText().toString().trim();
                    callback.onInputSubmitted(oldtext,newtext);
                })
                .setNegativeButton("Cancel", (d, which) -> d.dismiss())
                .create();

        dialog.show();
    }
}

