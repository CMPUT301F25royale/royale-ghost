// In file: app/src/main/java/com/example/project_part_3/Signup/Sign_up_view.java

package com.example.project_part_3.Signup;

import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.example.project_part_3.MainActivity;
import com.example.project_part_3.R;
import com.google.android.material.textfield.TextInputEditText;

import java.util.Objects;

public class Sign_up_view extends Fragment {
    private Button Organizer_button_sign_up;
    private Button Entrant_button_sign_up;
    private String selectedOption;
    private TextInputEditText nameText;
    private TextInputEditText passwordText;
    private TextInputEditText emailText;
    private TextInputEditText phoneText;
    Sign_up_model sign_up_model;


    public Sign_up_view() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
    public static Sign_up_view newInstance(String param1, String param2) {
        Sign_up_view fragment = new Sign_up_view();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.sign_up, container, false);
    }

    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState){
        super.onViewCreated(view, savedInstanceState);

        nameText = view.findViewById(R.id.name_sign_up_edit_text);
        passwordText = view.findViewById(R.id.password_sign_up_edit_text);
        emailText = view.findViewById(R.id.email_sign_up_edit_text);
        phoneText = view.findViewById(R.id.phone_num_sign_up_edit_text);
        Organizer_button_sign_up = view.findViewById(R.id.Organizer_button_sign_up);
        Entrant_button_sign_up = view.findViewById(R.id.Entrant_button_sign_up);
        Button submit_sign_up = view.findViewById(R.id.submit_sign_up);

        Organizer_button_sign_up.setOnClickListener(v -> selectOption(Organizer_button_sign_up));
        Entrant_button_sign_up.setOnClickListener(v -> selectOption(Entrant_button_sign_up));
        submit_sign_up.setOnClickListener(v -> {
            String name = Objects.requireNonNull(nameText.getText()).toString();
            String password = Objects.requireNonNull(passwordText.getText()).toString();
            String email = Objects.requireNonNull(emailText.getText()).toString();
            String phone = phoneText.getText().toString();
            if (name.isEmpty() || password.isEmpty() || email.isEmpty() || selectedOption == null) {
                Toast.makeText(getActivity(), "Please fill in all fields", Toast.LENGTH_SHORT).show();
                return;
            }
            sign_up_model = new Sign_up_model(name, password, email, phone, selectedOption);
            sign_up_model.registerUser().addOnSuccessListener(wasAdded -> {
                if (wasAdded) {
                    Toast.makeText(getActivity(), "Sign up successful", Toast.LENGTH_SHORT).show();
                    clearForm();
                    Bundle args = new Bundle();
                    args.putString("userEmail", email);
                    NavController navController = NavHostFragment.findNavController(Sign_up_view.this);
                    if ("Organizer".equals(selectedOption)) {
                        navController.navigate(R.id.action_sign_up_fragment_to_organizer_main_fragment, args);
                    } else if ("Entrant".equals(selectedOption)) {
                        // navController.navigate(R.id.action_sign_up_to_entrant_fragment, args);
                    }
                } else {
                    Toast.makeText(getActivity(), "Sign up failed User already exists", Toast.LENGTH_SHORT).show();
                }
            }).addOnFailureListener(e -> {
                Log.d("Sign_up", "Failed to sign up");
            });
        });
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

    private void clearForm(){
        nameText.setText("");
        passwordText.setText("");
        emailText.setText("");
        phoneText.setText("");
        Organizer_button_sign_up.setBackgroundColor(Color.DKGRAY);
    }
}
