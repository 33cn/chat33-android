package com.fzm.chat33.widget;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.viewpager.widget.ViewPager;

import android.util.AttributeSet;
import android.view.MotionEvent;

import com.baidu.crabsdk.CrabSDK;

/**
 * @author zhengjy
 * @since 2018/12/18
 * Description:防止在ViewPager中使用{@link com.fuzamei.common.widget.photoview.PhotoView}出现异常
 */
public class MyViewPager extends ViewPager {

    public MyViewPager(@NonNull Context context) {
        super(context);
    }

    public MyViewPager(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        try {
            return super.onTouchEvent(ev);
        } catch (Exception e) {
            e.printStackTrace();
            CrabSDK.uploadException(e);
        }
        return false;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        try {
            return super.onInterceptTouchEvent(ev);
        } catch (Exception e) {
            e.printStackTrace();
            CrabSDK.uploadException(e);
        }
        return false;
    }
}
