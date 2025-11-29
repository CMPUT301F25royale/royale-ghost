package com.example.project_part_3.Login;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
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

public class Login_view extends Fragment {
    private TextView name;
    private TextView password;
    private Button submit;

    public Login_view() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.login, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        name = view.findViewById(R.id.name_login_edit_text);
        password = view.findViewById(R.id.password_login_edit_text);
        submit = view.findViewById(R.id.Login_submit);

        Database db = new Database(FirebaseFirestore.getInstance());

        submit.setOnClickListener(v -> {
            submit.setText("Logging in...");
            submit.setEnabled(false);
            // handles the login logic in a separate thread
            new Handler(Looper.getMainLooper()).post(() -> {
                performLogin(db);
            });
        });
    }

    private void performLogin(Database db) {
        String nameText = name.getText().toString();
        String passwordText = password.getText().toString();

        // Save credentials
        if (getContext() != null) {
            SharedPreferences prefs = requireContext().getSharedPreferences("UserData", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString("username", nameText);
            editor.putString("password", passwordText);
            editor.apply();
        }

        if (nameText.isEmpty() || passwordText.isEmpty()) {
            Toast.makeText(getActivity(), "Please fill in all fields", Toast.LENGTH_SHORT).show();
            resetButton();
            return;
        }

        db.checkUser(nameText, passwordText).addOnSuccessListener(user -> {
            if (getView() == null || !isAdded()) return;

            if (user != null) {
                Toast.makeText(getActivity(), "Login successful", Toast.LENGTH_SHORT).show();

                String userType = user.getUserType();
                String userEmail = user.getEmail();

                Bundle args = new Bundle();
                args.putString("userEmail", userEmail);

                NavController nav = Navigation.findNavController(requireView());

                try {
                    switch (userType) {
                        case "Admin":
                            nav.navigate(R.id.action_loginFragment_to_admin_main, args);
                            break;
                        case "Organizer":
                            nav.navigate(R.id.action_loginFragment_to_organizer_main, args);
                            break;
                        case "Entrant":
                            nav.navigate(R.id.action_global_entrant_main, args);
                            break;
                        default:
                            Toast.makeText(getContext(), "Invalid user type!", Toast.LENGTH_SHORT).show();
                            resetButton();
                    }
                } catch (IllegalArgumentException e) {
                    Toast.makeText(getContext(), "Navigation Error", Toast.LENGTH_SHORT).show();
                    resetButton();
                }
            } else {
                Toast.makeText(getActivity(), "Login failed", Toast.LENGTH_SHORT).show();
                resetButton();
            }
        }).addOnFailureListener(e -> {
            if (getContext() != null) {
                Toast.makeText(getActivity(), "Login failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
            resetButton();
        });
    }

    private void resetButton() {
        submit.setEnabled(true);
        submit.setText("Login");
    }
}