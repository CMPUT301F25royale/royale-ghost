// File: com/example/project_part_3/Users/Admin_UI/Admin_search/Event_and_image_array_adapter.java

package com.example.project_part_3.Users.Admin_UI.Admin_search;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.project_part_3.Database_functions.EventDatabase;
import com.example.project_part_3.Database_functions.ImageDatabase;
import com.example.project_part_3.Events.Event;
import com.example.project_part_3.Image.Image_datamap;
import com.example.project_part_3.R;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;

public class Event_and_image_array_adapter extends ArrayAdapter<Object> {
    private final Context context;
    private final Admin_search_model viewModel; // Use ViewModel instead of direct DB
    private static final int TYPE_EVENT = 0;
    private static final int TYPE_IMAGE = 1;

    public Event_and_image_array_adapter(Context context, ArrayList<Object> items, Admin_search_model viewModel) {
        super(context, 0, items);
        this.context = context;
        this.viewModel = viewModel;
    }

    @Override
    public int getItemViewType(int position) {
        if (getItem(position) instanceof Event) {
            return TYPE_EVENT;
        } else {
            return TYPE_IMAGE;
        }
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // ViewHolder logic remains the same...
        int viewType = getItemViewType(position);
        Object item = getItem(position);

        if (viewType == TYPE_EVENT) {
            EventViewHolder eventHolder;
            if (convertView == null) {
                convertView = LayoutInflater.from(context).inflate(R.layout.admin_search_element, parent, false);
                eventHolder = new EventViewHolder();
                eventHolder.eventImage = convertView.findViewById(R.id.image_for_events);
                eventHolder.eventTitle = convertView.findViewById(R.id.admin_profiles_name);
                eventHolder.eventLocation = convertView.findViewById(R.id.admin_profiles_email);
                eventHolder.eventDate = convertView.findViewById(R.id.admin_date_search);
                eventHolder.eventAttendees = convertView.findViewById(R.id.number_of_attendees);
                eventHolder.eventDetail = convertView.findViewById(R.id.admin_search_detail_button);
                eventHolder.eventDelete = convertView.findViewById(R.id.admin_search_delete_event_button);
                convertView.setTag(eventHolder);
            } else { // TYPE_EVENT
                eventHolder = (EventViewHolder) convertView.getTag();
            }
            bindEventView(eventHolder, (Event) item);
        } else{
            ImageViewHolder imageHolder;
            if (convertView == null) {
                convertView = LayoutInflater.from(context).inflate(R.layout.admin_search_element, parent, false);
                imageHolder = new ImageViewHolder();
                imageHolder.imageView = convertView.findViewById(R.id.image_for_events);
                imageHolder.description = convertView.findViewById(R.id.admin_profiles_name);
                imageHolder.deleteButton = convertView.findViewById(R.id.admin_search_delete_event_button);
                convertView.setTag(imageHolder);
            } else { // TYPE_IMAGE
                imageHolder = (ImageViewHolder) convertView.getTag();
            }
            bindImageView(imageHolder, (Image_datamap) item);
        }

        if (viewType == TYPE_EVENT) {
            EventViewHolder holder = (EventViewHolder) convertView.getTag();
            bindEventView(holder, (Event) item);
        } else {
            ImageViewHolder holder = (ImageViewHolder) convertView.getTag();
            bindImageView(holder, (Image_datamap) item);
        }

        return convertView;
    }

    private void bindEventView(EventViewHolder holder, Event event) {
        if (event == null) return;
        holder.eventTitle.setText(event.getTitle());
        holder.eventLocation.setText(event.getLocation());

        if (event.getDate_close() != null) {
            SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
            holder.eventDate.setText("Closes: " + dateFormat.format(event.getDate_close()));
        }
        holder.eventAttendees.setText("Attendees: " + event.getConfirmedCount() + "/" + event.getCapacity());

        if (event.getImageInfo() != null && event.getImageInfo().getUrl() != null) {
            Glide.with(context)
                    .load(event.getImageInfo().getUrl())
                    .placeholder(android.R.drawable.ic_menu_report_image) // Use a consistent placeholder
                    .error(android.R.drawable.ic_menu_report_image)
                    .into(holder.eventImage);
        } else {
            Glide.with(context).clear(holder.eventImage);
            holder.eventImage.setImageResource(android.R.drawable.ic_menu_report_image); // Set a default
        }

        holder.eventDetail.setOnClickListener(v -> {
            Intent i = new Intent(context, Admin_event_detail_activity.class);
            i.putExtra("eventId", event.getId());
            context.startActivity(i);
        });

        // Use the ViewModel for the delete operation
        holder.eventDelete.setOnClickListener(v -> {
            viewModel.deleteEvent(event, new EventDatabase.OnEventDeleteListener() {
                @Override
                public void onSuccess() {
                    Toast.makeText(context, event.getTitle() + " deleted", Toast.LENGTH_SHORT).show();
                    // No need to call remove() or notifyDataSetChanged(). LiveData will do it for us.
                }

                @Override
                public void onFailure(String errorMessage) {
                    Toast.makeText(context, "Error: " + errorMessage, Toast.LENGTH_SHORT).show();
                    Log.e("Adapter", "Failed to delete event: " + errorMessage);
                }
            });
        });
    }

    private void bindImageView(ImageViewHolder holder, Image_datamap image) {
        if (image == null) return;
        holder.description.setText(image.getDescription());
        if (image.getUrl() != null && !image.getUrl().isEmpty()) {
            Glide.with(context)
                    .load(image.getUrl())
                    .placeholder(android.R.drawable.ic_menu_report_image)
                    .error(android.R.drawable.ic_menu_report_image)
                    .into(holder.imageView);
        } else {
            Glide.with(context).clear(holder.imageView);
            holder.imageView.setImageResource(android.R.drawable.ic_menu_report_image);
        }

        holder.deleteButton.setOnClickListener(v -> {
            viewModel.deleteImage(image, new ImageDatabase.OnImageDeleteListener() {
                @Override
                public void onSuccess() {
                    Toast.makeText(context, "Image deleted", Toast.LENGTH_SHORT).show();
                    }

                @Override
                public void onFailure(String errorMessage) {
                    Toast.makeText(context, "Failed to delete image: " + errorMessage, Toast.LENGTH_LONG).show();
                    Log.e("Adapter", "Failed to delete image: " + errorMessage);
                }
            });});
    }

    private static class EventViewHolder {
        ImageView eventImage;
        TextView eventTitle, eventLocation, eventDate, eventAttendees;
        Button eventDetail, eventDelete;
    }

    private static class ImageViewHolder {
        ImageView imageView;
        TextView description;
        Button deleteButton;
    }
}
