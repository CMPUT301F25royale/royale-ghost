package com.example.project_part_3.Users.Entrant_UI.Entrant_profile;

import android.app.AlertDialog;
import android.content.Context;
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

import com.bumptech.glide.Glide;
import com.example.project_part_3.Database_functions.Database;
import com.example.project_part_3.Users.Entrant;
import com.google.firebase.firestore.FirebaseFirestore;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.project_part_3.R;

public class Entrant_profile_view extends Fragment {
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
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        db = new Database(FirebaseFirestore.getInstance());

        SharedPreferences prefs = requireContext().getSharedPreferences("UserData", Context.MODE_PRIVATE);
        username = prefs.getString("username", "");

        profileImageView = view.findViewById(R.id.profile_photo);
        loadProfileImage();

        profileImageView.setOnClickListener(v -> showImagePopup());

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
        Button delete = view.findViewById(R.id.delete_user_button);
        delete.setOnClickListener(v -> {
            db.deleteUser(username).addOnSuccessListener(user -> {});
        });

    }

    private void loadProfileImage() {
        if (username != null && !username.isEmpty()) {
            db.fetchUser(username).addOnSuccessListener(user -> {
                if (user != null && user.getProfilePicUrl() != null) {
                    Glide.with(requireContext())
                            .load(user.getProfilePicUrl())
                            .placeholder(android.R.drawable.sym_def_app_icon)
                            .diskCacheStrategy(com.bumptech.glide.load.engine.DiskCacheStrategy.ALL)
                            .dontAnimate()
                            .into(profileImageView);
                }
            }).addOnFailureListener(e -> {
                Log.e("EntrantProfile", "Failed to load profile image.", e);
            });
        }
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

    private void showImagePopup() {
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

        dialog.show();
    }

    private final ActivityResultLauncher<String> galleryLauncher = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    ImageUri = uri;
                    uploadProfilePicture();
                }
            });
    private void uploadProfilePicture() {
        if (ImageUri != null) {
            db.uploadImage(ImageUri, "profile_pictures").addOnSuccessListener(downloadUrl -> {
                String imageUrl = downloadUrl.toString();
                db.fetchUser(username).addOnSuccessListener(user -> {
                    if (user != null) {
                        user.setProfilePicUrl(imageUrl);
                        db.setUser(user);
                        Glide.with(requireContext()).load(imageUrl).into(profileImageView);
                    }
                });
            }).addOnFailureListener(e -> {
            });
        }
    }

}

