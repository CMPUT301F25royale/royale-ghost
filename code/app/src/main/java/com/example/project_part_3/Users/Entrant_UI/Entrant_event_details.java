package com.example.project_part_3.Users.Entrant_UI;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.project_part_3.R;
import com.google.android.material.button.MaterialButton;

public class Entrant_event_details extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.entrant_event_details, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(v, savedInstanceState);

        ImageView imgPoster = v.findViewById(R.id.imgPoster);
        TextView tvTitle = v.findViewById(R.id.tvEventTitle);
        TextView tvOrganizer = v.findViewById(R.id.tvOrganizer);
        TextView tvDescription = v.findViewById(R.id.tvDescription);
        TextView tvLocation = v.findViewById(R.id.tvLocation);
        TextView tvDateTime = v.findViewById(R.id.tvDateTime);
        MaterialButton btnJoin = v.findViewById(R.id.btnJoinEvent);

        // 🧠 TODO: Replace with actual data (from bundle or database)
        tvTitle.setText("Beginner Swim Lessons");
        tvOrganizer.setText("Organized by shadowlynx");
        tvDescription.setText("A 6-week beginner swim course designed for new swimmers.");
        tvLocation.setText("Location: Local Rec Centre");
        tvDateTime.setText("Date: Nov 10, 2025 - Dec 20, 2025");

        btnJoin.setOnClickListener(view -> {
            // TODO: Handle join logic (e.g. add to waitlist, navigate to confirmation)
        });
    }
}
