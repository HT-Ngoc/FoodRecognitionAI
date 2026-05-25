package vn.huynhtuanngoc.foodai;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.ImageProxy;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity
        extends AppCompatActivity {

    private PreviewView previewView;
    private TextView resultText;

    private FoodClassifier classifier;

    private final ExecutorService cameraExecutor =
            Executors.newSingleThreadExecutor();

    private long lastAnalyzeTime = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        previewView =
                findViewById(R.id.previewView);

        resultText =
                findViewById(R.id.resultText);

        try {

            classifier =
                    new FoodClassifier(this);
        } catch (Exception e) {
            e.printStackTrace();

            resultText.setText(
                    e.getMessage()
            );
        }
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED) {

            startCamera();

        } else {

            ActivityCompat.requestPermissions(
                    this,
                    new String[]{
                            Manifest.permission.CAMERA
                    },
                    100
            );
        }
    }

    private void startCamera() {

        CameraHelper.startCamera(
                this,
                previewView,
                cameraExecutor,
                this::analyzeImage
        );
    }

    private void analyzeImage(ImageProxy image) {

        if (System.currentTimeMillis()
                - lastAnalyzeTime < 400) {

            image.close();
            return;
        }

        lastAnalyzeTime =
                System.currentTimeMillis();

        Bitmap bitmap =
                ImageProcessor.imageToBitmap(image);

        image.close();

        if (bitmap == null) {
            return;
        }

        FoodResult result =
                classifier.classify(bitmap);

        runOnUiThread(() -> {

            String text =
                    result.getLabel()
                            + "\nĐộ chính xác: "
                            + (int)(
                            result.getConfidence() * 100
                    )
                            + "%";

            resultText.setText(text);
        });
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode,
            @NonNull String[] permissions,
            @NonNull int[] grantResults
    ) {

        super.onRequestPermissionsResult(
                requestCode,
                permissions,
                grantResults
        );

        if (requestCode == 100
                && grantResults.length > 0
                && grantResults[0]
                == PackageManager.PERMISSION_GRANTED) {

            startCamera();
        }
    }

    @Override
    protected void onDestroy() {

        super.onDestroy();

        if (classifier != null) {
            classifier.close();
        }

        cameraExecutor.shutdown();
    }
}