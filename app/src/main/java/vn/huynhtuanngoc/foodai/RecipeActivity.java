package vn.huynhtuanngoc.foodai;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class RecipeActivity extends AppCompatActivity {

    private TextView tvRecipeName, tvIngredients, tvSteps;
    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Chi tiết công thức");
        }

        tvRecipeName = findViewById(R.id.tvRecipeName);
        tvIngredients = findViewById(R.id.tvIngredients);
        tvSteps = findViewById(R.id.tvSteps);

        String foodName = getIntent().getStringExtra("FOOD_NAME");

        if (foodName != null && !foodName.isEmpty()) {
            tvRecipeName.setText(foodName);
            loadRecipeFromFirebase(foodName);
        } else {
            tvRecipeName.setText("Không tìm thấy dữ liệu món ăn");
            tvIngredients.setText("Đã xảy ra lỗi khi truyền tên món.");
            tvSteps.setText("Vui lòng thử quét lại.");
        }
    }

    private void loadRecipeFromFirebase(String foodName) {
        mDatabase = FirebaseDatabase.getInstance().getReference("recipes").child(foodName);

        mDatabase.get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult().exists()) {

                DataSnapshot data = task.getResult();

                String ingredients = data.child("ingredients").getValue(String.class);
                String steps = data.child("steps").getValue(String.class);

                if (ingredients != null) {
                    ingredients = ingredients.replace("\\n", "\n");
                    tvIngredients.setText(ingredients);
                } else {
                    tvIngredients.setText("Đang cập nhật nguyên liệu...");
                }

                if (steps != null) {
                    steps = steps.replace("\\n", "\n");
                    tvSteps.setText(steps);
                } else {
                    tvSteps.setText("Đang cập nhật cách làm...");
                }

            } else {
                tvIngredients.setText("Trống");

                tvSteps.setText(
                        "Hiện tại chúng tôi chưa cập nhật công thức cho món '"
                                + foodName +
                                "'.\nBạn vui lòng quay lại sau nhé!"
                );

                Toast.makeText(
                        RecipeActivity.this,
                        "Chưa có công thức món này!",
                        Toast.LENGTH_SHORT
                ).show();
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}