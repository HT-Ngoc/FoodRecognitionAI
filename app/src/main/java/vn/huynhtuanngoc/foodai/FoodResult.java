package vn.huynhtuanngoc.foodai;

public class FoodResult {

    private final String label;
    private final float confidence;

    public FoodResult(String label, float confidence) {
        this.label = label;
        this.confidence = confidence;
    }

    public String getLabel() {
        return label;
    }

    public float getConfidence() {
        return confidence;
    }
}