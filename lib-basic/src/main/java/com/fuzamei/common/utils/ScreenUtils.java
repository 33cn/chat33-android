package com.fuzamei.common.utils;

import android.content.Context;
import android.content.res.Resources;
import android.util.DisplayMetrics;
import android.view.WindowManager;

import com.fuzamei.common.FzmFramework;

/**
 * Created by ljn on 2017/9/11.
 * Explain dp2px
 */
public class ScreenUtils {

    private static int screenHeight;
    private static int screenWidth;
    private static int height9;
    private static float density;
    private static int statusHeight;

    private static void calculateScreen() {
        DisplayMetrics metrics = FzmFramework.context.getResources().getDisplayMetrics();
        screenHeight = metrics.heightPixels;
        screenWidth = metrics.widthPixels;
        height9 = 9 * screenWidth / 16;
        density = metrics.density;
    }

    public static int getScreenHeight() {
        if (screenHeight == 0) {
            calculateScreen();
        }
        return screenHeight;
    }

    public static int getScreenWidth() {
        if (screenWidth == 0) {
            calculateScreen();
        }
        return screenWidth;
    }

    public static int getHeight9() {
        if (height9 == 0) {
            calculateScreen();
        }
        return height9;
    }

    public static float getDensity() {
        if (density == 0)
            calculateScreen();
        return density;
    }

    public static int dp2Px(float dp) {
        final float scale = FzmFramework.context.getResources().getDisplayMetrics().density;
        return (int) (dp * scale + 0.5f);
    }

    public static int dx2Dp(float px) {
        final float scale = FzmFramework.context.getResources().getDisplayMetrics().density;
        return (int) (px / scale + 0.5f);
    }
    /**
     * 将sp值转换为px值，保证文字大小不变
     *
     * @param spValue
     *            （DisplayMetrics类中属性scaledDensity）
     * @return
     */
    public static int sp2px( float spValue) {
        final float fontScale =  FzmFramework.context.getResources().getDisplayMetrics().scaledDensity;
        return (int) (spValue * fontScale + 0.5f);
    }

    public static int dp2px(Context context, float dpValue) {
        float density = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * density + 0.5f);
    }

    public static float px2dp(Context context, int pxValue) {
        float density = context.getResources().getDisplayMetrics().density;
        return pxValue / density;
    }

    public static int dp2px(float dpValue) {
        float density = Resources.getSystem().getDisplayMetrics().density;
        return (int) (dpValue * density + 0.5f);
    }

    public static int getScreenWidth(Context context) {
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics outMetrics = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(outMetrics);
        return outMetrics.widthPixels;
    }
}
