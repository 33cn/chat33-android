package com.fzm.chat33.utils;

import android.app.Dialog;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import androidx.annotation.NonNull;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;

import com.fzm.chat33.R;
import com.qmuiteam.qmui.layout.QMUILinearLayout;

/**
 * @author zhengjy
 * @since 2019/03/21
 * Description:
 */
public class TipsDialogUtil {

    private static Handler handler = new Handler(Looper.getMainLooper());
    public static final long LENGTH_SHORT = 1500L;
    public static final long LENGTH_LONG = 3000L;

    public static void showSuccessTips(Context context, String tips, long length) {
        showTips(context, tips, R.mipmap.icon_tips_success, length, null);
    }

    public static void showFailTips(Context context, String tips, long length) {
        showTips(context, tips, R.mipmap.icon_tips_fail, length, null);
    }

    public static void showSuccessTips(Context context, String tips, long length, Dialog.OnDismissListener listener) {
        showTips(context, tips, R.mipmap.icon_tips_success, length, listener);
    }

    public static void showFailTips(Context context, String tips, long length, Dialog.OnDismissListener listener) {
        showTips(context, tips, R.mipmap.icon_tips_fail, length, listener);
    }

    public static void showTips(Context context, String tips, int icon, long length, Dialog.OnDismissListener listener) {
        TipsDialog dialog = new TipsDialog(context);
        dialog.setTipsIcon(icon);
        dialog.setTipsWords(tips);
        if (listener != null) {
            dialog.setOnDismissListener(listener);
        }
        dialog.show();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                dialog.dismiss();
            }
        }, length);
    }

    static class TipsDialog extends Dialog {

        Window window;

        ImageView iv_tips;
        TextView tips_words;
        QMUILinearLayout ll_container;

        TipsDialog(@NonNull Context context) {
            super(context);
            init();
        }

        void init() {
            window = getWindow();
            window.setContentView(R.layout.layout_tips_dialog);
            setCanceledOnTouchOutside(false);
            ll_container = window.findViewById(R.id.ll_container);
            ll_container.setRadius(5);
            ll_container.setShadowAlpha(0.25f);
            ll_container.setShadowElevation(10);
            iv_tips = window.findViewById(R.id.iv_tips);
            tips_words = window.findViewById(R.id.tips_words);
        }

        void setTipsIcon(int icon) {
            iv_tips.setImageResource(icon);
        }

        void setTipsWords(String tips) {
            tips_words.setText(tips);
        }
    }
}
