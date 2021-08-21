package com.maning.mlkitscanner.scan.ui;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.view.PreviewView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.mlkit.vision.barcode.Barcode;
import com.google.mlkit.vision.common.InputImage;
import com.maning.mlkitscanner.R;
import com.maning.mlkitscanner.scan.MNScanManager;
import com.maning.mlkitscanner.scan.callback.OnCameraAnalyserCallback;
import com.maning.mlkitscanner.scan.camera.CameraManager;
import com.maning.mlkitscanner.scan.model.MNScanConfig;
import com.maning.mlkitscanner.scan.utils.BeepManager;
import com.maning.mlkitscanner.scan.utils.ImageUtils;
import com.maning.mlkitscanner.scan.utils.StatusBarUtil;
import com.maning.mlkitscanner.scan.view.ScanActionMenuView;
import com.maning.mlkitscanner.scan.view.ScanResultPointView;
import com.maning.mlkitscanner.scan.view.ViewfinderView;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

public class ScanPreviewActivity extends AppCompatActivity {

    //用来保存当前Activity
    private static WeakReference<ScanPreviewActivity> sActivityRef;
    private static final int REQUEST_CODE_PICK_IMAGE = 10010;
    private static final int REQUEST_CODE_PERMISSION_CAMERA = 10011;
    private static final int REQUEST_CODE_PERMISSION_STORAGE = 10012;
    private Context mContext;
    //闪光灯是否打开
    private boolean is_light_on = false;
    private MNScanConfig mScanConfig;

    private CameraManager cameraManager;
    private View fakeStatusBar;
    private PreviewView mPreviewView;
    private ViewfinderView viewfinderView;
    private ScanResultPointView result_point_view;
    private ScanActionMenuView action_menu_view;
    private RelativeLayout rl_act_root;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.mn_scan_activity_scan_preview);
        mContext = this;
        sActivityRef = new WeakReference<>(this);
        initConfig();
        initViews();
        initCamera();
        initStatusBar();
        initPermission();
    }


    private void initStatusBar() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            StatusBarUtil.setTransparentForWindow(this);
            int statusBarHeight = StatusBarUtil.getStatusBarHeight(mContext);
            Log.e("======", "statusBarHeight--" + statusBarHeight);
            ViewGroup.LayoutParams fakeStatusBarLayoutParams = fakeStatusBar.getLayoutParams();
            fakeStatusBarLayoutParams.height = statusBarHeight;
            fakeStatusBar.setLayoutParams(fakeStatusBarLayoutParams);
            //状态栏文字颜色
            if (mScanConfig.isStatusBarDarkMode()) {
                StatusBarUtil.setDarkMode(this);
            }
            //状态栏颜色
            String statusBarColor = mScanConfig.getStatusBarColor();
            fakeStatusBar.setBackgroundColor(Color.parseColor(statusBarColor));
        } else {
            ViewGroup.LayoutParams fakeStatusBarLayoutParams = fakeStatusBar.getLayoutParams();
            fakeStatusBarLayoutParams.height = 0;
            fakeStatusBar.setLayoutParams(fakeStatusBarLayoutParams);
        }
    }

    private void initPermission() {
        //检查相机权限
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                //没有相机权限
                requestPermissions(new String[]{Manifest.permission.CAMERA}, REQUEST_CODE_PERMISSION_CAMERA);
            } else {
                startCamera();
            }
        } else {
            startCamera();
        }
    }

    private void initCamera() {
        cameraManager = CameraManager.getInstance(sActivityRef.get(), mPreviewView);
        cameraManager.setScanConfig(mScanConfig);
        cameraManager.setOnCameraAnalyserCallback(new OnCameraAnalyserCallback() {
            @Override
            public void onSuccess(Bitmap bitmap, List<Barcode> barcodes) {
                result_point_view.setDatas(barcodes, bitmap);
                result_point_view.setVisibility(View.VISIBLE);
                if (barcodes.size() == 1) {
                    finishSuccess(barcodes.get(0).getRawValue());
                }
            }
        });
    }

    private void startCamera() {
        cameraManager.startCamera();
    }

    private void initConfig() {
        mScanConfig = (MNScanConfig) getIntent().getSerializableExtra(MNScanManager.INTENT_KEY_CONFIG_MODEL);
        if (mScanConfig == null) {
            mScanConfig = new MNScanConfig.Builder().builder();
        }
    }

    private void initViews() {
        rl_act_root = (RelativeLayout) findViewById(R.id.rl_act_root);
        mPreviewView = (PreviewView) findViewById(R.id.previewView);
        mPreviewView.setScaleType(PreviewView.ScaleType.FILL_CENTER);
        fakeStatusBar = (View) findViewById(R.id.fakeStatusBar);
        viewfinderView = (ViewfinderView) findViewById(R.id.viewfinderView);
        action_menu_view = (ScanActionMenuView) findViewById(R.id.action_menu_view);
        result_point_view = (ScanResultPointView) findViewById(R.id.result_point_view);

        action_menu_view.setOnScanActionMenuListener(new ScanActionMenuView.OnScanActionMenuListener() {
            @Override
            public void onClose() {
                finishCancle();
            }

            @Override
            public void onLight() {
                if (is_light_on) {
                    closeLight();
                } else {
                    openLight();
                }
            }

            @Override
            public void onPhoto() {
                getImageFromAlbum();
            }
        });

        result_point_view.setOnResultPointClickListener(new ScanResultPointView.OnResultPointClickListener() {
            @Override
            public void onPointClick(String result) {
                finishSuccess(result);
            }

            @Override
            public void onCancle() {
                cameraManager.setAnalyze(true);
                result_point_view.removeAllPoints();
                result_point_view.setVisibility(View.GONE);
            }
        });

        viewfinderView.setScanConfig(mScanConfig);
        result_point_view.setScanConfig(mScanConfig);
        action_menu_view.setScanConfig(mScanConfig, MNScanConfig.mCustomViewBindCallback);
    }

    private void openLight() {
        if (!is_light_on) {
            is_light_on = true;
            action_menu_view.openLight();
            cameraManager.openLight();
        }
    }

    private void closeLight() {
        if (is_light_on) {
            is_light_on = false;
            action_menu_view.closeLight();
            cameraManager.closeLight();
        }
    }

    /**
     * 获取相册中的图片
     */
    public void getImageFromAlbum() {
        if (checkStoragePermission()) {
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_PICK);
            intent.setType("image/*");
            startActivityForResult(intent, REQUEST_CODE_PICK_IMAGE);
        }
    }

    private boolean checkStoragePermission() {
        //判断权限
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_CODE_PERMISSION_STORAGE);
                return false;
            }
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CODE_PERMISSION_CAMERA:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission Granted 授予权限
                    //用户同意了权限申请
                    startCamera();
                } else {
                    // Permission Denied 权限被拒绝
                    Toast.makeText(mContext, "初始化相机失败,相机权限被拒绝", Toast.LENGTH_SHORT).show();
                    finishFailed("初始化相机失败,相机权限被拒绝");
                }
                break;
            case REQUEST_CODE_PERMISSION_STORAGE:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //用户同意使用write
                    getImageFromAlbum();
                } else {
                    //缺少权限
                    Toast.makeText(mContext, "打开相册失败,读写权限被拒绝", Toast.LENGTH_SHORT).show();
                }
            default:
                break;
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //去相册选择图片
        if (requestCode == REQUEST_CODE_PICK_IMAGE && resultCode == RESULT_OK) {
            if (data == null) {
                return;
            }
            Bitmap decodeAbleBitmap = ImageUtils.getBitmap(mContext, data.getData());
            if (decodeAbleBitmap == null) {
                Log.e("======", "decodeAbleBitmap == null");
                return;
            }
            cameraManager.setAnalyze(false);
            new Thread(new Runnable() {
                @Override
                public void run() {
                    //分析这个图片
                    InputImage inputImage = InputImage.fromBitmap(decodeAbleBitmap, 0);
                    cameraManager.getBarcodeAnalyser().getBarcodeScanner().process(inputImage)
                            .addOnSuccessListener(new OnSuccessListener<List<Barcode>>() {
                                @Override
                                public void onSuccess(@NonNull List<Barcode> barcodes) {
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            Log.e("======", "barcodes.size():" + barcodes.size());
                                            if (barcodes.size() == 0) {
                                                cameraManager.setAnalyze(true);
                                                Toast.makeText(mContext, "未找到二维码或者条形码", Toast.LENGTH_SHORT).show();
                                                return;
                                            }
                                            ArrayList<String> results = new ArrayList<>();
                                            for (Barcode barcode : barcodes) {
                                                String value = barcode.getRawValue();
                                                Log.e("======", "value:" + value);
                                                results.add(value);
                                            }

                                            finishSuccess(results);
                                        }
                                    });
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.e("======", "onFailure---:" + e.toString());
                                }
                            });
                }
            }).start();
        }
    }


    @Override
    public void onBackPressed() {
        if (result_point_view.getVisibility() == View.VISIBLE) {
            cameraManager.setAnalyze(true);
            result_point_view.removeAllPoints();
            result_point_view.setVisibility(View.GONE);
            return;
        }
        //取消扫码
        finishCancle();
    }

    @Override
    protected void onDestroy() {
        cameraManager.release();
        super.onDestroy();
    }

    private void finishCancle() {
        setResult(MNScanManager.RESULT_CANCLE, new Intent());
        finishFinal();
    }

    private void finishFailed(String errorMsg) {
        Intent intent = new Intent();
        intent.putExtra(MNScanManager.INTENT_KEY_RESULT_ERROR, errorMsg);
        setResult(MNScanManager.RESULT_FAIL, intent);
        finishFinal();
    }

    private void finishSuccess(String result) {
        ArrayList<String> results = new ArrayList<>();
        results.add(result);
        finishSuccess(results);
    }

    private void finishSuccess(ArrayList<String> results) {
        Intent intent = new Intent();
        intent.putStringArrayListExtra(MNScanManager.INTENT_KEY_RESULT_SUCCESS, results);
        setResult(MNScanManager.RESULT_SUCCESS, intent);
        finishFinal();
    }

    private void finishFinal() {
        closeLight();
        MNScanConfig.mCustomViewBindCallback = null;
        sActivityRef = null;
        viewfinderView.destroyView();
        cameraManager.release();
        rl_act_root.removeView(viewfinderView);
        rl_act_root.removeView(mPreviewView);
        rl_act_root.removeView(action_menu_view);
        finish();
        overridePendingTransition(0, mScanConfig.getActivityExitAnime() == 0 ? R.anim.mn_scan_activity_bottom_out : mScanConfig.getActivityExitAnime());
    }

    //---------对外提供方法----------

    /**
     * 关闭当前Activity
     */
    public static void closeScanPage() {
        if (sActivityRef != null && sActivityRef.get() != null) {
            sActivityRef.get().finishCancle();
        }
    }

    /**
     * 打开相册扫描图片
     */
    public static void openAlbumPage() {
        if (sActivityRef != null && sActivityRef.get() != null) {
            sActivityRef.get().getImageFromAlbum();
        }
    }

    /**
     * 打开手电筒
     */
    public static void openScanLight() {
        if (sActivityRef != null && sActivityRef.get() != null) {
            sActivityRef.get().openLight();
        }
    }

    /**
     * 关闭手电筒
     */
    public static void closeScanLight() {
        if (sActivityRef != null && sActivityRef.get() != null) {
            sActivityRef.get().closeLight();
        }
    }

    /**
     * 是否开启手电筒
     */
    public static boolean isLightOn() {
        if (sActivityRef != null && sActivityRef.get() != null) {
            return sActivityRef.get().is_light_on;
        }
        return false;
    }
}