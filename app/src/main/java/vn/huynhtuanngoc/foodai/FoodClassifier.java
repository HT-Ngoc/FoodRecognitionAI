package vn.huynhtuanngoc.foodai;

import android.content.Context;
import android.graphics.Bitmap;

import org.tensorflow.lite.Interpreter;

import java.io.FileInputStream;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

public class FoodClassifier {

    private final Interpreter interpreter;
    private final String[] labels;

    private static final int IMAGE_SIZE = 224;

    public FoodClassifier(Context context)
            throws Exception {

        Interpreter.Options options =
                new Interpreter.Options();

        options.setNumThreads(4);

        interpreter =
                new Interpreter(
                        loadModelFile(context),
                        options
                );

        labels =
                LabelLoader.loadLabels(
                        context,
                        "labels.txt"
                );
    }

    private ByteBuffer loadModelFile(
            Context context
    ) throws Exception {

        FileInputStream fis =
                new FileInputStream(
                        context.getAssets()
                                .openFd("foodmodel.tflite")
                                .getFileDescriptor()
                );

        FileChannel fileChannel =
                fis.getChannel();

        long startOffset =
                context.getAssets()
                        .openFd("foodmodel.tflite")
                        .getStartOffset();

        long declaredLength =
                context.getAssets()
                        .openFd("foodmodel.tflite")
                        .getDeclaredLength();

        return fileChannel.map(
                FileChannel.MapMode.READ_ONLY,
                startOffset,
                declaredLength
        );
    }

    public FoodResult classify(Bitmap bitmap) {

        ByteBuffer inputBuffer =
                ImageProcessor.bitmapToBuffer(
                        bitmap,
                        IMAGE_SIZE
                );

        float[][] output =
                new float[1][labels.length];

        interpreter.run(inputBuffer, output);

        int maxIndex = 0;
        float maxConfidence = output[0][0];

        for (int i = 1; i < output[0].length; i++) {

            if (output[0][i] > maxConfidence) {

                maxConfidence = output[0][i];
                maxIndex = i;
            }
        }

        return new FoodResult(
                labels[maxIndex],
                maxConfidence
        );
    }

    public void close() {

        if (interpreter != null) {
            interpreter.close();
        }
    }
}