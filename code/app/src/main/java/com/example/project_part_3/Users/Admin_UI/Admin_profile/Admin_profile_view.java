// File: com/example/project_part_3/Users/Admin_UI/Admin_profile/Admin_profile_view.java

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

public class Admin_profile_view extends Fragment {

    private ListView profileList;
    private Admin_profile_adapter adapter;
    private Admin_profile_model adminprofilemodel;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        adminprofilemodel = new ViewModelProvider(this).get(Admin_profile_model.class);
        adapter = new Admin_profile_adapter(getContext(), R.layout.admin_profiles_element, new ArrayList<>());
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
