package com.fuzamei.common.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * @author zhengjy
 * @since 2018/10/08
 * Description:
 */
public class PicUtils {
    /***
     * 图片的缩放方法
     *
     * @param bigImage   ：源图片资源
     * @param newWidth  ：缩放后宽度
     * @param newHeight ：缩放后高度
     * @return
     */
    public static Bitmap zoomImage(Bitmap bigImage, double newWidth,
                                   double newHeight) {
        // 获取这个图片的宽和高
        float width = bigImage.getWidth();
        float height = bigImage.getHeight();
        // 创建操作图片用的matrix对象
        Matrix matrix = new Matrix();
        // 计算宽高缩放率
        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;
        // 缩放图片动作
        matrix.postScale(scaleWidth, scaleHeight);
        Bitmap bitmap = Bitmap.createBitmap(bigImage, 0, 0, (int) width,
                (int) height, matrix, true);
        return bitmap;
    }

    public static File getFileThumb(Bitmap bitmap, String pathName, String fileName) {
        //获取系统sd卡路径
        //新建的路径一定要判断是否是不存在的，不存在就创建。
        File fileCard = new File(pathName);
        try {
            if (!fileCard.exists())
                fileCard.mkdirs();//mkdirs是可以生成嵌套文件夹的，mkdir只能生成不存在的一级文件夹

            //在此路径下创建一个图片文件名，用户id和币种名字
            fileCard = new File(pathName,fileName);
            fileCard.delete();
            BufferedOutputStream bos = null;
            bos = new BufferedOutputStream(new FileOutputStream(fileCard));
            //Bitmap bitmap = imageList.get(i);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 30, bos);
            bos.flush();
            bos.close();
            return fileCard;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }


    public static Bitmap getBitmapFromPath(String path) {

        if (!new File(path).exists()) {
            System.err.println("getBitmapFromPath: file not exists");
            return null;
        }
        // Bitmap bitmap = Bitmap.createBitmap(1366, 768, Config.ARGB_8888);
        // Canvas canvas = new Canvas(bitmap);
        // Movie movie = Movie.decodeFile(path);
        // movie.draw(canvas, 0, 0);
        //
        // return bitmap;
        byte[] buf = new byte[1024 * 1024];// 1M
        Bitmap bitmap = null;

        try {

            FileInputStream fis = new FileInputStream(path);
            int len = fis.read(buf, 0, buf.length);
            bitmap = BitmapFactory.decodeByteArray(buf, 0, len);
            if (bitmap == null) {
                System.out.println("len= " + len);
                System.err
                        .println("path: " + path + "  could not be decode!!!");
            }
        } catch (Exception e) {
            e.printStackTrace();

        }
        return bitmap;
    }
}
