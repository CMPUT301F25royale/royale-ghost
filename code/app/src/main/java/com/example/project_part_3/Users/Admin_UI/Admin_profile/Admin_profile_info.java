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
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.example.project_part_3.Database_functions.Database;
import com.example.project_part_3.R;
import com.google.firebase.firestore.FirebaseFirestore;

public class Admin_profile_info extends Fragment {
    private Database db;
    private String name;
    private String email;
    private String phone;

    public Admin_profile_info() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        db = new Database(FirebaseFirestore.getInstance());
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.admin_profile_info, container, false);
    }
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        TextView nameTextView = view.findViewById(R.id.profile_info_name);
        TextView emailTextView = view.findViewById(R.id.profile_info_email);
        TextView phoneTextView = view.findViewById(R.id.profile_info_phone);

        if(getArguments() != null){
            name = getArguments().getString("name");
            email = getArguments().getString("email");
            phone = getArguments().getString("phone");

            nameTextView.setText("Name: " + name);
            emailTextView.setText("Email: " + email);
            phoneTextView.setText("Phone: " + phone);
        }

        Button backButton = view.findViewById(R.id.button2);
        backButton.setOnClickListener(v -> {
            NavController navController = Navigation.findNavController(view);
            navController.navigateUp();
        });

        Button deleteButton = view.findViewById(R.id.button);
        deleteButton.setOnClickListener(v -> {
            if (email == null || email.isEmpty()) {
                Toast.makeText(getContext(), "Cannot delete user: email is missing.", Toast.LENGTH_SHORT).show();
                return;
            }

            db.deleteUser(email).addOnSuccessListener(success -> {
                if (success) {
                    Toast.makeText(getContext(), "User deleted successfully.", Toast.LENGTH_SHORT).show();
                    NavController navController = Navigation.findNavController(view);
                    navController.navigate(R.id.action_admin_profile_info_to_admin_profile_view);
                }
            }).addOnFailureListener(e -> {
                Log.e("Admin_profile_info", "Failed to delete user", e);
                Toast.makeText(getContext(), "Failed to delete user.", Toast.LENGTH_SHORT).show();
            });
        });
    }
}
