package com.fzm.chat33.widget;

import android.app.Dialog;
import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.alibaba.android.arouter.launcher.ARouter;
import com.fuzamei.common.utils.KeyboardUtils;
import com.fuzamei.componentservice.app.AppRoute;
import com.fzm.chat33.R;
import com.fzm.chat33.main.activity.PayPasswordActivity;

/**
 * @author zhengjy
 * @since 2019/03/13
 * Description:支付密码输入对话框
 */
public class PayPasswordDialog extends Dialog {

    private Window window;

    private ChatCodeView pay_password;
    private View ic_close, forget_password;
    private TextView pay_amount;

    public PayPasswordDialog(@NonNull Context context) {
        super(context);
        init();
    }

    public PayPasswordDialog(@NonNull Context context, int themeResId) {
        super(context, themeResId);
        init();
    }

    protected PayPasswordDialog(@NonNull Context context, boolean cancelable, @Nullable OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
        init();
    }

    private void init() {
        window = getWindow();
        window.setBackgroundDrawableResource(R.color.chat_transparent);
        window.setContentView(R.layout.layout_dialog_pay_password);
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        setCanceledOnTouchOutside(false);
        ic_close = window.findViewById(R.id.ic_close);
        pay_amount = window.findViewById(R.id.pay_amount);
        forget_password = window.findViewById(R.id.forget_password);
        pay_password = window.findViewById(R.id.pay_password);
        ic_close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
        forget_password.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ARouter.getInstance().build(AppRoute.PAY_PASSWORD)
                        .withInt("mode", PayPasswordActivity.UPDATE_PASSWORD_WITH_CODE)
                        .navigation();
            }
        });
    }

    @Override
    public void show() {
        super.show();
        pay_password.postDelayed(() -> KeyboardUtils.showKeyboard(pay_password.focus()), 100);
    }

    public void clear() {
        pay_password.clear();
    }

    public static class Builder {

        private PayPasswordDialog dialog;

        public Builder(Context context) {
            dialog = new PayPasswordDialog(context);
        }

        public Builder setAmount(String amount) {
            dialog.pay_amount.setText(amount);
            return this;
        }

        public Builder setOnDismissListener(OnDismissListener listener) {
            dialog.setOnDismissListener(listener);
            return this;
        }

        public Builder setOnCodeCompleteListener(final ChatCodeView.OnCodeCompleteListener listener) {
            dialog.pay_password.setOnCodeCompleteListener(listener);
            return this;
        }

        public PayPasswordDialog show() {
            dialog.show();
            return dialog;
        }
    }
}
