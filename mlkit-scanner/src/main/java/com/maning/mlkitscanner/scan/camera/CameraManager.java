package com.maning.mlkitscanner.scan.camera;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;

import androidx.camera.core.Camera;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.FocusMeteringAction;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.MeteringPoint;
import androidx.camera.core.Preview;
import androidx.camera.core.ZoomState;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LifecycleOwner;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.mlkit.vision.barcode.common.Barcode;
import com.maning.mlkitscanner.scan.analyser.BarcodeAnalyser;
import com.maning.mlkitscanner.scan.callback.OnCameraAnalyserCallback;
import com.maning.mlkitscanner.scan.model.MNScanConfig;
import com.maning.mlkitscanner.scan.utils.BeepManager;

import java.util.List;
import java.util.concurrent.Executors;

/**
 * @author : maning
 * @date : 8/19/21
 * @desc :
 */
public class CameraManager {

    private static final int HOVER_TAP_TIMEOUT = 150;
    private static final int HOVER_TAP_SLOP = 20;

    private ListenableFuture<ProcessCameraProvider> cameraProviderFuture;
    private LifecycleOwner mLifecycleOwner;
    private Camera mCamera;
    private long mLastHoveTapTime;
    private boolean isClickTap;
    private float mDownX;
    private float mDownY;
    private PreviewView mPreviewView;
    private Context mContext;
    private BarcodeAnalyser barcodeAnalyser;
    private OnCameraAnalyserCallback onCameraAnalyserCallback;
    private MNScanConfig scanConfig;
    private BeepManager beepManager;

    public static CameraManager getInstance(Context mContext, PreviewView mPreviewView) {
        return new CameraManager(mContext, mPreviewView);
    }

    public CameraManager(Context mContext, PreviewView mPreviewView) {
        this.mContext = mContext;
        this.mPreviewView = mPreviewView;
        initDatas();
    }

    public BarcodeAnalyser getBarcodeAnalyser() {
        return barcodeAnalyser;
    }

    public void setOnCameraAnalyserCallback(OnCameraAnalyserCallback callback) {
        this.onCameraAnalyserCallback = callback;
    }

    public void setScanConfig(MNScanConfig config) {
        scanConfig = config;
        beepManager.setPlayBeep(scanConfig.isShowBeep());
        beepManager.setVibrate(scanConfig.isShowVibrate());
    }

    public void setAnalyze(boolean analyze) {
        barcodeAnalyser.setAnalyze(analyze);
    }

    private void initDatas() {
        mLifecycleOwner = (LifecycleOwner) mContext;
        beepManager = new BeepManager(mContext);
        initBarcodeAnalyser();
        initScaleGesture();
    }

    public void startCamera() {
        cameraProviderFuture = ProcessCameraProvider.getInstance(mContext);
        cameraProviderFuture.addListener(new Runnable() {
            @Override
            public void run() {
                try {
                    ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                    Preview preview = new Preview.Builder().build();
                    //绑定预览
                    preview.setSurfaceProvider(mPreviewView.getSurfaceProvider());
                    //使用后置相机
                    CameraSelector cameraSelector = new CameraSelector.Builder()
                            .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                            .build();
                    //配置图片扫描
                    ImageAnalysis imageAnalysis = new ImageAnalysis.Builder()
                            .setTargetResolution(CameraSizeUtils.getSize(mContext))
                            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                            .build();
                    imageAnalysis.setAnalyzer(Executors.newSingleThreadExecutor(), barcodeAnalyser);
                    if (mCamera != null) {
                        cameraProviderFuture.get().unbindAll();
                    }
                    //将相机绑定到当前控件的生命周期
                    mCamera = cameraProvider.bindToLifecycle(mLifecycleOwner, cameraSelector, imageAnalysis, preview);
                } catch (Exception e) {
                }
            }
        }, ContextCompat.getMainExecutor(mContext));
    }

    private void initBarcodeAnalyser() {
        barcodeAnalyser = new BarcodeAnalyser();
        barcodeAnalyser.setPreviewView(mPreviewView);
        barcodeAnalyser.setAnalyze(true);
        barcodeAnalyser.setOnCameraAnalyserCallback(new OnCameraAnalyserCallback() {
            @Override
            public void onSuccess(Bitmap bitmap, List<Barcode> barcodes) {
                beepManager.playBeepSoundAndVibrate();
                if (onCameraAnalyserCallback != null) {
                    onCameraAnalyserCallback.onSuccess(bitmap, barcodes);
                }
            }
        });
    }

    private void initScaleGesture() {
        ScaleGestureDetector scaleGestureDetector = new ScaleGestureDetector(mContext, mOnScaleGestureListener);
        mPreviewView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                handlePreviewViewClickTap(event);
                if (scanConfig != null && scanConfig.isSupportZoom()) {
                    return scaleGestureDetector.onTouchEvent(event);
                }
                return false;
            }
        });
    }

    private ScaleGestureDetector.OnScaleGestureListener mOnScaleGestureListener = new ScaleGestureDetector.SimpleOnScaleGestureListener() {
        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            float scale = detector.getScaleFactor();
            if (mCamera != null) {
                float ratio = mCamera.getCameraInfo().getZoomState().getValue().getZoomRatio();
                zoomTo(ratio * scale);
            }
            return true;
        }

    };

    private void handlePreviewViewClickTap(MotionEvent event) {
        if (event.getPointerCount() == 1) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    isClickTap = true;
                    mDownX = event.getX();
                    mDownY = event.getY();
                    mLastHoveTapTime = System.currentTimeMillis();
                    break;
                case MotionEvent.ACTION_MOVE:
                    isClickTap = distance(mDownX, mDownY, event.getX(), event.getY()) < HOVER_TAP_SLOP;
                    break;
                case MotionEvent.ACTION_UP:
                    if (isClickTap && mLastHoveTapTime + HOVER_TAP_TIMEOUT > System.currentTimeMillis()) {
                        startFocusAndMetering(event.getX(), event.getY());
                    }
                    break;
            }
        }
    }

    private float distance(float aX, float aY, float bX, float bY) {
        float xDiff = aX - bX;
        float yDiff = aY - bY;
        return (float) Math.sqrt(xDiff * xDiff + yDiff * yDiff);
    }

    public void zoomTo(float ratio) {
        if (mCamera != null) {
            ZoomState zoomState = mCamera.getCameraInfo().getZoomState().getValue();
            float maxRatio = zoomState.getMaxZoomRatio();
            float minRatio = zoomState.getMinZoomRatio();
            float zoom = Math.max(Math.min(ratio, maxRatio), minRatio);
            mCamera.getCameraControl().setZoomRatio(zoom);
        }
    }

    private void startFocusAndMetering(float x, float y) {
        if (mCamera != null) {
            MeteringPoint point = mPreviewView.getMeteringPointFactory().createPoint(x, y);
            mCamera.getCameraControl().startFocusAndMetering(new FocusMeteringAction.Builder(point).build());
        }
    }

    public void openLight() {
        if (mCamera != null) {
            mCamera.getCameraControl().enableTorch(true);
        }
    }

    public void closeLight() {
        if (mCamera != null) {
            mCamera.getCameraControl().enableTorch(false);
        }
    }

    public void stopCamera() {
        try {
            if (cameraProviderFuture != null) {
                cameraProviderFuture.get().unbindAll();
            }
        } catch (Exception e) {
        }
    }

    public void release() {
        try {
            mPreviewView = null;
            mLifecycleOwner = null;
            stopCamera();
        } catch (Exception e) {
        }
    }

} 