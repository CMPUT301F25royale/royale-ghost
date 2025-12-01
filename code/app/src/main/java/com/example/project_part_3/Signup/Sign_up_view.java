package com.example.project_part_3.Signup;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.example.project_part_3.Database_functions.NotificationMessagingService;
import com.example.project_part_3.R;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.ArrayList;
import java.util.Objects;

/**
 * Fragment for signing up. Initializes various buttons and text fields to allow the user
 * to sign up and create a new account as an entrant or an organizer
 */
public class Sign_up_view extends Fragment {
    private Button Organizer_button_sign_up;
    private Button Entrant_button_sign_up;
    private Button submit_sign_up;
    private String selectedOption;
    private TextInputEditText nameText;
    private TextInputEditText passwordText;
    private TextInputEditText emailText;
    private TextInputEditText phoneText;
    private Sign_up_model sign_up_model;

    public Sign_up_view() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    public static Sign_up_view newInstance() {
        return new Sign_up_view();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.sign_up, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState){
        super.onViewCreated(view, savedInstanceState);

        initializeViews(view);

        Organizer_button_sign_up.setOnClickListener(v -> selectOption(Organizer_button_sign_up));
        Entrant_button_sign_up.setOnClickListener(v -> selectOption(Entrant_button_sign_up));

        submit_sign_up.setOnClickListener(v -> performSignUp());
    }

    private void initializeViews(@NonNull View view) {
        nameText = view.findViewById(R.id.name_sign_up_edit_text);
        passwordText = view.findViewById(R.id.password_sign_up_edit_text);
        emailText = view.findViewById(R.id.email_sign_up_edit_text);
        phoneText = view.findViewById(R.id.phone_num_sign_up_edit_text);
        Organizer_button_sign_up = view.findViewById(R.id.Organizer_button_sign_up);
        Entrant_button_sign_up = view.findViewById(R.id.Entrant_button_sign_up);
        submit_sign_up = view.findViewById(R.id.submit_sign_up);
    }

    private void performSignUp() {
        submit_sign_up.setText("Signing up...");
        submit_sign_up.setEnabled(false);

        String name = Objects.requireNonNull(nameText.getText()).toString().trim();
        String password = Objects.requireNonNull(passwordText.getText()).toString().trim();
        String email = Objects.requireNonNull(emailText.getText()).toString().trim();
        String phone = Objects.requireNonNull(phoneText.getText()).toString().trim();
        ArrayList<String> interest = new ArrayList<>();

        if (name.isEmpty() || password.isEmpty() || email.isEmpty() || selectedOption == null) {
            Toast.makeText(getContext(), "Please fill in all required fields", Toast.LENGTH_SHORT).show();
            resetButton();
            return;
        }

        sign_up_model = new Sign_up_model(name, password, email, phone, interest, selectedOption);
        sign_up_model.registerUser().addOnSuccessListener(wasAdded -> {
            if (!isAdded()) {
                return;
            }

            if (wasAdded) {
                Toast.makeText(getContext(), "Sign up successful", Toast.LENGTH_SHORT).show();

                FirebaseMessaging.getInstance().getToken()
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful() && task.getResult() != null) {
                                NotificationMessagingService.saveTokenForEmail(email, task.getResult());
                            } else {
                                Log.w("FCM", "Fetching FCM registration token failed", task.getException());
                            }
                        });

                SharedPreferences prefs = requireContext().getSharedPreferences("UserData", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = prefs.edit();
                editor.putString("userEmail", email);
                editor.putString("userType", selectedOption);
                editor.apply();

                Bundle args = new Bundle();
                args.putString("userEmail", email);
                NavController navController = NavHostFragment.findNavController(Sign_up_view.this);

                if ("Organizer".equals(selectedOption)) {
                    navController.navigate(R.id.action_sign_up_fragment_to_organizer_main_fragment, args);
                } else if ("Entrant".equals(selectedOption)) {
                    navController.navigate(R.id.action_sign_up_fragment_to_entrant_main, args);
                }

                clearForm();
            } else {
                Toast.makeText(getContext(), "Sign up failed: User already exists", Toast.LENGTH_SHORT).show();
                resetButton();
            }
        }).addOnFailureListener(e -> {
            if (!isAdded()) {
                return;
            }
            Log.e("SignUpFailure", "User registration failed.", e);
            Toast.makeText(getContext(), "Sign up failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            resetButton();
        });
    }

    private void resetButton() {
        if (submit_sign_up != null) {
            submit_sign_up.setEnabled(true);
            submit_sign_up.setText("Sign Up");
        }
    }

    private void selectOption(Button selectedButton) {
        Organizer_button_sign_up.setBackgroundColor(Color.GRAY);
        Entrant_button_sign_up.setBackgroundColor(Color.GRAY);

        if (selectedButton.getId() == R.id.Organizer_button_sign_up) {
            selectedOption = "Organizer";
            Organizer_button_sign_up.setBackgroundColor(Color.BLACK);
        } else if (selectedButton.getId() == R.id.Entrant_button_sign_up) {
            selectedOption = "Entrant";
            Entrant_button_sign_up.setBackgroundColor(Color.BLACK);
        }
    }

    private void clearForm() {
        if (nameText != null) nameText.setText("");
        if (passwordText != null) passwordText.setText("");
        if (emailText != null) emailText.setText("");
        if (phoneText != null) phoneText.setText("");

        if (Organizer_button_sign_up != null) Organizer_button_sign_up.setBackgroundColor(Color.GRAY);
        if (Entrant_button_sign_up != null) Entrant_button_sign_up.setBackgroundColor(Color.GRAY);
        selectedOption = null;

        resetButton();
    }
}
