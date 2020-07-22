package com.luck.picture.lib.tools;

import android.os.Build;
import android.os.Environment;

/**
 * @author：luck
 * @date：2019-07-17 15:12
 * @describe：Android Sdk版本判断
 */
public class SdkVersionUtils {
    /**
     * 判断是否是Android Q版本
     *
     * @return
     */
    public static boolean checkedAndroid_Q() {
        boolean flag = false;
        //如果本身是大于等于 29  并且开启沙盒 则返回false 走适配沙盒分支
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && !isExternalStorageLegacy()) {
            flag = true;
        }
        return flag;
    }

    private static boolean isExternalStorageLegacy(){
        return Environment.isExternalStorageLegacy();
    }

    public static boolean hasICS() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH;
    }

    public static boolean hasKitKat() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;
    }
}
