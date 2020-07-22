package com.fzm.chat33.widget.chatrow;

import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.fragment.app.FragmentActivity;

import com.fzm.chat33.R;
import com.fzm.chat33.core.db.bean.ChatMessage;
import com.fzm.chat33.main.adapter.ChatListAdapter;

public class ChatRowTextPacket extends ChatRowBase {

    TextView tvMessage, thumb_up;
    RelativeLayout chatMessageLayout;

    public ChatRowTextPacket(FragmentActivity activity, ChatMessage message, int position, ChatListAdapter adapter) {
        super(activity, message, position, adapter);
    }

    @Override
    int getLayoutId() {
        return message.isSentType() ? R.layout.chat_row_sent_text_packet : R.layout.chat_row_receive_text_packet;
    }

    @Override
    void onFindViewById() {
        tvMessage = rootView.findViewById(R.id.tv_message);
        thumb_up = rootView.findViewById(R.id.thumb_up);
        chatMessageLayout = rootView.findViewById(R.id.chat_message_layout);
    }

    @Override
    protected View chatMainView() {
        return chatMessageLayout;
    }

    @Override
    public TextView thumbUpView() {
        return thumb_up;
    }

    @Override
    void onSetUpView() {
        if (message.msg != null) {
            tvMessage.setText(message.msg.redBagRemark);
        }
    }
}
