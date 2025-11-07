package com.example.project_part_3.Users.Admin_UI.Admin_search;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.project_part_3.Database_functions.EventDatabase;
import com.example.project_part_3.Database_functions.ImageDatabase;
import com.example.project_part_3.Events.Event;
import com.example.project_part_3.Image.Image_holder;
import com.example.project_part_3.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;

public class Event_and_image_array_adapter extends ArrayAdapter<Object> {
    private Context context;
    private static final int TYPE_EVENT = 0;
    private static final int TYPE_IMAGE = 1;

    public Event_and_image_array_adapter(Context context, ArrayList<Object> items) {
        super(context, 0, items); // Use 0 for resource since we inflate manually
        this.context = context;
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
            } else {
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
            Event event = (Event) item;
            bindEventView(holder, event);
        } else {
            ImageViewHolder holder = (ImageViewHolder) convertView.getTag();
            Image_holder image = (Image_holder) item;
            bindImageView(holder, image);
        }

        return convertView;
    }

    private void bindEventView(EventViewHolder holder, Event event) {
        if (event != null) {
            holder.eventTitle.setText(event.getTitle());
            holder.eventLocation.setText(event.getLocation());

            SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
            String closeDate = dateFormat.format(event.getDate_close());
            holder.eventDate.setText("Closes: " + closeDate);

            holder.eventAttendees.setText("Attendees: " + event.getAttendees() + "/" + event.getCapacity() + "");

            if (event.getPoster() != null) {
                holder.eventImage.setImageBitmap(event.getPoster());
            } else {
                holder.eventImage.setImageResource(android.R.drawable.ic_menu_myplaces);
            }

            holder.eventDetail.setOnClickListener(v -> {
                Intent i = new Intent(context, com.example.project_part_3.Users.Admin_UI.Admin_search.Admin_event_detail_activity.class);
                i.putExtra("title", event.getTitle());
                i.putExtra("organizerName", event.getOrganizer() != null ? event.getOrganizer().getName() : "");
                context.startActivity(i);
            });
            holder.eventDelete.setOnClickListener(v -> {
                if (EventDatabase.getInstance().removeEvent(event.getTitle(), event.getOrganizer().getName())) {
                    remove(event);
                    notifyDataSetChanged();
                    Toast.makeText(context, event.getTitle() + " deleted", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(context, "Failed to delete event", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void bindImageView(ImageViewHolder holder, Image_holder image) {
        if (image != null) {
            holder.imageView.setImageBitmap(image.getImage());
            holder.description.setText(image.getDescription());

            holder.deleteButton.setOnClickListener(v -> {
                if (ImageDatabase.getInstance().removeImage(image)) {
                    remove(image);
                    notifyDataSetChanged();
                    Toast.makeText(context, "Image deleted", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(context, "Failed to delete image", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private static class EventViewHolder {
        ImageView eventImage;
        TextView eventTitle;
        TextView eventLocation;
        TextView eventDate;
        TextView eventAttendees;
        Button eventDetail;
        Button eventDelete;
    }
    private static class ImageViewHolder {
        ImageView imageView;
        TextView description;
        Button deleteButton;
    }
}
