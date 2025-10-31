package com.example.project_part_3.Enter;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.example.project_part_3.R;

public class WelcomeFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_welcome, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Button loginButton = view.findViewById(R.id.Login);
        Button signupButton = view.findViewById(R.id.Signup);

        loginButton.setOnClickListener(v -> {
            NavHostFragment.findNavController(WelcomeFragment.this)
                    .navigate(R.id.action_welcomeFragment_to_loginFragment);
        });

        signupButton.setOnClickListener(v -> {
            NavHostFragment.findNavController(WelcomeFragment.this)
                    .navigate(R.id.action_welcomeFragment_to_sign_up_fragment);
        });
    }
}
