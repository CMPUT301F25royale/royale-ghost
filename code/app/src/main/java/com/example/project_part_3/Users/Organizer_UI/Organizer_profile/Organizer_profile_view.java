package com.example.project_part_3.Users.Organizer_UI.Organizer_profile;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.project_part_3.R;

public class Organizer_profile_view extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.organizer_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Button myButton = view.findViewById(R.id.Pass_Reset);
        myButton.setOnClickListener(v -> {
            Toast.makeText(getContext(), "Button clicked!", Toast.LENGTH_SHORT).show();
        });
    }
}

