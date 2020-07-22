package com.fzm.chat33.core.request.chat;

import com.fzm.chat33.core.db.bean.ChatMessage;

import java.io.Serializable;
import java.util.List;

/**
 * @author zhengjy
 * @since 2019/09/03
 * Description:预转发消息信息传递
 */
public class PreForwardRequest implements Serializable {

    /**
     * 所在群的Id或好友Id
     */
    private String sourceId;
    /**
     * 1：群消息；2：好友消息
     */
    private int type;
    /**
     * 1：逐条转发; 2：合并转发
     */
    private int forwardType;
    /**
     * 消息数组
     */
    private List<ChatMessage> logArray;
    /**
     * 转发者用户昵称，即 自己的用户昵称
     */
    private String forwardUsername;

    public PreForwardRequest(String sourceId, int type, int forwardType, List<ChatMessage> logArray) {
        this.sourceId = sourceId;
        this.type = type;
        this.forwardType = forwardType;
        this.logArray = logArray;
    }

    public String getSourceId() {
        return sourceId;
    }

    public void setSourceId(String sourceId) {
        this.sourceId = sourceId;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getForwardType() {
        return forwardType;
    }

    public void setForwardType(int forwardType) {
        this.forwardType = forwardType;
    }

    public List<ChatMessage> getLogArray() {
        return logArray;
    }

    public void setLogArray(List<ChatMessage> logArray) {
        this.logArray = logArray;
    }

    public String getForwardUsername() {
        return forwardUsername;
    }

    public void setForwardUsername(String forwardUsername) {
        this.forwardUsername = forwardUsername;
    }
}
