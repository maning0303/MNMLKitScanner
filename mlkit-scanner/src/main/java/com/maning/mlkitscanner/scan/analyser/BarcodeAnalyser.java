package com.maning.mlkitscanner.scan.analyser;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;
import androidx.camera.view.PreviewView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.mlkit.vision.barcode.BarcodeScanner;
import com.google.mlkit.vision.barcode.BarcodeScannerOptions;
import com.google.mlkit.vision.barcode.BarcodeScanning;
import com.google.mlkit.vision.barcode.common.Barcode;
import com.google.mlkit.vision.common.InputImage;
import com.maning.mlkitscanner.scan.callback.OnCameraAnalyserCallback;
import com.maning.mlkitscanner.scan.utils.ImageUtils;

import java.util.List;

/**
 * @author : maning
 * @date : 8/18/21
 * @desc :
 */
public class BarcodeAnalyser implements ImageAnalysis.Analyzer {

    private OnCameraAnalyserCallback onCameraAnalyserCallback;
    private final BarcodeScanner barcodeScanner;
    /**
     * 是否分析
     */
    private volatile boolean isAnalyze = true;
    private Context context;
    private PreviewView mPreviewView;

    public BarcodeScanner getBarcodeScanner() {
        return barcodeScanner;
    }

    public void setAnalyze(boolean analyze) {
        isAnalyze = analyze;
    }

    public void setOnCameraAnalyserCallback(OnCameraAnalyserCallback onCameraAnalyserCallback) {
        this.onCameraAnalyserCallback = onCameraAnalyserCallback;
    }

    public BarcodeAnalyser() {
        BarcodeScannerOptions options = new BarcodeScannerOptions.Builder()
                .setBarcodeFormats(Barcode.FORMAT_ALL_FORMATS)
                .build();
        barcodeScanner = BarcodeScanning.getClient(options);
    }

    public void setPreviewView(PreviewView mPreviewView) {
        this.mPreviewView = mPreviewView;
    }

    private Bitmap cropBitmap(Bitmap bitmap, int cropWidth, int cropHeight) {
        int w = bitmap.getWidth();
        int h = bitmap.getHeight();
        return Bitmap.createBitmap(bitmap, (w - cropWidth) / 2, (h - cropHeight) / 2, cropWidth, cropHeight, null, false);
    }

    @Override
    public void analyze(@NonNull final ImageProxy imageProxy) {
        Bitmap bitmap = null;
        try {
            bitmap = ImageUtils.imageProxyToBitmap(imageProxy, imageProxy.getImageInfo().getRotationDegrees());
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (mPreviewView != null) {
            //bitmap转为16：9
            int height = mPreviewView.getHeight();
            int width = mPreviewView.getWidth();
            if (bitmap.getHeight() / (float) bitmap.getWidth() > mPreviewView.getHeight() / (float) mPreviewView.getWidth()) {
                int newHeight = bitmap.getWidth() * height / width;
                bitmap = cropBitmap(bitmap, bitmap.getWidth(), newHeight);
            } else if (bitmap.getHeight() / (float) bitmap.getWidth() < mPreviewView.getHeight() / (float) mPreviewView.getWidth()) {
                int newWith = bitmap.getHeight() * width / height;
                bitmap = cropBitmap(bitmap, newWith, bitmap.getHeight());
            }
            //大小控制和预览一样
            bitmap = Bitmap.createScaledBitmap(bitmap, width, height, true);
        }
        InputImage inputImage = InputImage.fromBitmap(bitmap, 0);
        //InputImage inputImage = InputImage.fromMediaImage(imageProxy.getImage(), imageProxy.getImageInfo().getRotationDegrees());
        final Bitmap finalBitmap = bitmap;
        barcodeScanner.process(inputImage)
                .addOnSuccessListener(new OnSuccessListener<List<Barcode>>() {
                    @Override
                    public void onSuccess(@NonNull List<Barcode> barcodes) {
                        if (barcodes.size() == 0) {
                            return;
                        }
                        if (!isAnalyze) {
                            return;
                        }
                        isAnalyze = false;
                        for (Barcode barcode : barcodes) {
                            Log.i("======", "barcode-getDisplayValue:" + barcode.getDisplayValue());
                            Log.i("======", "barcode-getRawValue:" + barcode.getRawValue());
                        }
                        if (onCameraAnalyserCallback != null) {
                            onCameraAnalyserCallback.onSuccess(finalBitmap, barcodes);
                        }
                    }
                })
                .addOnCompleteListener(new OnCompleteListener<List<Barcode>>() {
                    @Override
                    public void onComplete(@NonNull Task<List<Barcode>> task) {
                        imageProxy.close();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e("======", "onFailure---:" + e.toString());
                    }
                });
    }
}