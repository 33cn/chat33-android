package com.fzm.chat33.widget.chatrow;

import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import com.fzm.chat33.R;
import com.fzm.chat33.main.adapter.ChatListAdapter;
import com.fzm.chat33.core.db.bean.ChatMessage;

public class ChatRowRedPacket extends ChatRowBase {

    //View bubbleLayout;
    TextView tvMessage, thumb_up;
    LinearLayout chatMessageLayout;
    TextView tv_red_bag_title;
    TextView tv_red_bag;

    public ChatRowRedPacket(FragmentActivity activity, ChatMessage message, int position, ChatListAdapter adapter) {
        super(activity, message, position, adapter);
    }


    @Override
    int getLayoutId() {
        return message.isSentType() ? R.layout.chat_row_sent_red_packet : R.layout.chat_row_receive_red_packet;
    }

    @Override
    void onFindViewById() {
        // bubbleLayout = rootView.findViewById(R.id.chat_message_layout);
        tvMessage = rootView.findViewById(R.id.tv_message);
        tv_red_bag_title = rootView.findViewById(R.id.tv_red_bag_title);
        tv_red_bag = rootView.findViewById(R.id.tv_red_bag);
        chatMessageLayout = rootView.findViewById(R.id.chat_message_layout);
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
            tvMessage.setText(message.msg.redBagRemark);
            if (message.msg.isOpened) {
                //chatMessageLayout.setEnabled(false);

                tv_red_bag_title.setText(R.string.chat_red_packet_recevied);
                tv_red_bag_title.setTextColor(ContextCompat.getColor(activity, R.color.chat_white));
                tvMessage.setTextColor(ContextCompat.getColor(activity, R.color.chat_white));
                chatMessageLayout.setBackgroundResource(R.drawable.bg_chat_red_bag_normal);
                tv_red_bag.setTextColor(ContextCompat.getColor(activity, R.color.chat_red_bag_open));
                tv_red_bag.setText(R.string.icon_yilingquhongbao);
            } else {
                //chatMessageLayout.setEnabled(true);
                if (message.msg.redPacketStatus == 1) {
                    tv_red_bag_title.setText(R.string.chat_red_packet_look);
                    tv_red_bag_title.setTextColor(ContextCompat.getColor(activity, R.color.chat_white));
                    tvMessage.setTextColor(ContextCompat.getColor(activity, R.color.chat_white));
                    chatMessageLayout.setBackgroundResource(R.drawable.bg_chat_red_bag);
                    tv_red_bag.setTextColor(ContextCompat.getColor(activity, R.color.chat_red_packet_yellow));
                    tv_red_bag.setText(R.string.icon_hongbao);
                } else if (message.msg.redPacketStatus == 2) {
                    tv_red_bag_title.setText(R.string.chat_red_packet_recevie_finish);
                    tv_red_bag_title.setTextColor(ContextCompat.getColor(activity, R.color.chat_white));
                    tvMessage.setTextColor(ContextCompat.getColor(activity, R.color.chat_white));
                    chatMessageLayout.setBackgroundResource(R.drawable.bg_chat_red_bag_normal);
                    tv_red_bag.setTextColor(ContextCompat.getColor(activity, R.color.chat_red_bag_open));
                    tv_red_bag.setText(R.string.icon_yilingquhongbao);
                } else if (message.msg.redPacketStatus == 3) {
                    tv_red_bag_title.setText(R.string.chat_red_packet_overdue);
                    tv_red_bag_title.setTextColor(ContextCompat.getColor(activity, R.color.chat_white));
                    tvMessage.setTextColor(ContextCompat.getColor(activity, R.color.chat_white));
                    chatMessageLayout.setBackgroundResource(R.drawable.bg_chat_red_bag_normal);
                    tv_red_bag.setTextColor(ContextCompat.getColor(activity, R.color.chat_red_bag_open));
                    tv_red_bag.setText(R.string.icon_yilingquhongbao);
                } else if (message.msg.redPacketStatus == 4) {
                    tv_red_bag_title.setText(R.string.chat_red_packet_recevie_finish);
                    tv_red_bag_title.setTextColor(ContextCompat.getColor(activity, R.color.chat_white));
                    tvMessage.setTextColor(ContextCompat.getColor(activity, R.color.chat_white));
                    chatMessageLayout.setBackgroundResource(R.drawable.bg_chat_red_bag_normal);
                    tv_red_bag.setTextColor(ContextCompat.getColor(activity, R.color.chat_red_bag_open));
                    tv_red_bag.setText(R.string.icon_yilingquhongbao);
                } else {
                    tv_red_bag_title.setText(R.string.chat_red_packet_look);
                    tv_red_bag_title.setTextColor(ContextCompat.getColor(activity, R.color.chat_white));
                    tvMessage.setTextColor(ContextCompat.getColor(activity, R.color.chat_white));
                    chatMessageLayout.setBackgroundResource(R.drawable.bg_chat_red_bag);
                    tv_red_bag.setTextColor(ContextCompat.getColor(activity, R.color.chat_red_packet_yellow));
                    tv_red_bag.setText(R.string.icon_hongbao);
                }
            }
        }
    }
}
