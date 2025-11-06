package com.example.project_part_3.Image;

import android.graphics.Bitmap;

public class Image_holder {
    private Bitmap image;
    private String Description;
    private String type;
    private Object object;
    public Image_holder(Bitmap image, String description, String type,Object object) {
        this.image = image;
        this.Description = description; // Image description is what actually allows us to search for images in the database
        this.type = type;// profile pic or poster used for UI to know what items need to be notified
        this.object = object;// event object or user object that we can use to track specific user and Event
    }
    public Object getObject() { return object;}
    public Bitmap getImage() {
        return image;
    }
    public String getDescription() {
        return Description;
    }
    public String getType() {
        return type;
    }
    public void setImage(Bitmap image) {
        this.image = image;
    }
    public void setDescription(String description) {
        Description = description;
    }

    public void setType(String type) {
        this.type = type;
    }
}
