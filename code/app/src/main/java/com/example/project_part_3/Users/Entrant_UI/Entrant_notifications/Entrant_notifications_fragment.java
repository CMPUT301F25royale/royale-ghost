package com.example.project_part_3.Users.Entrant_UI.Entrant_notifications;

import android.content.Context;
import android.content.SharedPreferences;
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

import com.example.project_part_3.Notification.Notification_Entrant;
import com.example.project_part_3.Notification.Notification_entrant_adapter;
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

/**
 * Fragment responsible for displaying the list of notifications associated with the currently logged-in
 * entrant user.
 */
public class Entrant_notifications_fragment extends Fragment {

    private static final String TAG = "EntrantNotifications";

    private ListView listView;
    private Notification_entrant_adapter adapter;
    private final List<Notification_Entrant> notifications = new ArrayList<>();
    private FirebaseFirestore db;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        // Reuse organizer_notifications.xml
        return inflater.inflate(R.layout.organizer_notifications, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        listView = view.findViewById(R.id.organizer_notifications_list);
        db = FirebaseFirestore.getInstance();

        adapter = new Notification_entrant_adapter(requireContext(), notifications);
        listView.setAdapter(adapter);

        // Get the current entrant email
        String email = getCurrentUserEmail();
        if (email == null || email.isEmpty()) {
            Toast.makeText(getContext(), "No user email found", Toast.LENGTH_SHORT).show();
            return;
        }

        listenForNotifications(email);
    }

    private String getCurrentUserEmail() {
        // Using the same SharedPreferences as Login_view
        SharedPreferences prefs = requireContext().getSharedPreferences("UserData", Context.MODE_PRIVATE);
        return prefs.getString("username", null); // username is your email in login
    }

    private void listenForNotifications(@NonNull String email) {
        db.collection("users")
                .document(email)
                .collection("notifications")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value,
                                        @Nullable FirebaseFirestoreException error) {
                        if (error != null) {
                            Log.e(TAG, "Listen failed", error);
                            return;
                        }
                        if (value == null) return;

                        notifications.clear();

                        for (QueryDocumentSnapshot doc : value) {
                            String title = doc.getString("title");
                            if (title == null) title = "Event update";

                            String message = doc.getString("message");
                            if (message == null) message = "";

                            Timestamp ts = doc.getTimestamp("createdAt");

                            Notification_Entrant n =
                                    new Notification_Entrant(title, null, message, ts);
                            notifications.add(n);
                        }

                        adapter.notifyDataSetChanged();
                    }
                });
    }
}
