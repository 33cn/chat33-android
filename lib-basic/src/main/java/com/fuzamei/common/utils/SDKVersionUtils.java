package com.fuzamei.common.utils;

import android.os.Build;
import android.os.Environment;

/**
 * @author zhengjy
 * @since 2020/01/15
 * Description:SDK版本判断
 */
public class SDKVersionUtils {

    public static boolean isAndroidQ() {
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
}
