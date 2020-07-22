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
 * @since 2018/12/07
 * Description:首次登陆注册提示设置昵称
 */
public class EditNameDialog extends Dialog {

    public EditNameDialog(@NonNull Context context) {
        super(context);
    }

    public EditNameDialog(@NonNull Context context, int themeResId) {
        super(context, themeResId);
    }

    protected EditNameDialog(@NonNull Context context, boolean cancelable, @Nullable OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
    }

    public static class Builder {

        private EditNameDialog dialog;
        private Window window;

        private TextView tv_name_length;
        private EditText et_content;
        private View tv_left, tv_right;

        public Builder(Context context) {
            dialog = new EditNameDialog(context);
            window = dialog.getWindow();
            window.setBackgroundDrawableResource(R.color.chat_transparent);
            window.setContentView(R.layout.layout_dialog_edit_name);
            tv_name_length = window.findViewById(R.id.tv_name_length);
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

        public Builder setOnDismissListener(OnDismissListener listener) {
            dialog.setOnDismissListener(listener);
            return this;
        }

        public Builder setRightButton(final OnSubmitNameListener listener) {
            tv_right.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null) {
                        listener.onSubmitName(dialog, et_content.getText().toString().trim());
                    }
                }
            });
            return this;
        }

        public EditNameDialog show() {
            dialog.show();
            return dialog;
        }
    }

    public interface OnSubmitNameListener {

        void onSubmitName(Dialog dialog, String name);
    }
}
