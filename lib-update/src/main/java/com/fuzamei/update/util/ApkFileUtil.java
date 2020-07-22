package com.fuzamei.update.util;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.text.TextUtils;

import java.io.File;
import java.io.FileInputStream;
import java.math.BigInteger;
import java.security.MessageDigest;

/**
 * @author zhengjy
 * @since 2018/08/03
 * Description:
 */
public class ApkFileUtil {

    /**
     * 本地已经下载好的安装包是否为最新
     *
     * @param context
     * @param versionCode   服务器安装包的版本号
     * @param path          安装包路径
     */
    public static boolean isLatestApkFile(Context context, int versionCode, String path) {
        int localVersion = -1;
        PackageManager packageManager = context.getPackageManager();
        PackageInfo info = packageManager.getPackageArchiveInfo(path, PackageManager.GET_ACTIVITIES);
        if (info != null) {
            localVersion = info.versionCode;
        }
        return versionCode == localVersion;
    }

    /**
     * 本地已经下载好的安装包是否与服务端的相同
     *
     * @param apkFile   本地安装文件
     * @param md5       服务端安装文件MD5
     */
    public static boolean isSameApkFile(File apkFile, String md5) {
        if (md5 == null || md5.equals("")) {
            return false;
        }
        String localMD5 = getFileMD5(apkFile);
        return !TextUtils.isEmpty(localMD5) && localMD5.equalsIgnoreCase(md5);
    }

    /**
     * App需要更新
     *
     * @param context
     * @param versionCode   服务端更新包的版本号
     * @return              是否需要更新
     */
    public static boolean needToUpdate(Context context, int versionCode) {
        return getVersionCode(context) < versionCode;
    }

    /**
     * 得到当前应用版本名称的方法
     *
     * @param context :上下文
     */
    public static int getVersionCode(Context context) {
        try {
            PackageManager packageManager = context.getPackageManager();
            // getPackageName()是当前类的包名
            PackageInfo packInfo = packageManager.getPackageInfo(context.getPackageName(), 0);
            return packInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return 0;
        }
    }

    /**
     * 得到当前应用版本名称的方法
     *
     * @param context :上下文
     */
    public static String getVersionName(Context context) {
        try {
            PackageManager packageManager = context.getPackageManager();
            // getPackageName()是当前类的包名
            PackageInfo packInfo = packageManager.getPackageInfo(context.getPackageName(), 0);
            return packInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return "";
        }
    }

    public static String getFileMD5(File file) {
        if (!file.isFile()) {
            return null;
        }
        MessageDigest digest;
        FileInputStream in;
        byte buffer[] = new byte[1024];
        int len;
        try {
            digest = MessageDigest.getInstance("MD5");
            in = new FileInputStream(file);
            while ((len = in.read(buffer, 0, 1024)) != -1) {
                digest.update(buffer, 0, len);
            }
            in.close();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return bytesToHexString(digest.digest());
    }

    private static String bytesToHexString(byte[] src) {
        StringBuilder stringBuilder = new StringBuilder();
        if (src == null || src.length <= 0) {
            return null;
        }
        for (int i = 0; i < src.length; i++) {
            int v = src[i] & 0xFF;
            String hv = Integer.toHexString(v);
            if (hv.length() < 2) {
                stringBuilder.append(0);
            }
            stringBuilder.append(hv);
        }
        return stringBuilder.toString();
    }
}
