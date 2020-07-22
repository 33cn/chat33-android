package com.fzm.chat33.main.activity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Build;
import android.util.Log;
import android.view.View;

import androidx.core.content.ContextCompat;

import com.alibaba.android.arouter.facade.annotation.Autowired;
import com.alibaba.android.arouter.facade.annotation.Route;
import com.alibaba.android.arouter.launcher.ARouter;
import com.cjt2325.cameralibrary.JCameraView;
import com.cjt2325.cameralibrary.listener.ClickListener;
import com.cjt2325.cameralibrary.listener.ErrorListener;
import com.cjt2325.cameralibrary.listener.JCameraListener;
import com.fuzamei.common.utils.BarUtils;
import com.fuzamei.common.utils.ToolUtils;
import com.fuzamei.componentservice.app.AppRoute;
import com.fuzamei.componentservice.base.LoadableActivity;
import com.fuzamei.componentservice.config.AppConfig;
import com.fzm.chat33.R;

import java.io.File;

/**
 * @author zhengjy
 * @since 2019/02/12
 * Description:仿微信轻触拍照，长按摄像
 */
@Route(path = AppRoute.CAMERA_SHOOT)
public class ShootActivity extends LoadableActivity {
    private static final String TAG = "ShootActivity";

    private JCameraView jCameraView;
    /**
     * 拍摄模式
     * 1：照片  2：视频  3：照片和视频
     */
    @Autowired
    public int mode = 1;

    @Override
    protected void setStatusBar() {
        BarUtils.setStatusBarColor(this, ContextCompat.getColor(this, R.color.black), 0);
        BarUtils.setStatusBarLightMode(this, false);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_shoot;
    }

    protected void initView() {
        ARouter.getInstance().inject(this);
        // 全屏
        if (Build.VERSION.SDK_INT >= 19) {
            View decorView = getWindow().getDecorView();
            decorView.setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        } else {
            View decorView = getWindow().getDecorView();
            int option = View.SYSTEM_UI_FLAG_FULLSCREEN;
            decorView.setSystemUiVisibility(option);
        }
        jCameraView = findViewById(R.id.jcameraview);
    }

    protected void initData() {
        //设置视频保存路径
        jCameraView.setSaveVideoPath(getExternalCacheDir().getAbsolutePath() + File.separator + "JCamera");
        //设置只能录像或只能拍照或两种都可以（默认两种都可以）
        if (mode == 1) {
            jCameraView.setFeatures(JCameraView.BUTTON_STATE_ONLY_CAPTURE);
        } else if (mode == 2) {
            jCameraView.setFeatures(JCameraView.BUTTON_STATE_ONLY_RECORDER);
        } else {
            jCameraView.setFeatures(JCameraView.BUTTON_STATE_BOTH);
        }
        //设置视频质量
        jCameraView.setMediaQuality(JCameraView.MEDIA_QUALITY_MIDDLE);
    }

    protected void setEvent() {
        // JCameraView监听
        jCameraView.setErrorLisenter(new ErrorListener() {
            @Override
            public void onError() {
                //打开Camera失败回调
                Log.i(TAG, "open camera error");
            }
            @Override
            public void AudioPermissionError() {
                //没有录取权限回调
                Log.i(TAG, "AudioPermissionError");
            }
        });

        jCameraView.setJCameraLisenter(new JCameraListener() {
            @Override
            public void captureSuccess(Bitmap bitmap) {
                // 获取图片bitmap
                Log.i(TAG, "bitmap = " + bitmap.getWidth());
                Intent intent = new Intent();
                intent.putExtra("result", ToolUtils.saveBitmap(bitmap, AppConfig.APP_NAME_EN));
                setResult(Activity.RESULT_OK, intent);
                finish();
            }
            @Override
            public void recordSuccess(String url,Bitmap firstFrame) {
                // 获取视频路径
                Log.i(TAG, "url = " + url);
                Log.i(TAG, "firstFrame = " + firstFrame.getWidth());
                Intent intent = new Intent();
                intent.putExtra("video", url);
                intent.putExtra("duration", ToolUtils.getVideoDuration(url));
                setResult(Activity.RESULT_OK, intent);
                finish();
            }
        });
        // 左边按钮点击事件
        jCameraView.setLeftClickListener(new ClickListener() {
            @Override
            public void onClick() {
                finish();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        jCameraView.onResume();
    }
    @Override
    protected void onPause() {
        super.onPause();
        jCameraView.onPause();
    }
}
