package com.example.project_part_3.Users.Entrant_UI.Entrant_profile;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Switch;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.project_part_3.Database_functions.Database;
import com.example.project_part_3.MainActivity;
import com.example.project_part_3.Users.Entrant;
import com.example.project_part_3.Users.Organizer;
import com.google.firebase.Firebase;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.Fragment;

import java.util.ArrayList;

import com.example.project_part_3.R;
import com.example.project_part_3.Users.Organizer_UI.Organizer_profile.Organizer_profile_view;

/**
 * Fragment responsible for displaying the currently logged-in entrant's profile.
 */
public class Entrant_profile_view extends Organizer_profile_view {

    ArrayList<String> interests = new ArrayList<>();
    ArrayAdapter<String> adapter;
    private Database db;
    private String username;
    private ImageView profileImageView;
    private Uri ImageUri;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.entrant_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        db = new Database(FirebaseFirestore.getInstance());
        SharedPreferences prefs = requireContext().getSharedPreferences("UserData", Context.MODE_PRIVATE);
        String updateInterestEmail = prefs.getString("username", "");
        username = prefs.getString("username", "");

        profileImageView = view.findViewById(R.id.profile_photo);
        loadProfileImage();

        profileImageView.setOnClickListener(v -> showImagePopup());

        // change text at top so that it displays the user's name
        TextView profileName = view.findViewById(R.id.Profile_Title);
        db.fetchUser(prefs.getString("username", "")).addOnSuccessListener(user -> {
            profileName.setText("Profile: " + user.getName());
        });

        // add interest
        ListView listOfInterests = view.findViewById(R.id.InterestsListView);
        adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_list_item_1, interests);
        listOfInterests.setAdapter(adapter);

        db.getInterests(updateInterestEmail)
                .addOnSuccessListener(list -> {
                    interests.clear();
                    interests.addAll(list);
                    adapter.notifyDataSetChanged();
                });

        Button addInterest = view.findViewById(R.id.Add_interest_button);
        addInterest.setOnClickListener(v -> {
            InterestDialog((input) -> {
                if (input == null || input.isEmpty()) return;
                String username = prefs.getString("username", "");
                db.addInterest(username, input)
                        .addOnSuccessListener(result -> {
                            Toast.makeText(getActivity(), "Interest added!", Toast.LENGTH_SHORT).show();
                            interests.add(input);
                            adapter.notifyDataSetChanged();
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(getActivity(), "Failed to add interest: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        });
            });
        });

        // reset password
        Button passwordReset = view.findViewById(R.id.Pass_Reset);
        passwordReset.setOnClickListener(v -> {
            InputDialog((old, _new) -> {
                String username = prefs.getString("username", "");
                db.fetchUser(username).addOnSuccessListener(user -> {
                    if (user.getPassword().equals(old)) {
                        user.setPassword(_new);
                        db.setUser(user);
                    } else {
                        Toast.makeText(getActivity(), "Incorrect info given!!", Toast.LENGTH_SHORT).show();
                    }
                });
                SharedPreferences.Editor editor = prefs.edit();
                editor.putString("password", _new);
                editor.apply();
            });
        });

        // reset Name
        Button nameRest = view.findViewById(R.id.name_change);
        nameRest.setOnClickListener(v -> {
            InputDialog((old, _new) -> {
                String username = prefs.getString("username", "");
                db.fetchUser(username).addOnSuccessListener(user -> {
                    if (user.getName().equals(old)) {
                        user.setName(_new);
                        db.setUser(user);
                        profileName.setText(_new);
                    } else {
                        Toast.makeText(getActivity(), "Incorrect info given!!", Toast.LENGTH_SHORT).show();
                    }
                });
            });
        });

        // reset phone
        Button phone = view.findViewById(R.id.number_Change);
        phone.setOnClickListener(v -> {
            InputDialog((old, _new) -> {
                String username = prefs.getString("username", "");
                db.fetchUser(username).addOnSuccessListener(user -> {
                    if (user.getPhone().equals(old)) {
                        user.setPhone(_new);
                        db.setUser(user);
                    } else {
                        Toast.makeText(getActivity(), "Incorrect info given!!", Toast.LENGTH_SHORT).show();
                    }
                });
            });
        });

        // reset email
        Button emailReset = view.findViewById(R.id.change_email);
        emailReset.setOnClickListener(v -> {
            InputDialog((old, _new) -> {
                String username = prefs.getString("username", "");
                db.fetchUser(username).addOnSuccessListener(user -> {
                    String name = user.getName();
                    String password = user.getPassword();
                    String number = user.getPhone();
                    db.deleteUser(username);
                    Organizer new_user = new Organizer(name, password, _new, number);
                    db.addUser(new_user);

                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putString("username", _new);
                    editor.apply();
                });
            });
        });

        // delete user
        Button delete = view.findViewById(R.id.delete_user_button);
        delete.setOnClickListener(v -> {
            String username = prefs.getString("username", "");
            db.fetchUser(username).addOnSuccessListener(user -> {
                db.deleteUser(username);

                SharedPreferences.Editor editor = prefs.edit();
                editor.clear();
                editor.apply();
                Toast.makeText(getActivity(), "Have a nice day!", Toast.LENGTH_LONG).show();

                Intent intent = new Intent(requireActivity(), MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);

                requireActivity().finish();
            });
        });

        SwitchCompat notificationsSwitch = view.findViewById(R.id.entrant_notifications_switch);
        boolean localPref = prefs.getBoolean("receiveNotifications", true);
        notificationsSwitch.setChecked(localPref);
        notificationsSwitch.setEnabled(false);

        TextView interestsTitle = view.findViewById(R.id.Interests);
        interestsTitle.setVisibility(View.VISIBLE);

        listOfInterests.setOnItemClickListener((parent, view1, position, id) -> {
            String username = prefs.getString("username", "");
            String choice = adapter.getItem(position);
            db.deleteInterest(username, choice)
                    .addOnSuccessListener(result -> {
                        Toast.makeText(getActivity(), "Interest deleted!", Toast.LENGTH_SHORT).show();
                        interests.remove(position);
                        adapter.notifyDataSetChanged();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(getActivity(), "Failed to delete interest: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        });


        listOfInterests.setVisibility(View.VISIBLE);

        if (username == null || username.isEmpty()) {
            notificationsSwitch.setEnabled(false);
        } else {
            FirebaseFirestore ff = FirebaseFirestore.getInstance();
            ff.collection("users")
                    .document(username)
                    .get()
                    .addOnSuccessListener(doc -> {
                        Boolean remotePref = doc.getBoolean("receiveNotifications");
                        boolean remoteValue = (remotePref == null) ? true : remotePref;

                        if (remoteValue != localPref) {
                            notificationsSwitch.setChecked(remoteValue);

                            SharedPreferences.Editor editor = prefs.edit();
                            editor.putBoolean("receiveNotifications", remoteValue);
                            editor.apply();
                        }

                        attachNotificationListener(notificationsSwitch, ff, username, prefs);
                        notificationsSwitch.setEnabled(true);
                    })
                    .addOnFailureListener(e -> {
                        attachNotificationListener(notificationsSwitch, ff, username, prefs);
                        notificationsSwitch.setEnabled(true);
                    });
        }
    }

    private void InputDialog(InputDialogCallback callback) {
        LayoutInflater inflator = LayoutInflater.from(requireContext());
        View dialogView = inflator.inflate(R.layout.profile_popup, null);

        EditText old = dialogView.findViewById(R.id.oldpass);
        EditText _new = dialogView.findViewById(R.id.newpass);

        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setTitle("Enter Old value and New Value")
                .setView(dialogView)
                .setPositiveButton("OK", (d, which) -> {
                    String oldtext = old.getText().toString().trim();
                    String newtext = _new.getText().toString().trim();
                    callback.onInputSubmitted(oldtext, newtext);
                })
                .setNegativeButton("Cancel", (d, which) -> d.dismiss())
                .create();

        dialog.show();
    }

    private void InterestDialog(InterestDialogCallback callback) {
        LayoutInflater inflator = LayoutInflater.from(requireContext());
        View dialogView = inflator.inflate(R.layout.interest_add, null);

        EditText interest = dialogView.findViewById(R.id.interest_add_text);

        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setTitle("Enter Interest")
                .setView(dialogView)
                .setPositiveButton("OK", (d, which) -> {
                    String input = interest.getText().toString().trim();
                    callback.onInputSubmitted(input);
                })
                .setNegativeButton("Cancel", (d, which) -> d.dismiss())
                .create();

        dialog.show();
    }

    protected void showImagePopup() {
        LayoutInflater inflater = LayoutInflater.from(requireContext());
        View dialogView = inflater.inflate(R.layout.image_popup, null);

        ImageView popupImagePreview = dialogView.findViewById(R.id.popup_image_preview);
        Button changeImageButton = dialogView.findViewById(R.id.popup_change_image_button);

        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setView(dialogView)
                .create();

        Glide.with(requireContext())
                .load(profileImageView.getDrawable())
                .into(popupImagePreview);

        changeImageButton.setOnClickListener(v -> {
            galleryLauncher.launch("image/*");
            dialog.dismiss();
        });
    }

    private void attachNotificationListener(SwitchCompat notificationsSwitch,
                                            FirebaseFirestore ff,
                                            String username,
                                            SharedPreferences prefs) {
        notificationsSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            ff.collection("users")
                    .document(username)
                    .update("receiveNotifications", isChecked)
                    .addOnSuccessListener(unused -> {
                        SharedPreferences.Editor editor = prefs.edit();
                        editor.putBoolean("receiveNotifications", isChecked);
                        editor.apply();
                    })
                    .addOnFailureListener(e -> {
                        android.util.Log.e("EntrantProfile", "Failed to update receiveNotifications", e);
                        Toast.makeText(getContext(),
                                "Failed to update notification setting",
                                Toast.LENGTH_SHORT).show();
                        buttonView.setChecked(!isChecked);
                    });
        });
    }
}
