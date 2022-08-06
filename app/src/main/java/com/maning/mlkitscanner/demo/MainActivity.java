package com.maning.mlkitscanner.demo;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.maning.mlkitscanner.scan.MNScanManager;
import com.maning.mlkitscanner.scan.callback.act.MNScanCallback;
import com.maning.mlkitscanner.scan.utils.ZXingUtils;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private Button btnScanDefault;
    private Button btnScanCustom;
    private TextView tvResults;

    private ImageView imageView;
    private EditText editText;
    private CheckBox checkbox;
    private CheckBox checkbox2;
    private Spinner mSpColorBlack;
    private Spinner mSpColorWhite;
    private Spinner mSpMargin;

    private String error_correction_level;
    private int margin = 0;
    private int color_black = Color.BLACK;
    private int color_white = Color.WHITE;
    private Spinner mSpErrorCorrectionLevel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        requestCameraPerm();
    }

    private void initView() {
        btnScanDefault = (Button) findViewById(R.id.btn_scan_default);
        btnScanCustom = (Button) findViewById(R.id.btn_scan_custom);
        btnScanDefault.setOnClickListener(this);
        btnScanCustom.setOnClickListener(this);
        tvResults = (TextView) findViewById(R.id.tv_results);

        imageView = (ImageView) findViewById(R.id.imageView);
        editText = (EditText) findViewById(R.id.editText);
        checkbox = (CheckBox) findViewById(R.id.checkbox);
        checkbox2 = (CheckBox) findViewById(R.id.checkbox2);
        mSpColorBlack = (Spinner) findViewById(R.id.sp_color_black);
        mSpColorWhite = (Spinner) findViewById(R.id.sp_color_white);
        mSpMargin = (Spinner) findViewById(R.id.sp_margin);
        mSpMargin.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                margin = Integer.parseInt(getResources().getStringArray(R.array.spinarr_margin)[position]);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        mSpColorBlack.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String str_color_black = getResources().getStringArray(R.array.spinarr_color_black)[position];
                if (str_color_black.equals("黑色")) {
                    color_black = Color.BLACK;
                } else if (str_color_black.equals("白色")) {
                    color_black = Color.WHITE;
                } else if (str_color_black.equals("蓝色")) {
                    color_black = Color.BLUE;
                } else if (str_color_black.equals("绿色")) {
                    color_black = Color.GREEN;
                } else if (str_color_black.equals("黄色")) {
                    color_black = Color.YELLOW;
                } else if (str_color_black.equals("红色")) {
                    color_black = Color.RED;
                } else {
                    color_black = Color.BLACK;
                }

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        mSpColorWhite.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String str_color_white = getResources().getStringArray(R.array.spinarr_color_white)[position];
                if (str_color_white.equals("黑色")) {
                    color_white = Color.BLACK;
                } else if (str_color_white.equals("白色")) {
                    color_white = Color.WHITE;
                } else if (str_color_white.equals("蓝色")) {
                    color_white = Color.BLUE;
                } else if (str_color_white.equals("绿色")) {
                    color_white = Color.GREEN;
                } else if (str_color_white.equals("黄色")) {
                    color_white = Color.YELLOW;
                } else if (str_color_white.equals("红色")) {
                    color_white = Color.RED;
                } else {
                    color_white = Color.WHITE;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        mSpErrorCorrectionLevel = (Spinner) findViewById(R.id.sp_error_correction_level);
        mSpErrorCorrectionLevel.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                error_correction_level = getResources().getStringArray(R.array.spinarr_error_correction)[position];
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    public void requestCameraPerm() {
        //判断权限
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.CAMERA}, 10010);
            }
        }
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.btn_scan_default) {
            MNScanManager.startScan(this, new MNScanCallback() {
                @Override
                public void onActivityResult(int resultCode, Intent data) {
                    handlerResult(resultCode, data);
                }
            });
        }else if (view.getId() == R.id.btn_scan_custom) {
            //跳转到自定义界面
            startActivity(new Intent(this, CustomConfigActivity.class));
        }
    }

    private void handlerResult(int resultCode, Intent data) {
        if (data == null) {
            return;
        }
        switch (resultCode) {
            default:
                break;
            case MNScanManager.RESULT_SUCCESS:
                ArrayList<String> results = data.getStringArrayListExtra(MNScanManager.INTENT_KEY_RESULT_SUCCESS);
                StringBuilder resultStr = new StringBuilder();
                for (int i = 0; i < results.size(); i++) {
                    resultStr.append("第" + (i + 1) + "条：");
                    resultStr.append(results.get(i));
                    resultStr.append("\n");
                }
                tvResults.setText(resultStr.toString());
                break;
            case MNScanManager.RESULT_FAIL:
                String resultError = data.getStringExtra(MNScanManager.INTENT_KEY_RESULT_ERROR);
                showToast(resultError);
                break;
            case MNScanManager.RESULT_CANCLE:
                showToast("取消扫码");
                break;
        }
    }

    private void showToast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }


    public void createQRImage(View view) {
        String str = editText.getText().toString();

        if (TextUtils.isEmpty(str)) {
            Toast.makeText(this, "字符串不能为空", Toast.LENGTH_SHORT).show();
            return;
        }

        Bitmap qrImage;
        Bitmap logo = null;
        Bitmap foreground_bitmap = null;
        if (checkbox.isChecked()) {
            logo = BitmapFactory.decodeResource(getResources(), R.drawable.ic_wx);
        }
        if (checkbox2.isChecked()) {
            foreground_bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.tmp);
        }
        qrImage = ZXingUtils.createQRCodeImage(str, 500, margin, color_black, color_white, error_correction_level, logo, foreground_bitmap);
        if (qrImage != null) {
            imageView.setImageBitmap(qrImage);
        } else {
            Toast.makeText(this, "生成失败", Toast.LENGTH_SHORT).show();
        }

    }
}