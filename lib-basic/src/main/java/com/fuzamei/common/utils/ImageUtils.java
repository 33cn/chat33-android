package com.fuzamei.common.utils;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Environment;

import com.fuzamei.common.FzmFramework;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * 创建日期：2018/9/12 on 11:22
 * 描述:
 * 作者:wdl
 */
public class ImageUtils {

    public static String saveImageToGallery(Drawable drawable) {
        Bitmap bmp = drawable2Bitmap(drawable);
        return saveBitmapToGallery(bmp);
    }

    public static String saveBitmapToGallery(Bitmap bmp) {
        return saveBitmapToGallery(bmp, System.currentTimeMillis() + "");
    }

    public static String saveBitmapToGallery(Bitmap bmp, String name) {
        if (bmp == null) {
            return null;
        }
        String imageType = ".jpg";
        Context context = FzmFramework.context;
        File appDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM)
                .getAbsolutePath() + "/Camera");//有特殊手机不在这个目录下
        if (!appDir.exists()) {
            appDir.mkdir();
        }
        String fileName = name + imageType;
        File file = new File(appDir, fileName);
        try {
            FileOutputStream fos = new FileOutputStream(file);
            bmp.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.flush();
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        // 最后通知图库更新
        context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.parse("file://" + file.getAbsolutePath())));
        return file.getAbsolutePath();
    }

    public static Bitmap drawable2Bitmap(Drawable drawable) {
        Bitmap bitmap = Bitmap.createBitmap(
                drawable.getIntrinsicWidth(),
                drawable.getIntrinsicHeight(),
                drawable.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888 : Bitmap.Config.RGB_565);
        Canvas canvas = new Canvas(bitmap);
        //canvas.setBitmap(bitmap);
        drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
        drawable.draw(canvas);
        return bitmap;
    }

    public static byte[] bmpToByteArray(Bitmap bitmap, boolean recycle) {
        int i, j;
        i = bitmap.getWidth();
        j = bitmap.getHeight();

        Bitmap localBitmap = Bitmap.createBitmap(i, j, Bitmap.Config.RGB_565);
        Canvas localCanvas = new Canvas(localBitmap);

        while (true) {
            localCanvas.drawBitmap(bitmap, new Rect(0, 0, i, j), new Rect(0, 0, i, j), null);
            if (recycle) {
                bitmap.recycle();
            }
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            localBitmap.compress(Bitmap.CompressFormat.JPEG, 100, output);
            localBitmap.recycle();
            byte[] arrayOfByte = output.toByteArray();
            try {
                output.close();
                return arrayOfByte;
            } catch (Exception e) {
                e.printStackTrace();
            }
            i = bitmap.getHeight();
            j = bitmap.getHeight();
        }
    }
}
