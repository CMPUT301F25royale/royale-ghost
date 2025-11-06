// In file: app/src/main/java/com/example/project_part_3/Signup/Sign_up_view.java

package com.example.project_part_3.Signup;

import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.example.project_part_3.R;
import com.google.android.material.textfield.TextInputEditText;

public class Sign_up_view extends Fragment {
    private Button Organizer_button_sign_up;
    private Button Entrent_button_sign_up;
    private String selectedOption;

    private String mParam1;
    private String mParam2;

    private TextInputEditText nameText;
    private TextInputEditText passwordText;
    private TextInputEditText emailText;
    private TextInputEditText phoneText;
    private Button submit_sign_up;
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
        Entrent_button_sign_up = view.findViewById(R.id.Entrent_button_sign_up);
        submit_sign_up = view.findViewById(R.id.submit_sign_up);

        Organizer_button_sign_up.setOnClickListener(v -> selectOption(Organizer_button_sign_up));
        Entrent_button_sign_up.setOnClickListener(v -> selectOption(Entrent_button_sign_up));
        submit_sign_up.setOnClickListener(v -> {
            String name = nameText.getText().toString();
            String password = passwordText.getText().toString();
            String email = emailText.getText().toString();
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
                    //TODO: Add navigation logic here
                } else {
                    Toast.makeText(getActivity(), "Sign up failed User already exists", Toast.LENGTH_SHORT).show();
                }
            }).addOnFailureListener(e -> {
                Log.d("Sign_up", "Failed to sign up");
            });
        });
    }

    private void selectOption(Button selectedButton) {
        Organizer_button_sign_up.setBackgroundColor(Color.DKGRAY);
        Entrent_button_sign_up.setBackgroundColor(Color.DKGRAY);

        if (selectedButton.getId() == R.id.Organizer_button_sign_up) {
            selectedOption = "Organizer";
            Organizer_button_sign_up.setBackgroundColor(Color.BLACK);
        } else if (selectedButton.getId() == R.id.Entrent_button_sign_up) {
            selectedOption = "Entrant";
            Entrent_button_sign_up.setBackgroundColor(Color.BLACK);
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
