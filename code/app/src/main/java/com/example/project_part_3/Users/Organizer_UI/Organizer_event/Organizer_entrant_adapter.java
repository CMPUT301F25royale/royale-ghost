package com.example.project_part_3.Users.Organizer_UI.Organizer_event;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;

import com.bumptech.glide.Glide;
import com.example.project_part_3.R;
import com.example.project_part_3.Users.Entrant;

import java.util.ArrayList;

public class Organizer_entrant_adapter extends ArrayAdapter<Pair<Entrant, String>> {

    private final int resourceLayout;
    private final OnEntrantClickListener listener;

    public interface OnEntrantClickListener {
        void onDeclineClick(Entrant entrant);
    }

    private static class ViewHolder {
        TextView nameTextView;
        TextView emailTextView;
        TextView statusTextView;
        ImageView profileImageView;
        ImageButton declineButton;
    }

    public Organizer_entrant_adapter(Context context, int resource,
                                     ArrayList<Pair<Entrant, String>> entrantsAndStatuses,
                                     OnEntrantClickListener listener) {
        super(context, resource, entrantsAndStatuses);
        this.resourceLayout = resource;
        this.listener = listener;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        View view = convertView;
        ViewHolder holder;

        if (view == null) {
            view = LayoutInflater.from(getContext()).inflate(resourceLayout, parent, false);
            holder = new ViewHolder();
            holder.nameTextView = view.findViewById(R.id.organizer_event_entrant_name);
            holder.emailTextView = view.findViewById(R.id.organizer_event_entrant_email);
            holder.statusTextView = view.findViewById(R.id.organizer_event_entrant_status);
            holder.profileImageView = view.findViewById(R.id.organizer_event_entrant_profile_image);
            holder.declineButton = view.findViewById(R.id.organizer_event_entrant_decline_button);

            view.setTag(holder);
        } else {
            holder = (ViewHolder) view.getTag();
        }

        Pair<Entrant, String> entrantAndStatus = getItem(position);

        if (entrantAndStatus == null) {
            return view;
        }

        Entrant entrant = entrantAndStatus.first;
        String status = entrantAndStatus.second;

        if (entrant != null && status != null) {
            holder.nameTextView.setText(entrant.getName());
            holder.emailTextView.setText(entrant.getEmail());

            // Handle Decline Button
            if (holder.declineButton != null) {
                // Only show decline button if status is Pending
                if ("Declined".equalsIgnoreCase(status)) {
                    holder.declineButton.setVisibility(View.GONE);
                } else {
                    holder.declineButton.setVisibility(View.VISIBLE);
                    holder.declineButton.setOnClickListener(v -> {
                        if (listener != null) listener.onDeclineClick(entrant);
                    });
                }
            }

            // Handle Profile Image
            if (entrant.getProfilePicUrl() != null) {
                Glide.with(getContext())
                        .load(entrant.getProfilePicUrl())
                        .placeholder(R.drawable.ic_profile)
                        .diskCacheStrategy(com.bumptech.glide.load.engine.DiskCacheStrategy.ALL)
                        .dontAnimate()
                        .into(holder.profileImageView);
            } else {
                // Use default profile image if URL is null
                holder.profileImageView.setImageResource(R.drawable.ic_profile);
            }


            // Handle Status
            updateStatusColor(holder.statusTextView, status);
        }

        return view;
    }

    private void updateStatusColor(TextView statusView, String status) {
        Context context = getContext();
        statusView.setText(status);

        int colorResId;
        if ("Accepted".equalsIgnoreCase(status)) {
            colorResId = R.color.green;
        } else if ("Declined".equalsIgnoreCase(status)) {
            colorResId = R.color.my_red;
        } else if ("Pending".equalsIgnoreCase(status)) {
            colorResId = R.color.yellow;
        } else {
            // Default color if unknown
            colorResId = android.R.color.darker_gray;
        }

        int color = ContextCompat.getColor(context, colorResId);
        ViewCompat.setBackgroundTintList(statusView, ColorStateList.valueOf(color));
    }
}