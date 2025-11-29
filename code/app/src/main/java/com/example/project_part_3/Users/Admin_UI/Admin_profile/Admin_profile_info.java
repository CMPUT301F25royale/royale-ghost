// File: com/example/project_part_3/Users/Admin_UI/Admin_profile/Admin_profile_info.java

package com.example.project_part_3.Users.Admin_UI.Admin_profile;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.bumptech.glide.Glide;
import com.example.project_part_3.Database_functions.Database;
import com.example.project_part_3.Database_functions.UserDatabase;
import com.example.project_part_3.R;
import com.example.project_part_3.Users.User;
import com.google.firebase.Firebase;
import com.google.firebase.firestore.FirebaseFirestore;

public class Admin_profile_info extends Fragment {

    private Admin_profile_model adminprofilemodel;
    private String userEmail;
    private User currentUser;

    private TextView nameTextView;
    private TextView emailTextView;
    private TextView phoneTextView;
    private ImageView profilePhoto;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        adminprofilemodel = new ViewModelProvider(requireActivity()).get(Admin_profile_model.class);

        if (getArguments() != null) {
            userEmail = getArguments().getString("email");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.admin_profile_info, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        nameTextView = view.findViewById(R.id.profile_info_name);
        emailTextView = view.findViewById(R.id.profile_info_email);
        phoneTextView = view.findViewById(R.id.profile_info_phone);
        profilePhoto = view.findViewById(R.id.profile_info_image);

        Button backButton = view.findViewById(R.id.button2);
        backButton.setOnClickListener(v -> Navigation.findNavController(view).navigateUp());

        Button deleteButton = view.findViewById(R.id.button);
        deleteButton.setOnClickListener(v -> handleDelete());

        Button deletePhotoButton = view.findViewById(R.id.delete_photo_button);
        deletePhotoButton.setOnClickListener(v -> handleDeletePhoto());

        adminprofilemodel.getUserProfiles().observe(getViewLifecycleOwner(), users -> {
            if (users == null || userEmail == null) return;
            for (User user : users) {
                if (userEmail.equals(user.getEmail())) {
                    currentUser = user;
                    updateUI();
                    break;
                }
            }
        });
    }

    private void updateUI() {
        if (currentUser != null) {
            nameTextView.setText("Name: " + currentUser.getName());
            emailTextView.setText("Email: " + currentUser.getEmail());
            if (currentUser.getPhone() == null || currentUser.getPhone().isEmpty()) {
                phoneTextView.setText("Phone: Not provided"); // Placeholder text
            } else {
                phoneTextView.setText("Phone: " + currentUser.getPhone());
            }
            if (currentUser.getProfilePicUrl() != null && currentUser.getImageInfo().getUrl() != null) {
                Glide.with(getContext())
                        .load(currentUser.getImageInfo().getUrl())
                        .placeholder(R.drawable.ic_person) // Placeholder while loading
                        .error(R.drawable.ic_person)       // Placeholder on error
                        .into(profilePhoto);
            } else {
                Glide.with(getContext())
                        .load(R.drawable.ic_person)
                        .into(profilePhoto);
                profilePhoto.setImageResource(R.drawable.ic_person);
            }
        }
    }

    private void handleDelete() {
        if (currentUser == null) {
            Toast.makeText(getContext(), "Cannot delete user: data is missing.", Toast.LENGTH_SHORT).show();
            return;
        }

        if ("Admin".equals(currentUser.getUserType())) {
            Toast.makeText(getContext(), "Cannot delete an Admin user.", Toast.LENGTH_SHORT).show();
            return;
        }
        adminprofilemodel.deleteUser(currentUser.getEmail(), new UserDatabase.OnUserDeleteListener() {
            @Override
            public void onSuccess() {
                Toast.makeText(getContext(), "User deleted successfully.", Toast.LENGTH_SHORT).show();
                if (getView() != null) {
                    Navigation.findNavController(getView()).navigateUp();
                }
            }

            @Override
            public void onFailure(String errorMessage) {
                Log.e("Admin_profile_info", "Failed to delete user: " + errorMessage);
                Toast.makeText(getContext(), "Failed to delete user.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void handleDeletePhoto() {
        if (currentUser == null || currentUser.getImageInfo() == null || currentUser.getImageInfo().getUrl() == null) {
            Toast.makeText(getContext(), "Cannot delete photo: data is missing.", Toast.LENGTH_SHORT).show();
            return;
        }

        Database db = new Database(FirebaseFirestore.getInstance());
        db.deleteImage(currentUser.getEmail(), null, "profile_pic").addOnSuccessListener(aVoid -> {
            currentUser.setImageInfo(null);
            currentUser.setProfilePicUrl(null);
            Toast.makeText(getContext(), "Profile photo deleted successfully.", Toast.LENGTH_SHORT).show();
            profilePhoto.setImageResource(R.drawable.ic_person); // set back to default
        }).addOnFailureListener(e -> {
            Log.e("Admin_profile_info", "Failed to delete profile photo: " + e.getMessage());
            Toast.makeText(getContext(), "Failed to delete profile photo.", Toast.LENGTH_SHORT).show();
        });
    }
}