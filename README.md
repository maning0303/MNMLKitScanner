#   MNMLKitScanner 快速集成二维码扫描,速度比zxing快

##  基于Google ML Kit 快速集成二维码扫描，速度比zxing快，可配置相册，闪光灯，相机可以调整焦距放大缩小，自定义扫描线颜色，自定义背景颜色，自定义遮罩层，支持同时扫多个二维码和条形码
[![](https://jitpack.io/v/maning0303/MNMLKitScanner.svg)](https://jitpack.io/#maning0303/MNMLKitScanner)

##  功能：
    1：二维码扫描，手势缩放，无拉伸，样式自定义
    2：相册中选取图片识别
    3: 相机可以调整焦距放大缩小
    4: 完全自定义遮罩层
    5: 支持微信多个二维码/条形码同时扫描


## 截图:

<div align="center">
<img src = "screenshots/mn_mlkit_scanner_ss_01.jpg" width=200 >
<img src = "screenshots/mn_mlkit_scanner_ss_02.jpg" width=200 >
<img src = "screenshots/mn_mlkit_scanner_ss_03.jpg" width=200 >
<img src = "screenshots/mn_mlkit_scanner_ss_04.jpg" width=200 >
</div>

## 如何添加
### Gradle添加：
#### 1.在Project的build.gradle中添加仓库地址

``` gradle
	allprojects {
		repositories {
			...
			maven { url "https://jitpack.io" }
		}
	}
```

#### 2.在Module目录下的build.gradle中添加依赖
``` gradle
	dependencies {

            implementation 'com.github.maning0303:MNMLKitScanner:V1.0.0'

	}
```

## 使用方法:
###  进入需要提前申请相机权限；进入需要提前申请相机权限；进入需要提前申请相机权限；


``` java
        1：开始扫描：
            //默认扫描
            MNScanManager.startScan(this, new MNScanCallback() {
                   @Override
                   public void onActivityResult(int resultCode, Intent data) {
                    switch (resultCode) {
                        case MNScanManager.RESULT_SUCCESS:
                            String resultSuccess = data.getStringExtra(MNScanManager.INTENT_KEY_RESULT_SUCCESS);
                            break;
                        case MNScanManager.RESULT_FAIL:
                            String resultError = data.getStringExtra(MNScanManager.INTENT_KEY_RESULT_ERROR);
                            break;
                        case MNScanManager.RESULT_CANCLE:
                            showToast("取消扫码");
                            break;
                    }
                   }
            });
            
            //自定义扫描
            MNScanConfig scanConfig = new MNScanConfig.Builder()
                    //设置完成震动
                    .isShowVibrate(true)
                    //扫描完成声音
                    .isShowBeep(true)
                    //显示相册功能
                    .isShowPhotoAlbum(true)
                    //显示闪光灯
                    .isShowLightController(true)
                    //打开扫描页面的动画
                    .setActivityOpenAnime(R.anim.activity_anmie_in)
                    //退出扫描页面动画
                    .setActivityExitAnime(R.anim.activity_anmie_out)
                    //自定义文案
                    .setScanHintText("xxxx")
                    .setScanHintTextColor("#FF0000")
                    .setScanHintTextSize(14)
                    //扫描线的颜色
                    .setScanColor("#FF0000")
                    //是否支持手势缩放
                    .setSupportZoom(true)
                    //扫描线样式
                    .setLaserStyle(MNScanConfig.LaserStyle.Grid/MNScanConfig.LaserStyle.Line)
                    //背景颜色
                    .setBgColor("")
                    //网格扫描线的列数
                    .setGridScanLineColumn(30)
                    //网格高度
                    .setGridScanLineHeight(300)
                    //是否全屏扫描,默认全屏
                    .setFullScreenScan(true)
                    //单位dp
                    .setResultPointConfigs(36, 12, 3, colorResultPointStroke, colorResultPoint)
                    //状态栏设置
                    .setStatusBarConfigs(colorStatusBar, true)
                    //自定义遮罩
                    .setCustomShadeViewLayoutID(R.layout.layout_custom_view, new MNCustomViewBindCallback() {
                        @Override
                        public void onBindView(View customView) {
                            //TODO:通过findviewById 获取View
                        }
                    })
                    .builder();
            MNScanManager.startScan(this, scanConfig, new MNScanCallback() {
                @Override
                public void onActivityResult(int resultCode, Intent data) {
                    switch (resultCode) {
                        case MNScanManager.RESULT_SUCCESS:
                            String resultSuccess = data.getStringExtra(MNScanManager.INTENT_KEY_RESULT_SUCCESS);
                            break;
                        case MNScanManager.RESULT_FAIL:
                            String resultError = data.getStringExtra(MNScanManager.INTENT_KEY_RESULT_ERROR);
                            break;
                        case MNScanManager.RESULT_CANCLE:
                            showToast("取消扫码");
                            break;
                    }
                }
            });

        2.提供扫描界面相关方法（自定义遮罩层会使用）：
            /**
             * 关闭当前页面
             */
            MNScanManager.closeScanPage();

            /**
             * 打开相册扫描图片
             */
            MNScanManager.openAlbumPage();

            /**
             * 打开手电筒
             */
            MNScanManager.openScanLight();

            /**
             * 关闭手电筒
             */
            MNScanManager.closeScanLight();

            /**
             * 手电筒是否开启
             */
            MNScanManager.isLightOn();
```


## 感谢：

[googlesamples/mlkit](https://github.com/googlesamples/mlkit)

[jenly1314/MLKit](https://github.com/jenly1314/MLKit)

[Ye-Miao/StatusBarUtil](https://github.com/Ye-Miao/StatusBarUtil)

感谢所有开源的人；



## 推荐:
Name | Describe |
--- | --- |
[GankMM](https://github.com/maning0303/GankMM) | （Material Design & MVP & Retrofit + OKHttp & RecyclerView ...）Gank.io Android客户端：每天一张美女图片，一个视频短片，若干Android，iOS等程序干货，周一到周五每天更新，数据全部由 干货集中营 提供,持续更新。 |
[MNUpdateAPK](https://github.com/maning0303/MNUpdateAPK) | Android APK 版本更新的下载和安装,适配7.0,简单方便。 |
[MNImageBrowser](https://github.com/maning0303/MNImageBrowser) | 交互特效的图片浏览框架,微信向下滑动动态关闭 |
[MNCalendar](https://github.com/maning0303/MNCalendar) | 简单的日历控件练习，水平方向日历支持手势滑动切换，跳转月份；垂直方向日历选取区间范围。 |
[MClearEditText](https://github.com/maning0303/MClearEditText) | 带有删除功能的EditText |
[MNCrashMonitor](https://github.com/maning0303/MNCrashMonitor) | Debug监听程序崩溃日志,展示崩溃日志列表，方便自己平时调试。 |
[MNProgressHUD](https://github.com/maning0303/MNProgressHUD) | MNProgressHUD是对常用的自定义弹框封装,加载ProgressDialog,状态显示的StatusDialog和自定义Toast,支持背景颜色,圆角,边框和文字的自定义。 |
[MNXUtilsDB](https://github.com/maning0303/MNXUtilsDB) | xUtils3 数据库模块单独抽取出来，方便使用。 |
[MNVideoPlayer](https://github.com/maning0303/MNVideoPlayer) | SurfaceView + MediaPlayer 实现的视频播放器，支持横竖屏切换，手势快进快退、调节音量，亮度等。------代码简单，新手可以看一看。 |
[MNZXingCode](https://github.com/maning0303/MNZXingCode) | 快速集成二维码扫描和生成二维码 |
[MNChangeSkin](https://github.com/maning0303/MNChangeSkin) | Android夜间模式，通过Theme实现 |
[SwitcherView](https://github.com/maning0303/SwitcherView) | 垂直滚动的广告栏文字展示。 |
[MNPasswordEditText](https://github.com/maning0303/MNPasswordEditText) | 类似微信支付宝的密码输入框。 |
[MNSwipeToLoadDemo](https://github.com/maning0303/MNSwipeToLoadDemo) | 利用SwipeToLoadLayout实现的各种下拉刷新效果（饿了吗，京东，百度外卖，美团外卖，天猫下拉刷新等）。 |

