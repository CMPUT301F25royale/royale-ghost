package com.example.project_part_3.Users.Admin_UI.Admin_notifications;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.project_part_3.Notification.Notification_admin_adapter;
import com.example.project_part_3.Notification.Notification_Entrant;
import com.example.project_part_3.R;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class Admin_notifications_fragment extends Fragment {

    private static final String TAG = "AdminNotifications";

    private ListView listView;
    private Notification_admin_adapter adapter;
    private final List<Notification_Entrant> notifications = new ArrayList<>();
    private FirebaseFirestore db;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        // Reuse organizer_notifications layout
        return inflater.inflate(R.layout.organizer_notifications, container, false);
    }


    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        listView = view.findViewById(R.id.organizer_notifications_list);
        db = FirebaseFirestore.getInstance();

        adapter = new Notification_admin_adapter(requireContext(), notifications);
        listView.setAdapter(adapter);

        listenForAllEntrantNotifications();
    }

    private void listenForAllEntrantNotifications() {
        // collectionGroup picks up users/{email}/notifications/* across all users
        db.collectionGroup("notifications")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value,
                                        @Nullable FirebaseFirestoreException error) {
                        if (error != null) {
                            Log.e(TAG, "Listen failed", error);
                            if (getContext() != null) {
                                Toast.makeText(getContext(),
                                        "Failed to load notifications", Toast.LENGTH_SHORT).show();
                            }
                            return;
                        }
                        if (value == null) {
                            return;
                        }

                        notifications.clear();

                        for (QueryDocumentSnapshot doc : value) {
                            String title = doc.getString("title");
                            String message = doc.getString("message");
                            String eventTitle = doc.getString("eventTitle");
                            String eventId = doc.getString("eventId");
                            String type = doc.getString("type");
                            String userEmail = doc.getString("userEmail");

                            Timestamp ts = doc.getTimestamp("createdAt");

                            Notification_Entrant n = new Notification_Entrant(
                                    title,
                                    userEmail,
                                    message,
                                    ts,
                                    eventId,
                                    eventTitle,
                                    type
                            );
                            notifications.add(n);
                        }

                        adapter.notifyDataSetChanged();
                    }
                });
    }
}
