package com.example.project_part_3.Users.Entrant_UI;

import android.os.Bundle;
import android.view.*;
import android.widget.TextView;

import androidx.annotation.*;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.project_part_3.R;

import java.util.Arrays;
import java.util.List;

public class Entrant_notification extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        // Inflate your entrant_notification.xml layout
        return inflater.inflate(R.layout.entrant_notification, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle savedInstanceState) {
        // Find RecyclerView from layout
        RecyclerView rv = v.findViewById(R.id.rvNotifications);
        rv.setLayoutManager(new LinearLayoutManager(requireContext()));

        // Sample mock data for testing
        List<String> notifications = Arrays.asList(
                "🎉 Congratulations! You’ve been selected for Swim Lessons!",
                "⚠️ Reminder: Tennis Tournament starts tomorrow.",
                "✅ Your profile update was successful."
        );

        // Anonymous inline adapter — no need for a separate class file
        rv.setAdapter(new RecyclerView.Adapter<RecyclerView.ViewHolder>() {
            @NonNull
            @Override
            public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                // Create a simple TextView for each list item
                TextView tv = new TextView(parent.getContext());
                tv.setPadding(40, 32, 40, 32);
                tv.setTextSize(16);
                return new RecyclerView.ViewHolder(tv) {};
            }

            @Override
            public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
                ((TextView) holder.itemView).setText(notifications.get(position));
            }

            @Override
            public int getItemCount() {
                return notifications.size();
            }
        });
    }
}
