package com.fzm.chat33.core.request.chat;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * @author zhengjy
 * @since 2019/09/03
 * Description:客户端转发加密消息请求
 */
public class EncryptForwardRequest implements Serializable {

    /**
     * 转发到群体消息数组
     */
    public List<TargetMessage> roomLogs;
    /**
     * 转发到个人消息数组
     */
    public List<TargetMessage> userLogs;
    /**
     * 1：逐条转发; 2：合并转发
     */
    public int type;

    public EncryptForwardRequest(int type) {
        this.type = type;
        roomLogs = new ArrayList<>();
        userLogs = new ArrayList<>();
    }

    public static class TargetMessage implements Serializable {
        public String targetId;
        public List<Message> messages;

        public TargetMessage(String targetId, List<Message> messages) {
            this.targetId = targetId;
            this.messages = messages;
        }
    }

    public static class Message implements Serializable {
        public Object msg;
        public int msgType;

        public Message(int msgType) {
            this.msgType = msgType;
        }
    }
}
