package com.example.project_part_3.Users.Admin_UI.Admin_profile;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.example.project_part_3.R;
import com.example.project_part_3.Users.User;

import java.util.ArrayList;

/**
 * Fragment that displays a list of all users for the admin.
 * Allows navigation to detailed user profile views.
 */
public class Admin_profile_view extends Fragment {

    private ListView profileList;
    private Admin_profile_adapter adapter;
    private Admin_profile_model adminprofilemodel;

    /**
     * Initializes the ViewModel and sets up the adapter for displaying user profiles.
     *
     * @param savedInstanceState the saved instance state, may be null
     */
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        adminprofilemodel = new ViewModelProvider(this).get(Admin_profile_model.class);
        adapter = new Admin_profile_adapter(getContext(), R.layout.admin_profiles_element, new ArrayList<>());
    }

    /**
     * Inflates the main layout for the admin profile list screen.
     *
     * @param inflater used to inflate the layout
     * @param container parent container for the fragment
     * @param savedInstanceState saved state, may be null
     * @return the inflated view hierarchy
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.admin_profiles, container, false);
    }

    /**
     * Sets up the list view, observes user data, and handles navigation events.
     *
     * @param view the root view of the fragment
     * @param savedInstanceState saved state, may be null
     */
    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        profileList = view.findViewById(R.id.admin_profiles_list);
        profileList.setAdapter(adapter);

        adminprofilemodel.getUserProfiles().observe(getViewLifecycleOwner(), users -> {
            if (users != null) {
                adapter.clear();
                adapter.addAll(users);
                adapter.notifyDataSetChanged();
            }
        });

        profileList.setOnItemClickListener((parent, itemView, position, id) -> {
            User selectedProfile = adapter.getItem(position);
            if (selectedProfile != null) {
                Bundle bundle = new Bundle();
                bundle.putString("email", selectedProfile.getEmail());
                NavController navController = Navigation.findNavController(itemView);
                navController.navigate(R.id.action_admin_profile_view_to_admin_profile_info, bundle);
            }
        });
    }
}
