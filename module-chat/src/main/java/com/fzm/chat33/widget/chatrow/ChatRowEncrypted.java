package com.fzm.chat33.widget.chatrow;

import android.view.View;
import android.widget.TextView;

import androidx.fragment.app.FragmentActivity;

import com.fzm.chat33.R;
import com.fzm.chat33.core.db.bean.ChatMessage;
import com.fzm.chat33.main.adapter.ChatListAdapter;


/**
 * @author zhengjy
 * @since 2019/05/24
 * Description:无法解密的消息
 */
public class ChatRowEncrypted extends ChatRowBase {

    private View bubbleLayout;
    private TextView tvMessage;

    public ChatRowEncrypted(FragmentActivity activity, ChatMessage message, int position, ChatListAdapter adapter) {
        super(activity, message, position, adapter);
    }

    @Override
    int getLayoutId() {
        return message.isSentType() ? R.layout.chat_row_sent_encrypted : R.layout.chat_row_receive_encrypted;
    }

    @Override
    void onFindViewById() {
        bubbleLayout = rootView.findViewById(R.id.chat_message_layout);
        tvMessage = rootView.findViewById(R.id.tv_message);
    }

    @Override
    void onSetUpView() {
        bubbleLayout.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (itemClickListener != null) {
                    itemClickListener.onBubbleLongClick(v, message);
                    return true;
                }
                return false;
            }
        });
        tvMessage.setText(R.string.chat_encrypted_message);
    }
}
