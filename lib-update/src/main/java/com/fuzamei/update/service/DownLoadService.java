package com.fuzamei.update.service;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Environment;
import android.os.IBinder;
import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import android.text.TextUtils;

import com.fuzamei.common.FzmFramework;
import com.fuzamei.componentservice.config.AppConfig;
import com.fuzamei.update.Interface.OnAppDownloadListener;
import com.fuzamei.update.R;
import com.fuzamei.update.event.UnbindServiceEvent;
import com.fuzamei.update.util.ApkFileUtil;

import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.TimeUnit;

import io.reactivex.android.schedulers.AndroidSchedulers;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * 下载apk服务
 */
public class DownLoadService extends Service {

    // 下载链接
    private String url;
    // 通知标题
    private String title;
    private int icon;
    private int versionCode;
    private String md5;
    private boolean forceUpdate;
    private Class<?> target;

    private String destFileDir;
    private String destFileName;

    private NotificationManager notificationManager;
    private NotificationCompat.Builder mBuilder;
    private OnAppDownloadListener mListener;
    private int notification_id = 1;
    private int oldProgress = 0;

    @Override
    public IBinder onBind(Intent intent) {
        if (intent != null) {
            prepareParams(intent);
        }
        destFileDir = Environment.getExternalStorageDirectory().getPath() + "/" + AppConfig.APP_NAME_EN + "/download";
        destFileName = getApplicationContext().getPackageName() + ".apk";
        return binder;
    }

    public void startDownload() {
        mListener.onStart();
        File dir = new File(destFileDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        File apkFile = new File(destFileDir, destFileName);
        if (apkFile.exists() &&
                (ApkFileUtil.isLatestApkFile(getApplicationContext(), versionCode, apkFile.getPath())
                || ApkFileUtil.isSameApkFile(apkFile, md5))) {
            // 如果本地已经包含了指定版本号的安装包，则不需要下载
            mListener.onSuccess(forceUpdate, apkFile);
            finishDownload();
            return;
        }
        if (!TextUtils.isEmpty(url)) {
            showNotification();
            // 下载安装包
            OkHttpClient client = new OkHttpClient.Builder().readTimeout(15, TimeUnit.SECONDS)
                    .connectTimeout(15, TimeUnit.SECONDS).build();
            Request request = new Request.Builder().get().url(url)
                    .addHeader("FZM-REQUEST-OS", "android").tag(target).build();
            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(@NonNull Call call, @NonNull final IOException e) {
                    finishDownload();
                    AndroidSchedulers.mainThread().createWorker().schedule(new Runnable() {
                        @Override
                        public void run() {
                            mListener.onFail(forceUpdate, e);
                        }
                    });
                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                    if (response.isSuccessful()) {
                        mBuilder.setContentText(FzmFramework.getString(R.string.update_downloading));
                        final File file = saveFile(response);
                        finishDownload();
                        AndroidSchedulers.mainThread().createWorker().schedule(new Runnable() {
                            @Override
                            public void run() {
                                mListener.onSuccess(forceUpdate, file);
                            }
                        });
                    } else {
                        onFailure(call, new IOException(FzmFramework.getString(R.string.update_error_request)));
                    }
                }
            });
        }
    }

    /**
     * 将Response解析转换成File
     *
     * @param response  网络请求响应
     * @return          对应文件
     * @throws IOException
     */
    public File saveFile(Response response) throws IOException {
        if (response.body() == null) {
            return null;
        }
        long sum = 0;
        File dir = new File(destFileDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        File file = new File(dir, destFileName);
        if (file.exists()) {
            file.delete();
        }
        InputStream is = response.body().byteStream();
        FileOutputStream fos = new FileOutputStream(file);
        final long total = response.body().contentLength();
        byte[] buf = new byte[2048];
        int len = 0;
        while ((len = is.read(buf)) != -1) {
            sum += len;
            fos.write(buf, 0, len);
            final long finalSum = sum;
            int progress = (int) (finalSum * 1.0f / total * 100);
            if (progress != oldProgress) {
                //这里判断下，当进度值发生变化时再去更新
                oldProgress = progress;
                updateProgress(progress);
                AndroidSchedulers.mainThread().createWorker().schedule(new Runnable() {
                    @Override
                    public void run() {
                        mListener.onProgress(finalSum * 1.0f / total);
                    }
                });
            }
        }
        fos.flush();
        response.body().close();
        is.close();
        fos.close();
        return file;
    }

    private void updateProgress(int progress) {
        // 更新通知栏
        mBuilder.setProgress(100, progress, false);
        mBuilder.setSubText(progress + "%");
        notificationManager.notify(notification_id, mBuilder.build());
    }

    private void finishDownload() {
        // 下载结束，成功或失败都要停止服务，取消通知
        if (notificationManager != null) {
            notificationManager.cancel(notification_id);
        }
        EventBus.getDefault().post(new UnbindServiceEvent());
    }

    public void prepareParams(Intent intent) {
        url = intent.getStringExtra("url");
        title = intent.getStringExtra("title");
        icon = intent.getIntExtra("icon", 0);
        md5 = intent.getStringExtra("md5");
        versionCode = intent.getIntExtra("versionCode", 0);
        forceUpdate = intent.getBooleanExtra("force", false);
        target = (Class<?>) intent.getSerializableExtra("target");
    }

    /**
     * 下载的长度/文件的长度
     */
    private void showNotification() {
        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mBuilder = new NotificationCompat.Builder(this, "notification");
        mBuilder.setContentTitle(title);
        mBuilder.setProgress(0, 0, true);
        mBuilder.setOngoing(true);
        mBuilder.setSmallIcon(icon);
        mBuilder.setContentText(FzmFramework.getString(R.string.update_begin_download));
        mBuilder.setSubText("0%");

        if (target != null) {
            Intent intent = new Intent(this, target);
            PendingIntent contentIntent = PendingIntent.getActivity(this, 0, intent,
                    PendingIntent.FLAG_UPDATE_CURRENT);
            // 指定点击跳转页面
            mBuilder.setContentIntent(contentIntent);
        }
        // 下面两句是 在按home后，点击通知栏，返回之前activity 状态;
        // 有下面两句的话，假如service还在后台下载， 在点击程序图片重新进入程序时，直接到下载界面，相当于把程序MAIN 入口改了 - -
        // 是这么理解么。。。
        // intent.setAction(Intent.ACTION_MAIN);
        // intent.addCategory(Intent.CATEGORY_LAUNCHER);
        //这里用Flag_One_shot,这样的话，从通知栏进入的时候不会再次启动homeactivity的com.fuzamei.fxee。
        // 不用再次判断是否需要更新安装包，不知道是否正确

        notificationManager.notify(notification_id, mBuilder.build());
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    public void setOnAppDownloadListener(OnAppDownloadListener listener) {
        this.mListener = listener;
    }

    private DownloadBinder binder = new DownloadBinder();

    public class DownloadBinder extends Binder {
        public DownLoadService getService() {
            return DownLoadService.this;
        }
    }
}
