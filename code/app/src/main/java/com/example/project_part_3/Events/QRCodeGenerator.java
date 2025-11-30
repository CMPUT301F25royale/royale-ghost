package com.example.project_part_3.Events;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.widget.Toast;


import com.example.project_part_3.R;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;

public class QRCodeGenerator {

    final static int size = 256;
    public static Bitmap generateQRCode(String id) throws WriterException {
        String data = "RoyaleEventApp" + "://event/" + id;
        BitMatrix QRCode = new MultiFormatWriter().encode(data, BarcodeFormat.QR_CODE, size, size);

        int[] pixels = new int[size * size];
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                if (QRCode.get(j, i)) {
                    pixels[i * size + j] = Color.BLACK;
                } else {
                    pixels[i * size + j] = Color.WHITE;
                }
            }
        }
        Bitmap QRCodeImage = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);

        QRCodeImage.setPixels(pixels, 0, size, 0, 0, size, size); // Set the bitmap pixels
        return QRCodeImage;
    }

}
