package vn.huynhtuanngoc.foodai;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.ImageProxy;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {

    private PreviewView previewView;
    private TextView resultText;
    private Button btnViewRecipe;
    private Button btnViewHistory;
    private FoodClassifier classifier;

    private final ExecutorService cameraExecutor = Executors.newSingleThreadExecutor();

    private long lastAnalyzeTime = 0;
    private String currentFoodName = "";
    private int currentFoodCalories = 0;

    // === BIẾN CHỐNG TRÙNG: Lưu vết món ăn và thời gian của lần bấm nút trước ===
    private String lastSavedFoodName = "";
    private long lastSavedTime = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        previewView = findViewById(R.id.previewView);
        resultText = findViewById(R.id.resultText);
        btnViewRecipe = findViewById(R.id.btnViewRecipe);
        btnViewHistory = findViewById(R.id.btnViewHistory);
        btnViewRecipe.setOnClickListener(v -> {
            if (!currentFoodName.isEmpty()) {
                long currentTime = System.currentTimeMillis();
                if (currentFoodName.equals(lastSavedFoodName) && (currentTime - lastSavedTime < 3000)) {
                } else {
                    saveHistoryToFirebase(currentFoodName, currentFoodCalories);
                    lastSavedFoodName = currentFoodName;
                    lastSavedTime = currentTime;
                }
                Intent intent = new Intent(MainActivity.this, RecipeActivity.class);
                intent.putExtra("FOOD_NAME", currentFoodName);
                startActivity(intent);
            }
        });
        btnViewHistory.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, HistoryActivity.class);
            startActivity(intent);
        });
        try {
            classifier = new FoodClassifier(this);
        } catch (Exception e) {
            e.printStackTrace();
            resultText.setText(e.getMessage());
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            startCamera();
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, 100);
        }
    }

    private void startCamera() {
        CameraHelper.startCamera(this, previewView, cameraExecutor, this::analyzeImage);
    }

    private void analyzeImage(ImageProxy image) {
        if (System.currentTimeMillis() - lastAnalyzeTime < 400) {
            image.close();
            return;
        }

        lastAnalyzeTime = System.currentTimeMillis();

        Bitmap bitmap = ImageProcessor.imageToBitmap(image);
        image.close();

        if (bitmap == null) {
            return;
        }

        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        int cropSize = (int) (Math.min(width, height) * 0.8f);
        int startX = (width - cropSize) / 2;
        int startY = (height - cropSize) / 2;

        Bitmap croppedBitmap = Bitmap.createBitmap(bitmap, startX, startY, cropSize, cropSize);
        FoodResult result = classifier.classify(croppedBitmap);

        runOnUiThread(() -> {
            if (result.getConfidence() < 0.85f) {
                resultText.setText("Đang quét...\nVui lòng đưa món ăn vào khung");
                btnViewRecipe.setVisibility(View.GONE);
                currentFoodName = "";
            } else {
                currentFoodName = result.getLabel();
                currentFoodCalories = result.getCalories();

                String text = currentFoodName
                        + "\nLượng calo: " + currentFoodCalories + " kcal"
                        + "\nĐộ chính xác: " + (int) (result.getConfidence() * 100) + "%";

                resultText.setText(text);
                btnViewRecipe.setText("Xem công thức " + currentFoodName);
                btnViewRecipe.setVisibility(View.VISIBLE);
            }
        });
    }

    private void saveHistoryToFirebase(String name, int calo) {
        try {
            String deviceId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);

            DatabaseReference historyRef = FirebaseDatabase.getInstance()
                    .getReference("users")
                    .child(deviceId)
                    .child("history");

            HashMap<String, Object> map = new HashMap<>();
            map.put("foodName", name);
            map.put("calories", calo);
            map.put("timestamp", System.currentTimeMillis());

            historyRef.push().setValue(map);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 100 && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
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