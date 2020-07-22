package com.fzm.chat33.widget;

import android.app.Dialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
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
 * @since 2019/05/24
 * Description:验证助记词密码Dialog
 */
public class ExportWordsDialog extends Dialog {

    private Context mContext;
    private String mWords = "";

    private TextView tv_title;
    private TextView tv_words;

    public ExportWordsDialog(@NonNull Context context, String words) {
        super(context);
        this.mWords = words;
        init(context);
    }

    public ExportWordsDialog(@NonNull Context context, int themeResId, String words) {
        super(context, themeResId);
        this.mWords = words;
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

        View container = LayoutInflater.from(mContext).inflate(R.layout.dialog_export_words, null);
        container.setBackgroundResource(R.drawable.bg_dialog);
        window.setContentView(container);
        tv_title = container.findViewById(R.id.tv_title);
        tv_words = container.findViewById(R.id.tv_words);
        tv_words.setText(mWords);
        container.findViewById(R.id.confirm).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ClipboardManager cm = (ClipboardManager) mContext.getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData mClipData = ClipData.newPlainText("Label", mWords);
                if (cm != null) {
                    cm.setPrimaryClip(mClipData);
                    ShowUtils.showToastNormal(mContext, R.string.chat_tips_chat_operate4);
                }
            }
        });
        container.findViewById(R.id.iv_close).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
    }
}
