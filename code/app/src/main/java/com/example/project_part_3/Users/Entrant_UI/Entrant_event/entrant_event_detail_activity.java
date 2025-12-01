package com.example.project_part_3.Users.Entrant_UI.Entrant_event;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.project_part_3.Database_functions.Database;
import com.example.project_part_3.Events.Event;
import com.example.project_part_3.R;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.firestore.FirebaseFirestore;

import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.util.Date;
import java.util.List;


import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import android.location.Location;
import androidx.core.content.ContextCompat;
import androidx.core.app.ActivityCompat;
import android.content.pm.PackageManager;

/**
 * The activity for an entrant to view the details of an event.
 */
public class entrant_event_detail_activity extends AppCompatActivity {

    private FusedLocationProviderClient fusedLocationClient;
    private static final int REQ_LOCATION = 1001;
    private Event currentEvent;
    private String currentViewerEmail;

    private ImageView poster;
    private TextView title, organizer, locationName, locationAddress, regWindow, startEnd, price, description;
    private TextView vCapacity, vConfirmed, vRemaining, vWaitlist, vSelected, vDeclined, vAlternates;
    private MaterialButton joinBtn, closeBtn;
    private MaterialButton acceptBtn, declineBtn;

    private final NumberFormat currencyFmt = NumberFormat.getCurrencyInstance();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.entrant_event_detail);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        MaterialToolbar tb = findViewById(R.id.toolbar);
        tb.setNavigationOnClickListener(v -> finish());

        poster = findViewById(R.id.detail_poster);
        title = findViewById(R.id.detail_title);
        organizer = findViewById(R.id.detail_organizer);
        locationName = findViewById(R.id.detail_location_name);
        locationAddress = findViewById(R.id.detail_location_address);
        regWindow = findViewById(R.id.detail_reg_window);
        startEnd = findViewById(R.id.detail_start_end);
        price = findViewById(R.id.detail_price);
        description = findViewById(R.id.detail_description);
        vCapacity = findViewById(R.id.value_capacity);
        vConfirmed = findViewById(R.id.value_confirmed);
        vRemaining = findViewById(R.id.value_remaining);
        vWaitlist = findViewById(R.id.value_waitlist);
        vSelected = findViewById(R.id.value_selected);
        vDeclined = findViewById(R.id.value_declined);
        vAlternates = findViewById(R.id.value_alternates);
        joinBtn = findViewById(R.id.btn_join_lottery);
        closeBtn = findViewById(R.id.btn_close);
        acceptBtn = findViewById(R.id.btn_accept);
        declineBtn = findViewById(R.id.btn_decline);
        ImageButton infoBtn = findViewById(R.id.btn_event_info);

        if (closeBtn != null) {
            closeBtn.setOnClickListener(v -> finish());
        }

        infoBtn.setOnClickListener(v -> {
            new androidx.appcompat.app.AlertDialog.Builder(v.getContext())
                    .setTitle("Event Details")
                    .setMessage("Lotteries will be auto run when the registration date closes. Upon being selected by the lottery system, you will be prompted to accept or decline your spot. In the case that you are not selected, you may still have a chance to be selected if others decline.")
                    .setPositiveButton("OK", null)
                    .show();
        });

        Database db = new Database(FirebaseFirestore.getInstance());

        String eventId = getIntent().getStringExtra("eventId");
        String viewerUserEmail = getIntent().getStringExtra("viewerUserEmail");
        if (eventId == null || eventId.isEmpty()) {
            Toast.makeText(this, "Missing event id", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Load the event
        db.fetchEvent(eventId)
                .addOnSuccessListener(event -> {
                    if (event == null) {
                        Toast.makeText(this, "Event not found", Toast.LENGTH_SHORT).show();
                        finish();
                        return;
                    }
                    // Bind to UI
                    bindEventToUI(event);
                    setupJoinButton(db, event, viewerUserEmail);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to load event: " + (e != null ? e.getMessage() : "unknown"), Toast.LENGTH_LONG).show();
                    finish();
                });
    }


    /**
     * Binds an event to the UI.
     * @param event the event to bind to the UI
     */
    private void bindEventToUI(Event event) {
        title.setText(nonEmpty(event.getTitle(), "—"));
        organizer.setText(nonEmpty(event.getOrganizerId(), nonEmpty(event.getOrganizerId(), "—")));

        // Poster
        String imageUrl = event.getPosterImageUrl();

        if (imageUrl != null && !imageUrl.isEmpty()) {
            Glide.with(this)
                    .load(imageUrl)
                    .placeholder(android.R.drawable.ic_menu_report_image)
                    .error(android.R.drawable.ic_menu_report_image)
                    .into(poster);
        } else {
            poster.setImageResource(android.R.drawable.ic_menu_report_image);
        }

        // Location fields
        String locName = event.getLocationName();
        locationName.setText(nonEmpty(locName, nonEmpty(event.getLocation(), "—")));
        locationAddress.setText(nonEmpty(event.getLocation(), "—"));

        // Registration window
        regWindow.setText("Register from " + fmtDate(event.getDate_open()) + " to " + fmtDate(event.getDate_close()));

        // Start/End
        Date start = event.getTime();
        Timestamp endTs = null;
        startEnd.setText(
                (start != null ? fmtDate(start) : "—") +
                        (endTs != null ? (" to " + fmtDateTime(endTs)) : "")
        );

        // Price
        float eventPrice = (event.getPrice() != null && event.getPrice() > 0) ? event.getPrice() : 0f;
        price.setText(currencyFmt.format(eventPrice));

        // Capacity & counts
        int cap = event.getCapacity() != null ? event.getCapacity() : 0;
        int confirmed = event.getConfirmedUserIds() != null ? event.getConfirmedUserIds().size() : 0;
        int remaining = Math.max(cap - confirmed, 0);
        int waitlisted = event.getWaitlistUserIds() != null ? event.getWaitlistUserIds().size() : 0;
        int selected = event.getSelectedUserIds() != null ? event.getSelectedUserIds().size() : 0;
        int declined = event.getDeclinedUserIds() != null ? event.getDeclinedUserIds().size() : 0;
        int alternates = event.getAlternatesUserIds() != null ? event.getAlternatesUserIds().size() : 0;

        vCapacity.setText(String.valueOf(cap));
        vConfirmed.setText(String.valueOf(confirmed));
        vRemaining.setText(String.valueOf(remaining));
        vWaitlist.setText(String.valueOf(waitlisted));
        vSelected.setText(String.valueOf(selected));
        vDeclined.setText(String.valueOf(declined));
        vAlternates.setText(String.valueOf(alternates));

        description.setText(nonEmpty(event.getDescription(), "—"));
    }

    private void setupJoinButton(Database db, Event event, String viewerUserEmail) {
        // No user – nothing to interact with
        if (viewerUserEmail == null || viewerUserEmail.isEmpty()) {
            joinBtn.setVisibility(View.GONE);
            if (acceptBtn != null) acceptBtn.setVisibility(View.GONE);
            if (declineBtn != null) declineBtn.setVisibility(View.GONE);
            return;
        }

        long nowMs = System.currentTimeMillis();
        boolean openNow = isRegistrationOpen(event, nowMs);

        List<String> confirmed = event.getConfirmedUserIds();
        List<String> declined = event.getDeclinedUserIds();
        List<String> selected = event.getSelectedUserIds();
        List<String> alternates = event.getAlternatesUserIds();

        boolean isConfirmed = confirmed != null && confirmed.contains(viewerUserEmail);
        boolean isDeclined  = declined != null && declined.contains(viewerUserEmail);
        boolean isSelected  = selected != null && selected.contains(viewerUserEmail);
        boolean isAlternate = alternates != null && alternates.contains(viewerUserEmail);

        // Hide accept/decline by default
        acceptBtn.setVisibility(View.GONE);
        declineBtn.setVisibility(View.GONE);

        if (openNow) {
            // Registration still open → allow join / "Entered" as before
            joinBtn.setVisibility(View.VISIBLE);
            joinBtn.setEnabled(false);
            joinBtn.setText("Checking…");

            db.isUserOnWaitlistById(event.getId(), viewerUserEmail)
                    .addOnSuccessListener(isOnWaitlist -> {
                        if (isOnWaitlist) {
                            joinBtn.setText("Entered");
                            joinBtn.setEnabled(false);
                        } else {
                        joinBtn.setText("Enter lottery");
                        joinBtn.setEnabled(true);
                        joinBtn.setOnClickListener(v -> {
                            // 🔹 1. If event wants geolocation and we don't have permission yet, ask for it
                            if (event.getGeolocationEnabled() && !hasFineLocation()) {
                                ActivityCompat.requestPermissions(
                                        entrant_event_detail_activity.this,
                                        new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                                        REQ_LOCATION
                                );
                                // We STILL let them join immediately; logging will only work next time if they accept.
                            }

                            joinBtn.setEnabled(false);
                            joinBtn.setText("Entering…");

                            db.addUserToWaitlistById(event.getId(), viewerUserEmail)
                                    .addOnSuccessListener(ignored -> {
                                        joinBtn.setText("Entered");
                                        Toast.makeText(this, "You’ve been added to the waitlist", Toast.LENGTH_SHORT).show();
                                        List<String> list = event.getWaitlistUserIds();
                                        db.addEventToUser(viewerUserEmail, event.getId());
                                        int newCount = (list == null ? 0 : list.size()) + 1;
                                        vWaitlist.setText(String.valueOf(newCount));

                                        // 🔹 2. After join succeeds, try logging location (non-blocking)
                                        captureAndUploadLocation(db, event, viewerUserEmail);
                                    })
                                    .addOnFailureListener(e -> {
                                        joinBtn.setText("Enter lottery");
                                        joinBtn.setEnabled(true);
                                        Toast.makeText(this, "Failed to join: " +
                                                (e != null ? e.getMessage() : "unknown"), Toast.LENGTH_LONG).show();
                                    });
                                });
                        }
                    })
                    .addOnFailureListener(e -> joinBtn.setVisibility(View.GONE));

            return;
        }

        // Registration CLOSED: show status / accept/decline instead of join
        if (isConfirmed) {
            joinBtn.setVisibility(View.VISIBLE);
            joinBtn.setText("You’re confirmed");
            joinBtn.setEnabled(false);
            return;
        }

        if (isDeclined) {
            joinBtn.setVisibility(View.VISIBLE);
            joinBtn.setText("You declined");
            joinBtn.setEnabled(false);
            return;
        }

        if (isSelected) {
            // User won → show Accept / Decline buttons instead of join
            joinBtn.setVisibility(View.GONE);
            acceptBtn.setVisibility(View.VISIBLE);
            declineBtn.setVisibility(View.VISIBLE);

            setupAcceptDeclineHandlers(db, event, viewerUserEmail);
            return;
        }

        if (isAlternate) {
            joinBtn.setVisibility(View.VISIBLE);
            joinBtn.setText("You are an alternate");
            joinBtn.setEnabled(false);
            return;
        }

        // Not involved with this event
        joinBtn.setVisibility(View.GONE);
    }


    private void setupAcceptDeclineHandlers(Database db, Event event, String viewerUserEmail) {
        // Accept
        acceptBtn.setOnClickListener(v -> {
            acceptBtn.setEnabled(false);
            declineBtn.setEnabled(false);
            acceptBtn.setText("Accepting…");

            db.acceptEntrant(event, viewerUserEmail)
                    .addOnSuccessListener(ignored -> {
                        Toast.makeText(this, "You’ve confirmed your spot", Toast.LENGTH_SHORT).show();

                        // Update UI: show confirmed status instead
                        acceptBtn.setVisibility(View.GONE);
                        declineBtn.setVisibility(View.GONE);

                        joinBtn.setVisibility(View.VISIBLE);
                        joinBtn.setText("You’re confirmed");
                        joinBtn.setEnabled(false);

                        // Update counts on screen
                        try {
                            int confirmed = Integer.parseInt(vConfirmed.getText().toString());
                            int selected = Integer.parseInt(vSelected.getText().toString());
                            int remaining = Integer.parseInt(vRemaining.getText().toString());

                            vConfirmed.setText(String.valueOf(confirmed + 1));
                            vSelected.setText(String.valueOf(Math.max(selected - 1, 0)));
                            vRemaining.setText(String.valueOf(Math.max(remaining - 1, 0)));
                        } catch (NumberFormatException ignored2) {}
                    })
                    .addOnFailureListener(e -> {
                        acceptBtn.setEnabled(true);
                        declineBtn.setEnabled(true);
                        acceptBtn.setText("Accept");
                        Toast.makeText(this, "Failed to accept: " +
                                (e != null ? e.getMessage() : "unknown"), Toast.LENGTH_LONG).show();
                    });
        });

        // Decline
        declineBtn.setOnClickListener(v -> {
            acceptBtn.setEnabled(false);
            declineBtn.setEnabled(false);
            declineBtn.setText("Declining…");
                //accept and decline lottery don't exist in the database?
                db.declineLotterySelection(event.getId(), viewerUserEmail)
                        .addOnSuccessListener(ignored -> {
                            Toast.makeText(this, "You declined this spot", Toast.LENGTH_SHORT).show();

            // changed to decline entrant
            db.declineEntrant(event, viewerUserEmail)
                    .addOnSuccessListener(ignored -> {
                        Toast.makeText(this, "You declined this spot", Toast.LENGTH_SHORT).show();

                            joinBtn.setVisibility(View.VISIBLE);
                            joinBtn.setText("You declined");
                            joinBtn.setEnabled(false);

                            // Update counts on screen
                            try {
                                int declined = Integer.parseInt(vDeclined.getText().toString());
                                int selected = Integer.parseInt(vSelected.getText().toString());
                                vDeclined.setText(String.valueOf(declined + 1));
                                vSelected.setText(String.valueOf(Math.max(selected - 1, 0)));
                            } catch (NumberFormatException ignored2) {}
                        })
                        .addOnFailureListener(e -> {
                            acceptBtn.setEnabled(true);
                            declineBtn.setEnabled(true);
                            declineBtn.setText("Decline");
                            Toast.makeText(this, "Failed to decline: " +
                                    (e != null ? e.getMessage() : "unknown"), Toast.LENGTH_LONG).show();
                        
        });
    }

    private boolean isRegistrationOpen(Event e, long nowMs) {
        Date o = e.getDate_open(), c = e.getDate_close();
        return (o == null || nowMs >= o.getTime()) && (c == null || nowMs <= c.getTime());
    }

    private String nonEmpty(String s, String fb) {
        return (s != null && !s.trim().isEmpty()) ? s : fb;
    }

    private String fmtDate(Date d) {
        if (d == null) return "—";
        DateFormat df = DateFormat.getDateInstance(DateFormat.MEDIUM);
        return df.format(d);
    }

    private String fmtDateTime(Timestamp ts) {
        if (ts == null) return "—";
        DateFormat df = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT);
        return df.format(ts);
    }
    //permission helper
    private boolean hasFineLocation() {
        return ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED;
    }


    private void captureAndUploadLocation(Database db, Event event, String viewerUserEmail) {
        if (event == null || !event.getGeolocationEnabled()) {
            android.util.Log.d("GeoDebug", "Geo disabled for this event, skipping.");
            return;
        }

        if (!hasFineLocation()) {
            android.util.Log.d("GeoDebug", "No location permission, skipping capture.");
            return;
        }

        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(loc -> {
                    if (loc == null) {
                        android.util.Log.d("GeoDebug", "getLastLocation() returned null.");
                        return;
                    }

                    double lat = Math.round(loc.getLatitude() * 1000.0) / 1000.0;
                    double lng = Math.round(loc.getLongitude() * 1000.0) / 1000.0;

                    android.util.Log.d("GeoDebug", "Got location lat=" + lat + ", lng=" + lng);

                    db.saveUserLocationForEvent(event.getId(), viewerUserEmail, lat, lng)
                            .addOnSuccessListener(unused ->
                                    android.util.Log.d("GeoDebug", "Location saved to Firestore."))
                            .addOnFailureListener(e ->
                                    android.util.Log.d("GeoDebug", "Failed to save location: " +
                                            (e != null ? e.getMessage() : "unknown")));
                })
                .addOnFailureListener(e ->
                        android.util.Log.d("GeoDebug", "Location fetch failed: " +
                                (e != null ? e.getMessage() : "unknown")));
    }


}