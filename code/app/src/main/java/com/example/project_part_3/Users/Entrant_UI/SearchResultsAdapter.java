package com.example.project_part_3.Users.Entrant_UI;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.project_part_3.Events.Event;
import com.example.project_part_3.R;

import java.util.List;

public class SearchResultsAdapter extends RecyclerView.Adapter<SearchResultsAdapter.VH> {
    public interface OnClick { void onEvent(String eventId); }

    private final List<Event> data;
    private final OnClick onClick;

    public SearchResultsAdapter(List<Event> data, OnClick onClick) {
        this.data = data;
        this.onClick = onClick;
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView title;
        Button showDetails;
        VH(View v) {
            super(v);
            title = v.findViewById(R.id.title);          // from entrant_search_item_event.xml
            showDetails = v.findViewById(R.id.ShowDetails);
        }
    }

    @NonNull
    @Override public VH onCreateViewHolder(@NonNull ViewGroup p, int vt) {
        View v = LayoutInflater.from(p.getContext())
                .inflate(R.layout.entrant_search_item_event, p, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int pos) {
        Event e = data.get(pos);
        h.title.setText(e.getTitle());

        // Prefer real ID if present; fall back to something stable
        String id = (e.getEventId() != null) ? e.getEventId() : String.valueOf(pos);

        h.itemView.setOnClickListener(v -> onClick.onEvent(id));
        h.showDetails.setOnClickListener(v -> onClick.onEvent(id));
    }

    @Override public int getItemCount() { return data.size(); }
}
