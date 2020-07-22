package com.fuzamei.common.widget;

import android.content.Context;
import android.graphics.Typeface;
import androidx.appcompat.widget.AppCompatTextView;
import android.util.AttributeSet;

/**
 * 由于ui提供的图标为iconfont格式，写了一个自定义textview提供imageview相似功能
 * 支持使用iconfont显示图标
 * 支持iconfont动画播放
 * @setAnimResource 传入动画资源
 *
 * @author chengtao
 * */
public class IconView extends AppCompatTextView implements Runnable {

    private int[] animResource;
    private int animSize;
    private int duration;
    private int animPosition;
    private boolean isPlaying = false;

    public IconView(Context context) {
        super(context);
        init();
    }

    public IconView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public IconView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }


    private void init() {
//        设置字体图标
        this.setTypeface(Typeface.createFromAsset(getContext().getAssets(), "iconfont.ttf"));


    }


    public void setIconText(int resId) {
        super.setBackgroundDrawable(null);
        setText(resId);
    }


    public void setIconBackground(int resId) {
        setText("");
        super.setBackgroundResource(resId);
    }

    public void setAnimResource(int duration, int[] resIds) {
        animResource = resIds;
        animSize = animResource.length;
        this.duration = duration;
    }

    public void play() {
        if (animResource != null) {
            if (!isPlaying) {
                animPosition = 0;
                isPlaying = true;
                postDelayed(this, duration);
            }
        }
    }

    public void stop() {
        removeCallbacks(this);
        isPlaying = false;
        animPosition = animSize - 1;
        setText(animResource[animPosition]);
    }

    public void reset() {
        removeCallbacks(this);
        isPlaying = false;
        animPosition = animSize - 1;
        setText(animResource[animPosition]);
    }

    @Override
    public void run() {
        if (isPlaying) {
            setText(animResource[animPosition]);
            animPosition++;
            if (animPosition >= animSize) {
                animPosition = 0;
            }
            postDelayed(this, duration);
        }

    }
}
