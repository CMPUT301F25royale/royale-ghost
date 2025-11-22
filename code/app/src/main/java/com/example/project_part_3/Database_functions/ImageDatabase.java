package com.example.project_part_3.Database_functions;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.project_part_3.Image.Image_holder;

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
    private ArrayList<Image_holder> database;

    private final MutableLiveData<List<Image_holder>> allImagesLiveData = new MutableLiveData<>();

    public LiveData<List<Image_holder>> getAllImagesLiveData() {
        return allImagesLiveData;
    }

    private ImageDatabase(){
        database = new ArrayList<>();
        database.add(new Image_holder(
                null,
                "Profile picture of user John Doe",
                "profile"
        , null));
        database.add(new Image_holder(
                null,
                "Official poster for the Annual Tech Conference 2025",
                "poster"
        , null));
        database.add(new Image_holder(
                null,
                "Avatar for Jane Smith",
                "profile"
        , null));
        database.add(new Image_holder(
                null,
                "Promotional banner for the Summer Music Festival",
                "poster"
        , null));

    }

    public static synchronized ImageDatabase getInstance() {
        if (instance == null) {
            instance = new ImageDatabase();
        }
        return instance;
    }

    public Boolean addImage(Image_holder image){ //
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
