package com.example.project_part_3.Users.Entrant_UI.Entrant_event;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.project_part_3.Database_functions.EventDatabase;
import com.example.project_part_3.Events.Event;
import com.example.project_part_3.R;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;

import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.util.Date;

public class entrant_event_detail_activity extends AppCompatActivity {

    private ImageView poster;
    private TextView title, organizer, locationName, locationAddress, regWindow, startEnd, price, description;
    private TextView vCapacity, vConfirmed, vRemaining, vWaitlist, vSelected, vDeclined, vAlternates;
    private MaterialButton joinBtn;

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

        // Intent has these args from the bundle sent from the adapter
        String eventTitle = getIntent().getStringExtra("title");
        String organizerName = getIntent().getStringExtra("organizerName");
        String viewerUserEmail = getIntent().getStringExtra("viewerUserEmail"); // I need this to identify user type

        // Lookup event
        Event event = EventDatabase.getInstance().getEvent(eventTitle,
                new com.example.project_part_3.Users.Organizer(organizerName, "", "", "Organizer"));
        if (event == null) {
            Toast.makeText(this, "Event not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        title.setText(event.getTitle());
        organizer.setText(organizerName != null ? organizerName : "—");

        if (event.getPoster() != null) poster.setImageBitmap(event.getPoster());
        else poster.setImageResource(android.R.drawable.ic_menu_report_image);

        // Fallback to address with no specific locaiton name
        String locName = null;
        locationName.setText(nonEmpty(locName, event.getLocation()));
        locationAddress.setText(nonEmpty(event.getLocation(), "—"));

        regWindow.setText("Register from " + fmtDate(event.getDate_open()) + " to " + fmtDate(event.getDate_close()));

        Date start = event.getTime();
        startEnd.setText(start != null ? fmtDate(start) : "—");

        float eventPrice = 0;
        NumberFormat nf = NumberFormat.getCurrencyInstance();
        if (event.getPrice() != null && event.getPrice() > 0) eventPrice = event.getPrice();

        price.setText(nf.format(eventPrice));

        int cap = event.getCapacity() != null ? event.getCapacity() : 0;
        vCapacity.setText(String.valueOf(cap));
        vConfirmed.setText(String.valueOf(event.getAttendees() != null ? event.getAttendees() : 0));
        vRemaining.setText(String.valueOf(Math.max(cap - (event.getAttendees() != null ? event.getAttendees() : 0), 0)));
        vWaitlist.setText("0");
        vSelected.setText("0");
        vDeclined.setText("0");
        vAlternates.setText("0");

        // Show Enter lottery button if registration is open
        boolean openNow = isRegistrationOpen(event, System.currentTimeMillis());
        if (openNow && viewerUserEmail != null && !viewerUserEmail.isEmpty()) {
            joinBtn.setVisibility(android.view.View.VISIBLE);
            joinBtn.setEnabled(true);
            joinBtn.setText("Enter lottery");
            joinBtn.setOnClickListener(v -> {
                // TODO: plug into your waitlist from the event methid
                joinBtn.setEnabled(false); // Dont be able to click it again
                joinBtn.setText("Entered");
                Toast.makeText(this, "You’ve been added to the waitlist", Toast.LENGTH_SHORT).show();
                // TODO: Add to firestore... im not sure how we do this.
            });
        } else {
            joinBtn.setVisibility(android.view.View.GONE);
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
