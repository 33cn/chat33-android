package com.fzm.chat33.widget.chatrow;

import android.view.View;
import android.widget.TextView;

import androidx.fragment.app.FragmentActivity;

import com.fuzamei.common.utils.ToolUtils;
import com.fzm.chat33.R;
import com.fzm.chat33.main.adapter.ChatListAdapter;
import com.fzm.chat33.core.db.bean.ChatMessage;

public class ChatRowUnsupported extends ChatRowBase {

    TextView tvMessage;

    public ChatRowUnsupported(FragmentActivity activity, ChatMessage message, int position, ChatListAdapter adapter) {
        super(activity, message, position, adapter);
    }

    @Override
    int getLayoutId() {
        return R.layout.chat_row_notification ;
    }

    @Override
    void onFindViewById() {
        tvMessage = rootView.findViewById(R.id.tv_message);
    }

    @Override
    protected void setUpBaseView() {
        if (position == 0) {
            tvMessageTime.setText(ToolUtils.timeFormat(message.sendTime));
            tvMessageTime.setVisibility(View.VISIBLE);
        } else {
            //显示时间,如果与上一条间隔时间小于10分钟
            ChatMessage preChat = (ChatMessage) mAdapter.getItem(position - 1);
            if (preChat != null && message.sendTime - preChat.sendTime > 10 * 60 * 1000) {
                tvMessageTime.setText(ToolUtils.timeFormat(message.sendTime));
                tvMessageTime.setVisibility(View.VISIBLE);
            } else {
                tvMessageTime.setVisibility(View.GONE);
            }
        }
    }

    @Override
    void onSetUpView() {
        tvMessage.setText(R.string.chat_error_unsupport_message_type);
    }
}
