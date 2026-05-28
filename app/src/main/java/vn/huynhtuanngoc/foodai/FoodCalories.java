package vn.huynhtuanngoc.foodai;

import java.util.HashMap;

public class FoodCalories {

    private static final HashMap<String, Integer> caloriesMap =
            new HashMap<>();

    static {

        caloriesMap.put("cơm tấm", 540);
        caloriesMap.put("phở", 450);
        caloriesMap.put("bánh pía", 300);
        caloriesMap.put("bánh mì", 320);
        caloriesMap.put("gỏi cuốn", 150);
        caloriesMap.put("bánh tét", 800);
        caloriesMap.put("cháo lòng", 350);
        caloriesMap.put("bún đậu mắm tôm", 700);
        caloriesMap.put("bánh xèo", 285);
        caloriesMap.put("nem chua", 130);
    }

    public static int getCalories(String foodName) {

        if (foodName == null || foodName.isEmpty()) {
            return 0;
        }

        String lowerCaseName =
                foodName.toLowerCase();

        for (String key : caloriesMap.keySet()) {

            if (lowerCaseName.contains(key)) {
                return caloriesMap.get(key);
            }
        }

        return 0;
    }
}