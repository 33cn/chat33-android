package com.fuzamei.componentservice.base;

import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.ViewConfiguration;
import android.view.ViewTreeObserver;

import com.baidu.crabsdk.CrabSDK;
import com.fuzamei.common.utils.LogUtils;
import com.fuzamei.commonlib.R;

import java.util.List;

import pub.devrel.easypermissions.AppSettingsDialog;
import pub.devrel.easypermissions.EasyPermissions;

/**
 * @author zhengjy
 * @since 2018/9/14
 * Description: Activity抽象类
 */
public abstract class BaseActivity extends AppCompatActivity implements EasyPermissions.PermissionCallbacks {
    public BaseActivity instance = null;
    public String TAG = getClass().getSimpleName();
    protected long clickTime;
    // 右滑返回
    private MotionEvent mActionDownEvent;
    private VelocityTracker mVelocityTracker;
    private boolean enableSlidingBack;

    //获取fragment布局文件ID
    protected abstract int getLayoutId();

    protected abstract void initView();

    protected abstract void initData();

    protected abstract void setEvent();

    // 设置状态栏颜色等
    protected abstract void setStatusBar();

    static {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        int id = getLayoutId();
        if (id != 0) {
            setContentView(id);
        }
        instance = this;
        TAG = this.getClass().getSimpleName();
        enableSlidingBack = enableSlideBack();
        initView();
        setStatusBar();
        initData();
        setEvent();
    }

    protected boolean enableSlideBack() {
        return false;
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        if (!enableSlidingBack) {
            return super.dispatchTouchEvent(event);
        }
        if (mVelocityTracker == null) {
            mVelocityTracker = VelocityTracker.obtain();
        }
        mVelocityTracker.addMovement(event);

        if (event.getActionMasked() == MotionEvent.ACTION_DOWN) {
            if (mActionDownEvent != null) {
                mActionDownEvent.recycle();
            }
            // 记录按下时的事件
            mActionDownEvent = MotionEvent.obtain(event);
        } else if (event.getActionMasked() == MotionEvent.ACTION_UP) {
            // 右滑返回手势检测
            int pointerId = event.getPointerId(0);
            int maximumFlingVelocity = ViewConfiguration.get(this).getScaledMaximumFlingVelocity();
            int minimumFlingVelocity = ViewConfiguration.get(this).getScaledMinimumFlingVelocity();
            mVelocityTracker.computeCurrentVelocity(1000, maximumFlingVelocity);
            final float velocityX = mVelocityTracker.getXVelocity(pointerId);
            // 左边缘检测，可根据需要调整，单位像素
            if (/*mActionDownEvent.getX() <= 50
                    && */event.getX() - mActionDownEvent.getX() >= 300
                    && Math.abs(velocityX) >= minimumFlingVelocity) {
                // 有效触发距离，可根据需要调整，单位像素
                onBackPressed();
            }
        }

        return super.dispatchTouchEvent(event);
    }

    @Override
    protected void onPause() {
        CrabSDK.onPause(this);
        super.onPause();
    }

    @Override
    protected void onResume() {
        CrabSDK.onResume(this);
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mVelocityTracker != null) {
            mVelocityTracker.recycle();
        }
        instance = null;
    }

    protected void back() {
        this.finish();
    }

    /**
     * 跳转到下个Activity
     */
    protected void next(Class<?> cls) {
        startActivity(new Intent(this, cls));
        finish();
    }

    /**
     * 跳转到下个Activity
     */
    protected void nextWithoutFinish(Class<?> cls) {
        startActivity(new Intent(this, cls));
    }

    /**
     * 可以点击
     */
    protected boolean canClicked() {
        if (System.currentTimeMillis() - clickTime > 500) {
            clickTime = System.currentTimeMillis();
            return true;
        }
        return false;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        LogUtils.e("Activity onRequestPermissionsResult ");
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    @Override
    public void onPermissionsDenied(int requestCode, @NonNull List<String> perms) {
        LogUtils.e("requestCode = " + requestCode + " perms = " + perms);
        if (EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {
            new AppSettingsDialog.Builder(this)
                    .setTitle(getString(R.string.basic_permission_denied_title))
                    .setRationale(getString(R.string.basic_permission_denied_rationale))
                    .setRequestCode(requestCode)//用于onActivityResult回调做其它对应相关的操作
                    .build()
                    .show();
        }

    }

    @Override
    public void onPermissionsGranted(int requestCode, @NonNull List<String> perms) {
        LogUtils.e("requestCode = " + requestCode + " perms = " + perms);

    }

    protected void onShowKeyboard(int keyboardHeight) {
    }

    protected void onHideKeyboard() {
    }

    protected void attachKeyboardListeners() {
        if (keyboardListenersAttached) {
            return;
        }
        getWindow().getDecorView().getViewTreeObserver().addOnGlobalLayoutListener(keyboardLayoutListener);

        keyboardListenersAttached = true;
    }

    private boolean keyboardListenersAttached = false;
    int preDisplayHeight = -1;
    private ViewTreeObserver.OnGlobalLayoutListener keyboardLayoutListener = new ViewTreeObserver.OnGlobalLayoutListener() {


        @Override
        public void onGlobalLayout() {
            Rect rect = new Rect();
            getWindow().getDecorView().getWindowVisibleDisplayFrame(rect);
            int displayHeight = rect.bottom;
            int totalHeight = getWindow().getDecorView().getHeight();
            int keyboardHeight = totalHeight - displayHeight;

            if (preDisplayHeight != displayHeight) {
                boolean hide = (double) displayHeight / totalHeight > 0.8;
                if (hide) {
                    onHideKeyboard();
                } else {
                    onShowKeyboard(keyboardHeight);
                }
            }
            preDisplayHeight = displayHeight;

        }
    };
}
