package com.fzm.chat33.widget.popup;

import android.media.AudioManager;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.ViewModelProviders;

import com.fuzamei.componentservice.config.AppConfig;
import com.fuzamei.componentservice.config.AppPreference;
import com.fzm.chat33.R;
import com.fzm.chat33.core.db.bean.ChatMessage;
import com.fzm.chat33.core.db.bean.RoomUserBean;
import com.fzm.chat33.core.global.Chat33Const;
import com.fzm.chat33.main.mvvm.ChatViewModel;

import static com.fzm.chat33.core.bean.MessageState.SEND_SUCCESS;
import static com.fzm.chat33.core.consts.PraiseState.LIKE;
import static com.fzm.chat33.core.db.bean.ChatMessage.Type.AUDIO;
import static com.fzm.chat33.core.db.bean.ChatMessage.Type.FILE;
import static com.fzm.chat33.core.db.bean.ChatMessage.Type.FORWARD;
import static com.fzm.chat33.core.db.bean.ChatMessage.Type.IMAGE;
import static com.fzm.chat33.core.db.bean.ChatMessage.Type.INVITATION;
import static com.fzm.chat33.core.db.bean.ChatMessage.Type.NOTIFICATION;
import static com.fzm.chat33.core.db.bean.ChatMessage.Type.RECEIPT;
import static com.fzm.chat33.core.db.bean.ChatMessage.Type.RED_PACKET;
import static com.fzm.chat33.core.db.bean.ChatMessage.Type.SYSTEM;
import static com.fzm.chat33.core.db.bean.ChatMessage.Type.TEXT;
import static com.fzm.chat33.core.db.bean.ChatMessage.Type.TRANSFER;
import static com.fzm.chat33.core.db.bean.ChatMessage.Type.VIDEO;

public class ChatMessagePopupWindow extends PopupWindow implements View.OnClickListener{

    private ChatMessage message;
    private FragmentActivity activity;
    private TextView thumb;
    private TextView reward;
    private TextView copy;
    private TextView revokeMsg;
    private TextView disableSpeak;
    private TextView enableSpeak;
    private TextView communication_mode;
    private TextView normal_mode;
    private TextView multi_choose;
    private TextView delete;
    private TextView forward;
    private TextView share;
    private ChooseCallBack chooseCallBack;
    private int memberLevel;
    private ChatViewModel viewModel;

    public ChatMessagePopupWindow(FragmentActivity activity, ChatMessage message, int memberLevel) {
        super(activity);
        this.activity = activity;
        this.message = message;
        this.memberLevel = memberLevel;
        viewModel = ViewModelProviders.of(activity).get(ChatViewModel.class);
        init();
    }


    private void init() {

        LinearLayout linearLayout = (LinearLayout) LayoutInflater.from(activity).inflate(R.layout.popup_chat_message, null);
        setWidth(LinearLayout.LayoutParams.WRAP_CONTENT);
        setHeight(LinearLayout.LayoutParams.WRAP_CONTENT);
        thumb = linearLayout.findViewById(R.id.thumb);
        thumb.setOnClickListener(this);
        reward = linearLayout.findViewById(R.id.reward);
        reward.setOnClickListener(this);
        copy = linearLayout.findViewById(R.id.copy);
        copy.setOnClickListener(this);
        revokeMsg = linearLayout.findViewById(R.id.revoke);
        revokeMsg.setOnClickListener(this);
        forward = linearLayout.findViewById(R.id.forward);
        forward.setOnClickListener(this);
        disableSpeak = linearLayout.findViewById(R.id.disable_speak);
        disableSpeak.setOnClickListener(this);
        enableSpeak = linearLayout.findViewById(R.id.enable_speak);
        enableSpeak.setOnClickListener(this);
        communication_mode = linearLayout.findViewById(R.id.communication_mode);
        communication_mode.setOnClickListener(this);
        normal_mode = linearLayout.findViewById(R.id.normal_mode);
        normal_mode.setOnClickListener(this);
        multi_choose = linearLayout.findViewById(R.id.multi_choose);
        multi_choose.setOnClickListener(this);
        delete = linearLayout.findViewById(R.id.delete);
        delete.setOnClickListener(this);
        share = linearLayout.findViewById(R.id.share);
        share.setOnClickListener(this);
        setContentView(linearLayout);
        setBackgroundDrawable(ContextCompat.getDrawable(activity, R.drawable.img_operate));
        setOutsideTouchable(true);
        setTouchable(true);

        forward.setVisibility(View.GONE);
        disableSpeak.setVisibility(View.GONE);
        enableSpeak.setVisibility(View.GONE);
        multi_choose.setVisibility(View.GONE);
        delete.setVisibility(View.VISIBLE);
        share.setVisibility(View.GONE);

        if (message.channelType == Chat33Const.CHANNEL_FRIEND) {
            if (message.msgType == RED_PACKET || message.msgType == TRANSFER || message.msgType == RECEIPT
                    || message.msgType == INVITATION) {
                revokeMsg.setVisibility(View.GONE);
            } else if (message.messageState == SEND_SUCCESS && message.isSentType() && System.currentTimeMillis() - message.sendTime < 10 * 60 * 1000) {
                revokeMsg.setVisibility(View.VISIBLE);
            } else {
                revokeMsg.setVisibility(View.GONE);
            }
        } else {
            // 群聊消息
            if (message.msgType == RED_PACKET || message.msgType == NOTIFICATION) {
                revokeMsg.setVisibility(View.GONE);
            } else if (message.messageState == SEND_SUCCESS) {
                if (memberLevel > 1) {
                    revokeMsg.setVisibility(View.VISIBLE);
                } else if (message.isSentType() && System.currentTimeMillis() - message.sendTime < 10 * 60 * 1000) {
                    revokeMsg.setVisibility(View.VISIBLE);
                } else {
                    revokeMsg.setVisibility(View.GONE);
                }
            } else {
                revokeMsg.setVisibility(View.GONE);
            }
        }

        if (message.msgType == AUDIO) {
            int mode = AppPreference.INSTANCE.getSOUND_PLAY_MODE();
            if (mode == AudioManager.MODE_NORMAL) {
                communication_mode.setVisibility(View.VISIBLE);
                normal_mode.setVisibility(View.GONE);
            } else {
                communication_mode.setVisibility(View.GONE);
                normal_mode.setVisibility(View.VISIBLE);
            }
        } else {
            communication_mode.setVisibility(View.GONE);
            normal_mode.setVisibility(View.GONE);
        }
        if(message.msgType == AUDIO || message.msgType == IMAGE || message.msgType == VIDEO
                || message.msgType == FORWARD || message.msgType == FILE || message.msgType == TRANSFER
                || message.msgType == RECEIPT || message.msgType == INVITATION){
            copy.setVisibility(View.GONE);
        }

        if (message.msgType == RED_PACKET) {
            if (message.msg.packetMode == 1) {
                copy.setVisibility(View.VISIBLE);
            } else {
                copy.setVisibility(View.GONE);
            }
        }

        if (message.isSnap == 1 && !message.isSentType()) {
            copy.setVisibility(View.GONE);
        }
        if (message.msgType == SYSTEM || message.msgType == TEXT || message.msgType == AUDIO
                || message.msgType == IMAGE || message.msgType == RED_PACKET || message.msgType == VIDEO
                || message.msgType == FORWARD || message.msgType == FILE) {
            if (message.isSnap != 1) {
                multi_choose.setVisibility(View.VISIBLE);
            }
        }
        if (message.msgType == SYSTEM || message.msgType == TEXT || message.msgType == IMAGE
                || message.msgType == VIDEO || message.msgType == FILE) {
            if (message.isSnap != 1) {
                forward.setVisibility(View.VISIBLE);
            }
        }
        if (!TextUtils.isEmpty(AppConfig.WX_APP_ID)) {
            if (message.msgType == SYSTEM || message.msgType == TEXT || message.msgType == IMAGE
                    || message.msgType == VIDEO || message.msgType == FILE) {
                if (message.msgType == VIDEO || message.msgType == FILE) {
                    if (TextUtils.isEmpty(message.msg.getLocalPath())) {
                        share.setVisibility(View.GONE);
                    } else {
                        share.setVisibility(View.VISIBLE);
                    }
                } else {
                    share.setVisibility(View.VISIBLE);
                }
            } else if (message.msgType == RED_PACKET && message.msg.redPacketStatus == 1) {
                share.setVisibility(View.VISIBLE);
            }
        }
//        if (!AppConfig.APP_MESSAGE_REWARD
//                || message.msgType == NOTIFICATION
//                || message.msgType == SYSTEM
//                || message.isSnap == 1
//                || message.isSentType()) {
//            thumb.setVisibility(View.GONE);
//            reward.setVisibility(View.GONE);
//        } else {
//            if (message.praise != null && (message.praise.getState() & LIKE) == LIKE) {
//                thumb.setVisibility(View.GONE);
//            } else {
//                thumb.setVisibility(View.VISIBLE);
//            }
//            reward.setVisibility(View.VISIBLE);
//        }
        thumb.setVisibility(View.GONE);
        reward.setVisibility(View.GONE);

        if (message.channelType == Chat33Const.CHANNEL_ROOM && memberLevel > 1 && !message.isSentType()) {
            viewModel.getRoomUserInfo(message.receiveId, message.senderId).observe(activity, it -> {
                if(it.isSucceed()) {
                    RoomUserBean roomUserBean = it.data();
                    if (roomUserBean.getRoomMutedType() == 1) {
                        disableSpeak.setVisibility(View.VISIBLE);
                        enableSpeak.setVisibility(View.GONE);
                    } else if (roomUserBean.getRoomMutedType() == 2) {
                        if (roomUserBean.getMutedType() == 2) {
                            disableSpeak.setVisibility(View.GONE);
                            enableSpeak.setVisibility(View.VISIBLE);
                        } else {
                            disableSpeak.setVisibility(View.VISIBLE);
                            enableSpeak.setVisibility(View.GONE);
                        }
                    } else if (roomUserBean.getRoomMutedType() == 3) {
                        if (roomUserBean.getMutedType() == 3) {
                            disableSpeak.setVisibility(View.VISIBLE);
                            enableSpeak.setVisibility(View.GONE);
                        } else {
                            disableSpeak.setVisibility(View.GONE);
                            enableSpeak.setVisibility(View.VISIBLE);
                        }
                    } else if (roomUserBean.getRoomMutedType() == 4) {
                        disableSpeak.setVisibility(View.GONE);
                        enableSpeak.setVisibility(View.VISIBLE);
                    }
                } else {
                    disableSpeak.setVisibility(View.GONE);
                    enableSpeak.setVisibility(View.GONE);
                }

            });
        }
    }

    public void setChooseCallBack(ChooseCallBack chooseCallBack) {
        this.chooseCallBack = chooseCallBack;
    }

    public void show(View anchor) {
        if (copy.getVisibility() == View.GONE
                && revokeMsg.getVisibility() == View.GONE
                && forward.getVisibility() == View.GONE
                && disableSpeak.getVisibility() == View.GONE
                && enableSpeak.getVisibility() == View.GONE
                && communication_mode.getVisibility() == View.GONE
                && normal_mode.getVisibility() == View.GONE
                && multi_choose.getVisibility() == View.GONE
                && delete.getVisibility() == View.GONE) {
            return;
        }
        if (message.msgType != SYSTEM && message.msgType != IMAGE && !(message.msgType == RED_PACKET && message.msg.packetMode == 0)
                && message.msgType != VIDEO && message.msgType != FILE && message.msgType != TRANSFER
                && message.msgType != RECEIPT && message.msgType != INVITATION) {
            anchor.setBackgroundResource(message.isSentType() ? R.drawable.img_chat_send_pressed : R.drawable.img_chat_receive_pressed);
        }
        int[] location = computeLocation(anchor);
        showAtLocation(anchor, Gravity.NO_GRAVITY, location[0] + anchor.getWidth() / 2, location[1] + anchor.getHeight() / 2);
    }

    private int[] computeLocation(View anchor) {

        int[] location = new int[2];
        anchor.getLocationOnScreen(location);
        return location;
    }


    @Override
    public void onClick(View v) {
        dismiss();
        if(chooseCallBack !=null){
            chooseCallBack.onClick(v);
        }
    }

    public interface ChooseCallBack{
        void onClick(View v);
    }
}
