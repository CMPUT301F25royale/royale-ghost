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
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.project_part_3.Database_functions.Database;
import com.example.project_part_3.Events.Event;
import com.example.project_part_3.R;
import com.google.firebase.firestore.FirebaseFirestore;

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

        if (e.getPosterImageUrl() != null && !e.getPosterImageUrl().isEmpty()) {
            Glide.with(ctx)
                    .load(e.getPosterImageUrl())
                    .placeholder(android.R.drawable.sym_def_app_icon)
                    .error(android.R.drawable.sym_def_app_icon)
                    .into(h.img);
        } else {
            Glide.with(ctx).clear(h.img);
            h.img.setImageResource(android.R.drawable.sym_def_app_icon);
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
            h.btnSecondary.setEnabled(true);
            h.btnSecondary.setText("Cancel");

            h.btnSecondary.setOnClickListener(v -> {
                if (e.getId() == null || e.getId().isEmpty() || currentUserEmail == null || currentUserEmail.isEmpty()) {
                    Toast.makeText(ctx, "Missing event/user info", Toast.LENGTH_SHORT).show();
                    return;
                }

                h.btnSecondary.setEnabled(false);
                h.btnSecondary.setText("Removing...");

                Database db = new Database(FirebaseFirestore.getInstance());
                db.removeUserFromWaitlist(e.getId(), currentUserEmail)
                        .addOnSuccessListener(ignored -> {
                            Toast.makeText(ctx, "Removed from waitlist", Toast.LENGTH_SHORT).show();

                            // Remove this item from the list immediately
                            int idx = h.getBindingAdapterPosition();
                            if (idx != RecyclerView.NO_POSITION) {
                                items.remove(idx);
                                notifyItemRemoved(idx);
                            }
                        })
                        .addOnFailureListener(err -> {
                            h.btnSecondary.setEnabled(true);
                            h.btnSecondary.setText("Cancel");
                            Toast.makeText(ctx, "Failed to remove: " +
                                    (err != null ? err.getMessage() : "unknown error"), Toast.LENGTH_LONG).show();
                        });
            });

        } else {
            h.btnSecondary.setVisibility(View.GONE);
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
