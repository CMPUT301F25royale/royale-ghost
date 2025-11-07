package com.example.project_part_3.Users.Entrant_UI;

import android.os.Bundle;
import android.view.*;
import android.widget.Toast;
import androidx.annotation.*;
import androidx.fragment.app.Fragment;
import com.example.project_part_3.R;

public class Entrant_profile extends Fragment {

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.entrant_profile, container, false);
    }

    @Override public void onViewCreated(@NonNull View v, @Nullable Bundle s) {
        v.findViewById(R.id.btnResetPassword).setOnClickListener(
                x -> Toast.makeText(requireContext(), "Reset password clicked", Toast.LENGTH_SHORT).show());
        v.findViewById(R.id.btnDeleteProfile).setOnClickListener(
                x -> Toast.makeText(requireContext(), "Delete profile clicked", Toast.LENGTH_SHORT).show());
    }
}
