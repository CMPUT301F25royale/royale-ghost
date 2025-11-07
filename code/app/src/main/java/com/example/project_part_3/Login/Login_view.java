package com.example.project_part_3.Login;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.project_part_3.Database_functions.Database;
import com.example.project_part_3.Login.Login_model;
import com.example.project_part_3.R;
import com.google.firebase.firestore.FirebaseFirestore;

public class Login_view extends Fragment {
    private String userType;
    private TextView name;
    private TextView password;
    private Button submit;
    private Login_model login_model;

    public Login_view() {}

    public static Login_view newInstance(String param1, String param2) {
        Login_view fragment = new Login_view();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.login, container, false);
    }
    public void onViewCreated(View view, Bundle savedInstanceState){
        super.onViewCreated(view, savedInstanceState);
        name = view.findViewById(R.id.name_login_edit_text);
        password = view.findViewById(R.id.password_login_edit_text);
        submit = view.findViewById(R.id.Login_submit);
        Database db = new Database(FirebaseFirestore.getInstance());

        submit.setOnClickListener(v -> {
            String nameText = name.getText().toString();
            String passwordText = password.getText().toString();
            if (nameText.isEmpty() || passwordText.isEmpty()) {
                Toast.makeText(getActivity(), "Please fill in all fields", Toast.LENGTH_SHORT).show();
                return;
            }
            db.checkUser(nameText, passwordText).addOnSuccessListener(user -> {
                    if (user != null) {
                        Toast.makeText(getActivity(), "Login successful", Toast.LENGTH_SHORT).show();
                        userType = user.getUserType();
                        String userEmail = user.getEmail();
                        NavController navController = NavHostFragment.findNavController(this);
                        navigationBasedonType(userType, userEmail, navController);
                    } else {
                        Toast.makeText(getActivity(), "Login failed", Toast.LENGTH_SHORT).show();
                    }
            }).addOnFailureListener(e -> {
                Toast.makeText(getActivity(), "Login failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
        });
    }

    public void navigationBasedonType(String userType, String userEmail, NavController navController){
        switch (userType) {
            case "Admin":
                navController.navigate(R.id.action_loginFragment_to_admin_main);
                break;
            case "Organizer":
                //Toast.makeText(getContext(), "Organizer navigation not implemented.", Toast.LENGTH_SHORT).show();
                Bundle bundle = new Bundle();
                bundle.putString("userEmail", userEmail);
                Toast.makeText(getActivity(), userEmail, Toast.LENGTH_SHORT).show();
                navController.navigate(R.id.action_loginFragment_to_organizer_main, bundle);
                break;
            case "Entrant":
                Toast.makeText(getContext(), "Entrant navigation not implemented.", Toast.LENGTH_SHORT).show();
                break;
            default:
                Toast.makeText(getContext(), "Invalid user type!", Toast.LENGTH_SHORT).show();
                break;
        }
    }
}