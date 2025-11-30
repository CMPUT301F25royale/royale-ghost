package com.example.project_part_3.Users.Admin_UI.Admin_search;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.example.project_part_3.Database_functions.EventDatabase;
import com.example.project_part_3.Events.Event;
import com.example.project_part_3.R;

import java.text.DateFormat;
import java.util.Date;
import java.util.List;

public class Admin_event_detail_activity extends AppCompatActivity {

    // No direct database access needed here anymore
    private Admin_search_model viewModel;
    private EventDatabase eventDb;

    // A LiveData object to hold the details of just this one event
    private final MutableLiveData<Event> eventDetails = new MutableLiveData<>();

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
        setContentView(R.layout.admin_event_detail);

        // Initialize ViewModel and high-level EventDatabase
        viewModel = new ViewModelProvider(this).get(Admin_search_model.class);
        eventDb = new EventDatabase();

        setupViews();
        findViewById(R.id.btn_close).setOnClickListener(v -> finish());

        String eventId = getIntent().getStringExtra("eventId");
        if (eventId == null || eventId.isEmpty()) {
            Toast.makeText(this, "Event ID not found.", Toast.LENGTH_LONG).show();
            title.setText("Event not found");
            finish();
            return;
        }

        // Start listening for changes to this specific event
        eventDb.listenForSingleEvent(eventId, eventDetails);

        // Observe the LiveData for this event and update the UI when it changes
        eventDetails.observe(this, event -> {
            if (event != null) {
                populateUI(event);
            } else {
                // This will be triggered if the event is deleted while being viewed
                Toast.makeText(this, "This event has been removed.", Toast.LENGTH_LONG).show();
                finish();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Clean up the listeners from EventDatabase when the activity is destroyed
        if (eventDb != null) {
            eventDb.cleanupListeners();
        }
    }

    private void setupViews() {
        poster = findViewById(R.id.detail_poster);
        title = findViewById(R.id.detail_title);
        organizer = findViewById(R.id.detail_organizer);
        locationName = findViewById(R.id.detail_location_name);
        locationAddressOrLegacy = findViewById(R.id.detail_location_address);
        regWindow = findViewById(R.id.detail_reg_window);
        startEnd = findViewById(R.id.detail_start_end);
        price = findViewById(R.id.detail_price);
        capacity  = findViewById(R.id.value_capacity);
        confirmed = findViewById(R.id.value_confirmed);
        remaining = findViewById(R.id.value_remaining);
        waitlist  = findViewById(R.id.value_waitlist);
        selected  = findViewById(R.id.value_selected);
        declined  = findViewById(R.id.value_declined);
        alternates= findViewById(R.id.value_alternates);
        description = findViewById(R.id.detail_description);
    }

    private void populateUI(Event event) {
        if (event == null) {
            title.setText("Event data is null");
            return;
        }

        title.setText(event.getTitle());
        organizer.setText(nonEmpty(event.getOrganizerId(), "—"));

        if (event.getImageInfo() != null && event.getImageInfo().getUrl() != null) {
            Glide.with(this).load(event.getImageInfo().getUrl()).into(poster);
        } else {
            poster.setImageResource(android.R.drawable.ic_menu_report_image);
        }

        locationName.setText(nonEmpty(event.getLocationName(), event.getLocation()));
        locationAddressOrLegacy.setText(nonEmpty(event.getLocation(), "—"));

        String reg = "Register from " + fmtDate(event.getDate_open()) + " to " + fmtDate(event.getDate_close());
        regWindow.setText(reg);

        Date start = event.getEventStartAt();
        Date end = event.getEventEndAt();
        startEnd.setText((start != null ? fmtDateTime(start) : "—") + (end != null ? " to " + fmtDateTime(end) : ""));

        Float p = event.getPrice();
        price.setText(p != null && p > 0 ? String.format("%.2f", p) : "Free");

        int cap = event.getCapacity() != null ? event.getCapacity() : 0;
        capacity.setText(String.valueOf(cap));
        confirmed.setText(String.valueOf(event.getConfirmedCount()));
        remaining.setText(String.valueOf(event.getRemainingCapacity()));

        waitlist.setText(String.valueOf(sizeSafe(event.getWaitlistUserIds())));
        // These fields are not in your Event class, but this shows how to handle them if added
        // selected.setText(String.valueOf(sizeSafe(event.getSelectedUserIds())));
        // declined.setText(String.valueOf(sizeSafe(event.getDeclinedUserIds())));
        // alternates.setText(String.valueOf(sizeSafe(event.getAlternatesUserIds())));

        description.setText(nonEmpty(event.getDescription(), "—"));
    }

    private String nonEmpty(String s, String fallback) {
        return (s != null && !s.trim().isEmpty()) ? s : fallback;
    }

    private String fmtDate(Date d) {
        if (d == null) return "—";
        return DateFormat.getDateInstance(DateFormat.MEDIUM).format(d);
    }

    private String fmtDateTime(Date d) {
        if (d == null) return "—";
        return DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT).format(d);
    }

    private int sizeSafe(List<?> list) {
        return list == null ? 0 : list.size();
    }
}
