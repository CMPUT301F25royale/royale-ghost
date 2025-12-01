package com.example.project_part_3.Users.Organizer_UI.Organizer_event;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.InputType;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.example.project_part_3.Database_functions.Database;
import com.example.project_part_3.Events.Event;
import com.example.project_part_3.R;
import com.example.project_part_3.Users.Entrant;
import com.example.project_part_3.Users.Organizer_UI.OrganizerSharedViewModel;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
// 🔹 NEW imports for Google Map + Firestore GeoPoint
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.GeoPoint;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Fragment that displays a list of entrants for a selected event in the Organizer UI.
 * Each entrant's name, email, and status is displayed as well as a decline button if applicable
 */
public class Organizer_entrant_view extends Fragment {

    private SwitchCompat showChosenEntrantsSwitch;
    private SwitchCompat showCancelledEntrantsSwitch;
    private Organizer_entrant_adapter adapter;

    private OrganizerSharedViewModel model;
    private Database db;
    private Event event;

    private ArrayList<Pair<Entrant, String>> masterList; // holds ALL data fetched from DB
    private ArrayList<Pair<Entrant, String>> displayList; // holds only what is currently shown based on switches

    private ActivityResultLauncher<Intent> saveCsvLauncher;

    ArrayList<Pair<Entrant, String>> entrantArrayListAndStatuses;


    // 🔹 optional: keep current event reference
    private Event currentEvent;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        masterList = new ArrayList<>();
        displayList = new ArrayList<>();

        saveCsvLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        Uri uri = result.getData().getData();
                        if (uri != null) {
                            writeCsvToUri(uri);
                        }
                    }
                }
        );
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.organizer_event_entrant_view, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        model = new ViewModelProvider(requireActivity()).get(OrganizerSharedViewModel.class);
        db = new Database(FirebaseFirestore.getInstance());

        ListView listView = view.findViewById(R.id.organizer_event_entrant_list);
        showChosenEntrantsSwitch = view.findViewById(R.id.show_chosen_entrants_switch);
        showCancelledEntrantsSwitch = view.findViewById(R.id.show_cancelled_entrants_switch);

        setUpBackButton(view);
        setUpSwitches();
        setUpExportButton(view);

        adapter = new Organizer_entrant_adapter(getContext(), R.layout.organizer_event_entrant_element, displayList, this::declineEntrant);
        listView.setAdapter(adapter);

        model.getSelectedEvent().observe(getViewLifecycleOwner(), selectedEvent -> {
            if (selectedEvent != null) {
                this.event = selectedEvent;
                fetchEntrants(selectedEvent);
                model.getSelectedEvent().observe(getViewLifecycleOwner(), event -> {
                    if (event != null) {
                        currentEvent = event;              // 🔹 remember event
                        populateUI(view, event);
                    }
                });
            }
        });
    }



    public void populateUI(View view, Event event) {
        db.getAllEntrantsByEvent(event).addOnSuccessListener(entrants -> {
            ArrayList<Entrant> entrantArrayList = new ArrayList<>(entrants);
            ArrayList<String> statuses = new ArrayList<>();

            for (Entrant e : entrantArrayList) {
                String status;
                if (event.getConfirmedUserIds() != null &&
                        event.getConfirmedUserIds().contains(e.getEmail())) {
                    status = "Accepted";
                } else if (event.getDeclinedUserIds() != null &&
                        event.getDeclinedUserIds().contains(e.getEmail())) {
                    status = "Declined";
                } else {
                    status = "Pending";
                }
                statuses.add(status);
            }

            entrantArrayListAndStatuses = new ArrayList<>();
            for (int i = 0; i < entrantArrayList.size(); i++) {
                entrantArrayListAndStatuses.add(
                        new Pair<>(entrantArrayList.get(i), statuses.get(i))
                );
            }

            adapter = new Organizer_entrant_adapter(
                    getContext(),
                    R.layout.organizer_event_entrant_element,
                    entrantArrayListAndStatuses,
                    entrant -> declineEntrant(event, entrant)
            );

            ListView listView = view.findViewById(R.id.organizer_event_entrant_list);
            listView.setAdapter(adapter);

            // 🔹 after list is ready, also configure the map
            setUpMapForEvent(view, event);
        });
    }

//    private void setUpNotifyButton(View view) {
//        Button notifyButton = view.findViewById(R.id.notify_button);
//        notifyButton.setOnClickListener(v -> {
//            if (event == null) return;
//            showNotifyPopup();
//        });
//    }

//    private void showNotifyPopup() {
//        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
//        builder.setTitle("Send Notification");
//        builder.setMessage("Enter the message you want to send");
//
//        final EditText input = new EditText(requireContext());
//        input.setInputType(InputType.TYPE_CLASS_TEXT);
//        builder.setView(input);
//
//        builder.setPositiveButton("Send", new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialog, int which) {
//                String message = input.getText().toString();
//
//                ArrayList<String> toSend = new ArrayList<>();
//                for (Pair<Entrant, String> pair : displayList) {
//                    toSend.add(pair.first.getEmail()); // add each email
//                }
//                db.sendNotification(message, event, toSend, "message_from_organizer");
//            }
//        });
//
//        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialog, int which) {
//                dialog.cancel();
//            }
//        });
//
//        builder.show();
//    }

    /**
     * Fetches all entrants for the event and populates the masterList.
     */
    private void fetchEntrants(Event event) {
        db.getAllEntrantsByEvent(event).addOnSuccessListener(entrants -> {
            masterList.clear();
            ArrayList<Entrant> entrantArrayList = new ArrayList<>(entrants);

            // Calculate status for each entrant
            for (Entrant e : entrantArrayList) {
                String status = getStatus(event, e);
                masterList.add(new Pair<>(e, status));
            }

            // Initial filter call to populate UI based on default switch states
            filterList();

        }).addOnFailureListener(e -> {
            Log.e("Organizer_entrant_view", "Failed to fetch entrants", e);
            Toast.makeText(getContext(), "Error loading entrants", Toast.LENGTH_SHORT).show();
        });
    }

    /**
     * Sets up listeners for the toggle switches.
     */
    private void setUpSwitches() {
        showChosenEntrantsSwitch.setOnCheckedChangeListener(
                (buttonView, isChecked) -> filterList());
        showCancelledEntrantsSwitch.setOnCheckedChangeListener(
                (buttonView, isChecked) -> filterList());
    }

    /**
     * Rebuilds the displayList based on the masterList and switch states.
     */
    private void filterList() {
        displayList.clear();

        boolean showChosen = showChosenEntrantsSwitch.isChecked();
        boolean showCancelled = showCancelledEntrantsSwitch.isChecked();

        for (Pair<Entrant, String> pair : masterList) {
            String status = pair.second;

            if ((status.equals("Accepted") || status.equals("Pending"))) {
                if (showChosen) {
                    displayList.add(pair);
                }
            }
            else if (status.equals("Declined")) {
                if (showCancelled) {
                    displayList.add(pair);
                }
            }
        }

        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
    }

    /**
     * Determines the status string for a specific entrant.
     */
    private String getStatus(Event event, Entrant entrant) {
        if (event.getConfirmedUserIds() != null && event.getConfirmedUserIds().contains(entrant.getEmail())) {
            return "Accepted";
        } else if (event.getDeclinedUserIds() != null && event.getDeclinedUserIds().contains(entrant.getEmail())) {
            return "Declined";
        } else {
            return "Pending";
        }
    }

    /**
     * Declines an entrant and updates the local list immediately.
     */
    private void declineEntrant(Entrant entrant) {
        if (event == null) return;

        db.declineEntrant(event, entrant).addOnSuccessListener(success -> {
            if (success) {
                for (int i = 0; i < masterList.size(); i++) {
                    Entrant current = masterList.get(i).first;
                    if (current.getEmail().equals(entrant.getEmail())) {
                        masterList.set(i, new Pair<>(current, "Declined"));
                        break;
                    }
                }
                filterList();
            } else {
                Toast.makeText(getContext(), "Failed to decline entrant", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(e -> {
            Log.e("Organizer_entrant_view", "Failed to decline entrant", e);
            Toast.makeText(getContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }

    private void setUpBackButton(View view) {
        ImageButton back = view.findViewById(R.id.organizer_event_view_back_button);
        if (back != null) {
            back.setOnClickListener(v -> {
                NavController navBack = NavHostFragment.findNavController(this);
                navBack.navigate(R.id.action_organizer_entrant_view_to_organizerEventsFragment);
            });
        }
    }

    private void setUpExportButton(View view) {
        Button exportButton = view.findViewById(R.id.export_as_csv_button);
        exportButton.setOnClickListener(v -> exportAsCSV());
    }

    public void exportAsCSV() {
        if (event == null) return;

        String title = event.getTitle().replaceAll("[^a-zA-Z0-9._-]", "_");
        String fileName = "entrants_" + title + ".csv";

        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("text/csv");
        intent.putExtra(Intent.EXTRA_TITLE, fileName);

        saveCsvLauncher.launch(intent);
    }

    private void writeCsvToUri(Uri uri) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler mainHandler = new Handler(Looper.getMainLooper());

        executor.execute(() -> {
            try {
                // Open the output stream to the URI chosen by the user
                OutputStream outputStream = requireContext().getContentResolver().openOutputStream(uri);
                if (outputStream == null) {
                    throw new IOException("Failed to open output stream");
                }

                // Build CSV String
                StringBuilder sb = new StringBuilder();
                sb.append("Name,Email,Status\n");

                for (Pair<Entrant, String> pair : displayList) {
                    Entrant entrant = pair.first;
                    String status = pair.second;

                    sb.append(escapeCsv(entrant.getName())).append(",");
                    sb.append(escapeCsv(entrant.getEmail())).append(",");
                    sb.append(escapeCsv(status)).append("\n");
                }

                // Write and close
                outputStream.write(sb.toString().getBytes(StandardCharsets.UTF_8));
                outputStream.close();

                mainHandler.post(() ->
                        Toast.makeText(getContext(), "Saved successfully", Toast.LENGTH_LONG).show()
                );

            } catch (IOException e) {
                Log.e("CSV_SAVE", "Error writing CSV", e);
                mainHandler.post(() ->
                        Toast.makeText(getContext(), "Failed to save file", Toast.LENGTH_SHORT).show()
                );
            }
        });
    }

    private String escapeCsv(String data) {
        if (data == null) return "";
        String escaped = data.replace("\"", "\"\"");
        if (data.contains(",") || data.contains("\n") || data.contains("\"")) {
            return "\"" + escaped + "\"";
        }
        return data;
    }

    private void declineEntrant(Event event, Entrant entrant) {
        db.declineEntrant(event, entrant).addOnSuccessListener(success -> {
            if (success) {
                for (int i = 0; i < entrantArrayListAndStatuses.size(); i++) {
                    Entrant currentEntrant = entrantArrayListAndStatuses.get(i).first;
                    if (currentEntrant.getEmail().equals(entrant.getEmail())) {
                        entrantArrayListAndStatuses.set(i,
                                new Pair<>(entrant, "Declined"));
                        break;
                    }
                }
                if (adapter != null) {
                    adapter.notifyDataSetChanged();
                }
            } else {
                Toast.makeText(getContext(), "Failed to decline entrant",
                        Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(e -> {
            Log.e("Organizer_entrant_view", "Failed to decline entrant", e);
            Toast.makeText(getContext(), "Error: " + e.getMessage(),
                    Toast.LENGTH_SHORT).show();
        });
    }

    // 🔹 NEW: load entrant locations from Firestore and show markers on map
    private void setUpMapForEvent(View root, Event event) {
        SupportMapFragment mapFragment =
                (SupportMapFragment) getChildFragmentManager()
                        .findFragmentById(R.id.entrant_map_fragment);

        if (mapFragment == null) {
            Log.d("GeoDebug", "Map fragment not found in layout.");
            return;
        }

        mapFragment.getMapAsync(googleMap -> {
            db.getEntrantLocationsForEvent(event.getId())
                    .addOnSuccessListener(docs -> {
                        if (docs == null || docs.isEmpty()) {
                            Log.d("GeoDebug", "No entrant locations for this event.");
                            return;
                        }

                        LatLngBounds.Builder boundsBuilder = new LatLngBounds.Builder();
                        boolean hasAny = false;

                        for (DocumentSnapshot snap : docs) {
                            GeoPoint gp = snap.getGeoPoint("location");
                            if (gp == null) continue;

                            String email = snap.getString("userEmail");
                            LatLng pos = new LatLng(gp.getLatitude(), gp.getLongitude());

                            googleMap.addMarker(
                                    new MarkerOptions()
                                            .position(pos)
                                            .title(email != null ? email : "Entrant")
                            );

                            boundsBuilder.include(pos);
                            hasAny = true;
                        }

                        if (hasAny) {
                            LatLngBounds bounds = boundsBuilder.build();
                            googleMap.moveCamera(
                                    CameraUpdateFactory.newLatLngBounds(bounds, 100)
                            );
                        }
                    })
                    .addOnFailureListener(e -> Log.d("GeoDebug",
                            "Failed to load entrant locations: "
                                    + (e != null ? e.getMessage() : "unknown")));
        });
    }
}
