package com.fuzamei.common.utils;

import android.util.Log;

import com.fuzamei.commonlib.BuildConfig;
import com.orhanobut.logger.AndroidLogAdapter;
import com.orhanobut.logger.FormatStrategy;
import com.orhanobut.logger.Logger;
import com.orhanobut.logger.PrettyFormatStrategy;

public class LogUtils {

    public static final int VERBOSE = 0;
    public static final int DEBUG = 1;
    public static final int INFO = 2;
    public static final int WARNING = 3;
    public static final int ERROR = 4;
    public static final int NONE = 5;

    public static final String TAG = "Chat33";

    public static int LOG_LEVEL = VERBOSE;

    static {
        try {
            FormatStrategy formatStrategy = PrettyFormatStrategy.newBuilder()
                    .showThreadInfo(false)  // (Optional) Whether to show thread info or not. Default true
                    .methodCount(0)         // (Optional) How many method line to show. Default 2
                    .methodOffset(7)        // (Optional) Hides internal method calls up to offset. Default 5
                    .tag(TAG)
                    .build();
            Logger.addLogAdapter(new AndroidLogAdapter(formatStrategy) {
                @Override
                public boolean isLoggable(int priority, String tag) {
                    return BuildConfig.DEBUG;
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // verbose
    public static void v(String tag, String msg) {
        if (VERBOSE < LOG_LEVEL /*|| !BuildConfig.TYPE_DEBUG*/)
            return;
        logger(tag, msg, Log.VERBOSE);
    }

    public static void v(String msg) {
        if (VERBOSE < LOG_LEVEL /*|| !BuildConfig.TYPE_DEBUG*/)
            return;
        logger("", msg, Log.VERBOSE);
    }

    // debug
    public static void d(String tag, String msg) {
        if (DEBUG < LOG_LEVEL /*|| !BuildConfig.TYPE_DEBUG*/)
            return;
        logger(tag, msg, Log.DEBUG);
    }

    public static void d(String msg) {
        if (DEBUG < LOG_LEVEL /*|| !BuildConfig.TYPE_DEBUG*/)
            return;
        logger("", msg, Log.DEBUG);
    }

    // info
    public static void i(String tag, String msg) {
        if (INFO < LOG_LEVEL /*|| !BuildConfig.TYPE_DEBUG*/)
            return;
        logger(tag, msg, Log.INFO);
    }

    public static void i(String msg) {
        if (INFO < LOG_LEVEL /*|| !BuildConfig.TYPE_DEBUG*/)
            return;
        logger("", msg, Log.INFO);
    }

    // warning
    public static void w(String tag, String msg) {
        if (WARNING < LOG_LEVEL /*|| !BuildConfig.TYPE_DEBUG*/)
            return;
        logger(tag, msg, Log.WARN);
    }

    public static void w(String msg) {
        if (WARNING < LOG_LEVEL /*|| !BuildConfig.TYPE_DEBUG*/)
            return;
        logger("", msg, Log.WARN);
    }

    // error
    public static void e(String tag, String msg) {
        if (ERROR < LOG_LEVEL /*|| !BuildConfig.TYPE_DEBUG*/)
            return;
        logger(tag, msg, Log.ERROR);
    }

    public static void e(String msg) {
        if (ERROR < LOG_LEVEL /*|| !BuildConfig.TYPE_DEBUG*/)
            return;
        logger("", msg, Log.ERROR);
    }

    private static void logger(String tag, String msg, int logLevel) {
        switch (logLevel) {
            case Log.VERBOSE:
                Logger.t(tag).v(msg);
                break;
            case Log.DEBUG:
                Logger.t(tag).d(msg);
                break;
            case Log.INFO:
                Logger.t(tag).i(msg);
                break;
            case Log.WARN:
                Logger.t(tag).w(msg);
                break;
            case Log.ERROR:
                Logger.t(tag).e(msg);
                break;
            default:
                break;
        }
    }
}
