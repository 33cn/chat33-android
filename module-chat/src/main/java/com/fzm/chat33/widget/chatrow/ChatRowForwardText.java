package com.fzm.chat33.widget.chatrow;

import android.view.View;
import android.widget.TextView;

import androidx.fragment.app.FragmentActivity;

import com.fzm.chat33.R;
import com.fzm.chat33.main.adapter.ChatListAdapter;
import com.fzm.chat33.core.db.bean.ChatMessage;
import com.fzm.chat33.core.global.Chat33Const;


/**
 * @author zhengjy
 * @since 2019/1/4
 * Description:转发公告
 */
public class ChatRowForwardText extends ChatRowBase {

    private View bubbleLayout;
    private TextView tvMessage, tv_forward, thumb_up;

    public ChatRowForwardText(FragmentActivity activity, ChatMessage message, int position, ChatListAdapter adapter) {
        super(activity, message, position, adapter);
    }

    @Override
    int getLayoutId() {
        return message.isSentType() ? R.layout.chat_row_sent_text_f : R.layout.chat_row_receive_text_f;
    }

    @Override
    void onFindViewById() {
        bubbleLayout = rootView.findViewById(R.id.chat_message_layout);
        tvMessage = rootView.findViewById(R.id.tv_message);
        thumb_up = rootView.findViewById(R.id.thumb_up);
        if (message.isSentType()) {
            tv_forward = rootView.findViewById(R.id.tv_forward);
        }
    }

    @Override
    void onSetUpView() {
        if (message.isSentType()) {
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
        if (message.msgType == ChatMessage.Type.SYSTEM) {
            tvMessage.setText(message.msg.content);
        } else if (message.msgType == ChatMessage.Type.AUDIO) {
            tvMessage.setText(activity.getString(R.string.core_msg_type2));
        } else if (message.msgType == ChatMessage.Type.RED_PACKET) {
            tvMessage.setText(activity.getString(R.string.core_msg_type4));
        } else if (message.msgType == ChatMessage.Type.FORWARD) {
            tvMessage.setText(activity.getString(R.string.core_msg_type7));
        }
    }
}
