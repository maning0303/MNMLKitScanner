package com.maning.mlkitscanner.scan.callback;

import android.graphics.Bitmap;

import com.google.mlkit.vision.barcode.Barcode;

import java.util.List;

/**
 * @author : maning
 * @date : 8/19/21
 * @desc : 扫码分析结果回调
 */
public interface OnCameraAnalyserCallback {
    void onSuccess(Bitmap bitmap, List<Barcode> barcodes);
}
