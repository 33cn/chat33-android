package com.fzm.chat33.widget;

import android.app.Dialog;
import android.content.Context;
import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.fuzamei.common.utils.InstallUtil;
import com.fzm.chat33.R;

import java.io.File;

/**
 * @author zhengjy
 * @since 2018/08/06
 * Description:App更新Dialog
 */
public class UpdateDialog extends Dialog {

    private TextView tv_update_tip;
    private TextView tv_update_percent;
    private ProgressBar progressBar;
    private LinearLayout ll_user_option;
    private LinearLayout ll_update_progress;
    private TextView confirm;

    private String updateTips;
    private boolean first = true;

    public UpdateDialog(@NonNull Context context) {
        super(context);
    }

    public UpdateDialog(@NonNull Context context, int themeResId) {
        super(context, themeResId);
    }

    protected UpdateDialog(@NonNull Context context, boolean cancelable, @Nullable OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
    }

    public void performConfirm() {
        confirm.performClick();
    }

    public void setProgress(int progress) {
        if (progressBar != null) {
            if (ll_update_progress.getVisibility() == View.GONE) {
                ll_update_progress.setVisibility(View.VISIBLE);
            }
            if (ll_user_option.getVisibility() == View.VISIBLE) {
                ll_user_option.setVisibility(View.GONE);
            }
            if (progress != 0 && first) {
                // 首次进入则修改下载提示
                tv_update_tip.setText(updateTips);
                first = false;
            }
            progressBar.setProgress(progress);
            tv_update_percent.setText(progress + "%");
        }
    }

    public void reset() {
        if (ll_update_progress.getVisibility() == View.VISIBLE) {
            ll_update_progress.setVisibility(View.GONE);
        }
        if (ll_user_option.getVisibility() == View.GONE) {
            ll_user_option.setVisibility(View.VISIBLE);
        }
        first = true;
        tv_update_tip.setText(R.string.chat_update_download_begin);
        progressBar.setProgress(0);
        tv_update_percent.setText("0%");
    }

    /**
     * 下载完成后，按钮切换为安装
     *
     * @param context
     * @param file
     */
    public void complete(Context context, File file) {
        if (ll_update_progress.getVisibility() == View.VISIBLE) {
            ll_update_progress.setVisibility(View.GONE);
        }
        if (ll_user_option.getVisibility() == View.GONE) {
            ll_user_option.setVisibility(View.VISIBLE);
        }
        confirm.setText(R.string.chat_update_install);
        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                InstallUtil.install(context, file);
            }
        });
    }

    @Override
    public void onBackPressed() {

    }

    public static class Builder {

        private UpdateDialog dialog;
        private Window window;
        private Context mContext;

        private TextView tv_update_tip;
        private TextView tv_update_percent;
        private ProgressBar progressBar;
        private LinearLayout ll_user_option;
        private LinearLayout ll_update_progress;

        private String updateTips;

        public Builder(Context context) {
            dialog = new UpdateDialog(context);
            this.mContext = context;
            window = dialog.getWindow();
            window.setBackgroundDrawableResource(R.color.chat_transparent);
            WindowManager.LayoutParams lp = window.getAttributes();
            lp.gravity = Gravity.CENTER;
            lp.width = WindowManager.LayoutParams.MATCH_PARENT;
            lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
            dialog.getWindow().setAttributes(lp);
        }

        public Builder setContentView(@LayoutRes int layoutResID) {
            View container = LayoutInflater.from(mContext).inflate(layoutResID, null);
            container.setBackgroundResource(R.drawable.bg_dialog);
            window.setContentView(container);
            updateTips = mContext.getString(R.string.chat_update_downloading);
            tv_update_tip = (TextView) window.findViewById(R.id.tv_update_tip);
            tv_update_percent = (TextView) window.findViewById(R.id.tv_update_percent);
            progressBar = (ProgressBar) window.findViewById(R.id.pb_update_progress);
            ll_update_progress = (LinearLayout) window.findViewById(R.id.ll_update_progress);
            ll_user_option = (LinearLayout) window.findViewById(R.id.ll_user_option);
            return this;
        }

        public Builder setOnDismissListener(OnDismissListener listener) {
            dialog.setOnDismissListener(listener);
            return this;
        }

        public Builder setLeftButton(boolean visible, View.OnClickListener listener) {
            TextView tv = (TextView) window.findViewById(R.id.tv_dlg_left);
            if (visible) {
                tv.setVisibility(View.VISIBLE);
                tv.setOnClickListener(listener);
            } else {
                tv.setVisibility(View.GONE);
            }
            return this;
        }

        public Builder setRightButton(String str, View.OnClickListener listener) {
            TextView tv = (TextView) window.findViewById(R.id.tv_dlg_right);
            tv.setText(str);
            tv.setOnClickListener(listener);
            dialog.confirm = tv;
            return this;
        }

        public Builder setVersion(String version) {
            TextView tv_apk_version = (TextView) window.findViewById(R.id.tv_apk_version);
            tv_apk_version.setText(version);
            return this;
        }

        public Builder setFileSize(String size) {
            TextView tv_apk_size = (TextView) window.findViewById(R.id.tv_apk_size);
            tv_apk_size.setText(size);
            return this;
        }

        public Builder setMessage(String msg) {
            final TextView tv_dlg_detail = (TextView) window.findViewById(R.id.tv_dlg_detail_main);
            tv_dlg_detail.setText(msg);
            return this;
        }

        public Builder setCancelable(boolean flag) {
            dialog.setCancelable(flag);
            dialog.setCanceledOnTouchOutside(flag);
            return this;
        }

        public UpdateDialog show() {
            dialog.tv_update_tip = this.tv_update_tip;
            dialog.progressBar = this.progressBar;
            dialog.ll_update_progress = this.ll_update_progress;
            dialog.ll_user_option = this.ll_user_option;
            dialog.updateTips = this.updateTips;
            dialog.tv_update_percent = this.tv_update_percent;
            dialog.show();
            return dialog;
        }
    }
}
