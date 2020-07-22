package com.fzm.chat33.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import androidx.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

import com.fzm.chat33.R;

/**
 * @author zhengjy
 * @since 2019/04/01
 * Description:
 */
public class CardDivideView extends View {

    //分割线默认颜色
    private final int DIVIDELINE_DEFAULT_COLOR = 0xFFD9D9D9;
    //两端半圆默认颜色
    private final int PORTSHAPE_DEFAULT_COLOR = 0x00000000;
    //两端半圆默认高度(直径)
    private final int PORTSHAPE_DEFAULT_HEIGHT = dp2px(15);

    //分割线颜色
    private int mDividLineColor;
    //两端半圆颜色
    private int mPortShapeColor;
    //两端半圆高度(直径)
    private int mPortShapeHeight;
    //两端半圆半径
    private int mPortShapeRadius;

    private Paint mPaint;

    public CardDivideView(Context context) {
        this(context, null);
    }

    public CardDivideView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CardDivideView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.CardDivideView);
        mDividLineColor = ta.getColor(R.styleable.CardDivideView_divide_line_color, DIVIDELINE_DEFAULT_COLOR);
        mPortShapeColor = ta.getColor(R.styleable.CardDivideView_port_shape_color, PORTSHAPE_DEFAULT_COLOR);
        mPortShapeHeight = (int) ta.getDimension(R.styleable.CardDivideView_port_shape_height, PORTSHAPE_DEFAULT_HEIGHT);
        mPortShapeRadius = mPortShapeHeight / 2;
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setStrokeWidth(dp2px(1));
        ta.recycle();
    }

    private int dp2px(float dpValue) {
        final float scale = getContext().getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthVal = MeasureSpec.getSize(widthMeasureSpec);
        int heightVal = measureHeight(heightMeasureSpec);
        setMeasuredDimension(widthVal, heightVal);
    }

    /**
     * 测量控件高度
     *
     * @param heightMeasureSpec
     * @return
     */
    private int measureHeight(int heightMeasureSpec) {
        int height = MeasureSpec.getSize(heightMeasureSpec);
        int mode = MeasureSpec.getMode(heightMeasureSpec);
        int result = 0;
        if (mode == MeasureSpec.EXACTLY) {
            result = height;
        } else if (mode == MeasureSpec.AT_MOST) {
            result = getPaddingTop() + getPaddingBottom() + mPortShapeHeight;
        } else if (mode == MeasureSpec.UNSPECIFIED) {
            result = getPaddingTop() + getPaddingBottom() + mPortShapeHeight;
        }
        return result;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.save();
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setColor(mPortShapeColor);
        RectF rectF = new RectF(0, 0, mPortShapeHeight, mPortShapeHeight);
        canvas.translate(-mPortShapeRadius, 0);
        canvas.drawArc(rectF, 270, 180, true, mPaint);//从270度开始画，画180度圆弧。
        canvas.restore();
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setColor(mDividLineColor);
        DashPathEffect effect = new DashPathEffect(new float[]{5, 5}, 0);
        mPaint.setPathEffect(effect);
        Path path = new Path();
        path.moveTo(mPortShapeRadius + 3, getMeasuredHeight() / 2);
        path.lineTo(getMeasuredWidth() - mPortShapeRadius - 3, getMeasuredHeight() / 2);
        //虚线两端偏移3个像素
        canvas.drawPath(path, mPaint);
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setColor(mPortShapeColor);
        rectF = new RectF(getMeasuredWidth() - mPortShapeHeight, 0, getMeasuredWidth(), mPortShapeHeight);
        canvas.translate(mPortShapeRadius, 0);
        canvas.drawArc(rectF, 90, 180, true, mPaint);//从90度开始画，画180度圆弧。
    }

}
