package com.fzm.chat33.widget.chatrow;

import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.fragment.app.FragmentActivity;

import com.fzm.chat33.R;
import com.fzm.chat33.core.db.bean.ChatMessage;
import com.fzm.chat33.main.adapter.ChatListAdapter;

public class ChatRowReceipt extends ChatRowBase {

    TextView tvAmount;
    LinearLayout chatMessageLayout;
    TextView tv_request_tips, thumb_up;
    ImageView iv_status;

    public ChatRowReceipt(FragmentActivity activity, ChatMessage message, int position, ChatListAdapter adapter) {
        super(activity, message, position, adapter);
    }

    @Override
    int getLayoutId() {
        return message.isSentType() ? R.layout.chat_row_sent_receipt : R.layout.chat_row_receive_receipt;
    }

    @Override
    void onFindViewById() {
        tv_request_tips = rootView.findViewById(R.id.tv_request_tips);
        chatMessageLayout = rootView.findViewById(R.id.chat_message_layout);
        tvAmount = rootView.findViewById(R.id.tv_amount);
        iv_status = rootView.findViewById(R.id.iv_status);
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
            if (TextUtils.isEmpty(message.msg.recordId)) {
                if (message.isSentType()) {
                    tv_request_tips.setText(R.string.chat_receipt_to_other);
                } else {
                    tv_request_tips.setText(R.string.chat_receipt_to_me);
                }
                chatMessageLayout.setBackgroundResource(R.drawable.bg_chat_receipt);
                iv_status.setImageResource(R.mipmap.icon_chat_receipt);
            } else {
                tv_request_tips.setText(R.string.chat_receipt_payment);
                chatMessageLayout.setBackgroundResource(R.drawable.bg_chat_receipt_finished);
                iv_status.setImageResource(R.mipmap.icon_chat_receipt_finished);
            }
        }
    }
}
