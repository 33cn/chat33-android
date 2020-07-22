package com.fzm.chat33.widget.chatrow;

import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.text.SpannableString;
import android.util.Patterns;
import android.view.View;
import android.widget.TextView;

import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.ViewModelProviders;

import com.alibaba.android.arouter.launcher.ARouter;
import com.fuzamei.common.utils.RoomUtils;
import com.fuzamei.common.utils.ShowUtils;
import com.fuzamei.componentservice.app.AppRoute;
import com.fzm.chat33.R;
import com.fzm.chat33.core.db.ChatDatabase;
import com.fzm.chat33.core.db.bean.ChatMessage;
import com.fzm.chat33.core.db.dao.ChatMessageDao;
import com.fzm.chat33.core.global.Chat33Const;
import com.fzm.chat33.main.adapter.ChatListAdapter;
import com.fzm.chat33.main.mvvm.ChatViewModel;
import com.qmuiteam.qmui.widget.textview.QMUILinkTextView;

import java.util.regex.Matcher;

public class ChatRowText extends ChatRowBase implements SnapChat {

    View bubbleLayout, chat_message_snap, iv_lock;
    TextView tv_count, tv_forward;
    QMUILinkTextView tvMessage;
    private SnapChatCountDown timer;
    private ChatMessageDao chatDao;
    TextView tv_action, thumb_up;

    private boolean isExpand = false;
    private ChatViewModel viewModel;

    public ChatRowText(FragmentActivity activity, ChatMessage message, int position, ChatListAdapter adapter) {
        super(activity, message, position, adapter);
        chatDao = ChatDatabase.getInstance().chatMessageDao();
        viewModel = ViewModelProviders.of(activity).get(ChatViewModel.class);
    }

    @Override
    int getLayoutId() {
        return message.isSentType() ? R.layout.chat_row_sent_text : R.layout.chat_row_receive_text;
    }

    @Override
    void onFindViewById() {
        bubbleLayout = rootView.findViewById(R.id.chat_message_layout);
        tvMessage = rootView.findViewById(R.id.tv_message);
        iv_lock = rootView.findViewById(R.id.iv_lock);
        tv_action = rootView.findViewById(R.id.tv_action);
        thumb_up = rootView.findViewById(R.id.thumb_up);
        if (message.isSentType()) {
            tv_forward = rootView.findViewById(R.id.tv_forward);
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    void onSetUpView() {
        if (!message.isSentType()) {
            tv_count = rootView.findViewById(R.id.tv_count);
            chat_message_snap = rootView.findViewById(R.id.chat_message_snap);
        } else {
            if (message.msg.sourceChannel == Chat33Const.CHANNEL_ROOM) {
                tv_forward.setVisibility(View.VISIBLE);
                tv_forward.setText(activity.getString(R.string.chat_forward_room_content, message.msg.sourceName));
            } else if (message.msg.sourceChannel == Chat33Const.CHANNEL_FRIEND) {
                tv_forward.setVisibility(View.VISIBLE);
                tv_forward.setText(activity.getString(R.string.chat_forward_friend_content, message.msg.sourceName));
            } else {
                tv_forward.setVisibility(View.GONE);
            }
        }
        if(message.msg !=null){
            if (message.isSnap == 1) {
                if (message.isSentType()) {
                    iv_lock.setVisibility(View.VISIBLE);
                } else if (message.snapVisible == 0) {
                    // 如果是阅后即焚消息则先隐藏
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
//            tvMessage.setText(message.msg.content);
            // 把TextView的事件强制传递给父元素，因为TextView在有ClickSpan的情况下默认会消耗掉事件
            tvMessage.setNeedForceEventToParent(true);
            bubbleLayout.setClickable(true);
            tvMessage.setOnLinkClickListener(new QMUILinkTextView.OnLinkClickListener() {
                @Override
                public void onTelLinkClick(String phoneNumber) {
                    ClipboardManager cm = (ClipboardManager) activity.getSystemService(Context.CLIPBOARD_SERVICE);
                    ClipData mClipData = ClipData.newPlainText("Label", phoneNumber);
                    if (cm != null) {
                        cm.setPrimaryClip(mClipData);
                        ShowUtils.showToastNormal(activity, R.string.chat_copyed_message);
                    }
                }

                @Override
                public void onMailLinkClick(String mailAddress) {
                    ClipboardManager cm = (ClipboardManager) activity.getSystemService(Context.CLIPBOARD_SERVICE);
                    ClipData mClipData = ClipData.newPlainText("Label", mailAddress);
                    if (cm != null) {
                        cm.setPrimaryClip(mClipData);
                        ShowUtils.showToastNormal(activity, R.string.chat_copyed_message);
                    }
                }

                @Override
                public void onWebUrlLinkClick(String url) {
                    ARouter.getInstance().build(AppRoute.WEB_BROWSER)
                            .withString("url", url)
                            .navigation();
                    tvMessage.setScrollY(0);
                }
            });
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
            tv_action.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (isExpand) {
                        tvMessage.setMaxLines(2);
                        tvMessage.setScrollY(0);
                        tv_action.setText(R.string.chat_fold);
                    } else {
                        tvMessage.setMaxLines(Integer.MAX_VALUE);
                        tvMessage.setScrollY(0);
                        tv_action.setText(R.string.chat_unfold);
                    }
                    isExpand = !isExpand;
                }
            });
            processText();
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

    private void processText() {
        if (message.msg.content == null) {
            return;
        }
        if (message.msg.linkContent == null) {
            message.msg.linkContent = new SpannableString(message.msg.content);
            Matcher matcher = Patterns.WEB_URL.matcher(message.msg.content);
            if (matcher.find() && matcher.group(1).length() == message.msg.content.length() && message.msg.content.length() > 50) {
                tvMessage.setMaxLines(isExpand ? Integer.MAX_VALUE : 2);
                tv_action.setVisibility(View.VISIBLE);
            } else {
                tvMessage.setMaxLines(Integer.MAX_VALUE);
                tv_action.setVisibility(View.GONE);
            }
        } else {
            Matcher matcher = Patterns.WEB_URL.matcher(message.msg.content);
            if (matcher.find() && matcher.group(1).length() == message.msg.content.length() && message.msg.content.length() > 50) {
                tvMessage.setMaxLines(isExpand ? Integer.MAX_VALUE : 2);
                tv_action.setVisibility(View.VISIBLE);
            } else {
                tvMessage.setMaxLines(Integer.MAX_VALUE);
                tv_action.setVisibility(View.GONE);
            }
        }
        tvMessage.setText(message.msg.content);
    }

    @Override
    public void hideContent() {
        bubbleLayout.setVisibility(View.GONE);
        tv_count.setVisibility(View.GONE);
        chat_message_snap.setVisibility(View.VISIBLE);
        iv_lock.setVisibility(View.VISIBLE);
    }

    @Override
    public void showContent() {
        if (message.snapVisible == 0) {
            message.snapVisible = 1;
            RoomUtils.run(new Runnable() {
                @Override
                public void run() {
                    chatDao.updateVisible(message.snapVisible, message.channelType, message.logId);
                }
            });
        }
        bubbleLayout.setVisibility(View.VISIBLE);
        tv_count.setVisibility(View.VISIBLE);
        chat_message_snap.setVisibility(View.GONE);
        iv_lock.setVisibility(View.GONE);
        if (message.snapCounting == 0) {
            startCount();
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
            long viewTime;
            if (message.msg.content == null || message.msg.content.length() < 20) {
                viewTime = 10_000L;
            } else if (message.msg.content.length() < 50) {
                viewTime = 30_000L;
            } else if (message.msg.content.length() < 100) {
                viewTime = 60_000L;
            } else {
                viewTime = (message.msg.content.length() - 50) / 50 * 30_000L + 60_000L;
            }
            message.destroyTime = System.currentTimeMillis() + viewTime;
            RoomUtils.run(new Runnable() {
                @Override
                public void run() {
                    chatDao.updateDestroyTime(message.destroyTime, message.channelType, message.logId);
                }
            });
            return viewTime;
        } else {
            return message.destroyTime - System.currentTimeMillis();
        }
    }

    public void setNeedExpandFun(boolean needExpandFun) {
//        tvMessage.setNeedExpandFun(needExpandFun);
    }
}
