package com.example.project_part_3.Users.Entrant_UI.Entrant_qrscan;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.util.Size;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.example.project_part_3.R;
import com.example.project_part_3.Users.Entrant_UI.Entrant_event.entrant_event_detail_activity;
import com.example.project_part_3.Users.Organizer_UI.OrganizerSharedViewModel;
import com.google.common.util.concurrent.ListenableFuture;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Entrant_qrscan_view extends Fragment {

    private PreviewView cameraPreviewView;
    private ExecutorService cameraExecutor;
    private boolean isProcessingQr = false;


    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    startCamera();
                } else {
                    Toast.makeText(requireContext(), "Camera permission required", Toast.LENGTH_SHORT).show();
                }
            });

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.entrant_scan, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        cameraPreviewView = view.findViewById(R.id.qr_code_preview);
        cameraExecutor = Executors.newSingleThreadExecutor();

        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
            startCamera();
        } else {
            requestPermissionLauncher.launch(Manifest.permission.CAMERA);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (cameraExecutor != null) {
            cameraExecutor.shutdown();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        isProcessingQr = false;
    }

    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderListenableFuture
                = ProcessCameraProvider.getInstance(requireContext());
        cameraProviderListenableFuture.addListener( () -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderListenableFuture.get();
                bindCameraUseCases(cameraProvider);
            } catch (Exception e) {
                Log.e("CameraX", "Failed to bind camera use cases", e);
            }
        }, ContextCompat.getMainExecutor(requireContext()));
    }

    private void bindCameraUseCases(@NonNull ProcessCameraProvider cameraProvider) {
        Preview preview = new Preview.Builder().build();
        preview.setSurfaceProvider(cameraPreviewView.getSurfaceProvider());

        ImageAnalysis imageAnalysis = new ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build();

        imageAnalysis.setAnalyzer(cameraExecutor, new QR_code_handler(qrCode -> {
            if (isProcessingQr) return;

            if (qrCode != null) {
                String eventID = getEventIDFromQRCode(qrCode);

                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        if (eventID != null && !eventID.isEmpty()) {
                            isProcessingQr = true; // Stop further processing

                            Intent intent = new Intent(requireActivity(), entrant_event_detail_activity.class);
                            intent.putExtra("eventId", eventID);

                            Bundle args = getArguments();
                            if (args != null) {
                                intent.putExtra("viewerUserEmail", args.getString("userEmail"));
                            }

                            startActivity(intent);
                        }
                    });
                }
            }
        }));

        CameraSelector cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA;

        try {
            cameraProvider.unbindAll();

            cameraProvider.bindToLifecycle(
                    getViewLifecycleOwner(),
                    cameraSelector,
                    preview,
                    imageAnalysis
            );

        } catch (Exception e) {
            Log.e("CameraX", "Use case binding failed", e);
        }
    }

    private String getEventIDFromQRCode(String qrCode) {
        if (qrCode.startsWith("RoyaleEventApp://event/")) {
            return qrCode.replace("RoyaleEventApp://event/", "");
        }
        return null;
    }
}
