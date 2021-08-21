package com.maning.mlkitscanner.scan.camera;

import android.content.Context;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.Size;

/**
 * @author : maning
 * @date : 8/19/21
 * @desc :
 */
public class CameraSizeUtils {

    public static Size getSize(Context context) {
        Size mTargetSize;
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        int width = displayMetrics.widthPixels;
        int height = displayMetrics.heightPixels;
        Log.i("======", String.format("displayMetrics:%d x %d", width, height));
        //因为为了保持流畅性和性能，限制在1080p，在此前提下尽可能的找到屏幕接近的分辨率
        if (width < height) {
            int size = Math.min(width, 1080);
            float ratio = width / (float) height;
            if (ratio > 0.7) {//一般应用于平板
                mTargetSize = new Size(size, (int) (size / 3.0f * 4.0f));
            } else {
                mTargetSize = new Size(size, (int) (size / 9.0f * 16.0f));
            }
        } else {
            int size = Math.min(height, 1080);
            float ratio = height / (float) width;
            if (ratio > 0.7) {//一般应用于平板
                mTargetSize = new Size((int) (size / 3.0f * 4.0f), size);
            } else {
                mTargetSize = new Size((int) (size / 9.0f * 16.0), size);
            }
        }
        Log.i("======", "mTargetSize:" + mTargetSize.getWidth() + "," + mTargetSize.getHeight());
        return mTargetSize;
    }

} 