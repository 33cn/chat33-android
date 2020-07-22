package com.fuzamei.componentservice.widget.dialog;

import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.Drawable;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;

import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.fuzamei.componentservice.R;


/**
 * @author Mark
 * @since 2018/9/6
 * Description:简易对话框，支持修改背景资源(但若修改背景圆角资源需要外部传入)、左右选择按钮(也可支持一个按钮)
 * 可简单设置内容与颜色，或者外部传入复杂布局。关于字体、颜色应尽量在 UI 设计层面统一，App 拥有统一风格
 */
public class EasyDialog implements DialogInterface {

    private Builder builder;
    Dialog dialog;
    private int dpWidth = 310;
    private Context mContext;

    public EasyDialog(Context context, Builder builder) {
        this.mContext = context;
        this.builder = builder;
        dialog = builder.P.dialog;
    }

    public static class Builder {

        EasyParams P;


        public Builder() {
            P = new EasyParams();
        }

        public Builder setLayoutBackgroundDrawable(Drawable layoutBackgroundDrawable) {
            P.mLayoutBackgroundDrawable = layoutBackgroundDrawable;
            return this;
        }

        public Builder setHeaderTitle(String headerTitle) {
            P.mHeaderTitle = headerTitle;
            return this;
        }

        public Builder setHeaderTitleColor(int headerTitleColor) {
            P.mHeaderTitleColor = headerTitleColor;
            return this;
        }

        public Builder setSubTitle(String subTitle) {
            P.mSubTitle = subTitle;
            return this;
        }

        public Builder setSubTitleColor(int subTitleColor) {
            P.mSubTitleColor = subTitleColor;
            return this;
        }

        public Builder setHeaderLeftImageView(int headerLeftImageView) {
            P.mHeaderLeftImageView = headerLeftImageView;
            return this;
        }

        public Builder setHeaderLeftClickListener(OnClickListener clickListener) {
            P.mHeaderLeftClickListener = clickListener;
            return this;
        }

        public Builder setHeaderRightImageView(int headerRightImageView) {
            P.mHeaderRightImageView = headerRightImageView;
            return this;
        }

        public Builder setHeaderRightClickListener(OnClickListener clickListener) {
            P.mHeaderRightClickListener = clickListener;
            return this;
        }

        public Builder setContent(CharSequence content) {
            P.mContent = content;
            return this;
        }

        public Builder setContentColor(int contentColor) {
            P.mContentColor = contentColor;
            return this;
        }

        public Builder setBottomLeftText(String bottomLeftText) {
            P.mBottomLeftText = bottomLeftText;
            return this;
        }

        public Builder setBottomLeftColor(int bottomLeftTextColor) {
            P.mBottomLeftColor = bottomLeftTextColor;
            return this;
        }

        public Builder setBottomLeftClickListener(OnClickListener clickListener) {
            P.mBottomLeftClickListener = clickListener;
            return this;
        }

        public Builder setBottomRightText(String bottomRightText) {
            P.mBottomRightText = bottomRightText;
            return this;
        }

        public Builder setBottomRightColor(int bottomRightTextColor) {
            P.mBottomRightColor = bottomRightTextColor;
            return this;
        }

        public Builder setBottomRightClickListener(OnClickListener clickListener) {
            P.mBottomRightClickListener = clickListener;
            return this;
        }


        public Builder setView(@NonNull View view) {
            P.contentView = view;
            return this;
        }

        public Builder setCancelable(boolean cancelable) {
            P.cancelable = cancelable;
            return this;
        }

        public boolean isCancelable() {
            return P.cancelable;
        }

        public EasyDialog create(Context context) {
            LinearLayout ll_container = (LinearLayout) LayoutInflater.from(context).inflate(R.layout.basic_common_dialog_layout, null);
            RelativeLayout rl_header = ll_container.findViewById(R.id.rl_header);
            TextView tv_title = ll_container.findViewById(R.id.tv_title);
            TextView tv_sub_title = ll_container.findViewById(R.id.tv_sub_title);
            ImageView iv_header_left = ll_container.findViewById(R.id.iv_header_left);
            ImageView iv_header_right = ll_container.findViewById(R.id.iv_header_right);
            FrameLayout fl_content = ll_container.findViewById(R.id.fl_content);
            TextView tv_content = ll_container.findViewById(R.id.tv_content);
            LinearLayout ll_bottom = ll_container.findViewById(R.id.ll_bottom);
            View view_bottom_divider = ll_container.findViewById(R.id.view_bottom_divider);
            TextView tv_left = ll_container.findViewById(R.id.tv_left);
            TextView tv_right = ll_container.findViewById(R.id.tv_right);
            View view_choice_divider = ll_container.findViewById(R.id.view_choice_divider);
            setLayoutBackgroundDrawable(ContextCompat.getDrawable(context, R.drawable.basic_shape_common_dialog_color));

            // 添加外部

            P.dialog = new AlertDialog.Builder(context).setView(ll_container).create();
            P.dialog.setCancelable(P.cancelable);
            Window window = P.dialog.getWindow();
            if (window != null) {
                window.setBackgroundDrawableResource(android.R.color.transparent);
            }

            // 背景圆角或者颜色资源由外部决定
            if (P.mLayoutBackgroundDrawable != null) {
                ll_container.setBackground(P.mLayoutBackgroundDrawable);
            }

            if (!TextUtils.isEmpty(P.mHeaderTitle)) {
                tv_title.setVisibility(View.VISIBLE);
                tv_title.setText(P.mHeaderTitle);
                if (P.mHeaderTitleColor != 0) {
                    tv_title.setTextColor(P.mHeaderTitleColor);
                }
            } else {
                rl_header.setVisibility(View.GONE);
            }
            if (!TextUtils.isEmpty(P.mSubTitle)) {
                tv_sub_title.setVisibility(View.VISIBLE);
                tv_sub_title.setText(P.mSubTitle);
                if (P.mSubTitleColor != 0) {
                    tv_sub_title.setTextColor(P.mSubTitleColor);
                }
            } else {
                tv_sub_title.setVisibility(View.GONE);
            }
            if (P.mHeaderLeftImageView != 0) {
                iv_header_left.setVisibility(View.VISIBLE);
                iv_header_left.setImageResource(P.mHeaderLeftImageView);
            } else {
                iv_header_left.setVisibility(View.GONE);
            }
            if (P.mHeaderRightImageView != 0) {
                iv_header_right.setVisibility(View.VISIBLE);
                iv_header_right.setImageResource(P.mHeaderRightImageView);
            } else {
                iv_header_right.setVisibility(View.GONE);
            }
            // 头部资源都不可见，就隐藏布局
            if (tv_title.getVisibility() == View.GONE && iv_header_left.getVisibility() == View.GONE
                    && iv_header_right.getVisibility() == View.GONE) {
                rl_header.setVisibility(View.GONE);
            }
            if (!TextUtils.isEmpty(P.mContent)) {
                tv_content.setText(P.mContent);
                if (P.mContentColor != 0) {
                    tv_content.setTextColor(P.mContentColor);
                }
            } else {
                tv_content.setVisibility(View.GONE);
            }
            if (P.contentView != null) {
                // tv_content 与 外部传入 view ，以外部传入 view 为主要内容
                tv_content.setVisibility(View.GONE);
                fl_content.addView(P.contentView);
            }

            final EasyDialog easyDialog = new EasyDialog(context, this);

            // 底部选择按钮，当只需要一个存在时，占据整个底部位置
            // 选择底部左、右任意一个都可。
            if (!TextUtils.isEmpty(P.mBottomLeftText)) {
                tv_left.setText(P.mBottomLeftText);
                if (P.mBottomLeftColor != 0) {
                    tv_left.setTextColor(P.mBottomLeftColor);
                }
                if (P.mBottomLeftClickListener != null) {
                    tv_left.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            P.mBottomLeftClickListener.onClick(easyDialog);
                        }
                    });
                } else {
                    tv_left.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            easyDialog.dismiss();
                        }
                    });
                }
            } else {
                tv_left.setVisibility(View.GONE);
            }

            if (!TextUtils.isEmpty(P.mBottomRightText)) {
                tv_right.setText(P.mBottomRightText);
                if (P.mBottomRightColor != 0) {
                    tv_right.setTextColor(P.mBottomRightColor);
                }
                if (P.mBottomRightClickListener != null) {
                    tv_right.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            P.mBottomRightClickListener.onClick(easyDialog);
                        }
                    });
                } else {
                    tv_right.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            easyDialog.dismiss();
                        }
                    });
                }
            } else {
                tv_right.setVisibility(View.GONE);
            }

            if (!(tv_left.getVisibility() == View.VISIBLE && tv_right.getVisibility() == View.VISIBLE)) {
                tv_left.setBackgroundResource(R.drawable.basic_sl_btn_lr);
                tv_right.setBackgroundResource(R.drawable.basic_sl_btn_lr);
            }

            if (iv_header_left.getVisibility() == View.VISIBLE && P.mHeaderLeftClickListener != null) {
                iv_header_left.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        P.mHeaderLeftClickListener.onClick(easyDialog);
                    }
                });
            }

            if (iv_header_right.getVisibility() == View.VISIBLE && P.mHeaderRightClickListener != null) {
                iv_header_right.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        P.mHeaderRightClickListener.onClick(easyDialog);
                    }
                });
            }

            // 分割线控制
            if (tv_left.getVisibility() == View.VISIBLE && tv_right.getVisibility() == View.VISIBLE) {
                view_choice_divider.setVisibility(View.VISIBLE);
            } else if (tv_left.getVisibility() == View.GONE && tv_right.getVisibility() == View.GONE) {
                ll_bottom.setVisibility(View.GONE);
                view_bottom_divider.setVisibility(View.GONE);
            } else {
                view_choice_divider.setVisibility(View.GONE);
            }

            return easyDialog;
        }

        public static class EasyParams {
            Drawable mLayoutBackgroundDrawable;
            String mHeaderTitle;
            int mHeaderTitleColor;
            String mSubTitle;
            int mSubTitleColor;
            int mHeaderLeftImageView;
            OnClickListener mHeaderLeftClickListener = null;
            int mHeaderRightImageView;
            OnClickListener mHeaderRightClickListener = null;
            CharSequence mContent;
            int mContentColor;
            String mBottomLeftText;
            int mBottomLeftColor;
            OnClickListener mBottomLeftClickListener = null;
            String mBottomRightText;
            int mBottomRightColor;
            OnClickListener mBottomRightClickListener = null;

            View contentView;

            Dialog dialog;
            boolean cancelable = true;
        }
    }

    public void show() {
        show(dp2px(mContext, dpWidth), WindowManager.LayoutParams.WRAP_CONTENT);
    }

    public void show(int width, int height) {
        if (dialog == null) {
            throw new RuntimeException("easy dialog can not be null");
        }

        dialog.show();
        Window window = dialog.getWindow();
        WindowManager.LayoutParams lp = window.getAttributes();
        lp.gravity = Gravity.CENTER;
        lp.width = width;//宽高可设置具体大小
        lp.height = height;
        dialog.getWindow().setAttributes(lp);

    }

    private int dp2px(Context context, float dpValue) {
        float density = context.getResources().getDisplayMetrics().density;
        int pxValue = (int) (dpValue * density + 0.5f);
        return pxValue;
    }

    @Override
    public void cancel() {
        if (dialog != null) {
            dialog.cancel();
        }
    }

    @Override
    public void dismiss() {
        if (dialog != null) {
            dialog.dismiss();
        }
    }

    public void setOnDismissListener(android.content.DialogInterface.OnDismissListener listener) {
        dialog.setOnDismissListener(listener);
    }
}


