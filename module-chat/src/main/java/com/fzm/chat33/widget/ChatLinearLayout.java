package com.fzm.chat33.widget;

import android.content.Context;
import androidx.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.LinearLayout;

/**
 * @author zhengjy
 * @since 2018/12/22
 * Description:
 */
public class ChatLinearLayout extends LinearLayout {

    private boolean selectable = false;

    public ChatLinearLayout(Context context) {
        super(context);
    }

    public ChatLinearLayout(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public ChatLinearLayout(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setSelectable(boolean selectable) {
        this.selectable = selectable;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return selectable || super.onInterceptTouchEvent(ev);
    }
}
