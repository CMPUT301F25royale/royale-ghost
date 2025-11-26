package com.example.project_part_3.Users.Entrant_UI.Entrant_qrscan;

import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.util.Size;

import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;

import com.google.mlkit.vision.barcode.BarcodeScanner;
import com.google.mlkit.vision.barcode.BarcodeScanning;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

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
    public void analyze(@NonNull ImageProxy image) {

    }

    @Override
    public @Nullable Size getDefaultTargetResolution() {
        return ImageAnalysis.Analyzer.super.getDefaultTargetResolution();
    }

    @Override
    public int getTargetCoordinateSystem() {
        return ImageAnalysis.Analyzer.super.getTargetCoordinateSystem();
    }

    @Override
    public void updateTransform(@Nullable Matrix matrix) {
        ImageAnalysis.Analyzer.super.updateTransform(matrix);
    }


}
