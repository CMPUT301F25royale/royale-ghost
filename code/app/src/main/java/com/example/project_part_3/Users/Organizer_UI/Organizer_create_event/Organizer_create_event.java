// In: .../Organizer_UI/Organizer_create_event/Organizer_create_event.java

package com.example.project_part_3.Users.Organizer_UI.Organizer_create_event;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider; // Import ViewModelProvider

// Import your new shared view model
import com.example.project_part_3.Users.Organizer_UI.OrganizerSharedViewModel;
import com.example.project_part_3.R;

import java.nio.file.Paths;

public class Organizer_create_event extends Fragment {

    private OrganizerSharedViewModel sharedViewModel;

    public Organizer_create_event() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sharedViewModel = new ViewModelProvider(requireActivity()).get(OrganizerSharedViewModel.class);
    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.organizer_create_event, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        String currentEmail = sharedViewModel.getUserEmail().getValue();
        sharedViewModel.getUserEmail().observe(getViewLifecycleOwner(), email -> {
            if (email != null && !email.isEmpty()) {
                Toast.makeText(getContext(), "Current Email: " + email, Toast.LENGTH_SHORT).show();
                }
        });
    }
}

