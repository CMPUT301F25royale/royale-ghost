package com.example.project_part_3.Users.Entrant_UI;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.NavigationUI;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.project_part_3.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class Entrant_main extends Fragment {

    public Entrant_main() {}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.entrant_main, container, false);
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        RecyclerView rv = view.findViewById(R.id.eventsRecycler);
        rv.setLayoutManager(new LinearLayoutManager(requireContext()));

        BottomNavigationView bottom = view.findViewById(R.id.bottomNavigationView);

        // Use the NavController tied to THIS fragment's view
        NavController nav = Navigation.findNavController(view);

        NavigationUI.setupWithNavController(bottom, nav);
        bottom.setOnItemReselectedListener(item -> {});
    }}

