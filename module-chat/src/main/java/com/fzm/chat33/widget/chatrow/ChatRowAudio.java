package com.fzm.chat33.widget.chatrow;

import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.ViewModelProviders;

import com.fzm.chat33.R;
import com.fzm.chat33.main.adapter.ChatListAdapter;
import com.fzm.chat33.core.db.ChatDatabase;
import com.fzm.chat33.core.db.bean.ChatMessage;
import com.fzm.chat33.core.db.dao.ChatMessageDao;
import com.fzm.chat33.core.global.Chat33Const;
import com.fuzamei.common.widget.IconView;
import com.fuzamei.common.utils.RoomUtils;
import com.fuzamei.common.utils.ShowUtils;
import com.fuzamei.componentservice.config.AppConfig;
import com.fzm.chat33.main.mvvm.ChatViewModel;

public class ChatRowAudio extends ChatRowBase implements SnapChat {

    View bubbleLayout, iv_lock, chat_message_snap, rl_duration;
    IconView tvAudioIcon;
    TextView tvDuration, tv_count, thumb_up;
    View dot_unread;
    private SnapChatCountDown timer;
    private ChatMessageDao chatDao;

    private int mMinItemWith;// 设置对话框的最大宽度和最小宽度
    private int mMaxItemWith;
    private ChatViewModel viewModel;

    public ChatRowAudio(FragmentActivity activity, ChatMessage message, int position, ChatListAdapter adapter, int maxWidth, int minWidth) {
        super(activity, message, position, adapter);

        //这里参数有外面传入，避免重复计算
        this.mMaxItemWith = maxWidth;
        this.mMinItemWith = minWidth;
        chatDao = ChatDatabase.getInstance().chatMessageDao();
        viewModel = ViewModelProviders.of(activity).get(ChatViewModel.class);
    }


    @Override
    int getLayoutId() {
        return message.isSentType() ? R.layout.chat_row_sent_audio : R.layout.chat_row_receive_audio;

    }

    @Override
    void onFindViewById() {
        bubbleLayout = rootView.findViewById(R.id.chat_message_layout);
        tvAudioIcon = rootView.findViewById(R.id.tv_audio_icon);
        tvDuration = rootView.findViewById(R.id.tv_duration);
        dot_unread = rootView.findViewById(R.id.dot_unread);
        iv_lock = rootView.findViewById(R.id.iv_lock);
        thumb_up = rootView.findViewById(R.id.thumb_up);

        //设置动画资源
        int[] left = {R.string.icon_yuyin_left_vol1, R.string.icon_yuyin_left_vol2, R.string.icon_yuyin_left_vol3};
        int[] right = {R.string.icon_yuyin_right_vol1, R.string.icon_yuyin_right_vol2, R.string.icon_yuyin_right_vol3};
        tvAudioIcon.setAnimResource(300, message.isSentType() ? right : left);
    }

    @Override
    void onSetUpView() {
        if (!message.isSentType()) {
            rl_duration = rootView.findViewById(R.id.rl_duration);
            tv_count = rootView.findViewById(R.id.tv_count);
            chat_message_snap = rootView.findViewById(R.id.chat_message_snap);
        }
        //audio语音涉及播放状态，单独方法设置
        if (message.isSnap == 1) {
            if (message.isSentType()) {
                iv_lock.setVisibility(View.VISIBLE);
            } else if (message.snapVisible == 0) {
                hideContent();
            } else {
                showContent();
            }
        } else {
            bubbleLayout.setVisibility(View.VISIBLE);
            if (!message.isSentType()) {
                tv_count.setVisibility(View.GONE);
                chat_message_snap.setVisibility(View.GONE);
            }
            iv_lock.setVisibility(View.GONE);
        }
    }

    //audio语音涉及播放状态，单独方法设置
    public void setAudioView(boolean isPlaying, String playingUrl) {
        if(message.msg ==null){
            return;
        }
        if (dot_unread != null) {
            if (message.msg.isRead || message.isSnap == 1) {
                dot_unread.setVisibility(View.INVISIBLE);
            } else {
                dot_unread.setVisibility(View.VISIBLE);
            }
        }
        if (isPlaying && message.msg.getMediaUrl().equals(playingUrl)) {
            tvAudioIcon.play();
            mAdapter.playingViewAnim = tvAudioIcon;
        } else {
            tvAudioIcon.reset();
        }

        int durationTime = (int) message.msg.getDuration();
        if (durationTime > 0) {
            String duration = (int) (message.msg.getDuration() + 0.5f) + "s";
            tvDuration.setText(duration);
            tvDuration.setVisibility(View.VISIBLE);
        } else {
            tvDuration.setVisibility(View.GONE);
        }

        tvAudioIcon.setVisibility(View.VISIBLE);


        setViewWidthByDuration(message.msg.getDuration(), bubbleLayout);

        if (chat_message_snap != null) {
            chat_message_snap.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ChatMessageDao.visibleSnapMsg.add(message.channelType + "-" + message.logId);
                    viewModel.readSnapMessage(message.logId,
                            message.channelType == Chat33Const.CHANNEL_ROOM ? 1 : 2).observe(activity, result -> {
                        if (result.isSucceed()) {
                            showContent();
                        } else {
                            ChatMessageDao.visibleSnapMsg.remove(message.channelType + "-" + message.logId);
                            ShowUtils.showToast(activity, result.error().getMessage());
                        }
                    });
                }
            });
        }

    }

    public void clickBubble() {
        if (timer != null) {
            timer.cancel();
            message.timer = null;
            message.destroyTime = 0;
            message.snapCounting = 0;
        }
        if (itemClickListener != null) {
            itemClickListener.onAudioClick(tvAudioIcon, message, new Callback() {
                @Override
                public void shouldStartCount() {
                    // 开始倒计时
                    if (!message.isSentType() && message.isSnap == 1 && message.snapCounting == 0) {
                        startCount();
                    }
                }
            });
        }
    }

    @Override
    protected View chatMainView() {
        return bubbleLayout;
    }

    @Override
    public TextView thumbUpView() {
        return message.isSnap != 1 ? thumb_up : null;
    }

    //根据播放时长设置语音条长度
    private void setViewWidthByDuration(float duration, View view) {
        if (duration == 0) {
            ViewGroup.LayoutParams lParams = view.getLayoutParams();
            lParams.width = ViewGroup.LayoutParams.WRAP_CONTENT;
            view.setLayoutParams(lParams);
        } else {
            float timePercent = duration / AppConfig.DEFAULT_MAX_RECORD_TIME;
            ViewGroup.LayoutParams lParams = view.getLayoutParams();
            lParams.width = (int) (mMinItemWith + (mMaxItemWith - mMinItemWith) * timePercent);
            view.setLayoutParams(lParams);
        }
    }

    public interface Callback {

        /**
         * 需要开始倒计时
         */
        void shouldStartCount();
    }

    @Override
    public void hideContent() {
        rl_duration.setVisibility(View.GONE);
        bubbleLayout.setVisibility(View.GONE);
        tv_count.setVisibility(View.GONE);
        chat_message_snap.setVisibility(View.VISIBLE);
        iv_lock.setVisibility(View.VISIBLE);
    }

    @Override
    public void showContent() {
        rl_duration.setVisibility(View.VISIBLE);
        bubbleLayout.setVisibility(View.VISIBLE);
        tv_count.setVisibility(View.VISIBLE);
        chat_message_snap.setVisibility(View.GONE);
        iv_lock.setVisibility(View.GONE);
        if (message.snapVisible == 0) {
            message.snapVisible = 1;
            RoomUtils.run(new Runnable() {
                @Override
                public void run() {
                    chatDao.updateVisible(message.snapVisible, message.channelType, message.logId);
                }
            });
            bubbleLayout.performClick();
        }
        if (message.snapCounting == 0) {
            tv_count.setText(com.fzm.chat33.utils.StringUtils.formateTime(10_000));
            if (itemClickListener != null) {
                itemClickListener.onMessageShow(bubbleLayout, message, position);
            }
        } else {
            startCount();
        }
    }

    @Override
    public void startCount() {
        if (itemClickListener != null) {
            itemClickListener.onMessageCountDown(message.logId, timer);
        }
        if (message.snapCounting == 0) {
            message.snapCounting = 1;
            RoomUtils.run(new Runnable() {
                @Override
                public void run() {
                    chatDao.updateCounting(message.snapCounting, message.channelType, message.logId);
                }
            });
        }
        if (message.timer == null) {
            tv_count.setTag(message);
            timer = new SnapChatCountDown(calculateRemainTime(), 1000L, tv_count, message, new SnapChatCountDown.OnFinishListener() {
                @Override
                public void onFinish(Object object) {
                    destroyContent(object);
                }
            });
            message.timer = timer;
            timer.start();
        } else {
            timer = (SnapChatCountDown) message.timer;
            tv_count.setTag(message);
            tv_count.setText(timer.getCurrentText());
            timer.setCountView(tv_count);
        }
    }

    @Override
    public void destroyContent(Object object) {
        timer = null;
        ((ChatMessage) object).timer = null;
        if (itemClickListener != null) {
            itemClickListener.onMessageDestroy(rootView, (ChatMessage) object);
        }
    }

    @Override
    public long calculateRemainTime() {
        if (message.destroyTime == 0) {
            message.destroyTime = System.currentTimeMillis() + 10_000;
            RoomUtils.run(new Runnable() {
                @Override
                public void run() {
                    chatDao.updateDestroyTime(message.destroyTime, message.channelType, message.logId);
                }
            });
            return 10_000;
        } else {
            return message.destroyTime - System.currentTimeMillis();
        }
    }
}
