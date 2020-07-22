package com.fzm.chat33.utils;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Environment;
import androidx.core.app.ActivityCompat;
import android.webkit.MimeTypeMap;

import com.fuzamei.componentservice.config.AppConfig;

import java.io.File;
import java.io.FileInputStream;
import java.security.MessageDigest;

/**
 * @author zhengjy
 * @since 2018/12/27
 * Description:
 */
public class FileUtils {

    /**
     * 获取Image路径
     *
     * @param context
     * @return
     */
    public static String getImageCachePath(Context context) {
        String imagePath = context.getCacheDir() + File.separator + AppConfig.IMAGE_CACHE;
        if (!new File(imagePath).exists()) {
            boolean mkdirs = new File(imagePath).mkdirs();
        }

        return imagePath;
    }

    public static String getFileNameUnique(File folder, String name) {
        int count = 0;
        String fileName = name;
        File file = new File(folder, name);
        while (file.exists()) {
            count++;
            int dotPos = name.lastIndexOf('.');
            if (dotPos == -1) {
                fileName = name + "(" + count + ")";
            } else {
                fileName = name.substring(0, dotPos) + "(" + count + ")" + name.substring(dotPos);
            }
            file = new File(folder, fileName);
        }
        return fileName;
    }

    /**
     * 获取数据库路径
     *
     * @param context
     * @return
     */
    public static String getAudioCachePath(Context context) {
        String audioPath = context.getCacheDir() + File.separator + AppConfig.AUDIO_CACHE;
        if (!new File(audioPath).exists()) {
            boolean mkdirs = new File(audioPath).mkdirs();
        }

        return audioPath;
    }

    public static boolean isGrantCamera(int requestCode, Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            if (activity.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                    || activity.checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(activity, new String[]{
                        Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA
                }, requestCode);
                return false;
            } else {
                return true;
            }
        }

        return true;
    }

    public static boolean isGrantShoot(int requestCode, Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            if (activity.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                    || activity.checkSelfPermission(Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED
                    || activity.checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(activity, new String[]{
                        Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.RECORD_AUDIO, Manifest.permission.CAMERA
                }, requestCode);
                return false;
            } else {
                return true;
            }
        }

        return true;
    }

    public static boolean isGrantExternalRW(int requestCode, Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && activity.checkSelfPermission(
                Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(activity, new String[]{
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
            }, requestCode);

            return false;
        }

        return true;
    }

    public static boolean isAudioGrant(int requestCode, Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && (activity.checkSelfPermission(
                Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED || activity.checkSelfPermission(
                Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED)) {

            ActivityCompat.requestPermissions(activity, new String[]{
                    Manifest.permission.RECORD_AUDIO,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
            }, requestCode);

            return false;
        }

        return true;
    }

    /**
     * 本地已经下载好的文件是否与服务端的相同
     *
     * @param file      本地文件
     * @param md5       服务端文件MD5
     */
    public static boolean isSameFile(File file, String md5) {
        if (md5 == null || md5.equals("")) {
            return false;
        }
        String localMD5 = getFileMD5(file);
        return md5.equals(localMD5);
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

    /**
     * 获取文件大小
     */
    public static long getLength(String path) {
        File file = new File(path);
        if (!file.isFile() || !file.exists()) {
            return 0;
        }
        return file.length();
    }

    /**
     * 获取文件后缀,不包括“.”
     */
    public static String getExtension(String pathOrUrl) {
        if (pathOrUrl == null) {
            return "ext";
        }
        int dotPos = pathOrUrl.lastIndexOf('.');
        if (0 <= dotPos) {
            return pathOrUrl.substring(dotPos + 1).toLowerCase();
        } else {
            return "ext";
        }
    }

    /**
     * 获取文件的MIME类型
     */
    public static String getMimeType(String pathOrUrl) {
        String ext = getExtension(pathOrUrl);
        MimeTypeMap map = MimeTypeMap.getSingleton();
        String mimeType;
        if (map.hasExtension(ext)) {
            mimeType = map.getMimeTypeFromExtension(ext);
        } else {
            mimeType = "*/*";
        }
        return mimeType;
    }
}
