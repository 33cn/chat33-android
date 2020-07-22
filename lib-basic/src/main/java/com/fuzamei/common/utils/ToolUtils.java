package com.fuzamei.common.utils;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import androidx.core.content.FileProvider;
import android.text.TextUtils;

import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.load.model.LazyHeaders;
import com.fuzamei.common.FzmFramework;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

/**
 * @author zhengjy
 * @since 2018/09/26
 * Description:
 */
public class ToolUtils {

    /**
     * 加密手机号
     * 中间4位加密，其他不加密
     *
     * @param phoneNumber
     * @return
     */
    public static String encryptPhoneNumber(String phoneNumber) {
        return encryptString(phoneNumber, phoneNumber.length() - 8, phoneNumber.length() - 4);
    }

    /**
     * 将一个字符串按要求用'*'加密
     *
     * @param str  要加密的字符串
     * @param from from之后的字符加密
     * @param to   to之前的字符加密
     * @return 加密后的字符串
     */
    public static String encryptString(String str, int from, int to) {
        if (TextUtils.isEmpty(str)) {
            return "";
        }
        int length = str.length();
        if (length < from || length < to || from < 0 || to < 0) {
            return str;
        }
        StringBuilder sb = new StringBuilder();
        if (from <= to) {
            for (int i = 0; i < from; i++) {
                sb.append(str.charAt(i));
            }
            for (int i = from; i < to; i++) {
                sb.append("*");
            }
            for (int i = to; i < length; i++) {
                sb.append(str.charAt(i));
            }
        } else {
            for (int i = 0; i < to; i++) {
                sb.append("*");
            }
            for (int i = to; i < from; i++) {
                sb.append(str.charAt(i));
            }
            for (int i = from; i < length; i++) {
                sb.append("*");
            }
        }
        return sb.toString();
    }

    public static String byte2Mb(long size) {
        StringBuffer bytes = new StringBuffer();
        DecimalFormat format = new DecimalFormat("###.0");
        if (size >= 1024 * 1024 * 1024) {
            double i = (size / (1024.0 * 1024.0 * 1024.0));
            bytes.append(format.format(i)).append("GB");
        }
        else if (size >= 1024 * 1024) {
            double i = (size / (1024.0 * 1024.0));
            bytes.append(format.format(i)).append("MB");
        }
        else if (size >= 1024) {
            double i = (size / (1024.0));
            bytes.append(format.format(i)).append("KB");
        }
        else if (size < 1024) {
            if (size <= 0) {
                bytes.append("0B");
            }
            else {
                bytes.append((int) size).append("B");
            }
        }
        return bytes.toString();
    }

    public static String convertNum(int number) {
        if (number < 10000) {
            return String.valueOf(number);
        } else {
            return FinanceUtils.getPlainNum(number / 10000.0, 1) + "万";
        }
    }

    /**
     * 得到当前应用版本名称的方法
     *
     */
    public static String getVersionName() {
        try {
            PackageManager packageManager = FzmFramework.context.getPackageManager();
            PackageInfo packInfo = packageManager.getPackageInfo(FzmFramework.context.getPackageName(), 0);
            return packInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return "UNKNOWN";
        }
    }

    public static String formatVideoDuration(float time) {
        StringBuilder sb = new StringBuilder();
        int minutes = ((int) time) / 60;
        int second = ((int) time) % 60;
        if (minutes < 10) {
            sb.append(0);
        }
        sb.append(minutes).append(":");
        if (second < 10) {
            sb.append(0);
        }
        sb.append(second);
        return sb.toString();
    }

    public static String formatFileTime(long time) {
        SimpleDateFormat sdf = new SimpleDateFormat("_yyMMddHHmmss");
        return sdf.format(time);
    }

    public static String formatLogTime(long time) {
        SimpleDateFormat sdf = new SimpleDateFormat("yy-MM-dd HH:mm:ss");
        return sdf.format(time);
    }

    public static String formatMonth(long time) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM");
        return sdf.format(time);
    }

    public static String formatDay(long time) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
        return sdf.format(time);
    }

    public static String timeFormat(long time) {
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(time);
        Calendar cCurrent = Calendar.getInstance();
        SimpleDateFormat sdf;
        if ((cCurrent.get(Calendar.YEAR) - c.get(Calendar.YEAR)) > 0) {
            sdf = new SimpleDateFormat("yy-MM-dd HH:mm");
        } else if ((cCurrent.get(Calendar.DAY_OF_YEAR) - c.get(Calendar.DAY_OF_YEAR)) > 0) {
            sdf = new SimpleDateFormat("MM-dd HH:mm");
        } else if ((cCurrent.get(Calendar.DAY_OF_YEAR) - c.get(Calendar.DAY_OF_YEAR)) == 0) {
            sdf = new SimpleDateFormat("HH:mm");
        } else {
            sdf = new SimpleDateFormat("MM-dd HH:mm");
        }
        return sdf.format(time);
    }

    public static int[] getChatImageHeightWidth(Context context, int bitmapHeight, int bitmapWidth) {
        int[] result = new int[2];
        int maxWidth = ScreenUtils.dp2px(context, 150);
        int maxHeight = ScreenUtils.dp2px(context, 150);
        boolean isWidthImage = bitmapWidth - bitmapHeight >= 0;
        int height;
        int width;
        if (isWidthImage) {//宽图
            if (bitmapWidth < maxWidth) {
                result[0] = bitmapHeight;
                result[1] = bitmapWidth;
            } else {
                float d = (float) bitmapHeight / bitmapWidth;
                result[0] = (int) (d * maxWidth);
                result[1] = maxWidth;
            }

        } else {//长图
            if (bitmapHeight < maxHeight) {
                result[0] = bitmapHeight;
                result[1] = bitmapWidth;
            } else {
                float df = (float) bitmapWidth / bitmapHeight;
                result[0] = maxHeight;
                result[1] = (int) (df * maxHeight);
            }

        }
        return result;
    }

    public static GlideUrl createCookieUrl(String url) {
        if (TextUtils.isEmpty(url)) {
            return new GlideUrl("");
        }
        return new GlideUrl(url, new LazyHeaders.Builder().addHeader("Cookie", "chatimage").build());
    }

    public static Map<String, String> getCookieHeaderMap() {
        Map<String, String> headers = new HashMap<String, String>();
        headers.put("Cookie", "chatimage");
        return headers;
    }

    public static String getUUID() {
        String uuid = SharedPrefUtil.getInstance().getDeviceId();
        if (TextUtils.isEmpty(uuid)) {
            uuid = Md5Utils.md5M(android.os.Build.SERIAL + System.currentTimeMillis());
            SharedPrefUtil.getInstance().setDeviceId(uuid);
        }
        return uuid;
    }

    public static int[] getLocalImageHeightWidth(String localPath) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        /**
         * 关键options.inJustDecodeBounds = true;
         * 这里再decodeFile()，返回的bitmap为空，但此时调用options.outHeight时，已经包含了图片的高了
         */
        options.inJustDecodeBounds = true;
        Bitmap bitmap = BitmapFactory.decodeFile(localPath, options); // 此时返回的bitmap为null
        /**
         *options.outHeight为原始图片的高
         */
        int[] result = new int[2];
        result[0] = options.outHeight;
        result[1] = options.outWidth;

        return result;
    }

    /**
     * 根据路径得到视频缩略图
     *
     * @param videoPath
     * @return
     */
    public static Bitmap getVideoPhoto(String videoPath) {
        MediaMetadataRetriever media = new MediaMetadataRetriever();
        if (videoPath.startsWith("http://")
                || videoPath.startsWith("https://")
                || videoPath.startsWith("widevine://")) {
            media.setDataSource(videoPath, new Hashtable<String, String>());
        } else {
            media.setDataSource(videoPath);
        }
        return media.getFrameAtTime(0, MediaMetadataRetriever.OPTION_CLOSEST_SYNC);
    }

    /**
     * 获取视频总时长
     *
     * @param path
     * @return
     */
    public static long getVideoDuration(String path){
        MediaMetadataRetriever media = new MediaMetadataRetriever();
        media.setDataSource(path);
        String duration = media.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
        return (long) (Long.parseLong(duration) / 1000 + 0.5f);
    }

    public static String saveBitmap(Bitmap bmp, String name) {
        String destFileDir = Environment.getExternalStorageDirectory().getPath() + "/" + name + "/image";
        String destFileName = FzmFramework.context.getPackageName() + "_" + System.currentTimeMillis() + ".jpg";
        File dir = new File(destFileDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        File file = new File(destFileDir, destFileName);
        try {
            FileOutputStream fos = new FileOutputStream(file);
            bmp.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.flush();
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return file.getAbsolutePath();
    }

    public static boolean checkInstalled(Context context, String pkgName) {
        if (pkgName == null || pkgName.isEmpty()) {
            return false;
        }
        PackageInfo packageInfo;
        try {
            packageInfo = context.getPackageManager().getPackageInfo(pkgName, 0);
        } catch (PackageManager.NameNotFoundException e) {
            packageInfo = null;
            e.printStackTrace();
        }
        return packageInfo != null;
    }

}
