package com.example.project_part_3.Users.Admin_UI.Admin_search;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.project_part_3.Database_functions.EventDatabase;
import com.example.project_part_3.Events.Event;
import com.example.project_part_3.R;

import java.text.DateFormat;
import java.util.Date;

public class Admin_event_detail_activity extends AppCompatActivity {

    private ImageView poster;
    private TextView title;
    private TextView organizer;
    private TextView locationName;
    private TextView locationAddressOrLegacy;
    private TextView regWindow;
    private TextView startEnd;
    private TextView price;
    private TextView capacity;
    private TextView confirmed;
    private TextView remaining;
    private TextView waitlist;
    private TextView selected;
    private TextView declined;
    private TextView alternates;
    private TextView description;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_event_detail);

        poster = findViewById(R.id.detail_poster);
        title = findViewById(R.id.detail_title);
        organizer = findViewById(R.id.detail_organizer);
        locationName = findViewById(R.id.detail_location_name);
        locationAddressOrLegacy = findViewById(R.id.detail_location_address);
        regWindow = findViewById(R.id.detail_reg_window);
        startEnd = findViewById(R.id.detail_start_end);
        price = findViewById(R.id.detail_price);
        TextView capacity  = findViewById(R.id.value_capacity);
        TextView confirmed = findViewById(R.id.value_confirmed);
        TextView remaining = findViewById(R.id.value_remaining);
        TextView waitlist  = findViewById(R.id.value_waitlist);
        TextView selected  = findViewById(R.id.value_selected);
        TextView declined  = findViewById(R.id.value_declined);
        TextView alternates= findViewById(R.id.value_alternates);

        description = findViewById(R.id.detail_description);
        findViewById(R.id.btn_close).setOnClickListener(v -> finish());

        String t = getIntent().getStringExtra("title");
        String organizerName = getIntent().getStringExtra("organizerName");

        Event event = EventDatabase.getInstance().getEvent(t, organizerName);
        if (event == null) {
            title.setText("Event not found");
            return;
        }

        // Title & organizer
        title.setText(event.getTitle());
        organizer.setText(organizerName != null ? organizerName : "-"); // - if not set

        // Poster
        if (event.getPoster() != null) {
            poster.setImageBitmap(event.getPoster());
        } else {
            // In case we opt for the URL approach (which we probably wont) with default icon
            poster.setImageResource(android.R.drawable.ic_menu_report_image);
        }

        // Location
        String locName = event.getLocationName();
        String locAddr = event.getLocation();
        locationName.setText(nonEmpty(locName, event.getLocation())); // Fallback to address if no name
        locationAddressOrLegacy.setText(nonEmpty(locAddr, "-"));

        // Registration window
        String reg = "Register from " + fmtDate(event.getDate_open()) + " to " + fmtDate(event.getDate_close());
        regWindow.setText(reg);

        // If start is not set, just display the current time.
        Date start = event.getEventStartAt() != null ? event.getEventStartAt() : event.getTime();
        Date end = event.getEventEndAt();
        startEnd.setText(
                (start != null ? fmtDateTime(start) : "—")
                        + (end != null ? " to " + fmtDateTime(end) : "")
        );

        // Price
        float p = event.getPrice();
        price.setText(p > 0 ? Float.toString(p) : "Free");

        // Capacity & counts
        int cap = event.getCapacity() != null ? event.getCapacity() : 0;
        capacity.setText(String.valueOf(cap));
        confirmed.setText(String.valueOf(event.getConfirmedCount()));
        remaining.setText(String.valueOf(event.getRemainingCapacity()));

        // Lottery lists (may be null if not used)
        waitlist.setText(String.valueOf(sizeSafe(event.getWaitlistUserIds())));
        selected.setText(String.valueOf(sizeSafe(event.getSelectedUserIds())));
        declined.setText(String.valueOf(sizeSafe(event.getDeclinedUserIds())));
        alternates.setText(String.valueOf(sizeSafe(event.getAlternatesUserIds())));

        // Description
        description.setText(nonEmpty(event.getDescription(), "—"));
    }

    private String nonEmpty(String s, String fallback) {
        return (s != null && !s.trim().isEmpty()) ? s : fallback;
    }

    private String fmtDate(Date d) {
        if (d == null) return "—";
        DateFormat df = DateFormat.getDateInstance(DateFormat.MEDIUM);
        return df.format(d);
    }

    private String fmtDateTime(Date d) {
        if (d == null) return "—";
        DateFormat df = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT);
        return df.format(d);
    }

    private int sizeSafe(java.util.List<?> list) {
        return list == null ? 0 : list.size();
    }

}
