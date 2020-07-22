package com.fzm.chat33.widget.chatrow;

import android.os.CountDownTimer;
import android.widget.TextView;

/**
 * @author zhengjy
 * @since 2018/12/13
 * Description:阅后即焚消息计时器
 */
public class SnapChatCountDown extends CountDownTimer {

    private OnFinishListener listener;
    private TextView remainTime;
    private Object object;
    private String currentText = "";

    /**
     * @param millisInFuture    The number of millis in the future from the call
     *                          to {@link #start()} until the countdown is done and {@link #onFinish()}
     *                          is called.
     * @param countDownInterval The interval along the way to receive
     *                          {@link #onTick(long)} callbacks.
     */
    public SnapChatCountDown(long millisInFuture, long countDownInterval, TextView remainTime, Object object, OnFinishListener listener) {
        super(millisInFuture, countDownInterval);
        this.remainTime = remainTime;
        this.listener = listener;
        this.object = object;
    }

    /**
     * 设置计时器当前所影响的TextView
     *
     * @param remainTime
     */
    public void setCountView(TextView remainTime) {
        this.remainTime = remainTime;
    }

    /**
     * 当timer复用的时候remainTime上可能会显示1s之前的时间，直到onTick()执行，需要用这个方法手动更新界面
     *
     * @return  剩余倒计时时间
     */
    public String getCurrentText() {
        return currentText;
    }

    @Override
    public void onTick(long millisUntilFinished) {
        currentText = com.fzm.chat33.utils.StringUtils.formateTime(millisUntilFinished);
        if (remainTime != null && remainTime.getTag() == object) {
            remainTime.setText(currentText);
        }
    }

    @Override
    public void onFinish() {
        currentText = "";
        if (listener != null) {
            listener.onFinish(object);
        }
    }

    interface OnFinishListener {
        void onFinish(Object object);
    }
}
