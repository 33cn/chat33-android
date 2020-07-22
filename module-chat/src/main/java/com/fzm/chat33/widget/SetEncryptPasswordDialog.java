package com.fzm.chat33.widget;

import android.app.Dialog;
import android.content.Context;
import androidx.annotation.NonNull;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.fuzamei.common.utils.KeyboardUtils;
import com.fuzamei.common.utils.ShowUtils;
import com.fuzamei.common.widget.LoadingDialog;
import com.fzm.chat33.R;
import com.fzm.chat33.core.manager.CipherManager;
import com.fzm.chat33.utils.StringUtils;

import io.reactivex.android.schedulers.AndroidSchedulers;

/**
 * @author zhengjy
 * @since 2019/06/25
 * Description:设置助记词密码Dialog
 */
public class SetEncryptPasswordDialog extends Dialog {

    private Context mContext;
    private LoadingDialog mLoading = null;
    private boolean showWords = false;

    private TextView et_password;

    public SetEncryptPasswordDialog(@NonNull Context context, boolean show) {
        super(context);
        init(context, show);
    }

    public SetEncryptPasswordDialog(@NonNull Context context, int themeResId, boolean show) {
        super(context, themeResId);
        init(context, show);
    }

    private void init(Context context, boolean show) {
        this.mContext = context;
        this.showWords = show;
        Window window = getWindow();
        window.setBackgroundDrawableResource(R.color.chat_transparent);
        WindowManager.LayoutParams lp = window.getAttributes();
        lp.gravity = Gravity.CENTER;
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        window.setAttributes(lp);

        View container = LayoutInflater.from(mContext).inflate(R.layout.dialog_set_encrypt_password, null);
        container.setBackgroundResource(R.drawable.bg_dialog);
        window.setContentView(container);
        et_password = container.findViewById(R.id.et_password);
        container.findViewById(R.id.confirm).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String password = et_password.getText().toString().trim();
                if (password.length() < 8 || password.length() > 16) {
                    ShowUtils.showToastNormal(mContext, mContext.getString(R.string.chat_tips_update_encrypt_pwd3));
                    return;
                } else if (!StringUtils.isEncryptPassword(password)) {
                    ShowUtils.showToastNormal(mContext, mContext.getString(R.string.chat_tips_update_encrypt_pwd4));
                    return;
                }
                showLoading();
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        String words = CipherManager.getMnemonicString();
                        if (words != null) {
                            CipherManager.saveMnemonicString(words, password);
                        }
                        AndroidSchedulers.mainThread().createWorker().schedule(new Runnable() {
                            @Override
                            public void run() {
                                hideLoading();
                                String data = words;
                                if (data == null) {
                                    ShowUtils.showToastNormal(mContext, R.string.chat_error_encrypt_password);
                                } else {
                                    String temp = data.replace(" ", "");
                                    if (!temp.matches("[a-zA-Z]+")) {
                                        String[] word = data.split(" ");
                                        if (word.length > 0) {
                                            StringBuilder sb = new StringBuilder();
                                            for (int i = 0; i < word.length; i++) {
                                                sb.append(word[i]);
                                                if ((i + 1) % 3 == 0 && i != word.length - 1) {
                                                    sb.append(" ");
                                                }
                                            }
                                            data = sb.toString();
                                        }
                                    }
                                    dismiss();
                                    if (showWords) {
                                        new ExportWordsDialog(mContext, data).show();
                                    } else {
                                        ShowUtils.showToastNormal(mContext, R.string.chat_tips_group_info18);
                                    }
                                }
                            }
                        });
                    }
                }).start();
            }
        });
        container.findViewById(R.id.iv_close).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
    }

    @Override
    public void show() {
        super.show();
        et_password.postDelayed(new Runnable() {
            @Override
            public void run() {
                KeyboardUtils.showKeyboard(et_password);
            }
        }, 200);
    }

    private void showLoading() {
        if (mLoading == null) {
            mLoading = new LoadingDialog(mContext, true);
        }
        mLoading.show();
    }

    private void hideLoading() {
        if (mLoading != null) {
            if (mLoading.isShowing()) {
                mLoading.cancel();
            }
        }
    }
}
