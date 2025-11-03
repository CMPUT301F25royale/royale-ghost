package com.example.project_part_3.Users.Admin_UI.Admin_profile;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.example.project_part_3.Database_functions.UserDatabase;
import com.example.project_part_3.R;
import com.example.project_part_3.Users.User;

import java.util.ArrayList;

public class Admin_profile_view extends Fragment {
    private ListView profileList;
    private UserDatabase userDb;
    private Admin_profile_adapter adapter;
    private ArrayList<User> profiles;

    public Admin_profile_view() {

    }



    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.admin_profiles, container, false);


        userDb = UserDatabase.getInstance();

        profiles = new ArrayList<>(userDb.getAllUsers());
        adapter = new Admin_profile_adapter(getContext(), R.layout.admin_profiles_element, profiles);


        profileList = view.findViewById(R.id.admin_profiles_list);
        profileList.setAdapter(adapter);
        profileList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                User selectedProfile = (User) profiles.get(position);
                Bundle bundle = new Bundle();
                bundle.putString("name", selectedProfile.getName());
                bundle.putString("email", selectedProfile.getEmail());
                bundle.putString("phone", selectedProfile.getPhone());

                NavController navController = Navigation.findNavController(view);
                navController.navigate(R.id.action_admin_profile_view_to_admin_profile_info, bundle);
            }
        });
        return view;

    }
}
