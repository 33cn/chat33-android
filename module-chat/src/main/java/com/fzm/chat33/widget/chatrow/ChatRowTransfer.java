package com.fzm.chat33.widget.chatrow;

import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.fragment.app.FragmentActivity;

import com.fzm.chat33.R;
import com.fzm.chat33.core.db.bean.ChatMessage;
import com.fzm.chat33.main.adapter.ChatListAdapter;

public class ChatRowTransfer extends ChatRowBase {

    TextView tvAmount;
    LinearLayout chatMessageLayout;
    TextView tv_transfer_tips, thumb_up;

    public ChatRowTransfer(FragmentActivity activity, ChatMessage message, int position, ChatListAdapter adapter) {
        super(activity, message, position, adapter);
    }

    @Override
    int getLayoutId() {
        return message.isSentType() ? R.layout.chat_row_sent_transfer : R.layout.chat_row_receive_transfer;
    }

    @Override
    void onFindViewById() {
        tv_transfer_tips = rootView.findViewById(R.id.tv_transfer_tips);
        chatMessageLayout = rootView.findViewById(R.id.chat_message_layout);
        tvAmount = rootView.findViewById(R.id.tv_amount);
        thumb_up = rootView.findViewById(R.id.thumb_up);
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
            tvAmount.setText(message.msg.amount + message.msg.coinName);
            if (message.isSentType()) {
                tv_transfer_tips.setText(R.string.chat_transfer_to_other);
            } else {
                tv_transfer_tips.setText(R.string.chat_transfer_to_me);
            }
        }
    }
}
