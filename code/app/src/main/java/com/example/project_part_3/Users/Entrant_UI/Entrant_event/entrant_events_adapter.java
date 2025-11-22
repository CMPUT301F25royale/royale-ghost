package com.example.project_part_3.Users.Entrant_UI.Entrant_event;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.project_part_3.Events.Event;
import com.example.project_part_3.R;

import java.util.ArrayList;
import java.util.List;
public class entrant_events_adapter extends RecyclerView.Adapter<entrant_events_adapter.VH> {

    public enum Mode { MY_EVENTS, SEARCH } // Modes to determine which UI elements to show


    private final List<Event> items = new ArrayList<>();
    private final String currentUserEmail;
    private final Mode mode;

    public entrant_events_adapter(List<Event> initial,
                                  String currentUserEmail,
                                  Mode mode) {
        if (initial != null) items.addAll(initial);
        this.currentUserEmail = currentUserEmail;
        this.mode = mode;
    }

    /**
     * Updates the adapter's data list and notifies the RecyclerView of the change.
     * This method will be called by the LiveData observer in the Fragment.
     * @param newItems The new list of events to display.
     */
    public void setData(List<Event> newItems) {
        items.clear();
        if (newItems != null) {
            items.addAll(newItems);
        }
        notifyDataSetChanged();
    }

    @NonNull @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.entrant_main_item_event, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        Event e = items.get(position);
        Context ctx = h.itemView.getContext();

        h.title.setText(e.getTitle() != null ? e.getTitle() : "—");

        Bitmap bmp = e.getPoster();
        if (bmp != null) {
            h.img.setImageBitmap(bmp);
        } else {
            h.img.setImageResource(android.R.drawable.ic_menu_report_image);
        }

        // View details
        View.OnClickListener openDetails = v -> {
            Intent i = new Intent(ctx, entrant_event_detail_activity.class);
            i.putExtra("title", e.getTitle());
            i.putExtra("organizerName", e.getOrganizer() != null ? e.getOrganizer().getName() : "");
            i.putExtra("viewerUserEmail", currentUserEmail);
            i.putExtra("eventId", e.getId());
            ctx.startActivity(i);
        };
        h.itemView.setOnClickListener(openDetails);
        h.btnPrimary.setOnClickListener(openDetails);

        // Cancel button visibility depends on mode
        if (mode == Mode.MY_EVENTS) {
            h.btnSecondary.setVisibility(View.VISIBLE);
            h.btnSecondary.setText("Cancel");
            h.btnSecondary.setOnClickListener(v -> {
                // TODO: call DB to remove this user from the event if they cancel
                h.btnSecondary.setEnabled(false);
                h.btnSecondary.setText("Canceled");
            });
        } else {
            // SEARCH mode: hide the cancel button since it wouldnt make sense to have it here
            h.btnSecondary.setVisibility(View.GONE);
            h.btnSecondary.setOnClickListener(null);
        }
    }

    public void submitList(List<Event> newItems) {
        items.clear();
        if (newItems != null) items.addAll(newItems);
        notifyDataSetChanged();
    }

    @Override public int getItemCount() {
        return items.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        ImageView img;
        TextView title;
        Button btnPrimary, btnSecondary;
        VH(@NonNull View itemView) {
            super(itemView);
            img = itemView.findViewById(R.id.img);
            title = itemView.findViewById(R.id.title);
            btnPrimary = itemView.findViewById(R.id.btnPrimary);
            btnSecondary = itemView.findViewById(R.id.btnSecondary);
        }
    }
}
