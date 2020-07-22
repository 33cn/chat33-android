package com.fzm.chat33.widget;

import android.app.Dialog;
import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.TextView;

import com.fzm.chat33.R;
import com.fzm.chat33.utils.SimpleTextWatcher;

/**
 * @author zhengjy
 * @since 2018/12/25
 * Description:添加时回答验证问题的对话框
 */
public class AddVerifyDialog extends Dialog {

    public AddVerifyDialog(@NonNull Context context) {
        super(context);
    }

    public AddVerifyDialog(@NonNull Context context, int themeResId) {
        super(context, themeResId);
    }

    protected AddVerifyDialog(@NonNull Context context, boolean cancelable, @Nullable OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
    }

    public static class Builder {

        private AddVerifyDialog dialog;
        private Window window;

        private TextView tv_name_length, tv_content;
        private EditText et_content;
        private View tv_left, tv_right;

        public Builder(Context context) {
            dialog = new AddVerifyDialog(context);
            window = dialog.getWindow();
            window.setBackgroundDrawableResource(R.color.chat_transparent);
            window.setContentView(R.layout.layout_dialog_add_verify);
            tv_name_length = window.findViewById(R.id.tv_name_length);
            tv_content = window.findViewById(R.id.tv_content);
            et_content = window.findViewById(R.id.et_content);
            tv_left = window.findViewById(R.id.tv_left);
            tv_right = window.findViewById(R.id.tv_right);
            tv_left.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.dismiss();
                }
            });
            et_content.addTextChangedListener(new SimpleTextWatcher() {
                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if (TextUtils.isEmpty(s)) {
                        tv_name_length.setText("0/20");
                    } else {
                        tv_name_length.setText(s.length() + "/20");
                    }
                }
            });
        }

        public Builder setContent(String content) {
            tv_content.setText(content);
            return this;
        }

        public Builder setOnDismissListener(OnDismissListener listener) {
            dialog.setOnDismissListener(listener);
            return this;
        }

        public Builder setRightButton(final OnSubmitListener listener) {
            tv_right.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null) {
                        listener.onSubmit(dialog, et_content.getText().toString().trim());
                    }
                }
            });
            return this;
        }

        public AddVerifyDialog show() {
            dialog.show();
            return dialog;
        }
    }

    public interface OnSubmitListener {

        void onSubmit(Dialog dialog, String content);
    }
}
