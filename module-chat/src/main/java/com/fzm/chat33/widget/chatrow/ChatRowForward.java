package com.fzm.chat33.widget.chatrow;

import android.view.View;
import android.widget.TextView;

import androidx.fragment.app.FragmentActivity;

import com.fzm.chat33.R;
import com.fzm.chat33.main.adapter.ChatListAdapter;
import com.fzm.chat33.core.bean.comparator.DateComparator;
import com.fzm.chat33.core.db.bean.BriefChatLog;
import com.fzm.chat33.core.db.bean.ChatMessage;
import com.fzm.chat33.core.global.Chat33Const;

import java.util.Collections;

/**
 * @author zhengjy
 * @since 2018/12/25
 * Description:
 */
public class ChatRowForward extends ChatRowBase {

    private View bubbleLayout;
    private TextView tv_message_title, tv_message, tv_forward_count, thumb_up;

    public ChatRowForward(FragmentActivity activity, ChatMessage message, int position, ChatListAdapter adapter) {
        super(activity, message, position, adapter);
    }

    @Override
    int getLayoutId() {
        return message.isSentType() ? R.layout.chat_row_sent_forward : R.layout.chat_row_receive_forward;
    }

    @Override
    void onFindViewById() {
        bubbleLayout = rootView.findViewById(R.id.chat_message_layout);
        tv_message_title = rootView.findViewById(R.id.tv_message_title);
        tv_message = rootView.findViewById(R.id.tv_message);
        tv_forward_count = rootView.findViewById(R.id.tv_forward_count);
        thumb_up = rootView.findViewById(R.id.thumb_up);
    }

    @Override
    void onSetUpView() {
        if (message.msg.sourceChannel == Chat33Const.CHANNEL_FRIEND) {
            tv_message_title.setText(activity.getString(R.string.chat_title_forward_list1, message.msg.forwardUserName, message.msg.sourceName));
        } else if (message.msg.sourceChannel == Chat33Const.CHANNEL_ROOM) {
            tv_message_title.setText(activity.getString(R.string.chat_title_forward_list2, message.msg.sourceName));
        }
        StringBuilder content = new StringBuilder();
        Collections.sort(message.msg.sourceLog, new DateComparator());
        if (message.msg.sourceLog.size() >= 4) {
            for (int i = 0; i < 4; i++) {
                setDisplayContent(content, message.msg.sourceLog.get(i));
                if (i < 3) {
                    content.append("\n");
                }
            }
        } else {
            for (int i = 0; i < message.msg.sourceLog.size(); i++) {
                setDisplayContent(content, message.msg.sourceLog.get(i));
                if (i < message.msg.sourceLog.size() - 1) {
                    content.append("\n");
                }
            }
        }
        tv_message.setText(content.toString());
        tv_forward_count.setText(activity.getString(R.string.forward_count, message.msg.sourceLog.size()));
    }

    private void setDisplayContent(StringBuilder builder, BriefChatLog chatLog) {
        String temp = "";
        if (chatLog.msgType == ChatMessage.Type.SYSTEM
                || chatLog.msgType == ChatMessage.Type.TEXT) {
            temp = chatLog.msg.content == null ? activity.getString(R.string.core_msg_type11) : chatLog.msg.content;
        } else if (chatLog.msgType == ChatMessage.Type.AUDIO) {
            temp = activity.getString(R.string.core_msg_type2);
        } else if (chatLog.msgType == ChatMessage.Type.IMAGE) {
            temp = activity.getString(R.string.core_msg_type3);
        } else if (chatLog.msgType == ChatMessage.Type.RED_PACKET) {
            String remark = chatLog.msg.redBagRemark;
            if (remark == null) {
                remark = "";
            }
            temp = activity.getString(R.string.core_msg_type4) + remark;
        } else if (chatLog.msgType == ChatMessage.Type.VIDEO) {
            temp = activity.getString(R.string.core_msg_type5);
        } else if (chatLog.msgType == ChatMessage.Type.FORWARD) {
            temp = activity.getString(R.string.core_msg_type7);
        } else if (chatLog.msgType == ChatMessage.Type.FILE) {
            temp = chatLog.msg.fileName == null
                    ? activity.getString(R.string.core_msg_type12)
                    : activity.getString(R.string.core_msg_type12) + chatLog.msg.fileName;
        }
        builder.append(chatLog.senderInfo.getDisplayName()).append(":").append(temp);
    }

    @Override
    protected View chatMainView() {
        return bubbleLayout;
    }

    @Override
    public TextView thumbUpView() {
        return thumb_up;
    }
}
