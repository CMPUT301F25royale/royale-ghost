package com.example.project_part_3;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Check if user is logged in
        if (getSharedPreferences("UserData", MODE_PRIVATE).contains("userEmail")) {
            String userEmail = getSharedPreferences("UserData", MODE_PRIVATE).getString("userEmail", "");
            String userType = getSharedPreferences("UserData", MODE_PRIVATE).getString("userType", "");

            NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.nav_host_fragment);

            if (navHostFragment != null) {
                NavController nav = navHostFragment.getNavController();
                Bundle args = new Bundle();
                args.putString("userEmail", userEmail);

                try {
                    switch (userType) {
                        case "Admin":
                            nav.navigate(R.id.admin_main, args);
                            break;
                        case "Organizer":
                            nav.navigate(R.id.organizer_main, args);
                            break;
                        case "Entrant":
                            nav.navigate(R.id.entrant_main, args);
                            break;
                        default:
                            Log.e("MainActivity", "Invalid user type: " + userType);
                    }
                } catch (IllegalArgumentException e) {
                    Log.e("MainActivity", "Navigation Error", e);
                }
            } else {
                Log.e("MainActivity", "NavHostFragment not found! Check your XML ID.");
            }
        }
    }
}