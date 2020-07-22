package com.fzm.chat33.widget;

import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import androidx.annotation.NonNull;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.fuzamei.common.utils.ShowUtils;
import com.fzm.chat33.R;

/**
 * @author zhengjy
 * @since 2019/05/16
 * Description:微信分享跳转提示Dialog
 */
public class WeChatShareDialog extends Dialog {

    private Context mContext;

    private TextView tv_title;
    private TextView tv_content;

    public WeChatShareDialog(@NonNull Context context) {
        super(context);
        init(context);
    }

    public WeChatShareDialog(@NonNull Context context, int themeResId) {
        super(context, themeResId);
        init(context);
    }

    private void init(Context context) {
        this.mContext = context;
        Window window = getWindow();
        window.setBackgroundDrawableResource(R.color.chat_transparent);
        WindowManager.LayoutParams lp = window.getAttributes();
        lp.gravity = Gravity.CENTER;
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        window.setAttributes(lp);

        View container = LayoutInflater.from(mContext).inflate(R.layout.dialog_wechat_share, null);
        container.setBackgroundResource(R.drawable.bg_dialog);
        window.setContentView(container);
        tv_title = container.findViewById(R.id.tv_title);
        tv_content = container.findViewById(R.id.tv_content);
        container.findViewById(R.id.confirm).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                gotoWeChat(mContext);
                dismiss();
            }
        });
        container.findViewById(R.id.iv_close).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
    }

    private static void gotoWeChat(Context context) {
        try {
            Intent intent = new Intent(Intent.ACTION_MAIN);
            ComponentName cmp = new ComponentName("com.tencent.mm","com.tencent.mm.ui.LauncherUI");
            intent.addCategory(Intent.CATEGORY_LAUNCHER);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.setComponent(cmp);
            context.startActivity(intent);
        } catch (ActivityNotFoundException e) {
            ShowUtils.showToastNormal(context, R.string.chat_error_open_weixin);
        }
    }
}
