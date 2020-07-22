package com.fzm.chat33.core.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.text.TextUtils;

import com.fzm.chat33.core.Chat33;
import com.fzm.chat33.core.global.UserInfo;

/**
 * @author zhengjy
 * @since 2018/12/27
 * Description:保存和当前登录用户相关的信息
 */
public class UserInfoPreference {

    private final String USER_INFO_PRE = "chat33:user_info_";
    private static volatile UserInfoPreference sInstance;
    private SharedPreferences mPre;

    // 本地最老的群密钥消息时间
    public static final String OLDEST_MSG_TIME = "OLDEST_MSG_TIME";
    // 本地最新消息时间
    public static final String LATEST_MSG_TIME = "LATEST_MSG_TIME";
    // 本地消息确认的时间
    public static final String SYNC_MSG_TIME = "SYNC_MSG_TIME";
    // 服务端是否有更多的消息记录
    public static final String NO_MORE_CHAT_LOG = "NO_MORE_CHAT_LOG_";
    // 每个用户每次更新或重装首次启动
    public static final String START_UP = "START_UP_";

    // 设置相关
    public static final String NEED_CONFIRM = "NEED_CONFIRM";
    public static final String NEED_ANSWER = "NEED_ANSWER";
    public static final String VERIFY_QUESTION = "VERIFY_QUESTION";
    public static final String VERIFY_ANSWER = "VERIFY_ANSWER";
    public static final String NEED_CONFIRM_INVITE = "NEED_CONFIRM_INVITE";

    // 上一次选择发红包的币种
    public static final String LAST_PACKET_COIN = "LAST_PACKET_COIN";

    // 设置支付密码
    public static final String SET_PAY_PASSWORD = "SET_PAY_PASSWORD";

    // 用户公钥
    public static final String USER_PUBLIC_KEY = "USER_PUBLIC_KEY";
    // 用户私钥
    public static final String USER_PRIVATE_KEY = "USER_PRIVATE_KEY";
    // 用户地址
    public static final String USER_ADDRESS = "USER_ADDRESS";

    // 用户密聊密码hash
    public static final String USER_CHAT_KEY_PWD = "USER_CHAT_KEY_PWD";
    // 用户加密助记词
    public static final String USER_MNEMONIC_WORDS = "USER_MNEMONIC_WORDS";
    // 用户是否设置密聊密码
    public static final String USER_HAS_CHAT_KEY_PWD = "USER_HAS_CHAT_KEY_PWD";

    // 用户第一次发送加密消息失败时的提示
    public static final String SHOW_UNENCRYPTED_TIPS = "SHOW_UNENCRYPTED_TIPS_";
    // 聊天页面引导
    public static final String SHOW_CHAT_GUIDANCE = "SHOW_CHAT_GUIDANCE";
    // 消息红包入口引导
    public static final String SHOW_TEXT_PACKET_GUIDANCE = "SHOW_TEXT_PACKET_GUIDANCE";
    // 红包页面引导
    public static final String SHOW_SEND_PACKET_GUIDANCE = "SHOW_SEND_PACKET_GUIDANCE";

    private static volatile String prefId = null;

    public static UserInfoPreference getInstance() {
        if (sInstance == null || TextUtils.isEmpty(prefId)) {
            synchronized (UserInfoPreference.class) {
                if (sInstance == null || TextUtils.isEmpty(prefId)) {
                    sInstance = new UserInfoPreference(UserInfo.getInstance().id);
                }
            }
        }
        return sInstance;
    }

    public static UserInfoPreference getInstance(String id) {
        if (sInstance == null || TextUtils.isEmpty(prefId)) {
            synchronized (UserInfoPreference.class) {
                if (sInstance == null || TextUtils.isEmpty(prefId)) {
                    sInstance = new UserInfoPreference(id);
                }
            }
        }
        return sInstance;
    }

    public static void reset() {
        synchronized (UserInfoPreference.class) {
            sInstance = null;
        }
    }

    private UserInfoPreference(String uid) {
        prefId = uid;
        this.mPre = Chat33.getContext().getSharedPreferences(USER_INFO_PRE + prefId, Context.MODE_PRIVATE);
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

    /**
     * 某个用户，某次App版本的首次启动
     * @return
     */
    public boolean isFirstStart() {
        try {
            PackageManager packageManager = Chat33.getContext().getPackageManager();
            PackageInfo packInfo = packageManager.getPackageInfo(Chat33.getContext().getPackageName(), 0);
            boolean first = getBooleanPref(START_UP + packInfo.versionCode, true);
            if (first) {
                setBooleanPref(START_UP + packInfo.versionCode, false);
            }
            return first;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return false;
        }
    }
}
