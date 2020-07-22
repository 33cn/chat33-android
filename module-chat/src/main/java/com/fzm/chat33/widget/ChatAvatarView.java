package com.fzm.chat33.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import androidx.appcompat.content.res.AppCompatResources;
import android.util.AttributeSet;

import com.fuzamei.common.utils.ScreenUtils;
import com.fuzamei.common.widget.RoundRectImageView;
import com.fzm.chat33.R;

/**
 * @author zhengjy
 * @since 2019/07/22
 * Description:右下角有小图标的ImageView
 */
public class ChatAvatarView extends RoundRectImageView {

    // 右下角icon大小
    protected float mIconSize;
    // 右下角icon图标
    protected int mIconRes;

    private Bitmap mIcon = null;
    private Paint mPaint = new Paint();

    public ChatAvatarView(Context context) {
        this(context, null);
    }

    public ChatAvatarView(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.ChatAvatarView);
        mIconSize = ta.getDimension(R.styleable.ChatAvatarView_iconSize, ScreenUtils.dp2px(context, 10));
        mIconRes = ta.getResourceId(R.styleable.ChatAvatarView_iconSrc, -1);
        ta.recycle();

        init();
    }

    private void init() {
        if (mIconRes != -1) {
            Drawable drawable = AppCompatResources.getDrawable(getContext(), mIconRes);
            mIcon = drawable2Bitmap(drawable, (int) mIconSize, (int) mIconSize);
        } else {
            mIcon = null;
        }
    }

    public void setIconSize(int iconSize) {
        this.mIconSize = iconSize;
        init();
        invalidate();
    }

    public void setIconRes(int iconRes) {
        this.mIconRes = iconRes;
        init();
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (mIcon != null) {
            canvas.drawBitmap(mIcon, getMeasuredWidth() - mIcon.getWidth(), getMeasuredHeight() - mIcon.getHeight(), mPaint);
        }
    }
}
