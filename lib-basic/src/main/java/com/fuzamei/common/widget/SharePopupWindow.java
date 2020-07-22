package com.fuzamei.common.widget;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.PopupWindow;

import com.fuzamei.commonlib.R;

/**
 * @author zhengjy
 * @since 2019/03/19
 * Description:红包分享弹窗
 */
public class SharePopupWindow extends PopupWindow {

    private Context context;
    private View parent;

    private View ll_moment;
    private View ll_wechat;
    private View tv_close;
    private OnItemClickListener listener;

    private final static int ANIMATION_TIME = 250;

    public SharePopupWindow(Context context, OnItemClickListener listener) {
        this.context = context;
        this.listener = listener;
        initViews();
    }

    private void initViews() {
        parent = LayoutInflater.from(context).inflate(R.layout.basic_share_popup, null);
        setContentView(parent);
        ll_moment = parent.findViewById(R.id.ll_moment);
        ll_wechat = parent.findViewById(R.id.ll_wechat);
        tv_close = parent.findViewById(R.id.tv_close);
        //设置弹出窗体的高
        setWidth(ViewGroup.LayoutParams.MATCH_PARENT);
        setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
        setOutsideTouchable(true);
        setFocusable(true);
        setOnDismissListener(new OnDismissListener() {
            @Override
            public void onDismiss() {
                backgroundAlpha(1.0f);
            }
        });
        ll_moment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    listener.onItemClick(v, SharePopupWindow.this, 0);
                }
                dismiss();
            }
        });
        ll_wechat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    listener.onItemClick(v, SharePopupWindow.this, 1);
                }
                dismiss();
            }
        });
        tv_close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
    }

    @Override
    public void showAtLocation(View parent, int gravity, int x, int y) {
        super.showAtLocation(parent, gravity, x, y);
        getContentView().startAnimation(createInAnimation());
        backgroundAlpha(0.5f);
    }

    /**
     * 设置滑入动画
     */
    private Animation createInAnimation() {
        final TranslateAnimation animation = new TranslateAnimation(
                TranslateAnimation.RELATIVE_TO_SELF, 0, TranslateAnimation.RELATIVE_TO_SELF, 0,
                TranslateAnimation.RELATIVE_TO_SELF, 1, TranslateAnimation.RELATIVE_TO_SELF, 0);
        animation.setDuration(ANIMATION_TIME);
        animation.setInterpolator(new DecelerateInterpolator());

        return animation;
    }

    private void backgroundAlpha(float alpha) {
        WindowManager.LayoutParams layoutParams = ((Activity) context).getWindow().getAttributes();
        layoutParams.alpha = alpha;
        ((Activity) context).getWindow().setAttributes(layoutParams);
    }

    public interface OnItemClickListener {
        void onItemClick(View view, PopupWindow popupWindow, int position);
    }
}
