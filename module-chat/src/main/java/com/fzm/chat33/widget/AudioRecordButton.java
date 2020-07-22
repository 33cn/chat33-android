package com.fzm.chat33.widget;

import android.app.Activity;
import android.app.Service;
import android.content.Context;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.fzm.chat33.R;
import com.fuzamei.common.utils.LogUtils;
import com.fuzamei.common.utils.ShowUtils;
import com.fuzamei.componentservice.config.AppConfig;
import com.fzm.chat33.record.AudioManager;
import com.fzm.chat33.record.DialogManager;

import java.lang.ref.WeakReference;


public class AudioRecordButton extends androidx.appcompat.widget.AppCompatTextView implements AudioManager.AudioStageListener {

    private static final int STATE_NORMAL = 1;
    private static final int STATE_RECORDING = 2;
    private static final int STATE_WANT_TO_CANCEL = 3;
    private static final int DISTANCE_Y_CANCEL = 100;
    private static final int MAX_RECORD_TIME = AppConfig.DEFAULT_MAX_RECORD_TIME;//最长录音时间
    private static final int MIN_RECORD_TIME = 1;//最短录音时间
    private static final int COUNTDOWN_SECONDS = 10;//倒计时时间

    // 准备三个常量
    private static final int MSG_AUDIO_PREPARED = 0X110;
    private static final int MSG_VOICE_CHANGE = 0X111;
    private static final int MSG_DIALOG_DISMISS = 0X112;


    private int mCurrentState = STATE_NORMAL;
    // 已经开始录音
    private boolean isRecording = false;
    private DialogManager mDialogManager;
    private AudioManager mAudioManager;
    private float mDuration = 0;
    // 是否触发了onlongclick，准备好了
    private boolean mReady;
    private int isSnap = 2;

    private Thread voiceLevelThread;
    private boolean isCountDown = false;//是否是倒计时
    private boolean isSwitch = false;//是否是切换状态

    private Handler mHandler;
    private Context context;


    public AudioRecordButton(Context context) {
        super(context);
        this.context = context;
        init();
    }

    public AudioRecordButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        init();
    }

    public void setSnap(int isSnap) {
        this.isSnap = isSnap;
    }


    private void init() {
        //xml可视化解析
        if (isInEditMode()) {
            return;
        }
        mHandler = new MyHandler(this);
        mDialogManager = new DialogManager(getContext());
        // 这里没有判断储存卡是否存在，有空要判断
        if (Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED)) {
            String dir = com.fzm.chat33.utils.FileUtils.getAudioCachePath(getContext());

            mAudioManager = AudioManager.getInstance(dir);
            mAudioManager.setmMaxRecordeTime(MAX_RECORD_TIME);

            setOnLongClickListener(new OnLongClickListener() {

                @Override
                public boolean onLongClick(View v) {
                    if (activity == null || !com.fzm.chat33.utils.FileUtils.isAudioGrant(requestPermissionCode, activity)) {
                        return false;
                    }
                    //调用手机震动器短震
                    Vibrator vibrator = (Vibrator) context.getSystemService(Service.VIBRATOR_SERVICE);
                    if (vibrator != null) {
                        vibrator.vibrate(50);
                    }

                    mReady = true;
                    mAudioManager.setOnAudioStageListener(AudioRecordButton.this);
                    mAudioManager.prepareAudio();
                    return false;
                }
            });
        } else {
            //未安装SD卡
            ShowUtils.showToast(context, context.getString(R.string.chat_tips_input_msg3));
        }
    }

    /**
     * 录音完成后的回调，回调给activiy，可以获得mDuration和文件的路径
     */
    public interface AudioFinishRecorderListener {
        void onFinished(float seconds, String filePath);
    }

    private AudioFinishRecorderListener mListener;

    public void setAudioFinishRecorderListener(AudioFinishRecorderListener listener) {
        mListener = listener;
    }


    /**
     * 设置最后剩余录音时间
     *
     * @param duration 录音时间
     */
    private void showLastSeconds(int duration) {

        if (mCurrentState != STATE_WANT_TO_CANCEL) {
            if (duration >= MAX_RECORD_TIME) {//达到最长录音时间
                isRecording = false;//停止录音
                mDialogManager.tooLong();
                mAudioManager.release();
                if (mListener != null) {// 并且callbackActivity，保存录音
                    mListener.onFinished(duration, mAudioManager
                            .getCurrentFilePath());
                }
                mHandler.sendEmptyMessageDelayed(MSG_DIALOG_DISMISS, 300);
                isCountDown = false;
                reset();
            } else {
//                isCountDown = false;
                mDialogManager.updateVoiceSecond(MAX_RECORD_TIME - duration - 1);
            }
        }
    }

    // 在这里面发送一个handler的消息
    @Override
    public void wellPrepared() {
        // TODO Auto-generated method stub
        mHandler.sendEmptyMessage(MSG_AUDIO_PREPARED);
    }


    private Activity activity;
    private int requestPermissionCode;

    public void setPermissionParams(Activity activity, int requestPermissionCode) {
        this.activity = activity;
        this.requestPermissionCode = requestPermissionCode;
    }


        /**
     * 直接复写这个监听函数
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // TODO Auto-generated method stub

        int action = event.getAction();
        int x = (int) event.getX();
        int y = (int) event.getY();
        LogUtils.d("action:" + action + "  x:" + x + "--y:" + y);

        switch (action) {
            case MotionEvent.ACTION_DOWN:
                changeState(STATE_RECORDING);
                break;
            case MotionEvent.ACTION_MOVE:

                if (isRecording) {
                    // 根据x，y来判断用户是否想要取消
                    if (wantToCancel(x, y)) {
                        changeState(STATE_WANT_TO_CANCEL);
                    } else {
                        changeState(STATE_RECORDING);
                    }

                }

                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                // 首先判断是否有触发onlongclick事件，没有的话直接返回reset
                if (!mReady) {
                    reset();
                    return super.onTouchEvent(event);
                }
                if (mCurrentState == STATE_WANT_TO_CANCEL) {
                    // cancel
                    mAudioManager.cancel();
                    mDialogManager.dismissDialog();
                } else if (!isRecording || mDuration < MIN_RECORD_TIME) {
                    // 如果按的时间太短，还没准备好或者时间录制太短，就离开了，则显示这个dialog
                    if (null != mAudioManager) {
                        mDialogManager.tooShort();
                        mAudioManager.cancel();
                        mHandler.sendEmptyMessageDelayed(MSG_DIALOG_DISMISS, 1300);//
                        // 持续1.3s
                    }
                } else if (mCurrentState == STATE_RECORDING && isRecording) {//正常录制结束

                    mDialogManager.dismissDialog();
                    mAudioManager.release();// release释放一个mediarecorder
                    if (mListener != null) {// 并且callbackActivity，保存录音
                        mListener.onFinished(mDuration, mAudioManager.getCurrentFilePath());
                    }

                }

                //performClick();
                reset();// 恢复标志位
                break;

        }

        return super.onTouchEvent(event);
    }

    /**
     * 恢复标志位以及状态
     */
    private void reset() {
        isRecording = false;
        changeState(STATE_NORMAL);
        mReady = false;
        mDuration = 0;
    }

    private boolean wantToCancel(int x, int y) {

        if (x < 0 || x > getWidth()) {// 判断是否在左边，右边，上边，下边
            return true;
        }
        if (y < -DISTANCE_Y_CANCEL || y > getHeight() + DISTANCE_Y_CANCEL) {
            return true;
        }

        return false;
    }

    private void changeState(int state) {

        if (mCurrentState != state) {
            mCurrentState = state;
            switch (mCurrentState) {
                case STATE_NORMAL:
                    if (isSnap == 1) {
                        setBackgroundResource(R.drawable.bg_chatroom_send_record_btn_snap);
                    } else {
                        setBackgroundResource(R.drawable.bg_chatroom_send_record_btn);
                    }
                    setText(R.string.normal);
                    break;
                case STATE_RECORDING:
                    if (isSnap == 1) {
                        setBackgroundResource(R.drawable.bg_chatroom_send_record_btn_snap_press);
                    } else {
                        setBackgroundResource(R.drawable.bg_chatroom_send_record_btn_press);
                    }
                    setText(R.string.recording);
                    if (isRecording) {
                        if (!isCountDown) {
                            //  不是倒计时状态
                            mDialogManager.recording();
                            // 复写dialog.recording();
                        } else {//倒计时状态
                            mDialogManager.countdown();
                        }
                    }
                    break;

                case STATE_WANT_TO_CANCEL:
                    isSwitch = true;
                    if (isSnap == 1) {
                        setBackgroundResource(R.drawable.bg_chatroom_send_record_btn_snap_press);
                    } else {
                        setBackgroundResource(R.drawable.bg_chatroom_send_record_btn_press);
                    }
                    setText(R.string.want_to_cancle);
                    // dialog want to cancel
                    mDialogManager.wantToCancel();
                    break;
            }
        }
    }

    @Override
    public boolean onPreDraw() {
        // TODO Auto-generated method stub
        return false;
    }


    // 获取音量大小的runnable
    private Runnable mGetVoiceLevelRunnable = new Runnable() {

        @Override
        public void run() {
            // TODO Auto-generated method stub
            while (isRecording) {
                try {
                    Thread.sleep(100);
                    mDuration += 0.1f;
                    mHandler.sendEmptyMessage(MSG_VOICE_CHANGE);
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }
    };


    private static class MyHandler extends Handler {
        WeakReference<AudioRecordButton> weakReference;

        MyHandler(AudioRecordButton out) {
            weakReference = new WeakReference<>(out);
        }

        @Override
        public void handleMessage(Message msg) {
            AudioRecordButton out = weakReference.get();


            switch (msg.what) {
                case MSG_AUDIO_PREPARED:
                    // 显示应该是在audio end prepare之后回调
                    out.mDialogManager.showRecordingDialog();
                    out.isRecording = true;
                    // 需要开启一个线程来变换音量
                    out.voiceLevelThread = new Thread(out.mGetVoiceLevelRunnable);
                    out.voiceLevelThread.start();
                    break;
                case MSG_VOICE_CHANGE:
                    if (out.mDuration >= MAX_RECORD_TIME - COUNTDOWN_SECONDS - 1) {
//                        if (out.mDuration * 10 % 10 == 0) {//整秒
                            out.showLastSeconds((int) out.mDuration);
                            out.isCountDown = true;
//                        }
                        if (out.isSwitch && out.mCurrentState != STATE_WANT_TO_CANCEL) {//是切换状态且当前状态不是STATE_WANT_TO_CANCEL
                            out.showLastSeconds((int) out.mDuration);
                            out.isCountDown = true;
                            out.isSwitch = false;
                        }
                    } else {
                        out.isCountDown = false;
                    }
                    out.mDialogManager.updateVoiceLevel(out.mAudioManager.getVoiceLevel(7));
                    break;
                case MSG_DIALOG_DISMISS:
                    out.mDialogManager.dismissDialog();
                    break;

            }
        }
    }

}
