package com.fuzamei.componentservice.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.fuzamei.componentservice.R;


/**
 * 自定义标题栏
 */
public class CommonTitleBar extends FrameLayout {
    public TextView tv_back;
    public TextView tv_title_middle,tv_title_right;
    public ImageView iv_title_middle;
    public CommonTitleBar(Context context) {
        this(context, null);
    }
    public CommonTitleBar(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CommonTitleBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }
    private void init(Context context) {
        View view = LayoutInflater.from(context).inflate(R.layout.basic_ly_title, null);
        initView(view);
        tv_back = view.findViewById(R.id.iv_back);
        tv_title_middle = view.findViewById(R.id.tv_title_middle);
        tv_title_right = view.findViewById(R.id.tv_title_right);
    }

    private void initView(View view) {
        addView(view);
    }

    //返回区域是否显示
    public void setLeftVisible(boolean visible) {
        if (visible){
            tv_back.setVisibility(VISIBLE);
        }else {
            tv_back.setVisibility(INVISIBLE);
        }
    }

    //右侧区域是否显示
    public void setRightVisible(boolean visible) {
        if (visible){
            tv_title_right.setVisibility(VISIBLE);
        }else {
            tv_title_right.setVisibility(INVISIBLE);
        }
    }

    //返回区域的响应事件
    public void setLeftListener(OnClickListener leftListener) {
        tv_back.setOnClickListener(leftListener);
    }

    //中间标题的文字
    public void setMiddleText(String title) {
        tv_title_middle.setText(title);
    }

    public void setLeftText(String text) {
        tv_back.setText(text);
    }

    //右侧的文字显示，右侧没有内容时，直接填充“”即可
    public void setRightText(String text) {
        tv_title_right.setText(text);
    }

    //右侧的点击事件
    public void setRightListener(OnClickListener rightListener) {
        tv_title_right.setOnClickListener(rightListener);
    }

    // 右侧文字颜色
    public void setRightTextColor(int color) {
        tv_title_right.setTextColor(color);
    }
}
