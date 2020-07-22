package com.fzm.chat33.global;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Environment;
import android.os.Looper;
import android.text.TextUtils;
import android.widget.Toast;

import com.fuzamei.common.utils.ActivityUtils;
import com.fuzamei.componentservice.config.AppConfig;
import com.fzm.chat33.R;
import com.fzm.chat33.app.App;
import com.fzm.chat33.core.event.StopChatServiceEvent;

import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import io.reactivex.functions.Consumer;
import io.reactivex.plugins.RxJavaPlugins;

/**
 * @author zhengjy
 * @since 2018/11/22
 * Description:
 */
public class CrashHandler implements Thread.UncaughtExceptionHandler {

    private static CrashHandler sInstance = null;
    private Thread.UncaughtExceptionHandler mDefaultHandler;
    private Context mContext;
    // 保存手机信息和异常信息
    private Map<String, String> mMessage = new HashMap<>();

    public static CrashHandler getInstance() {
        if (sInstance == null) {
            synchronized (CrashHandler.class) {
                if (sInstance == null) {
                    synchronized (CrashHandler.class) {
                        sInstance = new CrashHandler();
                    }
                }
            }
        }
        return sInstance;
    }

    private CrashHandler() {
        RxJavaPlugins.setErrorHandler(new Consumer<Throwable>() {
            @Override
            public void accept(Throwable throwable) throws Exception {
                throwable.printStackTrace();
            }
        });
    }

    /**
     * 初始化默认异常捕获
     *
     * @param context context
     */
    public void init(Context context) {
        mContext = context;
        // 获取默认异常处理器
        mDefaultHandler = Thread.getDefaultUncaughtExceptionHandler();
        // 将此类设为默认异常处理器
        Thread.setDefaultUncaughtExceptionHandler(this);
    }

    @Override
    public void uncaughtException(Thread t, Throwable e) {
        handleException(e);
        // 已经人为处理,系统自己退出
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e1) {
            e1.printStackTrace();
        }
        EventBus.getDefault().post(new StopChatServiceEvent());
        ActivityUtils.exitApp();
    }

    /**
     * 是否人为捕获异常
     *
     * @param e Throwable
     * @return true:已处理 false:未处理
     */
    private void handleException(Throwable e) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                Looper.prepare();
                Toast.makeText(mContext, R.string.basic_error_exception_exit, Toast.LENGTH_LONG).show();
                Looper.loop();
            }
        }).start();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && mContext.checkSelfPermission(
                Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            collectErrorMessages();
            saveErrorMessages(e);
        }
    }

    /**
     * 1.收集错误信息
     */
    private void collectErrorMessages() {
        PackageManager pm = mContext.getPackageManager();
        try {
            PackageInfo pi = pm.getPackageInfo(mContext.getPackageName(), PackageManager.GET_ACTIVITIES);
            if (pi != null) {
                String versionName = TextUtils.isEmpty(pi.versionName) ? "null" : pi.versionName;
                String versionCode = "" + pi.versionCode;
                mMessage.put("versionName", versionName);
                mMessage.put("versionCode", versionCode);
            }
            // 通过反射拿到错误信息
            Field[] fields = Build.class.getFields();
            if (fields != null && fields.length > 0) {
                for (Field field : fields) {
                    field.setAccessible(true);
                    try {
                        mMessage.put(field.getName(), field.get(null).toString());
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    }
                }
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
    }

    /**
     * 2.保存错误信息
     *
     * @param e Throwable
     */
    private void saveErrorMessages(Throwable e) {
        if (e == null) {
            return;
        }
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, String> entry : mMessage.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            sb.append(key).append("=").append(value).append("\n");
        }
        Writer writer = new StringWriter();
        PrintWriter pw = new PrintWriter(writer);
        e.printStackTrace(pw);
        Throwable cause = e.getCause();
        // 循环取出Cause
        while (cause != null) {
            cause.printStackTrace(pw);
            cause = e.getCause();
        }
        pw.close();
        String result = writer.toString();
        sb.append(result);
        String time = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA).format(new Date());
        String fileName = "crash-" + time + ".log";
        // 有无SD卡
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            String path = Environment.getExternalStorageDirectory().getPath() + "/" + AppConfig.APP_NAME_EN + "/crash";
            File dir = new File(path);
            if (!dir.exists()) dir.mkdirs();
            FileOutputStream fos = null;
            try {
                fos = new FileOutputStream(new File(path, fileName));
                fos.write(sb.toString().getBytes());
            } catch (Exception e1) {
                e1.printStackTrace();
            } finally {
                if (fos != null) {
                    try {
                        fos.close();
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                }
            }
        }
    }
}
