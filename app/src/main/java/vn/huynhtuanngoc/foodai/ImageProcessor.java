package vn.huynhtuanngoc.foodai;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;

import androidx.camera.core.ImageProxy;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class ImageProcessor {

    public static Bitmap imageToBitmap(ImageProxy image) {

        try {

            ByteBuffer yBuffer =
                    image.getPlanes()[0].getBuffer();

            ByteBuffer uBuffer =
                    image.getPlanes()[1].getBuffer();

            ByteBuffer vBuffer =
                    image.getPlanes()[2].getBuffer();

            int ySize = yBuffer.remaining();
            int uSize = uBuffer.remaining();
            int vSize = vBuffer.remaining();

            byte[] nv21 =
                    new byte[ySize + uSize + vSize];

            yBuffer.get(nv21, 0, ySize);
            vBuffer.get(nv21, ySize, vSize);
            uBuffer.get(nv21, ySize + vSize, uSize);

            YuvImage yuvImage =
                    new YuvImage(
                            nv21,
                            ImageFormat.NV21,
                            image.getWidth(),
                            image.getHeight(),
                            null
                    );

            ByteArrayOutputStream out =
                    new ByteArrayOutputStream();

            yuvImage.compressToJpeg(
                    new Rect(
                            0,
                            0,
                            image.getWidth(),
                            image.getHeight()
                    ),
                    100,
                    out
            );

            byte[] imageBytes =
                    out.toByteArray();

            return BitmapFactory.decodeByteArray(
                    imageBytes,
                    0,
                    imageBytes.length
            );

        } catch (Exception e) {

            e.printStackTrace();
            return null;
        }
    }

    public static ByteBuffer bitmapToBuffer(
            Bitmap bitmap,
            int imageSize
    ) {

        Bitmap resizedBitmap =
                Bitmap.createScaledBitmap(
                        bitmap,
                        imageSize,
                        imageSize,
                        true
                );

        ByteBuffer byteBuffer =
                ByteBuffer.allocateDirect(
                        4 * imageSize * imageSize * 3
                );

        byteBuffer.order(ByteOrder.nativeOrder());

        int[] intValues =
                new int[imageSize * imageSize];

        resizedBitmap.getPixels(
                intValues,
                0,
                imageSize,
                0,
                0,
                imageSize,
                imageSize
        );

        int pixel = 0;

        for (int i = 0; i < imageSize; i++) {

            for (int j = 0; j < imageSize; j++) {

                int value = intValues[pixel++];

                float r =
                        ((value >> 16) & 0xFF) / 255f;

                float g =
                        ((value >> 8) & 0xFF) / 255f;

                float b =
                        (value & 0xFF) / 255f;

                byteBuffer.putFloat(r);
                byteBuffer.putFloat(g);
                byteBuffer.putFloat(b);
            }
        }

        return byteBuffer;
    }
}