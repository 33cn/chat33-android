package com.fuzamei.common.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import androidx.annotation.Nullable;
import android.util.AttributeSet;

import com.fuzamei.common.utils.ScreenUtils;
import com.fuzamei.commonlib.R;

/**
 * @author zhengjy
 * @since 2018/07/12
 * Description:圆角矩形ImageView
 */
public class RoundRectImageView extends androidx.appcompat.widget.AppCompatImageView {

    private Paint mPaint;
    // 圆角半径
    protected float mRadius;
    private Rect mRectSrc;
    private Rect mRectDest;

    public RoundRectImageView(Context context) {
        super(context);
    }

    public RoundRectImageView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public RoundRectImageView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(Context context, @Nullable AttributeSet attrs) {
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.RoundRectImageView);
        setParams(context, typedArray);
        typedArray.recycle();

        mRectSrc = new Rect();
        mRectDest = new Rect();
        setScaleType(ScaleType.CENTER_CROP);
    }

    protected void setParams(Context context, TypedArray typedArray) {
        mRadius = typedArray.getDimension(R.styleable.RoundRectImageView_radius, ScreenUtils.dp2px(context, 5));
    }

    @Override
    protected void onDraw(Canvas canvas) {
        Drawable drawable = getDrawable();
        if (null != drawable && drawable.getIntrinsicHeight() > 0 && drawable.getIntrinsicWidth() > 0) {
            Bitmap mBitmap = getRoundBitmap(drawable);
            mRectSrc.set(0, 0, mBitmap.getWidth(), mBitmap.getHeight());
            mRectDest.set(0, 0, getWidth(), getHeight());
            mPaint.reset();
            canvas.drawBitmap(mBitmap, mRectSrc, mRectDest, mPaint);
        } else {
            super.onDraw(canvas);
        }
    }

    /**
     * 获取圆角矩形图片方法
     *
     */
    private Bitmap getRoundBitmap(Drawable drawable) {
        Bitmap output = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);

        mPaint = new Paint();
        final Rect rect = new Rect(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
        final RectF rectF = new RectF(rect);
        mPaint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);

        canvas.drawRoundRect(rectF, mRadius, mRadius, mPaint);
        mPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(drawable2Bitmap(drawable), rect, rect, mPaint);
        return output;
    }

    public Bitmap drawable2Bitmap(Drawable drawable) {
        return drawable2Bitmap(drawable, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
    }

    public Bitmap drawable2Bitmap(Drawable drawable, int width, int height) {
        Bitmap bitmap = Bitmap.createBitmap(width, height,
                drawable.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888 : Bitmap.Config.RGB_565);
        Canvas canvas = new Canvas(bitmap);
        //canvas.setBitmap(bitmap);
        drawable.setBounds(0, 0, width, height);
        drawable.draw(canvas);
        return bitmap;
    }
}
