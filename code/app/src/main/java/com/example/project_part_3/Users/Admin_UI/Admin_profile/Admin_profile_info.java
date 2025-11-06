package com.example.project_part_3.Users.Admin_UI.Admin_profile;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.example.project_part_3.Database_functions.UserDatabase;
import com.example.project_part_3.R;

public class Admin_profile_info extends Fragment {
    private UserDatabase userDb;
    private String name;
    private String email;
    private String phone;

    public Admin_profile_info() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.admin_profile_info, container, false);
    }
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        userDb = UserDatabase.getInstance();
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
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NavController navController = Navigation.findNavController(view);
                navController.navigate(R.id.action_admin_profile_info_to_admin_profile_view);
            }
        });

        Button deleteButton = view.findViewById(R.id.button);
        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                userDb = UserDatabase.getInstance();
                boolean output = userDb.removeUser(email, name);
                if (output == true){
                    Toast.makeText(getContext(), "User deleted: " + output, Toast.LENGTH_SHORT).show();
                }
                else{
                    Toast.makeText(getContext(), "User not deleted as is an instance of admin: " + output, Toast.LENGTH_SHORT).show();
                }

                NavController navController = Navigation.findNavController(view);
                navController.navigate(R.id.action_admin_profile_info_to_admin_profile_view);
            }
            });

    }
}
