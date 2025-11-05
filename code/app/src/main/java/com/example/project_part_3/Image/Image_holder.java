package com.example.project_part_3.Image;

import android.graphics.Bitmap;

public class Image_holder {
    private Bitmap image;
    private String Description;
    private String type;
    public Image_holder(Bitmap image, String description, String type) {
        this.image = image;
        this.Description = description; // Image description is what actually allows us to search for images in the database
        this.type = type;// profile pic or poster used for UI to know what items need to be notified
    }
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
