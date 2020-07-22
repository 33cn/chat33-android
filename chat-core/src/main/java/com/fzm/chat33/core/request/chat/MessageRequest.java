package com.fzm.chat33.core.request.chat;

import com.fzm.chat33.core.request.BaseRequest;

/**
 * @author zhengjy
 * @since 2018/7/27
 * Description:消息发送请求
 */
public class MessageRequest extends BaseRequest {

    /**
     * 是否为阅后即焚消息
     * 1是 2否
     */
    public int isSnap;
    /**
     * 消息类型，0为一般的聊天消息，其他是事件通知
     */
    public int eventType;
    /**
     * 本地发送消息时生成的随机id
     */
    public String msgId;
    /**
     * 消息发送对象
     * @see com.fzm.chat33.core.global.Chat33Const#CHANNEL_ROOM
     * @see com.fzm.chat33.core.global.Chat33Const#CHANNEL_FRIEND
     */
    public int channelType;
    /**
     * 发送目标的id
     */
    public String targetId;
    /**
     * 消息类型
     * @see com.fzm.chat33.core.db.bean.ChatMessage.Type
     */
    public int msgType;
    /**
     * 消息体
     */
    public Object msg;
}
