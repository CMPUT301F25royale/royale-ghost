package com.example.project_part_3;

import android.os.Bundle;

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
    }
    public void NavigationForUserType(String userType){
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);
        NavController navController = navHostFragment.getNavController();

        int graphId;
        switch(userType){
                case "Organizer":
                graphId = R.navigation.organizer_main;
                break;
                case "Entrant":
                graphId = R.navigation.entrant_main;
                break;
                case "Admin":
                graphId = R.navigation.admin_main;
                break;
                default:
                throw new IllegalArgumentException("Invalid user type: " + userType);
        }
        navController.setGraph(graphId);

    }
}

