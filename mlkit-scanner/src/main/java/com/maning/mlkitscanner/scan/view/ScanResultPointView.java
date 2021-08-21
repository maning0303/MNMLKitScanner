package com.maning.mlkitscanner.scan.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.mlkit.vision.barcode.Barcode;
import com.maning.mlkitscanner.R;
import com.maning.mlkitscanner.scan.model.MNScanConfig;
import com.maning.mlkitscanner.scan.utils.CommonUtils;
import com.maning.mlkitscanner.scan.utils.StatusBarUtil;

import java.util.List;

import static android.graphics.drawable.GradientDrawable.RECTANGLE;

/**
 * @author : maning
 * @date : 2021/1/7
 * @desc : 扫描结果点View展示
 */
public class ScanResultPointView extends FrameLayout {

    private MNScanConfig scanConfig;
    private List<Barcode> resultPoint;
    private OnResultPointClickListener onResultPointClickListener;

    private int resultPointColor;
    private int resultPointStrokeColor;
    private int resultPointWithdHeight;
    private int resultPointRadiusCorners;
    private int resultPointStrokeWidth;
    private TextView tv_cancle;
    private FrameLayout fl_result_point_root;
    private View fakeStatusBar;
    private int statusBarHeight;
    private ImageView iv_show_result;
    private Bitmap barcodeBitmap;

    public void setOnResultPointClickListener(OnResultPointClickListener onResultPointClickListener) {
        this.onResultPointClickListener = onResultPointClickListener;
    }

    public interface OnResultPointClickListener {
        void onPointClick(String result);

        void onCancle();
    }

    public ScanResultPointView(Context context) {
        this(context, null);
    }

    public ScanResultPointView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ScanResultPointView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    private void initView() {
        View view = LayoutInflater.from(getContext()).inflate(R.layout.mn_scan_result_point_view, this);
        fakeStatusBar = view.findViewById(R.id.fakeStatusBar2);
        iv_show_result = view.findViewById(R.id.iv_show_result);
        tv_cancle = view.findViewById(R.id.tv_cancle);
        fl_result_point_root = view.findViewById(R.id.fl_result_point_root);

        statusBarHeight = StatusBarUtil.getStatusBarHeight(getContext());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            ViewGroup.LayoutParams fakeStatusBarLayoutParams = fakeStatusBar.getLayoutParams();
            fakeStatusBarLayoutParams.height = statusBarHeight;
            fakeStatusBar.setLayoutParams(fakeStatusBarLayoutParams);
        }

        tv_cancle.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                //隐藏View
                if (onResultPointClickListener != null) {
                    onResultPointClickListener.onCancle();
                }
                removeAllPoints();
            }
        });
        iv_show_result.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                //拦截点击事件
            }
        });
    }

    public void setScanConfig(MNScanConfig config) {
        scanConfig = config;
        initResultPointConfigs();
    }

    private void initResultPointConfigs() {
        if (scanConfig == null) {
            return;
        }
        resultPointRadiusCorners = CommonUtils.dip2px(getContext(), scanConfig.getResultPointCorners());
        resultPointWithdHeight = CommonUtils.dip2px(getContext(), scanConfig.getResultPointWithdHeight());
        resultPointStrokeWidth = CommonUtils.dip2px(getContext(), scanConfig.getResultPointStrokeWidth());
        String resultPointColorStr = scanConfig.getResultPointColor();
        String resultPointStrokeColorStr = scanConfig.getResultPointStrokeColor();
        if (resultPointWithdHeight == 0) {
            resultPointWithdHeight = CommonUtils.dip2px(getContext(), 36);
        }
        if (resultPointRadiusCorners == 0) {
            resultPointRadiusCorners = CommonUtils.dip2px(getContext(), 36);
        }
        if (resultPointStrokeWidth == 0) {
            resultPointStrokeWidth = CommonUtils.dip2px(getContext(), 3);
        }
        if (!TextUtils.isEmpty(resultPointColorStr)) {
            resultPointColor = Color.parseColor(resultPointColorStr);
        } else {
            resultPointColor = getContext().getResources().getColor(R.color.mn_scan_viewfinder_laser_result_point);
        }
        if (!TextUtils.isEmpty(resultPointStrokeColorStr)) {
            resultPointStrokeColor = Color.parseColor(resultPointStrokeColorStr);
        } else {
            resultPointStrokeColor = getContext().getResources().getColor(R.color.mn_scan_viewfinder_laser_result_point_border);
        }
    }

    public void setDatas(List<Barcode> results, Bitmap barcode) {
        this.resultPoint = results;
        this.barcodeBitmap = barcode;
        drawableResultPoint();
    }

    public void removeAllPoints() {
        fl_result_point_root.removeAllViews();
    }

    private void drawableResultPoint() {
        Log.e(">>>>>>", "drawableResultPoint---start");
        iv_show_result.setImageBitmap(barcodeBitmap);
        removeAllPoints();
        if (resultPoint == null || resultPoint.size() == 0) {
            if (onResultPointClickListener != null) {
                onResultPointClickListener.onCancle();
            }
            return;
        }
        if (scanConfig == null) {
            scanConfig = new MNScanConfig.Builder().builder();
        }
        if (resultPoint.size() == 1) {
            tv_cancle.setVisibility(View.INVISIBLE);
        } else {
            tv_cancle.setVisibility(View.VISIBLE);
        }

        for (int j = 0; j < resultPoint.size(); j++) {
            Barcode barcode = resultPoint.get(j);
            Rect boundingBox = barcode.getBoundingBox();
            int centerX = boundingBox.centerX();
            int centerY = boundingBox.centerY();

            View inflate = LayoutInflater.from(getContext()).inflate(R.layout.mn_scan_result_point_item_view, null);
            RelativeLayout rl_root = inflate.findViewById(R.id.rl_root);
            ImageView iv_point_bg = inflate.findViewById(R.id.iv_point_bg);
            ImageView iv_point_arrow = inflate.findViewById(R.id.iv_point_arrow);

            //位置
            RelativeLayout.LayoutParams lpRoot = new RelativeLayout.LayoutParams(resultPointWithdHeight, resultPointWithdHeight);
            rl_root.setLayoutParams(lpRoot);

            rl_root.setX(centerX - resultPointWithdHeight / 2.0f);
            rl_root.setY(centerY - resultPointWithdHeight / 2.0f);

            GradientDrawable gradientDrawable = new GradientDrawable();
            gradientDrawable.setCornerRadius(resultPointRadiusCorners);
            gradientDrawable.setShape(RECTANGLE);
            gradientDrawable.setStroke(resultPointStrokeWidth, resultPointStrokeColor);
            gradientDrawable.setColor(resultPointColor);

            iv_point_bg.setImageDrawable(gradientDrawable);

            //点的大小
            ViewGroup.LayoutParams lpPoint = iv_point_bg.getLayoutParams();
            lpPoint.width = resultPointWithdHeight;
            lpPoint.height = resultPointWithdHeight;
            iv_point_bg.setLayoutParams(lpPoint);

            //箭头大小
            if (resultPoint.size() > 1) {
                ViewGroup.LayoutParams lpArrow = iv_point_arrow.getLayoutParams();
                lpArrow.width = resultPointWithdHeight / 2;
                lpArrow.height = resultPointWithdHeight / 2;
                iv_point_arrow.setLayoutParams(lpArrow);
                iv_point_arrow.setVisibility(View.VISIBLE);
            } else {
                //一个不需要箭头
                iv_point_arrow.setVisibility(View.GONE);
            }

            iv_point_bg.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (onResultPointClickListener != null) {
                        onResultPointClickListener.onPointClick(barcode.getRawValue());
                    }
                }
            });

            fl_result_point_root.addView(inflate);
        }
        int childCount = fl_result_point_root.getChildCount();
        Log.e(">>>>>>", "fl_result_point_root---childCount：" + childCount);
        if (childCount <= 0) {
            //关闭页面
            if (onResultPointClickListener != null) {
                onResultPointClickListener.onCancle();
            }
        }
        Log.e(">>>>>>", "drawableResultPoint---end");
    }

}