package com.fuzamei.common.utils;

import android.content.Context;
import android.os.Build;
import android.os.VibrationEffect;
import android.os.Vibrator;

/**
 * @author zhengjy
 * @since 2018/10/25
 * Description:震动工具类
 */
public class VibrateUtils {

    private static Vibrator vibrator;

    /**
     * 简单震动
     *
     * @param context     调用震动的Context
     * @param millisecond 震动的时间，毫秒
     */
    @SuppressWarnings("static-access")
    public static void simple(Context context, int millisecond) {
        if (context == null) {
            return;
        }
        vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        if (vibrator != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createOneShot(millisecond, VibrationEffect.DEFAULT_AMPLITUDE));
            } else {
                vibrator.vibrate(millisecond);
            }
        }
    }

    /**
     * 复杂的震动
     *
     * @param context 调用震动的Context
     * @param pattern 震动形式
     * @param repeate 震动的次数，-1不重复，非-1为从pattern的指定下标开始重复
     */
    @SuppressWarnings("static-access")
    public static void complicated(Context context, long[] pattern, int repeate) {
        if (context == null) {
            return;
        }
        vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        if (vibrator != null) {
            vibrator.vibrate(pattern, repeate);
        }
    }

    /**
     * 停止震动
     */
    public static void stop() {
        if (vibrator != null) {
            vibrator.cancel();
        }
    }
}
