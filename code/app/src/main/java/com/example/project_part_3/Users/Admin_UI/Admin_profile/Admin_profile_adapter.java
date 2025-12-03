package com.example.project_part_3.Users.Admin_UI.Admin_profile;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.project_part_3.R;
import com.example.project_part_3.Users.User;

import java.util.ArrayList;

/**
 * Adapter for displaying a list of User profiles, including their name,
 * email, and profile picture.
 */
public class Admin_profile_adapter extends ArrayAdapter<User> {

    private final int resourceLayout;

    /**
     * Creates a new profile adapter for displaying User objects.
     *
     * @param context  the current context
     * @param resource the layout resource used for each list item
     * @param users    the list of User objects to display
     */
    public Admin_profile_adapter(Context context, int resource, ArrayList<User> users) {
        super(context, 0, users);
        this.resourceLayout = resource;
    }

    /**
     * Returns a populated view for a given position in the user list.
     *
     * @param position     the index of the item in the list
     * @param convertView  an existing view to reuse if possible
     * @param parent       the parent view group
     * @return the populated view for the list item
     */
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        ViewHolder holder;

        if (view == null) {
            view = LayoutInflater.from(getContext()).inflate(resourceLayout, parent, false);
            holder = new ViewHolder();
            holder.nameTextView = view.findViewById(R.id.admin_profiles_name);
            holder.emailTextView = view.findViewById(R.id.admin_profiles_email);
            holder.imageView = view.findViewById(R.id.imageView);
            view.setTag(holder);
        } else {
            holder = (ViewHolder) view.getTag();
        }

        User profile = getItem(position);

        if (profile != null) {
            holder.nameTextView.setText(profile.getName());
            holder.emailTextView.setText(profile.getEmail());

            if (profile.getImageInfo() != null && profile.getImageInfo().getUrl() != null) {
                Glide.with(getContext())
                        .load(profile.getImageInfo().getUrl())
                        .placeholder(R.drawable.ic_person)
                        .error(R.drawable.ic_person)
                        .into(holder.imageView);
            } else {
                holder.imageView.setImageResource(R.drawable.ic_person);
            }
        }

        return view;
    }

    /**
     * ViewHolder class used for efficient view recycling.
     */
    private static class ViewHolder {
        TextView nameTextView;
        TextView emailTextView;
        ImageView imageView;
    }
}
