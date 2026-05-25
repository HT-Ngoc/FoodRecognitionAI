package vn.huynhtuanngoc.foodai;

import android.content.Context;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class LabelLoader {

    public static String[] loadLabels(
            Context context,
            String fileName
    ) throws Exception {

        BufferedReader reader =
                new BufferedReader(
                        new InputStreamReader(
                                context.getAssets().open(fileName)
                        )
                );

        ArrayList<String> labels =
                new ArrayList<>();

        String line;

        while ((line = reader.readLine()) != null) {
            labels.add(line);
        }

        reader.close();

        return labels.toArray(new String[0]);
    }
}