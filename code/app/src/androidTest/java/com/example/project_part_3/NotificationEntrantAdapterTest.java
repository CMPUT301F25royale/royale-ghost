package com.example.project_part_3.Notification;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.project_part_3.R;
import com.google.firebase.Timestamp;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

@RunWith(AndroidJUnit4.class)
public class NotificationEntrantAdapterTest {

    @Test
    public void adapterBindsDataToRowViews() {
        Context context = ApplicationProvider.getApplicationContext();

        // Arrange: create one notification
        String title = "Results: Cool Event";
        String email = "user@example.com";
        String message = "You were selected!";
        Timestamp ts = Timestamp.now();
        String eventId = "event123";
        String eventTitle = "Cool Event";
        String type = "lottery_won";

        Notification_Entrant notif = new Notification_Entrant(
                title,
                email,
                message,
                ts,
                eventId,
                eventTitle,
                type
        );

        List<Notification_Entrant> list = new ArrayList<>();
        list.add(notif);

        Notification_entrant_adapter adapter = new Notification_entrant_adapter(context, list);

        // Ask adapter for a row view
        ViewGroup parent = new FrameLayout(context);
        View row = adapter.getView(0, null, parent);

        // Check that row's text views show the expected content
        TextView titleView = row.findViewById(R.id.organizer_notification_event_title);
        TextView messageView = row.findViewById(R.id.organizer_notifications_event_message);
        TextView dateView = row.findViewById(R.id.organizer_notifications_date_sent);

        assertNotNull(titleView);
        assertNotNull(messageView);
        assertNotNull(dateView);

        assertEquals("Event title text must match", eventTitle, titleView.getText().toString());
        assertEquals("Message text must match", message, messageView.getText().toString());
        assertFalse("Date text should not be empty", dateView.getText().toString().isEmpty());
    }

    @Test
    public void adapterCountAndItemsAreCorrect() {
        Context context = ApplicationProvider.getApplicationContext();

        List<Notification_Entrant> list = new ArrayList<>();
        list.add(new Notification_Entrant());
        list.add(new Notification_Entrant());

        Notification_entrant_adapter adapter = new Notification_entrant_adapter(context, list);

        assertEquals(2, adapter.getCount());
        assertSame(list.get(0), adapter.getItem(0));
        assertSame(list.get(1), adapter.getItem(1));
    }
}
