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

import io.reactivex.android.schedulers.AndroidSchedulers;

/**
 * @author zhengjy
 * @since 2019/05/24
 * Description:验证助记词密码Dialog
 */
public class VerifyEncryptPasswordDialog extends Dialog {

    private Context mContext;
    private LoadingDialog mLoading = null;

    private TextView et_password;

    public VerifyEncryptPasswordDialog(@NonNull Context context) {
        super(context);
        init(context);
    }

    public VerifyEncryptPasswordDialog(@NonNull Context context, int themeResId) {
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

        View container = LayoutInflater.from(mContext).inflate(R.layout.dialog_verify_encrypt_password, null);
        container.setBackgroundResource(R.drawable.bg_dialog);
        window.setContentView(container);
        et_password = container.findViewById(R.id.et_password);
        container.findViewById(R.id.confirm).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String password = et_password.getText().toString().trim();
                if (password.length() < 8 || password.length() > 16) {
                    ShowUtils.showToastNormal(mContext, R.string.chat_error_encrypt_password_length);
                    return;
                }
                showLoading();
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        String words = CipherManager.getMnemonicString(password);
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
                                    new ExportWordsDialog(mContext, data).show();
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
