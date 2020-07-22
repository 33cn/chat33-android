package com.fuzamei.common.utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;

import androidx.annotation.RequiresApi;
import androidx.core.content.FileProvider;

import com.fuzamei.commonlib.R;

import java.io.File;

/**
 * @author zhengjy
 * @since 2019/11/11
 * Description:
 */
public class InstallUtil {

    public static int UNKNOWN_CODE = 2018;

    public static void install(Context context, File file){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) startInstallO(context, file);
        else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) startInstallN(context, file);
        else startInstall(context, file);
    }

    /**
     * android1.x-6.x
     */
    private static void startInstall(Context context, File file) {
        Intent install = new Intent(Intent.ACTION_VIEW);
        install.setDataAndType(Uri.fromFile(file), "application/vnd.android.package-archive");
        install.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(install);
    }

    /**
     * android7.x
     */
    @RequiresApi(api = Build.VERSION_CODES.N)
    private static void startInstallN(Context context, File file) {
        //参数1 上下文, 参数2 在AndroidManifest中的android:authorities值, 参数3  共享的文件
        Uri apkUri = FileProvider.getUriForFile(context, context.getPackageName() + ".fileprovider", file);
        Intent install = new Intent(Intent.ACTION_VIEW);
        //由于没有在Activity环境下启动Activity,设置下面的标签
        install.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        //添加这一句表示对目标应用临时授权该Uri所代表的文件
        install.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
        install.setDataAndType(apkUri, "application/vnd.android.package-archive");
        context.startActivity(install);
    }

    /**
     * android8.x
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    private static void startInstallO(Context context, File file) {
        boolean isGranted = context.getPackageManager().canRequestPackageInstalls();
        if (isGranted) startInstallN(context, file);
        else new AlertDialog.Builder(context)
                .setCancelable(false)
                .setTitle(context.getString(R.string.basic_request_install_unknown))
                .setPositiveButton(context.getString(R.string.basic_confirm), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface d, int w) {
                        Intent intent = new Intent();
                        intent.setData(Uri.parse("package:" + context.getPackageName()));
                        intent.setAction(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES);
                        ((Activity) context).startActivityForResult(intent, UNKNOWN_CODE);
                    }
                })
                .show();
    }
}
