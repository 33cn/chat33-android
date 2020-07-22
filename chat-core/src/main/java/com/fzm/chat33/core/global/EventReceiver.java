package com.fzm.chat33.core.global;

import com.fzm.chat33.core.event.BaseChatEvent;

/**
 * @author zhengjy
 * @since 2018/12/29
 * Description:
 */
public interface EventReceiver {

    /**
     * 接收事件
     *
     * @param event  事件类型
     */
    void onReceiveEvent(BaseChatEvent event);
}
