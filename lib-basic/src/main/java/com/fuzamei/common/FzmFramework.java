package com.fuzamei.common;

import android.content.Context;

import androidx.annotation.StringRes;

public class FzmFramework {
    public static Context context;
    public static void init(Context context){
        FzmFramework.context = context;
    }

    public static String getString(@StringRes int resId, Object... formatArgs) {
        if(context == null) return "";
        return context.getString(resId, formatArgs);
    }
}
