package com.fzm.chat33.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;

import com.fuzamei.common.utils.ScreenUtils;
import com.fuzamei.common.widget.RoundRectImageView;

/**
 * @author zhengjy
 * @since 2019/01/15
 * Description:
 */
public class ResizableImageView extends RoundRectImageView {

    public ResizableImageView(Context context) {
        super(context);
    }

    public ResizableImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void setParams(Context context, TypedArray typedArray) {
        mRadius = typedArray.getDimension(com.fuzamei.commonlib.R.styleable.RoundRectImageView_radius, ScreenUtils.dp2px(context, 0));
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec){
        Drawable d = getDrawable();

        if(d!=null){
            // ceil not round - avoid thin vertical gaps along the left/right edges
            int width = MeasureSpec.getSize(widthMeasureSpec);
            //高度根据使得图片的宽度充满屏幕计算而得
            int height = (int) Math.ceil((float) width * (float) d.getIntrinsicHeight() / (float) d.getIntrinsicWidth());
            setMeasuredDimension(width, height);
        }else{
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        }
    }

}
