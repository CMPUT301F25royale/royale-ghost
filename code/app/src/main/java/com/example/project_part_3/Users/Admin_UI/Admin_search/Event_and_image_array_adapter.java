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
import com.example.project_part_3.Database_functions.EventDatabase;
import com.example.project_part_3.Events.Event;
import com.example.project_part_3.Image.Image_holder;
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

        if (convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(context);
            if (viewType == TYPE_EVENT) {
                convertView = inflater.inflate(R.layout.admin_search_element, parent, false);
                EventViewHolder eventHolder = new EventViewHolder();
                eventHolder.eventImage = convertView.findViewById(R.id.image_for_events);
                eventHolder.eventTitle = convertView.findViewById(R.id.admin_profiles_name);
                eventHolder.eventLocation = convertView.findViewById(R.id.admin_profiles_email);
                eventHolder.eventDate = convertView.findViewById(R.id.admin_date_search);
                eventHolder.eventAttendees = convertView.findViewById(R.id.number_of_attendees);
                eventHolder.eventDetail = convertView.findViewById(R.id.admin_search_detail_button);
                eventHolder.eventDelete = convertView.findViewById(R.id.admin_search_delete_event_button);
                convertView.setTag(eventHolder);
            } else { // TYPE_IMAGE
                convertView = inflater.inflate(R.layout.admin_search_image, parent, false);
                ImageViewHolder imageHolder = new ImageViewHolder();
                imageHolder.imageView = convertView.findViewById(R.id.image_general);
                imageHolder.description = convertView.findViewById(R.id.image_description);
                imageHolder.deleteButton = convertView.findViewById(R.id.image_delete_button);
                convertView.setTag(imageHolder);
            }
        }

        if (viewType == TYPE_EVENT) {
            EventViewHolder holder = (EventViewHolder) convertView.getTag();
            bindEventView(holder, (Event) item);
        } else {
            ImageViewHolder holder = (ImageViewHolder) convertView.getTag();
            bindImageView(holder, (Image_holder) item);
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

        if (event.getPoster() != null) {
            holder.eventImage.setImageBitmap(event.getPoster());
        } else {
            holder.eventImage.setImageResource(android.R.drawable.ic_menu_myplaces);
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

    private void bindImageView(ImageViewHolder holder, Image_holder image) {
        if (image == null) return;
        holder.imageView.setImageBitmap(null);//@TODO: add image
        holder.description.setText(image.getDescription());

        holder.deleteButton.setOnClickListener(v -> {
            viewModel.deleteImage(image);
            Toast.makeText(context, "Image deleted", Toast.LENGTH_SHORT).show();
        });
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
