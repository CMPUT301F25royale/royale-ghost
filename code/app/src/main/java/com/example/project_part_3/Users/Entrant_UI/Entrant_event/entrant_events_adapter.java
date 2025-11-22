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


    private final List<Event> items = new ArrayList<>();
    private final String currentUserEmail;

    public entrant_events_adapter(String currentUserEmail) {
        this.currentUserEmail = currentUserEmail;
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

        View.OnClickListener openDetails = v -> {
            Intent i = new Intent(ctx, entrant_event_detail_activity.class);

            i.putExtra("eventId", e.getId());
            i.putExtra("viewerUserEmail", currentUserEmail);
            ctx.startActivity(i);
        };

        h.itemView.setOnClickListener(openDetails);
        h.btnPrimary.setOnClickListener(openDetails);

        h.btnSecondary.setOnClickListener(v -> {
            h.btnSecondary.setEnabled(false);
            h.btnSecondary.setText("Canceled");
        });
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
