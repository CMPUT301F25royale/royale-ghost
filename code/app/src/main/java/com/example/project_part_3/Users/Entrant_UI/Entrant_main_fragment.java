// In: com/example/project_part_3/Users/Organizer_UI/Organizer_main_fragment.java

package com.example.project_part_3.Users.Entrant_UI;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.NavOptions;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import com.example.project_part_3.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;

/**
 * The main fragment for the entrant UI which sets up and
 * manages the navigation components.
 */
public class Entrant_main_fragment extends Fragment {

    public Entrant_main_fragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            String email = getArguments().getString("userEmail");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.entrant_host, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        String userEmail = getArguments() != null ? getArguments().getString("userEmail") : null;

        NavHostFragment childHost = (NavHostFragment)
                getChildFragmentManager().findFragmentById(R.id.entrant_nav_host_fragment);
        if (childHost == null) return;
        NavController childNav = childHost.getNavController();

        // Give the start destination the arg
        Bundle sharedArgs = new Bundle();
        sharedArgs.putString("userEmail", userEmail);
        childNav.setGraph(R.navigation.entrant_nav, sharedArgs);

        BottomNavigationView bottom = view.findViewById(R.id.entrant_bottom_nav);

        NavOptions navOpts = new NavOptions.Builder()
                .setLaunchSingleTop(true)
                .setRestoreState(true)
                .setPopUpTo(childNav.getGraph().getId(), false, true)
                .build();

        bottom.setOnItemSelectedListener(item -> {
            try {
                childNav.navigate(item.getItemId(), sharedArgs, navOpts);
                return true;
            } catch (IllegalArgumentException ignored) {
                return true;
            }
        });

        childNav.addOnDestinationChangedListener((controller, destination, arguments) -> {
            bottom.getMenu().findItem(destination.getId()).setChecked(true);
        });
    }


}



