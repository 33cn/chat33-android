package com.fzm.chat33.global;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Build;
import android.view.View;

import com.fuzamei.common.utils.InstallUtil;
import com.fzm.chat33.app.App;
import com.fuzamei.common.net.rxjava.ApiException;
import com.fuzamei.common.net.rxjava.HttpResult;
import com.fuzamei.common.utils.ShowUtils;
import com.fuzamei.common.utils.ToolUtils;
import com.fuzamei.update.Interface.IUpdateInfo;
import com.fuzamei.update.Interface.OnAppDownloadListener;
import com.fuzamei.update.Interface.OnCheckUpdateListener;
import com.fuzamei.update.Interface.UserDecision;
import com.fuzamei.update.XUpdate;
import com.fuzamei.update.util.ApkFileUtil;
import com.fzm.chat33.R;
import com.fzm.chat33.bean.UpdateApkInfo;
import com.fzm.chat33.net.AppRequestManager;
import com.fzm.chat33.widget.UpdateDialog;

import java.io.File;
import java.net.SocketTimeoutException;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.functions.Function;

/**
 * @author zhengjy
 * @since 2018/11/01
 * Description:本地数据更新工具
 */
public class LocalData {

    private static UpdateDialog updateDialog;

    public static void checkUpdate(final Context context, final boolean showToast, OnApkFileDownloadListener listener) {
        int version = ApkFileUtil.getVersionCode(context);
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
                    if (showToast) {
                        ShowUtils.showToastNormal(context, R.string.chat_current_version_newest);
                    }
                    return;
                }
                updateDialog = new UpdateDialog.Builder(context)
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
                        .setRightButton(context.getString(R.string.chat_update), new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && context.checkSelfPermission(
                                        Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                                    ((Activity) context).requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 2);
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

                            }
                        })
                        .setCancelable(!force).show();
                App.getInstance().ignoreUpdate = true;
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
                updateDialog.complete(context, file);
                if (listener != null) {
                    listener.onApkFileDownload(file);
                }
                InstallUtil.install(context, file);
            }

            @Override
            public void onFail(boolean force, Throwable e) {
                if (showToast) {
                    if (e instanceof SocketTimeoutException) {
                        ShowUtils.showToast(context, R.string.basic_error_network);
                    } else {
                        ShowUtils.showToast(context, e.getMessage());
                    }
                }
                if (force) {
                    if (updateDialog != null) {
                        updateDialog.reset();
                    }
                } else {
                    if (updateDialog != null) {
                        updateDialog.dismiss();
                    }
                }
                updateDialog = null;
            }
        });
    }

    public static interface OnApkFileDownloadListener {
        void onApkFileDownload(File apkFile);
    }
}
