package com.fzm.shell;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import android.text.TextUtils;
import android.util.Log;
import android.view.View;

import com.alibaba.android.arouter.facade.annotation.Autowired;
import com.alibaba.android.arouter.facade.annotation.Route;
import com.alibaba.android.arouter.launcher.ARouter;
import com.fuzamei.common.net.rxjava.ApiException;
import com.fuzamei.common.net.rxjava.HttpResult;
import com.fuzamei.common.utils.InstallUtil;
import com.fuzamei.common.utils.PermissionUtil;
import com.fuzamei.common.utils.ShowUtils;
import com.fuzamei.common.utils.ToolUtils;
import com.fuzamei.componentservice.app.AppRoute;
import com.fuzamei.componentservice.base.LoadableActivity;
import com.fuzamei.componentservice.config.AppConfig;
import com.fuzamei.componentservice.config.AppPreference;
import com.fzm.chat33.bean.AdDisplayBean;
import com.fzm.chat33.core.Chat33;
import com.fzm.chat33.core.bean.AdInfoBean;
import com.fzm.chat33.core.global.UserInfo;
import com.fuzamei.update.Interface.IUpdateInfo;
import com.fuzamei.update.Interface.OnAppDownloadListener;
import com.fuzamei.update.Interface.OnCheckUpdateListener;
import com.fuzamei.update.Interface.UserDecision;
import com.fuzamei.update.XUpdate;
import com.fuzamei.update.util.ApkFileUtil;
import com.fzm.chat33.bean.UpdateApkInfo;
import com.fzm.chat33.core.net.RequestManager;
import com.fzm.chat33.core.utils.Md5Utils;
import com.fuzamei.componentservice.app.RouterHelper;
import com.fzm.chat33.main.fragment.BaseSplashAdFragment;
import com.fzm.chat33.main.fragment.SplashGifAdFragment;
import com.fzm.chat33.main.fragment.SplashJpgAdFragment;
import com.fzm.chat33.main.fragment.SplashVideoAdFragment;
import com.fzm.chat33.net.AppRequestManager;
import com.fzm.chat33.utils.FileUtils;
import com.fzm.chat33.widget.UpdateDialog;
import com.umeng.message.PushAgent;

import java.io.File;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

/**
 * @author zhengjy
 * @since 2018/11/06
 * Description:App启动页
 */
@Route(path = AppRoute.SPLASH)
public class SplashActivity extends LoadableActivity {

    private UpdateDialog updateDialog;
    private final int REQUEST_START_UP = 1;
    private final int REQUEST_UPDATE = 2;

    private View fl_container;
    private BaseSplashAdFragment splashAdFragment;

    private File installApk;

    @Autowired
    public String params;
    @Autowired
    public Bundle data;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_splash;
    }

    @Override
    protected void initView() {
        ARouter.getInstance().inject(this);
        fl_container = findViewById(R.id.fl_container);
        fl_container.setAlpha(0f);
        if (data == null) {
            data = new Bundle();
        }
        if (params != null) {
            Uri route = Uri.parse(RouterHelper.APP_LINK + "?" + params);
            data.putParcelable("route", route);
        }
    }

    @AfterPermissionGranted(REQUEST_START_UP)
    @Override
    protected void initData() {
        if (!PermissionUtil.hasWriteExternalPermission()) {
            EasyPermissions.requestPermissions(instance, getString(R.string.app_error_permission_storage),
                    REQUEST_START_UP, Manifest.permission.WRITE_EXTERNAL_STORAGE);
            return;
        }
        checkUpdate();
    }

    @SuppressLint("CheckResult")
    private void startApp() {
        String token = AppPreference.INSTANCE.getTOKEN();
        // 配置为不显示广告或者没登录，则只显示启动页
        if (!AppConfig.APP_SHOW_AD || TextUtils.isEmpty(token)) {
            // 不显示广告则延时进入主界面
            getWindow().getDecorView().postDelayed(new Runnable() {
                @Override
                public void run() {
                    mainPage();
                }
            }, 1000);
            return;
        }
        if (UserInfo.getInstance().isLogin()) {
            mainPage();
            return;
        }
        RequestManager.INS.getSplashAdInfo()
                .subscribeOn(Schedulers.io())
                .delay(1000, TimeUnit.MILLISECONDS)
                .flatMap(new Function<HttpResult<AdInfoBean>, ObservableSource<AdDisplayBean>>() {
                    @Override
                    public ObservableSource<AdDisplayBean> apply(HttpResult<AdInfoBean> result) throws Exception {
                        if (result.getCode() != 0) {
                            throw new ApiException(result.getCode(), result.getMessage());
                        }
                        AdInfoBean adInfo = result.getData();
                        if (adInfo == null) {
                            return Observable.error(new Exception("no AD supplied!"));
                        }
                        File folder = new File(getFilesDir() + "/ad");
                        if (!folder.exists()) {
                            folder.mkdirs();
                        }
                        String ext = FileUtils.getExtension(adInfo.url);
                        int type;
                        if ("jpg".equals(ext)) {
                            type = 0;
                        } else if ("mp4".equals(ext)) {
                            type = 1;
                        } else if ("gif".equals(ext)) {
                            type = 2;
                        } else {
                            return Observable.error(new IllegalStateException("not supported AD type!"));
                        }
                        File pic = new File(folder, Md5Utils.md5M(adInfo.url) + "." + ext);
                        if (!pic.exists()) {
                            Log.d("ad_splash", "start " + System.currentTimeMillis());
                            RequestManager.INS.downloadSplashAd(folder.getAbsolutePath(), pic.getName(), adInfo.url);
                            Log.d("ad_splash", "  end " + System.currentTimeMillis());
                        }
                        return Observable.just(new AdDisplayBean(adInfo.link, type, adInfo.duration, pic.getAbsolutePath()));
                    }
                })
                .subscribeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<AdDisplayBean>() {
                    @Override
                    public void accept(AdDisplayBean adDisplay) throws Exception {
                        if (adDisplay.type == 0) {
                            splashAdFragment = SplashJpgAdFragment.create(adDisplay);
                        } else if (adDisplay.type == 1) {
                            splashAdFragment = SplashVideoAdFragment.create(adDisplay);
                        } else if (adDisplay.type == 2) {
                            splashAdFragment = SplashGifAdFragment.create(adDisplay);
                        }
                        if (splashAdFragment != null) {
                            splashAdFragment.setOnAdDisplayFinish(new BaseSplashAdFragment.OnAdDisplayFinishListener() {
                                @Override
                                public void adDisplayFinish(String route) {
                                    mainPage(route);
                                }
                            });
                            addFragment(R.id.fl_container, splashAdFragment);
                            fl_container.animate().alpha(1f).setDuration(500).setListener(null);
                        } else {
                            mainPage();
                        }
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        throwable.printStackTrace();
                        mainPage();
                    }
                });
    }

    private void mainPage(String route) {
        if (!TextUtils.isEmpty(route)) {
            Uri uri = Uri.parse(route);
            data.putParcelable("route", uri);
        }
        String token = AppPreference.INSTANCE.getTOKEN();
        if (!TextUtils.isEmpty(token)) {
            ARouter.getInstance().build(AppRoute.MAIN)
                    .withBundle("data", data)
                    .navigation();
        } else {
            ARouter.getInstance().build(AppRoute.LOGIN)
//                    .withBoolean("showAD", true)
                    .withBundle("data", data)
                    .navigation();
        }
        finish();
    }

    private void mainPage() {
        mainPage(null);
    }

    @Override
    public void onBackPressed() {

    }

    @Override
    protected void setEvent() {

    }

    @Override
    protected void setStatusBar() {

    }

    private void checkUpdate() {
        int version = ApkFileUtil.getVersionCode(this);
        XUpdate.checkUpdate(AppRequestManager.INS.checkUpdate(version).map(new Function<HttpResult<UpdateApkInfo>, IUpdateInfo>() {
            @Override
            public IUpdateInfo apply(HttpResult<UpdateApkInfo> result) throws Exception {
                if ((result.getCode() != 0)) {
                    throw new ApiException(result.getCode(), result.getMessage());
                }
                return result.getData();
            }
        }).flatMap(new Function<IUpdateInfo, ObservableSource<IUpdateInfo>>() {
            @Override
            public ObservableSource<IUpdateInfo> apply(IUpdateInfo iUpdateInfo) throws Exception {
                return Observable.just(iUpdateInfo);
            }
        }), new OnCheckUpdateListener() {
            @Override
            public void onCheckUpdateResult(boolean needToUpdate, final boolean force, final UserDecision decision) {
                if (!needToUpdate) {
                    startApp();
                    return;
                }
                updateDialog = new UpdateDialog.Builder(instance)
                        .setContentView(R.layout.dialog_update)
                        .setVersion("V" + decision.getUpdateInfo().getVersionName())
                        .setFileSize(ToolUtils.byte2Mb(decision.getUpdateInfo().getApkSize()))
                        .setMessage(decision.getUpdateInfo().getDescription())
                        .setLeftButton(!force, new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                decision.update(false);
                                updateDialog.dismiss();
                            }
                        })
                        .setRightButton(getString(R.string.app_update_immediately), new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                if (!PermissionUtil.hasWriteExternalPermission()) {
                                    EasyPermissions.requestPermissions(instance, getString(R.string.app_error_update_need_permission_storage),
                                            REQUEST_UPDATE, Manifest.permission.WRITE_EXTERNAL_STORAGE);
                                    return;
                                }
                                updateDialog.setCancelable(false);
                                decision.update(true);
                                updateDialog.setProgress(0);
                            }
                        })
                        .setOnDismissListener(new DialogInterface.OnDismissListener() {
                            @Override
                            public void onDismiss(DialogInterface dialog) {
                                startApp();
                            }
                        })
                        .setCancelable(!force).show();
                Chat33.INSTANCE.setIgnoreUpdate(true);
            }
        }).start(new OnAppDownloadListener() {
            @Override
            public void onStart() {

            }

            @Override
            public void onProgress(float progress) {
                // 更新进度
                updateDialog.setProgress((int) (100 * progress));
            }

            @Override
            public void onSuccess(boolean force, File file) {
                updateDialog.complete(instance, file);
                try {
                    installApk = file;
                    InstallUtil.install(instance, file);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFail(boolean force, Throwable e) {
                if (e instanceof SocketTimeoutException
                        || e instanceof ConnectException) {
                    ShowUtils.showToast(instance, getString(R.string.app_error_network));
                } else {
                    ShowUtils.showToast(instance, e.getMessage());
                }
                if (force) {
                    if (updateDialog != null) {
                        updateDialog.reset();
                    }
                } else {
                    startApp();
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == InstallUtil.UNKNOWN_CODE) {
                InstallUtil.install(instance, installApk);
            }
        }
    }

    @AfterPermissionGranted(REQUEST_UPDATE)
    public void updateApp() {
        updateDialog.performConfirm();
    }

    @Override
    public void onPermissionsDenied(int requestCode, @NonNull List<String> perms) {
        super.onPermissionsDenied(requestCode, perms);
        if (requestCode == REQUEST_START_UP) {
            checkUpdate();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        PushAgent.getInstance(this).onAppStart();
        if (updateDialog != null) {
            // 防止启动时，如果打开"最近任务"再回来使dialog不可见
            updateDialog.hide();
            updateDialog.show();
        }
    }
}
