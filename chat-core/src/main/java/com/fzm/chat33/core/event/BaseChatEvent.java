package com.fzm.chat33.core.event;

import com.fzm.chat33.core.response.MsgSocketResponse;

import java.io.Serializable;

/**
 * @author zhengjy
 * @since 2018/12/29
 * Description:
 */
public class BaseChatEvent implements Serializable {

    public int eventType;
    public MsgSocketResponse msg;

    public BaseChatEvent(int eventType, MsgSocketResponse msg) {
        this.eventType = eventType;
        this.msg = msg;
    }
}
