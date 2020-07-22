package com.fzm.chat33.widget;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.CountDownTimer;
import android.text.Html;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.alibaba.android.arouter.launcher.ARouter;
import com.fuzamei.common.net.subscribers.Cancelable;
import com.fuzamei.common.utils.KeyboardUtils;
import com.fuzamei.common.utils.ShowUtils;
import com.fuzamei.common.utils.ToolUtils;
import com.fuzamei.componentservice.app.AppRoute;
import com.fuzamei.componentservice.config.AppConfig;
import com.fzm.chat33.R;
import com.fzm.chat33.core.Chat33;
import com.fzm.chat33.core.bean.DepositSMS;
import com.fzm.chat33.main.activity.PayPasswordActivity;
import com.fzm.chat33.main.mvvm.PayPasswordViewModel;
import com.fzm.chat33.utils.CodeTimer;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

/**
 * @author zhengjy
 * @since 2019/03/13
 * Description:验证码输入弹窗
 */
public class VerifyCodeDialog extends Dialog {

    private final int SEND_SMS = 1;
    private final int SUGGEST_VOICE = 2;
    private final int SEND_VOICE = 3;

    private Fragment fragment;
    private Context context;

    private ChatCodeView verify_code;
    private TextView code_tips, send_code, bottom_tips;
    private View ic_close;

    private CodeTimer timer;
    private String phone = "";
    private String realPhone = "";
    private String codeBizId = "";
    private String voiceBizId = "";
    private String ticket = "";
    private String mCode = "";
    private SpannableString spStr = new SpannableString(Chat33.getContext().getString(R.string.chat_error_receive_message));

    private String sendType = DepositSMS.SEND_SMS;
    private String bizIdType = DepositSMS.SEND_SMS;

    private Map<String, Cancelable> requestMap = new HashMap<>();
    private OnVerifyCodeCompleteListener onVerifyCodeCompleteListener;
    private LifecycleOwner lifecycleOwner;
    private PayPasswordViewModel viewModel;

    private VerifyCodeDialog(@NonNull Context context, Fragment fragment) {
        super(context);
        this.lifecycleOwner = (LifecycleOwner) context;
        viewModel = ViewModelProviders.of((FragmentActivity) context).get(PayPasswordViewModel.class);
        this.context = context;
        this.fragment = fragment;
        init();
    }

    private void init() {
        Window window = getWindow();
        window.setBackgroundDrawableResource(R.color.chat_transparent);
        window.setContentView(R.layout.layout_dialog_verify_code);
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        setCanceledOnTouchOutside(false);
        ic_close = window.findViewById(R.id.ic_close);
        verify_code = window.findViewById(R.id.verify_code);
        send_code = window.findViewById(R.id.send_code);
        code_tips = window.findViewById(R.id.code_tips);
        bottom_tips = window.findViewById(R.id.bottom_tips);
        bottom_tips.setHighlightColor(ContextCompat.getColor(context, R.color.chat_transparent));

        ClickableSpan clickableSpan = new ClickableSpan() {
            @Override
            public void updateDrawState(@NonNull TextPaint ds) {
                ds.setColor(ContextCompat.getColor(context, R.color.chat_color_accent));
                ds.setUnderlineText(false);
            }

            @Override
            public void onClick(@NonNull View widget) {
                sendVoiceCode("", "");
            }
        };
        spStr.setSpan(clickableSpan, spStr.length() - 4, spStr.length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
        setObserver();
    }

    private void setObserver() {
        viewModel.getDepositSMS().observe(lifecycleOwner, new Observer<DepositSMS>() {
            @Override
            public void onChanged(@Nullable DepositSMS depositSMS) {
                String jsUrl = depositSMS.getData().getJsUrl();
                if (!TextUtils.isEmpty(jsUrl)) {
                    bizIdType = DepositSMS.SEND_SMS;
                    codeBizId = depositSMS.getData().getBusinessId();
                    ARouter.getInstance().build(AppRoute.PIC_VERIFY)
                            .withString("jsurl", jsUrl)
                            .navigation((Activity) context, PayPasswordActivity.REQUEST_CODE_VERIFY);
                } else {
                    switchState(SEND_SMS);
                    sendType = DepositSMS.SEND_SMS;
                    ShowUtils.showToastNormal(context, R.string.chat_send_code_success);
                    timer = new CodeTimer(send_code) {
                        @Override
                        public void onFinish() {
                            super.onFinish();
                            switchState(SUGGEST_VOICE);
                        }
                    };
                    timer.start();
                }
            }
        });
        viewModel.getDepositVoiceSMS().observe(lifecycleOwner, new Observer<DepositSMS>() {
            @Override
            public void onChanged(@Nullable DepositSMS depositSMS) {
                String jsUrl = depositSMS.getData().getJsUrl();
                if (!TextUtils.isEmpty(jsUrl)) {
                    bizIdType = DepositSMS.SEND_VOICE;
                    voiceBizId = depositSMS.getData().getBusinessId();
                    ARouter.getInstance().build(AppRoute.PIC_VERIFY)
                            .withString("jsurl", jsUrl)
                            .navigation((Activity) context, PayPasswordActivity.REQUEST_CODE_VERIFY);
                } else {
                    switchState(SEND_VOICE);
                    sendType = DepositSMS.SEND_VOICE;
                    ShowUtils.showToastNormal(context, R.string.chat_send_code_success);
                    timer = new CodeTimer(send_code) {
                        @Override
                        public void onFinish() {
                            super.onFinish();
                            switchState(SUGGEST_VOICE);
                        }
                    };
                    timer.start();
                }
            }
        });
        viewModel.getVerifyCode().observe(lifecycleOwner, new Observer<Object>() {
            @Override
            public void onChanged(@Nullable Object o) {
                if (onVerifyCodeCompleteListener != null) {
                    onVerifyCodeCompleteListener.onVerifyCodeComplete(verify_code, sendType, mCode);
                }
                dismiss();
            }
        });
    }

    public CountDownTimer getTimer() {
        return timer;
    }

    @Override
    public void show() {
        code_tips.setText(Html.fromHtml(context.getString(R.string.chat_send_code_to, AppConfig.APP_ACCENT_COLOR_STR, ToolUtils.encryptPhoneNumber(phone))));
        super.show();
        verify_code.postDelayed(() -> KeyboardUtils.showKeyboard(verify_code.focus()), 100);
    }

    @Override
    public void onBackPressed() {

    }

    public static class Builder {

        private VerifyCodeDialog mDialog;

        public Builder(Context context, Fragment fragment) {
            mDialog = new VerifyCodeDialog(context, fragment);
        }

        public Builder setPhone(String phone) {
            mDialog.phone = phone;
            mDialog.realPhone = phone.substring(2);
            return this;
        }

        public Builder setOnDismissListener(OnDismissListener listener) {
            mDialog.setOnDismissListener(new OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialog) {
                    for (Map.Entry<String, Cancelable> entry : mDialog.requestMap.entrySet()) {
                        if (entry.getValue() != null) {
                            entry.getValue().cancel();
                        }
                    }
                    mDialog.requestMap.clear();
                    mDialog.getTimer().cancel();
                    if (listener != null) {
                        listener.onDismiss(dialog);
                    }
                }
            });
            return this;
        }

        public Builder setOnDialogClickListener(OnDialogClickListener listener) {
            mDialog.setOnDialogClickListener(listener);
            return this;
        }

        public Builder setOnCodeCompleteListener(final OnVerifyCodeCompleteListener listener) {
            mDialog.setOnCodeCompleteListener(listener);
            return this;
        }

        public VerifyCodeDialog show() {
            mDialog.show();
            return mDialog;
        }
    }

    private void switchState(int state) {
        switch (state) {
            case SEND_SMS:
                bottom_tips.setText(R.string.chat_send_code_success_tip);
                bottom_tips.setTextColor(ContextCompat.getColor(context, R.color.chat_text_grey_light));
                bottom_tips.setVisibility(View.VISIBLE);
                break;
            case SUGGEST_VOICE:
                bottom_tips.setText(spStr);
                bottom_tips.setTextColor(ContextCompat.getColor(context, R.color.chat_text_grey_dark));
                bottom_tips.setMovementMethod(LinkMovementMethod.getInstance());
                bottom_tips.setVisibility(View.VISIBLE);
                break;
            case SEND_VOICE:
                bottom_tips.setText(R.string.chat_warn_receive_radio_phone);
                bottom_tips.setTextColor(ContextCompat.getColor(context, R.color.chat_text_grey_dark));
                bottom_tips.setVisibility(View.VISIBLE);
                break;
        }
    }

    public void sendCode(String bizId, String ticket) {
        if (DepositSMS.SEND_SMS.equals(bizIdType)) {
            sendSMS(bizId, ticket);
        } else if (DepositSMS.SEND_VOICE.equals(bizIdType)) {
            sendVoiceCode(bizId, ticket);
        }
    }

    private void sendSMS(String bizId, String ticket) {
        if (bizId == null) {
            bizId = codeBizId;
        }
        viewModel.sendSMS4(realPhone, bizId, ticket);
    }

    private void sendVoiceCode(String bizId, String ticket) {
        if (bizId == null) {
            bizId = voiceBizId;
        }
        viewModel.sendVoiceCode(realPhone, bizId, ticket);
    }

    public void verifyCode(String code) {
        mCode = code;
        viewModel.verifyCode(realPhone, sendType, code);
    }

    public void setOnDialogClickListener(OnDialogClickListener listener) {
        ic_close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
                if (listener != null) {
                    listener.onClose(ic_close);
                }
            }
        });
        send_code.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendSMS("", "");
                if (listener != null) {
                    listener.onSendCode(send_code);
                }
            }
        });
    }

    public void setOnCodeCompleteListener(final OnVerifyCodeCompleteListener listener) {
        this.onVerifyCodeCompleteListener = listener;
        verify_code.setOnCodeCompleteListener(new ChatCodeView.OnCodeCompleteListener() {
            @Override
            public void onCodeComplete(@org.jetbrains.annotations.Nullable View view, @NotNull String code) {
                verifyCode(code);
            }
        });
    }

    public static abstract class OnDialogClickListener {
        public void onClose(View view) {
        }

        public void onSendCode(View view) {
        }

        public void onVoiceCode(View view) {
        }
    }

    public interface OnVerifyCodeCompleteListener {
        void onVerifyCodeComplete(View view, String type, String code);
    }
}
