package vn.huynhtuanngoc.foodai;

public class FoodHistory {
    private String foodName;
    private int calories;
    private long timestamp;

    public FoodHistory() {
    }
    public FoodHistory(String foodName, int calories, long timestamp) {
        this.foodName = foodName;
        this.calories = calories;
        this.timestamp = timestamp;
    }
    public String getFoodName() { return foodName; }
    public int getCalories() { return calories; }
    public long getTimestamp() { return timestamp; }
}
