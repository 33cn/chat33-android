package com.fzm.chat33.utils;

import android.os.CountDownTimer;
import android.widget.TextView;

import com.fzm.chat33.R;
import com.fzm.chat33.core.Chat33;

import java.lang.ref.WeakReference;

/**
 * @author zhengjy
 * @since 2019/03/13
 * Description:
 */
public class CodeTimer extends CountDownTimer {

    private WeakReference<TextView> tv_code;

    public CodeTimer(TextView textView) {
        super(60_000L, 1_000L);
        tv_code = new WeakReference<>(textView);
    }

    public CodeTimer(long millisInFuture, long countDownInterval) {
        super(millisInFuture, countDownInterval);
    }

    @Override
    public void onTick(long millisUntilFinished) {
        if (tv_code.get() != null) {
            tv_code.get().setClickable(false);
            tv_code.get().setText(Chat33.getContext().getString(R.string.chat_send_code_timer, millisUntilFinished / 1000));
        }
    }

    @Override
    public void onFinish() {
        if (tv_code.get() != null) {
            tv_code.get().setText(R.string.chat_send_code);
            tv_code.get().setClickable(true);
        }
    }
}