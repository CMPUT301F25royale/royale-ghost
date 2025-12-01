package com.example.project_part_3.Users.Admin_UI;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import com.example.project_part_3.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;

/**
 * Fragment that serves as the main container for admin navigation.
 * It initializes and connects the admin bottom navigation bar with the navigation host.
 */
public class Admin_main_fragment extends Fragment {

    /**
     * Inflates and returns the layout for the admin main fragment.
     *
     * @param inflater  layout inflater used to inflate the UI
     * @param container the parent view group
     * @param savedInstanceState saved fragment state
     * @return the inflated fragment view
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.admin_host, container, false);
    }

    /**
     * Called after the view is created. Sets up the bottom navigation bar
     * and connects it to the admin navigation controller.
     *
     * @param view the fragment's root view
     * @param savedInstanceState saved instance state, if any
     */
    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {

        super.onViewCreated(view, savedInstanceState);

        BottomNavigationView bottomNav = view.findViewById(R.id.admin_bottom_nav);

        NavHostFragment navHostFragment = (NavHostFragment)
                getChildFragmentManager().findFragmentById(R.id.admin_nav_host_fragment);

        if (navHostFragment != null) {
            NavController navController = navHostFragment.getNavController();
            NavigationUI.setupWithNavController(bottomNav, navController);
        }
    }
}
