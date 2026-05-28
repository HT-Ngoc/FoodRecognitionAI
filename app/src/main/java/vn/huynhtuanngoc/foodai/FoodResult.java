package vn.huynhtuanngoc.foodai;

public class FoodResult {

    private final String label;
    private final float confidence;
    private final int calories;

    public FoodResult(
            String label,
            float confidence,
            int calories
    ) {
        this.label = label;
        this.confidence = confidence;
        this.calories = calories;
    }

    public String getLabel() {
        return label;
    }

    public float getConfidence() {
        return confidence;
    }

    public int getCalories() {
        return calories;
    }
}