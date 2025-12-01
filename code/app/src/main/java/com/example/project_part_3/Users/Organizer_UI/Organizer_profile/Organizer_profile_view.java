package com.example.project_part_3.Users.Organizer_UI.Organizer_profile;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.project_part_3.Database_functions.Database;
import com.example.project_part_3.MainActivity;
import com.example.project_part_3.Users.Organizer;
import com.google.android.material.chip.Chip;
import com.google.android.material.materialswitch.MaterialSwitch;
import com.google.firebase.firestore.FirebaseFirestore;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.project_part_3.R;

/**
 * Fragment that displays a profile for an organizer.
 */
public class Organizer_profile_view extends Fragment {
    protected Database db;
    protected String username;
    private ImageView OrganizerProfileImageView;
    private Button interests;
    private MaterialSwitch notification;
    private Uri ImageUri;
    protected SharedPreferences prefs;

    // Interface for the input dialog
    protected interface InputDialogCallback {
        void onInputSubmitted(String oldValue, String newValue);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.entrant_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        db = new Database(FirebaseFirestore.getInstance());


        //get password
        prefs = requireContext().getSharedPreferences("UserData", Context.MODE_PRIVATE);
        username = prefs.getString("username", "");

        OrganizerProfileImageView = view.findViewById(R.id.profile_photo);
        loadProfileImage();
        OrganizerProfileImageView.setOnClickListener(v -> showImagePopup());

        // change text at top so that it displays the user's name
        TextView profileName = view.findViewById(R.id.Profile_Title);
        db.fetchUser(prefs.getString("username", "")).addOnSuccessListener(user -> {
            profileName.setText("Profile: " + user.getName());
        });

        //reset password here
        setupButtons(view, profileName);

    }

    
    /**
     * Sets up all standard profile buttons.
     */
    protected void setupButtons(View view, TextView profileNameLabel) {
        interests = view.findViewById(R.id.Add_interest_button);
        //interests.setVisibility(View.GONE);
        notification = view.findViewById(R.id.entrant_notifications_switch);
        //notification.setVisibility(View.GONE);
        // Reset Password
        Button passwordReset = view.findViewById(R.id.Pass_Reset);
        if (passwordReset != null) {
            passwordReset.setOnClickListener(v -> {
                InputDialog((old, _new) -> {
                    db.fetchUser(username).addOnSuccessListener(user -> {
                        if (user.getPassword().equals(old)) {
                            user.setPassword(_new);
                            db.setUser(user);
                            SharedPreferences.Editor editor = prefs.edit();
                            editor.putString("password", _new);
                            editor.apply();
                            Toast.makeText(getActivity(), "Password updated", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(getActivity(), "Incorrect old password", Toast.LENGTH_SHORT).show();
                        }
                    });
                });
            });
        }

        // Change Name
        Button nameReset = view.findViewById(R.id.name_change);
        if (nameReset != null) {
            nameReset.setOnClickListener(v -> {
                InputDialog((old, _new) -> {
                    db.fetchUser(username).addOnSuccessListener(user -> {
                        if (user.getName().equals(old)) {
                            user.setName(_new);
                            db.setUser(user);
                            profileNameLabel.setText("Profile: " + _new);
                            Toast.makeText(getActivity(), "Name updated", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(getActivity(), "Incorrect old name", Toast.LENGTH_SHORT).show();
                        }
                    });
                });
            });
        }

        // Change Phone
        Button phone = view.findViewById(R.id.number_Change);
        if (phone != null) {
            phone.setOnClickListener(v -> {
                InputDialog((old, _new) -> {
                    db.fetchUser(username).addOnSuccessListener(user -> {
                        if (user.getPhone().equals(old)) {
                            user.setPhone(_new);
                            db.setUser(user);
                            Toast.makeText(getActivity(), "Phone updated", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(getActivity(), "Incorrect old number", Toast.LENGTH_SHORT).show();
                        }
                    });
                });
            });
        }

        // Change Email
        Button emailReset = view.findViewById(R.id.change_email);
        if (emailReset != null) {
            emailReset.setOnClickListener(v -> {
                InputDialog((old, _new) -> {
                    db.fetchUser(username).addOnSuccessListener(user -> {
                        // In a real app, you might want to re-authenticate before deleting/re-creating
                        String name = user.getName();
                        String password = user.getPassword();
                        String number = user.getPhone();

                        // Delete old document and create new one
                        db.deleteUser(username);
                        Organizer new_user = new Organizer(name, password, _new, number);
                        // Copy profile pic URL if needed
                        if (user.getProfilePicUrl() != null) new_user.setProfilePicUrl(user.getProfilePicUrl());

                        db.addUser(new_user);

                        // Update local tracking
                        username = _new;
                        SharedPreferences.Editor editor = prefs.edit();
                        editor.putString("username", _new);
                        editor.apply();
                        Toast.makeText(getActivity(), "Email updated", Toast.LENGTH_SHORT).show();
                    });
                });
            });
        }

        // Delete Account
        Button delete = view.findViewById(R.id.delete_user_button);
        if (delete != null) {
            delete.setOnClickListener(v -> {
                db.deleteUser(username);
                logoutUser();
                Toast.makeText(getActivity(), "Account deleted. Have a nice day!", Toast.LENGTH_LONG).show();
            });
        }

        // Logout
        Button logout = view.findViewById(R.id.entrant_logout_button);
        if (logout != null) {
            logout.setOnClickListener(v -> logoutUser());
        }
    }

    protected void logoutUser() {
        SharedPreferences.Editor editor = prefs.edit();
        editor.clear();
        editor.apply();

        Intent intent = new Intent(requireActivity(), MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        requireActivity().finish();
    }

        
    /**    
    * Loads the profile image for the organizer.
    */
    protected void loadProfileImage() {
        
        if (username != null && !username.isEmpty()) {
            db.fetchUser(username).addOnSuccessListener(user -> {
                if (user != null && user.getProfilePicUrl() != null) {
                    Glide.with(requireContext())
                            .load(user.getProfilePicUrl())
                            .placeholder(android.R.drawable.sym_def_app_icon)
                            .diskCacheStrategy(com.bumptech.glide.load.engine.DiskCacheStrategy.ALL)
                            .dontAnimate()
                            .into(OrganizerProfileImageView);
                }
            }).addOnFailureListener(e -> {
                Log.e("Profile", "Failed to load profile image.", e);
            });
        }
    }

    /**
     * Displays a custom input dialog with two EditText fields for entering
     * an old value and a new value.
     *
     * @param callback Callback interface for handling input submission.
     */
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

    /**
     * Function to display an image popup.
     */
    protected void showImagePopup() {
        LayoutInflater inflater = LayoutInflater.from(requireContext());
        View dialogView = inflater.inflate(R.layout.image_popup, null);

        ImageView popupImagePreview = dialogView.findViewById(R.id.popup_image_preview);
        Button changeImageButton = dialogView.findViewById(R.id.popup_change_image_button);

        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setView(dialogView)
                .create();

        Glide.with(requireContext())
                .load(OrganizerProfileImageView.getDrawable())
                .into(popupImagePreview);

        changeImageButton.setOnClickListener(v -> {
            galleryLauncher.launch("image/*");
            dialog.dismiss();
        });

        dialog.show();
    }

    /**
     * ActivityResultLauncher used to open the device's gallery and allow
     * the user to pick an image.
     */
    protected final ActivityResultLauncher<String> galleryLauncher = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    ImageUri = uri;
                    uploadProfilePicture();
                }
            });

    /**
     * Uploads the selected image to Firebase Storage and updates the organizer's profile picture.
     */
    protected void uploadProfilePicture() {
        String desc = "profile picture" + username;
        if (ImageUri != null) {
            db.uploadImage(ImageUri,"profile_pic", desc, username, null).addOnSuccessListener(ImageMetadata -> {
                if(getContext() == null) return;
                db.fetchUser(username).addOnSuccessListener(user -> {
                    if (user != null) {
                        String imageUrl = ImageMetadata.getUrl();
                        user.setImageInfo(ImageMetadata);
                        user.setProfilePicUrl(imageUrl);

                        db.setUser(user);
                        Glide.with(requireContext()).load(imageUrl).into(OrganizerProfileImageView);
                    }
                }).addOnFailureListener(e -> {
                    Log.e("EntrantProfile", "Failed to load profile image.", e);
                });
            }).addOnFailureListener(e -> {
                Log.e("EntrantProfile", "Failed to upload profile image.", e);
            });
        }
    }
}