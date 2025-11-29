package com.example.project_part_3.Image;

import android.graphics.Bitmap;

/**
 * Used for storing images and related information for the database.
 */

public class Image_holder {
    private String url;
    private String Description;
    private String type;
    private Object object;
    public Image_holder(String url, String description, String type,Object object) {
        this.url = url; // changed to url so that we can use glide instead of storing a bitmap
        this.Description = description; // Image description is what actually allows us to search for images in the database
        this.type = type;// profile pic or poster used for UI to know what items need to be notified
        this.object = object;// event object or user object that we can use to track specific user and Event
    }
    public Object getObject() { return object;}
    public String getImage() {
        return url;
    }
    public String getDescription() {
        return Description;
    }
    public String getType() {
        return type;
    }
    public void setImage(String url) {
        this.url = url;
    }
    public void setDescription(String description) {
        Description = description;
    }

    public void setType(String type) {
        this.type = type;
    }
}
