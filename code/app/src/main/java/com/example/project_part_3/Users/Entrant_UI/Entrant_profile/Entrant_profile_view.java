package com.example.project_part_3.Users.Entrant_UI.Entrant_profile;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.project_part_3.R;
import com.example.project_part_3.Users.Organizer_UI.Organizer_profile.Organizer_profile_view;

public class Entrant_profile_view extends Organizer_profile_view {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.entrant_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // unhide the interests list
        TextView interestsTitle = view.findViewById(R.id.Interests);
        interestsTitle.setVisibility(View.VISIBLE);

        ListView interestsListView = view.findViewById(R.id.InterestsListView);
        interestsListView.setVisibility(View.VISIBLE);

        // ... get interests and do stuff
    }
}