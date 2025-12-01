package com.example.project_part_3.Users.Entrant_UI.Entrant_qrscan;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.media.Image;
import android.util.Log;
import android.util.Size;

import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;

import com.google.mlkit.vision.barcode.BarcodeScanner;
import com.google.mlkit.vision.barcode.BarcodeScanning;
import com.google.mlkit.vision.barcode.common.Barcode;
import com.google.mlkit.vision.common.InputImage;

/**
 * QR_Coder_Handler analyzes QR codes and triggers a callback when a QR code is scanned.
 */
public class QR_code_handler implements ImageAnalysis.Analyzer {
    private final BarcodeScanner scanner = BarcodeScanning.getClient();
    private final OnQrScanListener listener;

    public interface OnQrScanListener {
        void onQrScanned(String qrCode);
    }

    public QR_code_handler(OnQrScanListener listener) {
        this.listener = listener;
    }

    @Override
    @SuppressLint("UnsafeOptInUsageError")
    public void analyze(ImageProxy imageProxy) {
        Image mediaImage = imageProxy.getImage();
        if (mediaImage == null) {
            imageProxy.close();
            return;
        }

        InputImage image =
                InputImage.fromMediaImage(mediaImage, imageProxy.getImageInfo().getRotationDegrees());
        scanner.process(image)
                .addOnSuccessListener(barcodes -> {
                    for (Barcode barcode : barcodes) {
                        if (barcode.getFormat() == Barcode.FORMAT_QR_CODE) {
                            String qrCode = barcode.getRawValue();
                            if (qrCode != null) {
                                listener.onQrScanned(qrCode);
                            }
                        }
                    }
                }).addOnFailureListener(e -> {
                    Log.e("QR_code_handler", "Failed to scan QR code", e);
                }).addOnCompleteListener(task -> imageProxy.close());
    }
}
