package com.example.project_part_3.Users.Admin_UI.Admin_profile;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.project_part_3.R;
import com.example.project_part_3.Users.User;

import java.util.ArrayList;

public class Admin_profile_adapter extends ArrayAdapter<User> {

    private final int resourceLayout;

    public Admin_profile_adapter(Context context,int resource, ArrayList<User> users){
        super(context, 0, users);
        this.resourceLayout = resource;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        ViewHolder holder;

        if (view == null) {
            view = LayoutInflater.from(getContext()).inflate(resourceLayout,
                    parent, false);
            holder = new ViewHolder();
            holder.nameTextView = view.findViewById(R.id.admin_profiles_name);
            holder.emailTextView = view.findViewById(R.id.admin_profiles_email);
            view.setTag(holder);
        } else {
            holder = (ViewHolder) view.getTag();
        }

        User profile = getItem(position);

        if (profile != null) {
            holder.nameTextView.setText(profile.getName());
            holder.emailTextView.setText(profile.getEmail());
        }
        return view;

    }
    //view Holder patter for optimization
    private static class ViewHolder {
        TextView nameTextView;
        TextView emailTextView;
    }
}
