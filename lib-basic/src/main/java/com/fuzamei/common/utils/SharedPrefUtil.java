package com.fuzamei.common.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;

import com.fuzamei.common.FzmFramework;

public class SharedPrefUtil {
    private static final String SHARE_PREF = "com.fuzamei.common.sharedpreference";
    public static final String TAG = SharedPrefUtil.class.getSimpleName();
    private static SharedPrefUtil sInstance;
    private SharedPreferences mPre;

    // 当前已读的群公告
    public static final String READ_CURRENT_NOTICE = "READ_CURRENT_NOTICE_";
    // 设备id，在每次重新安装之后发生改变
    private static final String DEVICE_ID = "DEVICE_ID";

    private SharedPrefUtil(Context context) {
        this.mPre = context.getSharedPreferences(SHARE_PREF, 0);
    }

    public static SharedPrefUtil getInstance() {
        if (sInstance == null) {
            synchronized (SharedPrefUtil.class) {
                if (sInstance == null) {
                    sInstance = new SharedPrefUtil(FzmFramework.context);
                }
            }
        }
        return sInstance;
    }

    public int getIntPref(String name, int def) {
        return this.mPre.getInt(name, def);
    }

    public void setIntPref(String name, int def) {
        this.mPre.edit().putInt(name, def).apply();
    }

    public long getLongPref(String name, long def) {
        return this.mPre.getLong(name, def);
    }

    public void setLongPref(String name, long def) {
        this.mPre.edit().putLong(name, def).apply();
    }

    public boolean getBooleanPref(String name, boolean def) {
        return this.mPre.getBoolean(name, def);
    }

    public void setBooleanPref(String name, boolean value) {
        this.mPre.edit().putBoolean(name, value).apply();
    }

    public String getStringPref(String name, String def) {
        return this.mPre.getString(name, def);
    }

    public void setStringPref(String name, String def) {
        this.mPre.edit().putString(name, def).apply();
    }

    public float getFloadPref(String name, float def) {
        return this.mPre.getFloat(name, def);
    }

    public void setFloadPref(String name, float def) {
        this.mPre.edit().putFloat(name, def).apply();
    }

    public void removePref(String name) {
        this.mPre.edit().remove(name).apply();
    }

    public String getDeviceId() {
        return getStringPref(DEVICE_ID, "");
    }

    @SuppressLint("ApplySharedPref")
    public void setDeviceId(String deviceId) {
        this.mPre.edit().putString(DEVICE_ID, deviceId).commit();
    }
}