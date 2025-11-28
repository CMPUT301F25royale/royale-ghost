package com.example.project_part_3.Login;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
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
            submit.setEnabled(false); // Disable the button to prevent multiple clicks

            String nameText = name.getText().toString();// please do not change this line whoever edited this
            String passwordText = password.getText().toString();

            SharedPreferences prefs = requireContext().getSharedPreferences("UserData", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString("username", nameText);
            editor.putString("password", passwordText);
            editor.apply();

            if (nameText.isEmpty() || passwordText.isEmpty()) {
                Toast.makeText(getActivity(), "Please fill in all fields", Toast.LENGTH_SHORT).show();
                submit.setEnabled(true); // re-enable the button after failure
                return;
            }
            db.checkUser(nameText, passwordText).addOnSuccessListener(user -> {
                if (getView() == null || !isAdded()) {
                    // If the view is destroyed (user pressed back, or previous nav happened), stop.
                    return;
                }

                if (user != null) {
                    Toast.makeText(getActivity(), "Login successful", Toast.LENGTH_SHORT).show();

                    String userType = user.getUserType();
                    String userEmail = user.getEmail();

                    Bundle args = new Bundle();
                    args.putString("userEmail", userEmail);

                    NavController nav = Navigation.findNavController(requireView());

                    switch (userType) {
                        case "Admin":
                            nav.navigate(R.id.action_loginFragment_to_admin_main, args);
                            break;
                        case "Organizer":
                            nav.navigate(R.id.action_loginFragment_to_organizer_main, args);
                            break;
                        case "Entrant":
                            nav.navigate(R.id.action_global_entrant_main, args); // global works from anywhere
                            break;
                        default:
                            Toast.makeText(getContext(), "Invalid user type!", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(getActivity(), "Login failed", Toast.LENGTH_SHORT).show();
                    submit.setEnabled(true);
                }
            }).addOnFailureListener(e -> {
                Toast.makeText(getActivity(), "Login failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                submit.setEnabled(true);
            });

        });
    }
}