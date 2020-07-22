package com.fuzamei.update;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;

import com.fuzamei.update.Interface.IUpdateInfo;
import com.fuzamei.update.Interface.OnCheckUpdateListener;
import com.fuzamei.update.Interface.UserDecision;
import com.fuzamei.update.Interface.OnAppDownloadListener;
import com.fuzamei.update.event.UnbindServiceEvent;
import com.fuzamei.update.service.DownLoadService;
import com.fuzamei.update.util.ApkFileUtil;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

/**
 * @author zhengjy
 * @since 2018/08/02
 * Description:APP更新
 */
public class UpdateManager {

    private DownLoadService mService;
    private ServiceConnection mConnection;
    private Context application;
    private boolean running = false;

    private String title;
    private int icon;
    private IUpdateInfo updateInfo;
    private Class<?> target;

    private UpdateManager(Builder builder) {
        EventBus.getDefault().register(this);
        this.title = builder.title;
        this.icon = builder.icon;
        this.updateInfo = builder.updateInfo;
        this.target = builder.target;
        this.application = builder.application;
    }

    private void startDownload(final OnAppDownloadListener listener) {
        if (running) {
            return;
        }
        Intent intent = new Intent(application, DownLoadService.class);
        if (updateInfo != null) {
            // 升级下载配置
            intent.putExtra("url", updateInfo.getDownloadUrl());
            intent.putExtra("force", updateInfo.isForceUpdate());
            intent.putExtra("versionCode", updateInfo.getVersionCode());
            intent.putExtra("md5", updateInfo.getFileMD5());
        }
        // 通知栏配置
        intent.putExtra("title", title);
        intent.putExtra("icon", icon);
        intent.putExtra("target", target);
        mConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                mService = ((DownLoadService.DownloadBinder) service).getService();
                // 设置下载进度监听
                mService.setOnAppDownloadListener(listener);
                // 开始下载安装包
                mService.startDownload();
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {

            }
        };
        running = application.bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
    }

    @Subscribe
    public void onServiceFinish(UnbindServiceEvent event) {
        if (running) {
            // 接收Service解绑事件，进行Service的解绑操作
            application.unbindService(mConnection);
            running = false;
        }
    }

    public static class Builder {
        private Context application;
        private String title;
        private int icon;
        private IUpdateInfo updateInfo;
        private Observable<IUpdateInfo> observable;
        private Class<?> target;
        private OnCheckUpdateListener mListener;

        private Builder() {
            this.application = XUpdate.get().getContext();
            this.title = XUpdate.get().getTitle();
            this.icon = XUpdate.get().getIcon();
            this.target = XUpdate.get().getTarget();
        }

        public Builder(Observable<IUpdateInfo> observable, OnCheckUpdateListener listener) {
            this();
            this.observable = observable;
            this.mListener = listener;
        }

        @SuppressLint("CheckResult")
        public void start(final OnAppDownloadListener listener) {
            observable.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(new Consumer<IUpdateInfo>() {
                @Override
                public void accept(final IUpdateInfo updateInfo) {
                    Builder.this.updateInfo = updateInfo;
                    // 比较服务端最新版本与当前安装版本差异，判断是否需要更新
                    boolean needToUpdate = ApkFileUtil.needToUpdate(application, updateInfo.getVersionCode());
                    mListener.onCheckUpdateResult(needToUpdate, updateInfo.isForceUpdate(), new UserDecision() {
                        @Override
                        public void update(boolean update) {
                            if (update) {
                                new UpdateManager(Builder.this).startDownload(listener);
                            } else if (updateInfo.isForceUpdate()) {
                                // 强制更新下如果用户选择不更新，则退出App
                                System.exit(0);
                            }
                        }

                        @Override
                        public IUpdateInfo getUpdateInfo() {
                            return updateInfo;
                        }
                    });
                }
            }, new Consumer<Throwable>() {
                @Override
                public void accept(Throwable throwable) {
                    // 空方法，必须实现，否在在抛出异常时无法捕获导致崩溃
                    listener.onFail(false, throwable);
                }
            });
        }
    }
}
