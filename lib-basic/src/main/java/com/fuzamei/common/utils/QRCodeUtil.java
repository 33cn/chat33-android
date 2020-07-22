package com.fuzamei.common.utils;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import androidx.annotation.ColorInt;
import androidx.annotation.Nullable;

import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.text.TextUtils;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.ChecksumException;
import com.google.zxing.DecodeHintType;
import com.google.zxing.EncodeHintType;
import com.google.zxing.FormatException;
import com.google.zxing.NotFoundException;
import com.google.zxing.RGBLuminanceSource;
import com.google.zxing.Result;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.common.CharacterSetECI;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.QRCodeReader;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

import java.util.EnumMap;
import java.util.Hashtable;
import java.util.Map;

/**
 * @author zhengjy
 * @since 2018/10/26
 * Description:二维码工具类
 */
public class QRCodeUtil {

    /**
     * 创建二维码位图
     *
     * @param content 字符串内容(支持中文)
     * @param width 位图宽度(单位:px)
     * @param height 位图高度(单位:px)
     * @return
     */
    @Nullable
    public static Bitmap createQRCodeBitmap(String content, int width, int height){
        return createQRCodeBitmap(content, width, height, "UTF-8", "H", "1", Color.BLACK, Color.WHITE);
    }

    @Nullable
    public static Bitmap createQRCodeBitmapWithLogo(String content, int width, int height, Bitmap logo){
        return addLogo(createQRCodeBitmap(content, width, height, "UTF-8", "H", "1", Color.BLACK, Color.WHITE), logo);
    }

    /**
     * 创建二维码位图 (支持自定义配置和自定义样式)
     *
     * @param content 字符串内容
     * @param width 位图宽度,要求>=0(单位:px)
     * @param height 位图高度,要求>=0(单位:px)
     * @param character_set 字符集/字符转码格式 (支持格式:{@link CharacterSetECI })。传null时,zxing源码默认使用 "ISO-8859-1"
     * @param error_correction 容错级别 (支持级别:{@link ErrorCorrectionLevel })。传null时,zxing源码默认使用 "L"
     * @param margin 空白边距 (可修改,要求:整型且>=0), 传null时,zxing源码默认使用"4"。
     * @param color_black 黑色色块的自定义颜色值
     * @param color_white 白色色块的自定义颜色值
     * @return
     */
    @Nullable
    public static Bitmap createQRCodeBitmap(String content, int width, int height,
                                            @Nullable String character_set, @Nullable String error_correction, @Nullable String margin,
                                            @ColorInt int color_black, @ColorInt int color_white){

        /** 1.参数合法性判断 */
        if(TextUtils.isEmpty(content)){ // 字符串内容判空
            return null;
        }

        if(width < 0 || height < 0){ // 宽和高都需要>=0
            return null;
        }

        try {
            /** 2.设置二维码相关配置,生成BitMatrix(位矩阵)对象 */
            Hashtable<EncodeHintType, String> hints = new Hashtable<>();

            if(!TextUtils.isEmpty(character_set)) {
                hints.put(EncodeHintType.CHARACTER_SET, character_set); // 字符转码格式设置
            }

            if(!TextUtils.isEmpty(error_correction)){
                hints.put(EncodeHintType.ERROR_CORRECTION, error_correction); // 容错级别设置
            }

            if(!TextUtils.isEmpty(margin)){
                hints.put(EncodeHintType.MARGIN, margin); // 空白边距设置
            }
            BitMatrix bitMatrix = new QRCodeWriter().encode(content, BarcodeFormat.QR_CODE, width, height, hints);

            /** 3.创建像素数组,并根据BitMatrix(位矩阵)对象为数组元素赋颜色值 */
            int[] pixels = new int[width * height];
            for(int y = 0; y < height; y++){
                for(int x = 0; x < width; x++){
                    if(bitMatrix.get(x, y)){
                        pixels[y * width + x] = color_black; // 黑色色块像素设置
                    } else {
                        pixels[y * width + x] = color_white; // 白色色块像素设置
                    }
                }
            }

            /** 4.创建Bitmap对象,根据像素数组设置Bitmap每个像素点的颜色值,之后返回Bitmap对象 */
            Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            bitmap.setPixels(pixels, 0, width, 0, 0, width, height);
            return bitmap;
        } catch (WriterException e) {
            e.printStackTrace();
        }

        return null;
    }

    public static Result decodeFromPicture(Bitmap bitmap) {
        if (bitmap == null) {
            return null;
        }
        // 解析配置参数
        Map<DecodeHintType, Object> hints = new EnumMap<>(DecodeHintType.class);
        hints.put(DecodeHintType.CHARACTER_SET, "utf-8");
        hints.put(DecodeHintType.TRY_HARDER, true);
        // 放大/缩小限制次数(每次放大/缩小2倍)
        int sizeAddLimit = 1;
        Matrix sizeAddMatrix = new Matrix();
        sizeAddMatrix.postScale(2.0f, 2.0f);
        int sizeSubLimit = 1;
        Matrix sizeSubMatrix = new Matrix();
        sizeSubMatrix.postScale(0.5f, 0.5f);
        // 开始解析
        Bitmap scanBitmap = toGrayScale(bitmap);
        try {
            while (true) {
                int picWidth = scanBitmap.getWidth();
                int picHeight = scanBitmap.getHeight();
                int[] pix = new int[picWidth * picHeight];
                scanBitmap.getPixels(pix, 0, picWidth, 0, 0, picWidth, picHeight);
                //构造LuminanceSource对象
                RGBLuminanceSource rgbLuminanceSource = new RGBLuminanceSource(picWidth
                        , picHeight, pix);
                BinaryBitmap bb = new BinaryBitmap(new HybridBinarizer(rgbLuminanceSource));
                //因为解析的条码类型是二维码，所以这边用QRCodeReader最合适。
                QRCodeReader qrCodeReader = new QRCodeReader();
                Result result = null;
                try {
                    result = qrCodeReader.decode(bb, hints);
                    return result;
                } catch (NotFoundException | ChecksumException | FormatException e) {
                    e.printStackTrace();
                }
                if (sizeAddLimit > 0) {
                    scanBitmap = Bitmap.createBitmap(scanBitmap, 0, 0, scanBitmap.getWidth(), scanBitmap.getHeight(), sizeAddMatrix, true);
                    sizeAddLimit--;
                } else if (sizeSubLimit > 0) {
                    scanBitmap = Bitmap.createBitmap(scanBitmap, 0, 0, scanBitmap.getWidth(), scanBitmap.getHeight(), sizeSubMatrix, true);
                    sizeSubLimit--;
                } else {
                    return null;
                }
            }
        } finally {
            scanBitmap.recycle();
        }
    }

    private static Bitmap toGrayScale(Bitmap original) {
        int width, height;
        height = original.getHeight();
        width = original.getWidth();

//        while (width >= 540 || height >= 960) {
//            width /= 2;
//            height /= 2;
//        }
        Bitmap bmpGrayscale = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
        Canvas c = new Canvas(bmpGrayscale);
        Paint paint = new Paint();
        ColorMatrix cm = new ColorMatrix();
        cm.setSaturation(0);
        ColorMatrixColorFilter f = new ColorMatrixColorFilter(cm);
        paint.setColorFilter(f);
        c.drawBitmap(original, 0, 0, paint);
        return bmpGrayscale;
    }

    /**
     * 在二维码中间添加Logo图案
     */
    private static Bitmap addLogo(Bitmap src, Bitmap logo) {
        if (src == null) {
            return null;
        }
        if (logo == null) {
            return src;
        }
        //获取图片的宽高
        int srcWidth = src.getWidth();
        int srcHeight = src.getHeight();
        int logoWidth = logo.getWidth();
        int logoHeight = logo.getHeight();
        if (srcWidth == 0 || srcHeight == 0) {
            return null;
        }
        if (logoWidth == 0 || logoHeight == 0) {
            return src;
        }
        //logo大小为二维码整体大小的1/5
        float scaleFactor = srcWidth * 1.0f / 6 / logoWidth;
        Bitmap bitmap = Bitmap.createBitmap(srcWidth, srcHeight, Bitmap.Config.ARGB_8888);
        try {
            Canvas canvas = new Canvas(bitmap);
            canvas.drawBitmap(src, 0, 0, null);
            canvas.scale(scaleFactor, scaleFactor, srcWidth / 2, srcHeight / 2);
            canvas.drawBitmap(logo, (srcWidth - logoWidth) / 2, (srcHeight - logoHeight) / 2, null);
//            canvas.save(Canvas.ALL_SAVE_FLAG);
            // todo : 2019/9/19
            canvas.save();
            canvas.restore();
        } catch (Exception e) {
            bitmap = null;
            e.getStackTrace();
        }
        return bitmap;
    }
}
