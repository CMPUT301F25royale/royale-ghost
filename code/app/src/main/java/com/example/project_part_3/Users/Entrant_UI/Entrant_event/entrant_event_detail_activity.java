package com.example.project_part_3.Users.Entrant_UI.Entrant_event;

import android.os.Bundle;
import android.view.View;
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

public class entrant_event_detail_activity extends AppCompatActivity {

    private ImageView poster;
    private TextView title, organizer, locationName, locationAddress, regWindow, startEnd, price, description;
    private TextView vCapacity, vConfirmed, vRemaining, vWaitlist, vSelected, vDeclined, vAlternates;
    private MaterialButton joinBtn, closeBtn;

    private final NumberFormat currencyFmt = NumberFormat.getCurrencyInstance();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.entrant_event_detail);

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

        if (closeBtn != null) {
            closeBtn.setOnClickListener(v -> finish());
        }

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


    private void bindEventToUI(Event event) {
        title.setText(nonEmpty(event.getTitle(), "—"));
        organizer.setText(nonEmpty(event.getOrganizerId(), nonEmpty(event.getOrganizerId(), "—")));

        // Poster
        if (event.getImageInfo() != null && event.getImageInfo().getUrl() != null) {
            Glide.with(this).load(event.getImageInfo().getUrl()).into(poster);
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
        boolean openNow = isRegistrationOpen(event, System.currentTimeMillis());
        if (!openNow || viewerUserEmail == null || viewerUserEmail.isEmpty()) {
            joinBtn.setVisibility(android.view.View.GONE);
            return;
        }

        joinBtn.setVisibility(android.view.View.VISIBLE);
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
                            joinBtn.setEnabled(false);
                            joinBtn.setText("Entering…");
                            db.addUserToWaitlistById(event.getId(), viewerUserEmail)
                                    .addOnSuccessListener(ignored -> {
                                        joinBtn.setText("Entered");
                                        Toast.makeText(this, "You’ve been added to the waitlist", Toast.LENGTH_SHORT).show();
                                        // Bump the waitlist count on screen... will add checks and such later.
                                        List<String> list = event.getWaitlistUserIds();
                                        db.addEventToUser(viewerUserEmail, event.getId());//adds to array holding each event User signed up for
                                        int newCount = (list == null ? 0 : list.size()) + 1;
                                        vWaitlist.setText(String.valueOf(newCount));
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
                .addOnFailureListener(e -> {
                    joinBtn.setVisibility(android.view.View.GONE);
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
}