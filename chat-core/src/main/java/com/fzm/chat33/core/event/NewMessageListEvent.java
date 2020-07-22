package com.fzm.chat33.core.event;

import com.fzm.chat33.core.db.bean.ChatMessage;
import com.fzm.chat33.core.response.MsgSocketResponse;

import java.util.List;

/**
 * @author zhengjy
 * @since 2019/05/08
 * Description:批量收到新消息事件
 */
public class NewMessageListEvent extends BaseChatEvent {

    public List<ChatMessage> messages;

    public NewMessageListEvent(int eventType, MsgSocketResponse msg, List<ChatMessage> messages) {
        super(eventType, msg);
        this.messages = messages;
    }
}
