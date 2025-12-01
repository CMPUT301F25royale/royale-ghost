package com.example.project_part_3.Database_functions;

import android.net.Uri;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.MutableLiveData;

import com.example.project_part_3.Image.Image_datamap;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;


public class ImageDatabase {

    private final Database db;
    private final MutableLiveData<List<Image_datamap>> allImages = new MutableLiveData<>();
    private ListenerRegistration allImagesListener;
    private ListenerRegistration singleImageListener;

    public interface OnImageUploadListener {
        void onSuccess(Image_datamap metadata);
        void onFailure(String errorMessage);
    }

    public interface OnImageDeleteListener {
        void onSuccess();
        void onFailure(String errorMessage);
    }

    public ImageDatabase() {
        this.db = new Database(FirebaseFirestore.getInstance());
        listenForAllImages();
    }


    private void listenForAllImages() {
        if (allImagesListener != null) {
            allImagesListener.remove();
        }

        CollectionReference imagesCollection = db.getDb().collection("images");

        allImagesListener = imagesCollection.addSnapshotListener((snapshots, error) -> {
            if (error != null) {
                Log.e("ImageDatabase", "Listen for all images failed", error);
                allImages.postValue(null); // Signal an error state
                return;
            }
            if (snapshots != null) {
                List<Image_datamap> imageList = new ArrayList<>();
                for (QueryDocumentSnapshot doc : snapshots) {
                    Image_datamap metadata = doc.toObject(Image_datamap.class);
                    imageList.add(metadata);
                }
                allImages.postValue(imageList);
            } else {
                allImages.postValue(new ArrayList<>()); // Post an empty list if snapshot is null
            }
        });
    }

    public MutableLiveData<List<Image_datamap>> getAllImages() {
        return allImages;
    }

    public void deleteImage(Image_datamap image, OnImageDeleteListener listener) {
        if (image == null || image.getId() == null || image.getPath() == null) {
            listener.onFailure("Invalid or incomplete Image_datamap provided.");
            return;
        }

        db.deleteImageFromMetadata(image)
                .addOnSuccessListener(aVoid -> listener.onSuccess())
                .addOnFailureListener(e -> listener.onFailure(e.getMessage()));

    }

    public void uploadImage(@NonNull Uri imageUri, @NonNull String imageType, @NonNull String description, @NonNull String ownerId, @Nullable String eventId, OnImageUploadListener listener) {
        db.uploadImage(imageUri, imageType, description, ownerId, eventId)
                .addOnSuccessListener(listener::onSuccess)
                .addOnFailureListener(e -> listener.onFailure(e.getMessage()));
    }
    public void cleanupListeners() {
        if (allImagesListener != null) {
            allImagesListener.remove();
            allImagesListener = null;
        }
        if (singleImageListener != null) {
            singleImageListener.remove();
            singleImageListener = null;
        }
        Log.d("ImageDatabase", "All image listeners have been cleaned up.");
    }
}