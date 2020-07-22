package com.fuzamei.common.widget;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.GradientDrawable;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.PopupWindow;

import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.fuzamei.common.recycleviewbase.CommonAdapter;
import com.fuzamei.common.recycleviewbase.MultiItemTypeAdapter;
import com.fuzamei.common.recycleviewbase.ViewHolder;
import com.fuzamei.commonlib.R;

import java.util.List;

/**
 * @author zhengjy
 * @since 2018/11/1
 * Description:底部弹出的popupWindow
 */
public class BottomPopupWindow extends PopupWindow {

    private Context context;
    private View parent;

    private RecyclerView rv_options;
    private List<String> options;
    private List<String> tips;
    private CommonAdapter<String> adapter;
    private OnItemClickListener listener;

    private final static int ANIMATION_TIME = 250;

    public BottomPopupWindow(Context context, List<String> options, OnItemClickListener listener) {
        this.context = context;
        this.options = options;
        this.listener = listener;
        initViews();
    }

    public BottomPopupWindow(Context context, List<String> options, List<String> tips, OnItemClickListener listener) {
        this.context = context;
        this.options = options;
        this.tips = tips;
        this.listener = listener;
        initViews();
    }

    private void initViews() {
        parent = LayoutInflater.from(context).inflate(R.layout.basic_bottom_popup, null);
        setContentView(parent);
        rv_options = parent.findViewById(R.id.rv_options);
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

        rv_options.setLayoutManager(new LinearLayoutManager(context));
        adapter = new CommonAdapter<String>(context, R.layout.basic_item_bottom_popup, options) {
            @Override
            protected void convert(ViewHolder holder, String s, int position) {
                final View container = holder.getView(R.id.ll_container);
                holder.setText(R.id.tv_options, s);
                if (tips != null && !TextUtils.isEmpty(tips.get(position))) {
                    holder.setVisible(R.id.tv_tips, true);
                    holder.setText(R.id.tv_tips, tips.get(position));
                } else {
                    holder.setVisible(R.id.tv_tips, false);
                }
                container.post(new Runnable() {
                    @Override
                    public void run() {
                        GradientDrawable background = new GradientDrawable();
                        background.setColor(ContextCompat.getColor(context, R.color.basic_color_bg));
                        background.setCornerRadius(container.getMeasuredHeight() / 2.0f);
                        container.setBackground(background);
                    }
                });
            }
        };
        adapter.setOnItemClickListener(new MultiItemTypeAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, RecyclerView.ViewHolder holder, int position) {
                if (listener != null) {
                    listener.onItemClick(view, BottomPopupWindow.this, position);
                }
            }

            @Override
            public boolean onItemLongClick(View view, RecyclerView.ViewHolder holder, int position) {
                return false;
            }
        });
        rv_options.setAdapter(adapter);
    }

    public void notifyItemInserted(int position) {
        adapter.notifyItemInserted(position);
    }

    public int optionSize() {
        return options == null ? 0 : options.size();
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
