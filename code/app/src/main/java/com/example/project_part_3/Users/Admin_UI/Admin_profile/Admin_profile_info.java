// File: com/example/project_part_3/Users/Admin_UI/Admin_profile/Admin_profile_info.java

package com.example.project_part_3.Users.Admin_UI.Admin_profile;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.example.project_part_3.Database_functions.UserDatabase;
import com.example.project_part_3.R;
import com.example.project_part_3.Users.User;

/**
 * The fragment that displays profile information about a user to an admin.
 * It also allows an admin to view and delete profiles of non-admin users.
 */
public class Admin_profile_info extends Fragment {

    private Admin_profile_model adminprofilemodel;
    private String userEmail;
    private User currentUser;

    private TextView nameTextView;
    private TextView emailTextView;
    private TextView phoneTextView;

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

        Button backButton = view.findViewById(R.id.button2);
        backButton.setOnClickListener(v -> Navigation.findNavController(view).navigateUp());

        Button deleteButton = view.findViewById(R.id.button);
        deleteButton.setOnClickListener(v -> handleDelete());

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
            phoneTextView.setText("Phone: " + currentUser.getPhone());
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
}
