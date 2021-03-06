/*
 * Copyright (C) 2008 ZXing authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.maning.mlkitscanner.scan.view;

import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;

import com.maning.mlkitscanner.R;
import com.maning.mlkitscanner.scan.model.MNScanConfig;
import com.maning.mlkitscanner.scan.utils.CommonUtils;


/**
 * This view is overlaid on top of the camera preview. It adds the viewfinder rectangle and partial
 * transparency outside it, as well as the laser scanner animation and result points.
 *
 * @author dswitkin@google.com (Daniel Switkin)
 */
public final class ViewfinderView extends View {

    private static final String TAG = "ViewfinderView";
    private final Paint paint;
    private Paint paintResultPoint;
    private Paint paintText;
    private Paint paintTextBg;
    private Paint paintLine;
    private Paint paintLaser;
    private int maskColor;
    private int laserColor;

    private Rect frame;
    private String hintMsg;
    private String hintTextColor = "#FFFFFF";
    private int hintTextSize = 13;
    private int linePosition = 0;
    private int margin;
    private int laserLineW;
    private int cornerLineH;
    private int cornerLineW;
    private int gridColumn;
    private int gridHeight;

    //??????????????????0??????1??????
    private MNScanConfig.LaserStyle laserStyle = MNScanConfig.LaserStyle.Line;

    private MNScanConfig mnScanConfig;

    private ValueAnimator anim;
    private boolean needAnimation = true;

    public ViewfinderView(Context context, AttributeSet attrs) {
        super(context, attrs);

        // Initialize these once for performance rather than calling them every time in onDraw().
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paintResultPoint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paintText = new Paint(Paint.ANTI_ALIAS_FLAG);
        paintTextBg = new Paint(Paint.ANTI_ALIAS_FLAG);
        paintLine = new Paint(Paint.ANTI_ALIAS_FLAG);
        paintLaser = new Paint(Paint.ANTI_ALIAS_FLAG);
        Resources resources = getResources();
        maskColor = resources.getColor(R.color.mn_scan_viewfinder_mask);
        laserColor = resources.getColor(R.color.mn_scan_viewfinder_laser);
        hintMsg = "????????????/?????????";
        //??????
        paintText.setColor(Color.WHITE);
        paintText.setTextSize(CommonUtils.sp2px(getContext(), hintTextSize));
        paintText.setTextAlign(Paint.Align.CENTER);
        paintTextBg.setColor(laserColor);
        paintTextBg.setTextAlign(Paint.Align.CENTER);
        //??????
        paintLine.setColor(laserColor);
        //?????????
        paintLaser.setColor(laserColor);
        paintResultPoint.setColor(laserColor);
        //?????????????????????
        initSize();
    }

    private void initSize() {
        //??????
        margin = CommonUtils.dip2px(getContext(), 4);
        //??????????????????
        laserLineW = CommonUtils.dip2px(getContext(), 4);
        //????????????
        cornerLineH = CommonUtils.dip2px(getContext(), 2);
        cornerLineW = CommonUtils.dip2px(getContext(), 14);
        //???????????????????????????
        gridColumn = 24;
        gridHeight = CommonUtils.getScreenWidth(getContext()) * 7 / 10;
    }

    /**
     * ????????????
     *
     * @param laserColor
     */
    public void setLaserColor(int laserColor) {
        this.laserColor = laserColor;
        paintLine.setColor(this.laserColor);
        paintLaser.setColor(this.laserColor);
    }

    /**
     * ??????????????????
     *
     * @param laserStyle
     */
    public void setLaserStyle(MNScanConfig.LaserStyle laserStyle) {
        this.laserStyle = laserStyle;
    }

    /**
     * ?????????
     *
     * @param maskColor
     */
    public void setMaskColor(int maskColor) {
        this.maskColor = maskColor;
    }

    /**
     * ??????????????????
     *
     * @param gridColumn
     */
    public void setGridScannerColumn(int gridColumn) {
        if (gridColumn > 0) {
            this.gridColumn = gridColumn;
        }
    }

    /**
     * ?????????????????????????????????????????????
     *
     * @param gridHeight
     */
    public void setGridScannerHeight(int gridHeight) {
        this.gridHeight = gridHeight;
    }


    public void setScanConfig(MNScanConfig scanConfig) {
        this.mnScanConfig = scanConfig;

        //??????????????????
        setHintText(mnScanConfig.getScanHintText(), mnScanConfig.getScanHintTextColor(), mnScanConfig.getScanHintTextSize());

        //?????????????????????
        if (!TextUtils.isEmpty(mnScanConfig.getScanColor())) {
            setLaserColor(Color.parseColor(mnScanConfig.getScanColor()));
        }
        setLaserStyle(mnScanConfig.getLaserStyle());

        if (!TextUtils.isEmpty(mnScanConfig.getBgColor())) {
            setMaskColor(Color.parseColor(mnScanConfig.getBgColor()));
        }
        setGridScannerColumn(mnScanConfig.getGridScanLineColumn());
        setGridScannerHeight(mnScanConfig.getGridScanLineHeight());
    }

    /**
     * ????????????
     */
    public void setHintText(String hintMsg, String hintTextColor, int hintTextSize) {
        //??????
        if (!TextUtils.isEmpty(hintMsg)) {
            this.hintMsg = hintMsg;
        } else {
            this.hintMsg = "";
        }
        //????????????
        if (!TextUtils.isEmpty(hintTextColor)) {
            this.hintTextColor = hintTextColor;
        }
        //????????????
        if (hintTextSize > 0) {
            this.hintTextSize = hintTextSize;
        }
        paintText.setColor(Color.parseColor(this.hintTextColor));
        paintText.setTextSize(CommonUtils.sp2px(getContext(), this.hintTextSize));
    }

    @SuppressLint("DrawAllocation")
    @Override
    public void onDraw(Canvas canvas) {
        int width = canvas.getWidth();
        int height = canvas.getHeight();
        int txtMargin = CommonUtils.dip2px(getContext(), 20);

        int frameWidth = width * 7 / 10;
        if (mnScanConfig != null && mnScanConfig.isFullScreenScan()) {
            frameWidth = width * 9 / 10;
        }
        int left = (width - frameWidth) / 2;
        int top = (height - frameWidth) / 2;
        frame = new Rect(left, top, left + frameWidth, top + frameWidth);

        //????????????
        frame.top = (height - (frame.right - frame.left)) / 2;
        frame.bottom = frame.top + (frame.right - frame.left);
        frame.left = (width - (frame.right - frame.left)) / 2;
        frame.right = frame.left + (frame.right - frame.left);

        paintLine.setShader(null);
        //????????????
        int rectH = cornerLineW;
        int rectW = cornerLineH;
        //???????????????????????????
        if (mnScanConfig != null && mnScanConfig.isFullScreenScan()) {
            //????????????
            paint.setColor(Color.TRANSPARENT);
            canvas.drawRect(0, 0, width, height, paint);
            //??????????????????
            laserLineW = CommonUtils.dip2px(getContext(), 4);
        } else {
            //??????????????????
            laserLineW = CommonUtils.dip2px(getContext(), 2);
            // ???????????????
            paint.setColor(maskColor);

            canvas.drawRect(0, 0, width, frame.top, paint);
            canvas.drawRect(0, frame.top, frame.left, frame.bottom + 1, paint);
            canvas.drawRect(frame.right + 1, frame.top, width, frame.bottom + 1, paint);
            canvas.drawRect(0, frame.bottom + 1, width, height, paint);
            //?????????
            canvas.drawRect(frame.left, frame.top, frame.left + rectW, frame.top + rectH, paintLine);
            canvas.drawRect(frame.left, frame.top, frame.left + rectH, frame.top + rectW, paintLine);
            //?????????
            canvas.drawRect(frame.right - rectW, frame.top, frame.right + 1, frame.top + rectH, paintLine);
            canvas.drawRect(frame.right - rectH, frame.top, frame.right + 1, frame.top + rectW, paintLine);
            //?????????
            canvas.drawRect(frame.left, frame.bottom - rectH, frame.left + rectW, frame.bottom + 1, paintLine);
            canvas.drawRect(frame.left, frame.bottom - rectW, frame.left + rectH, frame.bottom + 1, paintLine);
            //?????????
            canvas.drawRect(frame.right - rectW, frame.bottom - rectH, frame.right + 1, frame.bottom + 1, paintLine);
            canvas.drawRect(frame.right - rectH, frame.bottom - rectW, frame.right + 1, frame.bottom + 1, paintLine);
        }

        //???????????????????????????????????????
//        float textWidth = CommonUtils.getTextWidth(hintMsg, paintText);
//        float textHeight = CommonUtils.getTextHeight(hintMsg, paintText);
//        float startX = (width - textWidth) / 2 - CommonUtils.dip2px(getContext(), 20);
//        float startY = frame.bottom + txtMargin;
//        float endX = startX + textWidth + CommonUtils.dip2px(getContext(), 40);
//        float endY = startY + textHeight + CommonUtils.dip2px(getContext(), 12);
//        RectF rectF = new RectF(startX, startY, endX, endY);
//        canvas.drawRoundRect(rectF, 100, 100, paintTextBg);
//        if (mnScanConfig.isSupportZoom() && mnScanConfig.isShowZoomController() && mnScanConfig.getZoomControllerLocation() == MNScanConfig.ZoomControllerLocation.Bottom) {
//            canvas.drawText(hintMsg, width / 2, frame.top - txtMargin, paintText);
//        } else {
//            canvas.drawText(hintMsg, width / 2, startY + (rectF.height() - textHeight) + (rectF.height() - textHeight) / 2f, paintText);
//        }
        //??????
        canvas.drawText(hintMsg, width / 2, frame.bottom + txtMargin + CommonUtils.getTextHeight(hintMsg, paintText), paintText);

        //?????????????????????
        if (linePosition <= 0) {
            linePosition = frame.top + margin;
        }
        //?????????
        if (laserStyle == MNScanConfig.LaserStyle.Line) {
            drawLineScanner(canvas, frame);
        } else if (laserStyle == MNScanConfig.LaserStyle.Grid) {
            drawGridScanner(canvas, frame);
        }
        //????????????
        startAnimation();
    }

    /**
     * ?????????????????????
     *
     * @param canvas
     * @param frame
     */
    private void drawLineScanner(Canvas canvas, Rect frame) {
        //????????????
        LinearGradient linearGradient = new LinearGradient(
                frame.left, linePosition,
                frame.left, linePosition + laserLineW,
                shadeColor(laserColor),
                laserColor,
                Shader.TileMode.MIRROR);
        paintLine.setShader(linearGradient);
        RectF rect = new RectF(frame.left + margin, linePosition, frame.right - margin, linePosition + laserLineW);
        canvas.drawOval(rect, paintLaser);
    }

    /**
     * ?????????????????????
     *
     * @param canvas
     * @param frame
     */
    private void drawGridScanner(Canvas canvas, Rect frame) {
        if (gridHeight <= 0) {
            gridHeight = frame.bottom - frame.top;
        }
        int stroke = 2;
        paintLaser.setStrokeWidth(stroke);
        //??????Y???????????????
        int startY;
        if (gridHeight > 0 && linePosition - frame.top > gridHeight) {
            startY = linePosition - gridHeight;
        } else {
            startY = frame.top;
        }

        LinearGradient linearGradient = new LinearGradient(frame.left + frame.width() / 2, startY, frame.left + frame.width() / 2, linePosition, new int[]{shadeColor(laserColor), laserColor}, new float[]{0, 1f}, LinearGradient.TileMode.CLAMP);
        //????????????????????????
        paintLaser.setShader(linearGradient);

        float wUnit = frame.width() * 1.0f / gridColumn;
        float hUnit = wUnit;
        //????????????????????????
        for (int i = 0; i <= gridColumn; i++) {
            float startX;
            float stopX;
            if (i == 0) {
                startX = frame.left + 1;
            } else if (i == gridColumn) {
                startX = frame.left + i * wUnit - 1;
            } else {
                startX = frame.left + i * wUnit;
            }
            stopX = startX;
            canvas.drawLine(startX, startY, stopX, linePosition, paintLaser);
        }
        int height = gridHeight > 0 && linePosition - frame.top > gridHeight ? gridHeight : linePosition - frame.top;
        //????????????????????????
        for (int i = 0; i <= height / hUnit; i++) {
            canvas.drawLine(frame.left, linePosition - i * hUnit, frame.right, linePosition - i * hUnit, paintLaser);
        }
    }

    /**
     * ??????????????????
     *
     * @param color
     * @return
     */
    public int shadeColor(int color) {
        String hax = Integer.toHexString(color);
        String result = "01" + hax.substring(2);
        return Integer.valueOf(result, 16);
    }

    public void startAnimation() {
        if (anim != null) {
            return;
        }
        anim = ValueAnimator.ofInt(frame.top - 2, frame.bottom + 2);
        anim.setRepeatCount(ValueAnimator.INFINITE);
        anim.setRepeatMode(ValueAnimator.RESTART);
        anim.setDuration(2400);
        anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                if (!needAnimation) {
                    return;
                }
                linePosition = (int) animation.getAnimatedValue();
                try {
                    postInvalidate(
                            frame.left - 2,
                            frame.top - 2,
                            frame.right + 2,
                            frame.bottom + 2);
                } catch (Exception e) {
                    postInvalidate();
                }
            }
        });
        anim.start();
    }

    public void destroyView() {
        if (anim != null) {
            anim.removeAllUpdateListeners();
            anim.cancel();
            anim.end();
            anim = null;
        }
    }

}
