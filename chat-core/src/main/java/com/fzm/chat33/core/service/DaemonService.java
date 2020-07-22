package com.fzm.chat33.core.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import androidx.annotation.Nullable;

import com.baidu.crabsdk.CrabSDK;
import com.fuzamei.common.utils.ActivityUtils;
import com.fuzamei.common.utils.LogUtils;
import com.fuzamei.common.utils.ToolUtils;
import com.fzm.chat33.core.Chat33;

import java.util.Timer;
import java.util.TimerTask;

/**
 * @author zhengjy
 * @since 2018/11/15
 * Description:守护Service，防止{@link MessageService}被意外停止
 */
@Deprecated
public class DaemonService extends Service {

    private Intent service;
    private Timer timer;
    private TimerTask task;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        initDaemon();
        return START_STICKY;
    }

    private void initDaemon() {
        service = new Intent(DaemonService.this, MessageService.class);
        timer = new Timer();
        task = new TimerTask() {
            @Override
            public void run() {
                LogUtils.d("DaemonService Run: " + ToolUtils.formatLogTime(System.currentTimeMillis()));
                if (!Chat33.isServiceWorked() && !ActivityUtils.isBackground()) {
                    try {
                        LogUtils.i("重启消息Service");
                        startService(service);
                    } catch (Exception e) {
                        CrabSDK.uploadException(e);
                    }
                }
            }
        };
        try {
            timer.schedule(task, 0, 5000);
        } catch (Exception e) {
            e.printStackTrace();
            CrabSDK.uploadException(e);
        }
    }

    @Override
    public void onDestroy() {
        LogUtils.d("DaemonService destroy: " + ToolUtils.formatLogTime(System.currentTimeMillis()));
        super.onDestroy();
        try {
            if (timer != null) {
                timer.cancel();
            }
            if (task != null) {
                task.cancel();
            }
        } catch (Exception e) {
            e.printStackTrace();
            CrabSDK.uploadException(e);
        }
    }
}
