package com.example.project_part_3.Database_functions;

import com.example.project_part_3.Image.Image_holder;

import java.util.ArrayList;

public class Image_database {

    private static Image_database instance;
    private ArrayList<Image_holder> database;

    private Image_database(){
        database = new ArrayList<>();
    }

    public static synchronized Image_database getInstance() {
        if (instance == null) {
            instance = new Image_database();
        }
        return instance;
    }

    public Boolean addImage(Image_holder image){
        database.add(image);
        return true;
    }

    public Boolean removeImage(Image_holder image){
        return database.remove(image);
    }

    public Image_holder getImage(String token){
        for (Image_holder image : database) {
            if (image.getDescription().contains(token)) {
                return image;
            }
        }
        return null;
    }

    public ArrayList<Image_holder> getAllImages(){
        return new ArrayList<>(database);
    }

}
