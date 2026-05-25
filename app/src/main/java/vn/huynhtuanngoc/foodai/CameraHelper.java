package vn.huynhtuanngoc.foodai;

import android.util.Size;

import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LifecycleOwner;

import java.util.concurrent.ExecutorService;

public class CameraHelper {

    public interface FrameListener {
        void onFrame(
                androidx.camera.core.ImageProxy image
        );
    }

    public static void startCamera(
            LifecycleOwner owner,
            PreviewView previewView,
            ExecutorService executorService,
            FrameListener listener
    ) {

        ProcessCameraProvider
                .getInstance(previewView.getContext())
                .addListener(() -> {

                    try {

                        ProcessCameraProvider provider =
                                ProcessCameraProvider
                                        .getInstance(
                                                previewView.getContext()
                                        )
                                        .get();

                        Preview preview =
                                new Preview.Builder()
                                        .build();

                        preview.setSurfaceProvider(
                                previewView.getSurfaceProvider()
                        );

                        ImageAnalysis analysis =
                                new ImageAnalysis.Builder()
                                        .setTargetResolution(
                                                new Size(224, 224)
                                        )
                                        .setBackpressureStrategy(
                                                ImageAnalysis
                                                        .STRATEGY_KEEP_ONLY_LATEST
                                        )
                                        .build();

                        analysis.setAnalyzer(
                                executorService,
                                listener::onFrame
                        );

                        provider.unbindAll();

                        provider.bindToLifecycle(
                                owner,
                                CameraSelector
                                        .DEFAULT_BACK_CAMERA,
                                preview,
                                analysis
                        );

                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }, ContextCompat.getMainExecutor(
                        previewView.getContext()
                ));
    }
}