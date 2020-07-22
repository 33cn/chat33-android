package com.fzm.chat33.core.event;

import com.fzm.chat33.core.bean.NotificationBean;
import com.fzm.chat33.core.response.MsgSocketResponse;

/**
 * @author zhengjy
 * @since 2018/12/29
 * Description:显示通知事件
 */
public class NotificationEvent extends BaseChatEvent {

    public NotificationBean notification;

    public NotificationEvent(int eventType, MsgSocketResponse msg, NotificationBean notification) {
        super(eventType, msg);
        this.notification = notification;
    }
}
