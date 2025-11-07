package com.example.project_part_3.Users.Entrant_UI;

import android.os.Bundle;
import android.view.*;
import android.widget.Toast;
import androidx.annotation.*;
import androidx.fragment.app.Fragment;
import com.example.project_part_3.R;

public class Entrant_scan extends Fragment {

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.entrant_scan, container, false);
    }

    @Override public void onViewCreated(@NonNull View v, @Nullable Bundle s) {
        v.findViewById(R.id.fabTorch).setOnClickListener(
                x -> Toast.makeText(requireContext(), "Toggle flashlight", Toast.LENGTH_SHORT).show());
        v.findViewById(R.id.fabGallery).setOnClickListener(
                x -> Toast.makeText(requireContext(), "Pick from gallery", Toast.LENGTH_SHORT).show());
        // Hook up CameraX later using previewView (R.id.previewView).
    }
}
