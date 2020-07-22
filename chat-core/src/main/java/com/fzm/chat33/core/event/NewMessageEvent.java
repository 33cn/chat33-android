package com.fzm.chat33.core.event;

import com.fzm.chat33.core.db.bean.ChatMessage;
import com.fzm.chat33.core.response.MsgSocketResponse;

/**
 * @author zhengjy
 * @since 2018/12/29
 * Description:收到新消息事件
 */
public class NewMessageEvent extends BaseChatEvent {

    public ChatMessage message;

    public NewMessageEvent(int eventType, MsgSocketResponse msg, ChatMessage message) {
        super(eventType, msg);
        this.message = message;
    }
}
