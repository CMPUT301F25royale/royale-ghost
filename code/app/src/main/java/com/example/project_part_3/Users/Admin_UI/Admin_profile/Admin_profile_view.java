package com.example.project_part_3.Users.Admin_UI.Admin_profile;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.example.project_part_3.Database_functions.Database;
import com.example.project_part_3.R;
import com.example.project_part_3.Users.User;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

public class Admin_profile_view extends Fragment {
    private ListView profileList;
    private Database db;
    private Admin_profile_adapter adapter;
    private ArrayList<User> profiles;

    public Admin_profile_view() {

    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        db = new Database(FirebaseFirestore.getInstance());
        profiles = new ArrayList<>();
        adapter = new Admin_profile_adapter(getContext(), R.layout.admin_profiles_element, profiles);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        return inflater.inflate(R.layout.admin_profiles, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        profileList = view.findViewById(R.id.admin_profiles_list);
        profileList.setAdapter(adapter);

        db.getAllUsers().addOnSuccessListener(userList -> {

            profiles.clear();
            profiles.addAll(userList);
            adapter.notifyDataSetChanged();
        }).addOnFailureListener(e -> {

            Log.e("Admin_profile_view", "Error fetching users", e);
            Toast.makeText(getContext(), "Failed to load profiles.", Toast.LENGTH_SHORT).show();
        });


        profileList.setOnItemClickListener((parent, itemView, position, id) -> {
            User selectedProfile = profiles.get(position);
            Bundle bundle = new Bundle();
            bundle.putString("name", selectedProfile.getName());
            bundle.putString("email", selectedProfile.getEmail());
            bundle.putString("phone", selectedProfile.getPhone());

            NavController navController = Navigation.findNavController(itemView);
            navController.navigate(R.id.action_admin_profile_view_to_admin_profile_info, bundle);
        });
    }
}
