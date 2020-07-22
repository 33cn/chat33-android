package com.fzm.chat33.widget.chatrow;

import android.graphics.drawable.Drawable;
import android.os.CountDownTimer;
import android.text.TextUtils;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import com.alibaba.android.arouter.launcher.ARouter;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.fuzamei.common.utils.ScreenUtils;
import com.fuzamei.common.utils.ShowUtils;
import com.fuzamei.common.utils.VibrateUtils;
import com.fuzamei.componentservice.app.AppRoute;
import com.fuzamei.componentservice.config.AppConfig;
import com.fzm.chat33.R;
import com.fuzamei.common.utils.ToolUtils;
import com.fzm.chat33.core.consts.PraiseState;
import com.fzm.chat33.core.db.bean.RoomUserBean;
import com.fzm.chat33.main.adapter.ChatListAdapter;
import com.fzm.chat33.core.bean.MessageState;
import com.fzm.chat33.core.provider.ChatInfoStrategy;
import com.fzm.chat33.core.provider.InfoProvider;
import com.fzm.chat33.core.provider.OnFindInfoListener;
import com.fzm.chat33.core.db.bean.ChatMessage;
import com.fzm.chat33.core.db.bean.InfoCacheBean;
import com.fzm.chat33.core.global.Chat33Const;
import com.fzm.chat33.utils.StringUtils;
import com.fzm.chat33.widget.ChatAvatarView;
import com.fzm.chat33.widget.ChatLinearLayout;
import com.fuzamei.common.widget.IconView;

import static com.fzm.chat33.core.db.bean.ChatMessage.Type.AUDIO;
import static com.fzm.chat33.core.db.bean.ChatMessage.Type.FILE;
import static com.fzm.chat33.core.db.bean.ChatMessage.Type.FORWARD;
import static com.fzm.chat33.core.db.bean.ChatMessage.Type.IMAGE;
import static com.fzm.chat33.core.db.bean.ChatMessage.Type.INVITATION;
import static com.fzm.chat33.core.db.bean.ChatMessage.Type.RECEIPT;
import static com.fzm.chat33.core.db.bean.ChatMessage.Type.RED_PACKET;
import static com.fzm.chat33.core.db.bean.ChatMessage.Type.SYSTEM;
import static com.fzm.chat33.core.db.bean.ChatMessage.Type.TEXT;
import static com.fzm.chat33.core.db.bean.ChatMessage.Type.TRANSFER;
import static com.fzm.chat33.core.db.bean.ChatMessage.Type.VIDEO;
import static com.fzm.chat33.core.global.Chat33Const.LEVEL_ADMIN;
import static com.fzm.chat33.core.global.Chat33Const.LEVEL_USER;


public abstract class ChatRowBase {

    FragmentActivity activity;
    ChatLinearLayout rootView;
    LayoutInflater inflater;
    ChatMessage message;
    ChatListAdapter mAdapter;
    ImageView iv_encrypt;
    int position;
    boolean selectable = false;
    MessageListItemClickListener itemClickListener;

    TextView tvMessageTime;
    ChatAvatarView ivUserHead;
    TextView tvUserName;
    TextView tvMemberLevel;
    CheckBox cbSelect;

    private GestureDetector detector;

    public ChatRowBase(FragmentActivity activity, ChatMessage message, int position, ChatListAdapter adapter) {
        this.activity = activity;
        this.inflater = LayoutInflater.from(activity);
        this.message = message;
        this.position = position;
        this.mAdapter = adapter;

        initView();
    }

    private void initView() {
        this.rootView = (ChatLinearLayout) inflater.inflate(getLayoutId(), null);
        tvMessageTime = rootView.findViewById(R.id.tv_message_time);
        ivUserHead = rootView.findViewById(R.id.iv_user_head);
        tvUserName = rootView.findViewById(R.id.tv_user_name);
        if (message.msgType == SYSTEM || message.msgType == TEXT || message.msgType == AUDIO
                || message.msgType == IMAGE || message.msgType == RED_PACKET || message.msgType == VIDEO
                || message.msgType == FORWARD || message.msgType == FILE) {
            cbSelect = rootView.findViewById(R.id.cb_select);
        }
        tvMemberLevel = rootView.findViewById(R.id.tv_member_level);
        rootView.findViewById(R.id.layout_row).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAdapter.messageCallBack.onHideKeyboard();
            }
        });

        onFindViewById();
    }

    public LinearLayout getRootView() {
        return rootView;
    }

    abstract int getLayoutId();

    abstract void onFindViewById();

    abstract void onSetUpView();

    protected View chatMainView() {
        return null;
    }

    public TextView thumbUpView() {
        return null;
    }

    public void setView(ChatMessage chatMessage, int position, boolean selectable, final MessageListItemClickListener listener) {
        this.message = chatMessage;
        this.position = position;
        this.itemClickListener = listener;
        this.selectable = selectable;
        if (message.msgType == SYSTEM || message.msgType == TEXT || message.msgType == AUDIO
                || message.msgType == IMAGE || message.msgType == RED_PACKET || message.msgType == VIDEO
                || message.msgType == FORWARD || message.msgType == FILE) {
            if (cbSelect != null) {
                // 阅后即焚消息不能多选
                if (message.isSnap != 1) {
                    cbSelect.setVisibility(selectable ? View.VISIBLE : View.GONE);
                    if (selectable) {
                        cbSelect.setChecked(message.isSelected);
                        cbSelect.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                            @Override
                            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                                message.isSelected = isChecked;
                                if (itemClickListener != null) {
                                    itemClickListener.onMessageSelected(message);
                                }
                            }
                        });
                    } else {
                        cbSelect.setOnCheckedChangeListener(null);
                    }
                } else {
                    cbSelect.setVisibility(View.GONE);
                }
            }
        }

        setUpBaseView();

        onSetUpView();

        if (chatMainView() != null && !selectable) {
            detector = new GestureDetector(activity, new GestureDetector.SimpleOnGestureListener(){
                @Override
                public boolean onDown(MotionEvent e) {
                    return true;
                }

                @Override
                public boolean onSingleTapConfirmed(MotionEvent e) {
                    chatMainView().performClick();
                    if (itemClickListener != null) {
                        itemClickListener.onBubbleClick(chatMainView(), message, ChatRowBase.this);
                    }
                    return true;
                }

                @Override
                public boolean onDoubleTap(MotionEvent e) {
                    if (itemClickListener != null) {
                        itemClickListener.onBubbleDoubleClick(chatMainView(), message);
                    }
                    return true;
                }

                @Override
                public void onLongPress(MotionEvent e) {
                    VibrateUtils.simple(activity, 40);
                    if (itemClickListener != null) {
                        itemClickListener.onBubbleLongClick(chatMainView(), message);
                    }
                }
            });
            chatMainView().setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    boolean process = false;
                    if (itemClickListener != null) {
                        process = itemClickListener.onTouchChatMainView(v, event);
                    }
                    if (!process) {
                        return detector.onTouchEvent(event);
                    } else {
                        return true;
                    }
                }
            });
        }
    }

    protected void setUpBaseView() {
        if (message.showTime) {
            tvMessageTime.setText(ToolUtils.timeFormat(message.sendTime));
            tvMessageTime.setVisibility(View.VISIBLE);
        } else {
            tvMessageTime.setVisibility(View.GONE);
        }
        if (message.channelType == Chat33Const.CHANNEL_FRIEND) {
            tvUserName.setVisibility(View.GONE);
        } else {
            tvUserName.setVisibility(View.VISIBLE);
        }
        if (message.channelType == Chat33Const.CHANNEL_ROOM && tvMemberLevel != null && mAdapter != null && mAdapter.getRoomUsers() != null) {
            RoomUserBean bean = mAdapter.getRoomUsers().get(message.senderId);
            if (bean != null) {
                if (bean.getMemberLevel() == LEVEL_USER) {
                    tvMemberLevel.setVisibility(View.GONE);
                } else if (bean.getMemberLevel() == LEVEL_ADMIN) {
                    tvMemberLevel.setVisibility(View.VISIBLE);
                    tvMemberLevel.setText(R.string.core_tips_group_admin);
                    tvMemberLevel.setBackgroundResource(R.drawable.shape_yellow_r4);
                } else {
                    tvMemberLevel.setVisibility(View.VISIBLE);
                    tvMemberLevel.setText(R.string.core_tips_group_master);
                    tvMemberLevel.setBackgroundResource(R.drawable.shape_blue_r4);
                }
            } else {
                tvMemberLevel.setVisibility(View.GONE);
            }
        }
        tvUserName.setTextColor(ContextCompat.getColor(activity, R.color.chat_text_grey_light));
        tvUserName.setTag(message);

        final RequestOptions options = new RequestOptions().placeholder(R.mipmap.default_avatar_round);
        InfoProvider.getInstance().strategy(new ChatInfoStrategy(message)).load(new OnFindInfoListener<InfoCacheBean>() {
            @Override
            public void onFindInfo(InfoCacheBean data, int place) {
                if (activity == null || activity.isFinishing()) {
                    return;
                }
                if (!message.equals(tvUserName.getTag())) {
                    return;
                }
                Glide.with(activity).load(StringUtils.aliyunFormat(data.getAvatar(), ScreenUtils.dp2px(activity, 35)
                        , ScreenUtils.dp2px(activity, 35)))
                        .apply(options)
                        .into(ivUserHead);
                ivUserHead.setIconRes(data.isIdentified() ? R.drawable.ic_user_identified : -1);
                tvUserName.setText(data.getDisplayName());
            }

            @Override
            public void onNotExist() {
                ivUserHead.setImageResource(R.mipmap.default_avatar_round);
                tvUserName.setText(R.string.chat_tips_no_name);
                tvMemberLevel.setVisibility(View.GONE);
            }
        });

        if (iv_encrypt != null) {
            if (message.channelType == Chat33Const.CHANNEL_ROOM && !AppConfig.DEVELOP) {
                iv_encrypt.setVisibility(View.GONE);
            } else {
                iv_encrypt.setVisibility(message.encrypted == 1 ? View.VISIBLE : View.GONE);
            }
        }

        if (message.isSentType() && (message.msgType == TEXT || message.msgType == AUDIO
                || message.msgType == IMAGE || message.msgType == RED_PACKET || message.msgType == VIDEO
                || message.msgType == FORWARD || message.msgType == FILE || message.msgType == TRANSFER
                || message.msgType == RECEIPT|| message.msgType == INVITATION)) {
            ProgressBar pr_loading = rootView.findViewById(R.id.pr_loading);
            ImageView iv_fail = rootView.findViewById(R.id.iv_fail);
            if (message.messageState == MessageState.SEND_SUCCESS) {
                pr_loading.setVisibility(View.INVISIBLE);
                iv_fail.setVisibility(View.INVISIBLE);
            } else if (message.messageState == MessageState.SENDING) {
                pr_loading.setVisibility(View.VISIBLE);
                iv_fail.setVisibility(View.INVISIBLE);
            } else if (message.messageState == MessageState.SEND_FAIL) {
                pr_loading.setVisibility(View.INVISIBLE);
                iv_fail.setVisibility(View.VISIBLE);
            } else {
                pr_loading.setVisibility(View.INVISIBLE);
                iv_fail.setVisibility(View.INVISIBLE);
            }
            iv_fail.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (itemClickListener != null) {
                        itemClickListener.onResendClick(v, message);
                    }
                }
            });
        }
        rootView.setSelectable(selectable);
        if (selectable) {
            rootView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (cbSelect != null) {
                        if (mAdapter.getSelectedMsg().size() == 50 && !cbSelect.isChecked()) {
                            ShowUtils.showToastNormal(activity, R.string.chat_error_select_max_count);
                        } else {
                            cbSelect.performClick();
                        }
                    }
                }
            });
        } else {
            rootView.setOnClickListener(null);
        }
        ivUserHead.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (itemClickListener != null) {
                    itemClickListener.onUserAvatarClick(view, message);
                }
            }
        });
        ivUserHead.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                // 只能在群聊里@别人
                if (!message.isSentType() && message.channelType == Chat33Const.CHANNEL_ROOM) {
                    if (itemClickListener != null) {
                        RoomUserBean user = mAdapter.getRoomUsers().get(message.senderId);
                        String name;
                        if (!TextUtils.isEmpty(user.getRoomNickname())) {
                            name = user.getRoomNickname();
                        } else {
                            name = user.getNickname();
                        }
                        itemClickListener.onUserAvatarLongClick(view, message.senderId, name);
                    }
                }
                return true;
            }
        });
        if (thumbUpView() != null && AppConfig.APP_MESSAGE_REWARD) {
            if (message.messageState == MessageState.SEND_SUCCESS
                    && message.isSnap != 1) {
                if (message.praise != null && message.praise.getPraiseNum() > 0) {
                    thumbUpView().setVisibility(View.VISIBLE);
                    Drawable drawable;
                    if (message.isSentType()) {
                        if (message.praise.beRewarded()) {
                            thumbUpView().setTextColor(ContextCompat.getColor(activity, R.color.chat_reward_orange));
                            drawable = ContextCompat.getDrawable(activity, R.drawable.ic_thumb_up_orange);
                        } else {
                            thumbUpView().setTextColor(ContextCompat.getColor(activity, R.color.chat_color_accent));
                            drawable = ContextCompat.getDrawable(activity, R.drawable.ic_thumb_up_accent);
                        }
                        drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());
                        thumbUpView().setCompoundDrawables(null, null, drawable, null);
                    } else {
                        if (message.praise.getState() == PraiseState.NONE) {
                            thumbUpView().setTextColor(ContextCompat.getColor(activity, R.color.chat_text_grey_light));
                            drawable = ContextCompat.getDrawable(activity, R.drawable.ic_thumb_up_grey);
                        } else if (message.praise.getState() == PraiseState.LIKE) {
                            thumbUpView().setTextColor(ContextCompat.getColor(activity, R.color.chat_color_accent));
                            drawable = ContextCompat.getDrawable(activity, R.drawable.ic_thumb_up_accent);
                        } else {
                            thumbUpView().setTextColor(ContextCompat.getColor(activity, R.color.chat_reward_orange));
                            drawable = ContextCompat.getDrawable(activity, R.drawable.ic_thumb_up_orange);
                        }
                        drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());
                        thumbUpView().setCompoundDrawables(drawable, null, null, null);
                    }
                    if (message.channelType == Chat33Const.CHANNEL_FRIEND) {
                        thumbUpView().setText("");
                    } else {
                        thumbUpView().setText(ToolUtils.convertNum(message.praise.getPraiseNum()));
                    }
                    thumbUpView().setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            ARouter.getInstance().build(AppRoute.MESSAGE_PRAISE)
                                    .withInt("channelType", message.channelType)
                                    .withString("logId", message.logId)
                                    .withString("targetId", message.receiveId)
                                    .navigation();
                        }
                    });
                } else {
                    thumbUpView().setVisibility(View.GONE);
                }
            } else {
                thumbUpView().setVisibility(View.GONE);
            }
        }
    }

    /**
     * 聊天内容等点击时间监听接口，根据需要添加方法和参数
     */
    public interface MessageListItemClickListener {
        /**
         * 触摸ChatMainView
         * @param v
         * @param event
         */
        boolean onTouchChatMainView(View v, MotionEvent event);
        /**
         * 预留
         * @param v
         * @param message
         */
        void onResendClick(View v, ChatMessage message);

        /**
         * 点击气泡
         * @param v
         * @param message
         */
        void onBubbleClick(View v, ChatMessage message, ChatRowBase chatRow);

        /**
         * 双击气泡
         * @param v
         * @param message
         */
        void onBubbleDoubleClick(View v, ChatMessage message);

        /**
         * 长按气泡
         * @param v
         * @param message
         */
        void onBubbleLongClick(View v, ChatMessage message);

        /**
         * 点击红包
         * @param v
         * @param message
         */
        void onRedBagClick(View v, ChatMessage message);

        /**
         * 点击头像
         * @param v
         * @param message
         */
        void onUserAvatarClick(View v, ChatMessage message);

        /**
         * 长按头像
         * @param v
         * @param username
         */
        void onUserAvatarLongClick(View v, String id, String username);

        /**
         * 点击语音播放或暂停
         * @param animView
         * @param message
         * @param callback
         */
        void onAudioClick(IconView animView, ChatMessage message, ChatRowAudio.Callback callback);

        /**
         * 阅后即焚显示
         * @param v
         * @param message
         * @param position
         */
        void onMessageShow(View v, ChatMessage message, int position);

        /**
         * 阅后即焚倒计时开始
         * @param key
         * @param timer
         */
        void onMessageCountDown(String key, CountDownTimer timer);

        /**
         * 阅后即焚消息销毁
         * @param view
         * @param message
         */
        void onMessageDestroy(View view, ChatMessage message);

        /**
         * 消息被选中
         * @param message
         */
        void onMessageSelected(ChatMessage message);
    }
}
