package com.example.project_part_3.Database_functions;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.project_part_3.Image.ImageMetadata;

import java.util.ArrayList;
import java.util.List;


/**
 * Our Figma wasn't very descriptive but to search images we needed some string tied to Image types
 * And our CRC cards were flawed because we need to relate the concept that Images can be profile pics
 * And posters so deleting elements from the database must delete images from both the events and
 * User profiles. If an Image is of type Profile it must be deleted the moment the User
 * profile is deleted. If an Image is of type Poster it must be deleted the moment the Event
 * is deleted. This class might get retconned but we need to implement the functions within to
 * maintain the UI.
 */
public class ImageDatabase {

    private static ImageDatabase instance;
    private ArrayList<ImageMetadata> database;

    private final MutableLiveData<List<ImageMetadata>> allImagesLiveData = new MutableLiveData<List<ImageMetadata>>();

    public LiveData<List<ImageMetadata>> getAllImagesLiveData() {
        return allImagesLiveData;
    }

    private ImageDatabase(){
        database = new ArrayList<>();
    }

    public static synchronized ImageDatabase getInstance() {
        if (instance == null) {
            instance = new ImageDatabase();
        }
        return instance;
    }

    public Boolean addImage(ImageMetadata image){ //
        database.add(image);
        return true;
    }

    public Boolean removeImage(ImageMetadata image){
        return database.remove(image);
    }

    public ImageMetadata getImage(String token){
        for (ImageMetadata image : database) {
            if (image.getDescription().contains(token)) {
                return image;
            }
        }
        return null;
    }

    public ArrayList<ImageMetadata> getAllImages(){
        return new ArrayList<>(database);
    }

}
