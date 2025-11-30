package com.example.project_part_3.Users.Organizer_UI.Organizer_event;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.project_part_3.Events.QRCodeGenerator;
import com.example.project_part_3.R;
import com.example.project_part_3.Users.Organizer_UI.OrganizerSharedViewModel;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import java.io.OutputStream;

/**
 * Fragment that displays a QR code for a selected event.
 * The QR code can be viewed and saved to the user's photo gallery.
 */
public class Organizer_event_qrcode_view extends Fragment {
    private ImageButton backButton;
    private Button saveButton;
    private ImageView qrCodeView;
    private OrganizerSharedViewModel model;

    public Organizer_event_qrcode_view() {
        // Required empty public constructor
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (model != null) {
            model.setSelectedEvent(null);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.organizer_event_qrcode_view, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        model = new ViewModelProvider(requireActivity()).get(OrganizerSharedViewModel.class);

        setUpBackButton(view);

        model.getSelectedEvent().observe(getViewLifecycleOwner(), event -> {
            if (event != null) {
                Bitmap bmp = displayQRCode(view, event.getId());
                setUpSaveButton(view, bmp, event.getId());
            }
        });
    }

    /**
     * Sets up the back button in the view.
     * @param view The view to set up the back button on.
     */
    public void setUpBackButton(View view) {
        backButton = view.findViewById(R.id.event_qr_code_back_button);
        if (backButton != null) {
            backButton.setOnClickListener(v -> {
                NavController navBack = NavHostFragment.findNavController(this);
                navBack.navigate(R.id.action_organizer_event_qrcode_view_to_organizerEventsFragment);
            });
        }
    }

    /**
     * Generates and displays a QR code for the given event.
     * @param view The view to display the QR code in.
     * @param eventID The ID of the event to generate the QR code for.
     * @return The generated QR code as a Bitmap, or null if an error occurred.
     */
    public Bitmap displayQRCode(View view, String eventID) {
        qrCodeView = view.findViewById(R.id.event_qr_code_view);
        try {
            Bitmap qrCodeBitmap = QRCodeGenerator.generateQRCode(eventID);
            if (qrCodeView != null) {
                qrCodeView.setImageBitmap(qrCodeBitmap);
            }
            return qrCodeBitmap;
        } catch (Exception e) {
            Toast.makeText(getContext(), "Error generating QR code", Toast.LENGTH_SHORT).show();
        }
        return null;
    }

    /**
     * Sets up the save button to save the QR code to the user's photo gallery.
     * @param view The view to set up the save button on.
     * @param qrCodeBitmap The QR code to save.
     * @param eventID The ID of the event the QR code is for.
     */
    public void setUpSaveButton(View view, Bitmap qrCodeBitmap, String eventID) {
        saveButton = view.findViewById(R.id.organizer_qrcode_save_to_photos_button);

        if (saveButton == null) return;

        if (qrCodeBitmap == null) {
            saveButton.setEnabled(false);
            saveButton.setVisibility(View.GONE);
            return;
        } else {
            saveButton.setEnabled(true);
            saveButton.setVisibility(View.VISIBLE);
        }

        saveButton.setOnClickListener(v -> saveImageToGallery(qrCodeBitmap, eventID));
    }

    /**
     * Saves the given QR code to the user's photo gallery.
     * @param bitmap The QR code to save.
     * @param eventID The ID of the event the QR code is for.
     */
    private void saveImageToGallery(Bitmap bitmap, String eventID) {
        String fileName = "event_" + eventID + "_qr_code";

        ContentResolver resolver = requireContext().getContentResolver();
        ContentValues contentValues = new ContentValues();
        contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, fileName);
        contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "image/png");

        // Saving to "Pictures" folder
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH, "Pictures/EventQR");
        }

        Uri imageUri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);

        if (imageUri != null) {
            try (OutputStream out = resolver.openOutputStream(imageUri)) {
                if (out != null) {
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
                    Toast.makeText(getContext(), "QR Saved to Photos!", Toast.LENGTH_SHORT).show();
                }
            } catch (Exception e) {
                Toast.makeText(getContext(), "Failed to save QR Code", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
        }
    }
}