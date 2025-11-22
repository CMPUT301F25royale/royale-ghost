package com.example.project_part_3.Users.Entrant_UI.Entrant_event;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

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

public class entrant_event_detail_activity extends AppCompatActivity {

    private ImageView poster;
    private TextView title, organizer, locationName, locationAddress, regWindow, startEnd, price, description;
    private TextView vCapacity, vConfirmed, vRemaining, vWaitlist, vSelected, vDeclined, vAlternates;
    private MaterialButton joinBtn;

    private String eventId; // Firestore event id
    private String viewerUserEmail; // Email passed from adapter

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.entrant_event_detail);

        // Toolbar back
        MaterialToolbar tb = findViewById(R.id.toolbar);
        if (tb != null) {
            tb.setNavigationOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());
        }

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

        // Intent args are set by adapter - theres a better way to do this but it works for now (talk to Avi)
        eventId = getIntent().getStringExtra("eventId");
        viewerUserEmail = getIntent().getStringExtra("viewerUserEmail");

        if (eventId == null || eventId.isEmpty()) {
            Toast.makeText(this, "Missing event id", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Load from Firestore
        Database db = new Database(FirebaseFirestore.getInstance());
        db.fetchEvent(eventId)
                .addOnSuccessListener(event -> {
                    if (event == null) {
                        Toast.makeText(this, "Event not found", Toast.LENGTH_SHORT).show();
                        finish();
                        return;
                    }
                    bindEventToUI(event);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to load event: " +
                            (e != null ? e.getMessage() : "unknown"), Toast.LENGTH_LONG).show();
                    finish();
                });
    }

    /** Binds an Event (loaded from Firestore) to the UI and wires the “Enter lottery” button. */
    private void bindEventToUI(Event event) {
        // Title / organizer
        title.setText(nonEmpty(event.getTitle(), "—"));
        String orgName = (event.getOrganizer() != null) ? event.getOrganizer().getName() : null;
        organizer.setText(nonEmpty(orgName, "—"));

        // Poster (assuming bitmap? IDK how we are approaching this)
        if (event.getPoster() != null) {
            poster.setImageBitmap(event.getPoster());
        } else {
            poster.setImageResource(android.R.drawable.ic_menu_report_image);
        }

        // Location (fallback to address)
        String locName = null;
        locationName.setText(nonEmpty(locName, event.getLocation()));
        locationAddress.setText(nonEmpty(event.getLocation(), "—"));

        // Registration window
        regWindow.setText("Register from " + fmtDate(event.getDate_open()) +
                " to " + fmtDate(event.getDate_close()));

        // Start / end
        Date start = event.getTime();
        startEnd.setText(start != null ? fmtDate(start) : "—");

        // Price
        NumberFormat nf = NumberFormat.getCurrencyInstance();
        float dollars = (event.getPrice() != null) ? event.getPrice() : 0;
        price.setText(nf.format(dollars));

        // Capacity & basic counts
        int cap = (event.getCapacity() != null) ? event.getCapacity() : 0;
        int attending = (event.getAttendees() != null) ? event.getAttendees() : 0;
        vCapacity.setText(String.valueOf(cap));
        vConfirmed.setText(String.valueOf(attending));
        vRemaining.setText(String.valueOf(Math.max(cap - attending, 0)));

        // Ill wire these later once lottery works for now just show 0
        vWaitlist.setText("0");
        vSelected.setText("0");
        vDeclined.setText("0");
        vAlternates.setText("0");

        // “Enter lottery” visibility + action
        boolean openNow = isRegistrationOpen(event, System.currentTimeMillis());
        if (openNow && viewerUserEmail != null && !viewerUserEmail.isEmpty()) {
            joinBtn.setVisibility(View.VISIBLE);
            joinBtn.setEnabled(true);
            joinBtn.setText("Enter lottery");
            joinBtn.setOnClickListener(v -> {
                // Add this entrant email to the event’s waitlist in Firestore
                new Database(FirebaseFirestore.getInstance()).addEntrant(eventId, viewerUserEmail);
                joinBtn.setEnabled(false);
                joinBtn.setText("Entered");
                Toast.makeText(this, "You’ve been added to the waitlist", Toast.LENGTH_SHORT).show();
            });
        } else {
            joinBtn.setVisibility(View.GONE);
        }
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
