package com.example.project_part_3.Login;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.project_part_3.R;

public class Login_view extends Fragment {
    private TextView name;
    private TextView password;
    private Button submit;
    private Login_model login_model;
    public Login_view() {
    }
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
        submit.setOnClickListener(v -> {
            String nameText = name.getText().toString();
            String passwordText = password.getText().toString();
            if (nameText.isEmpty() || passwordText.isEmpty()) {
                Toast.makeText(getActivity(), "Please fill in all fields", Toast.LENGTH_SHORT).show();
                return;
            }
            login_model = new Login_model(nameText, passwordText);
            if (login_model.getSuccess()){
                Toast.makeText(getActivity(), "Login successful", Toast.LENGTH_SHORT).show();
                //TODO: Add navigation logic here
            }else
            {
                Toast.makeText(getActivity(), "Login failed", Toast.LENGTH_SHORT).show();
                return;
            };
            });
    }
}