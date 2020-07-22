package com.fzm.chat33.core.request.chat;

import com.fzm.chat33.core.db.bean.ChatMessage;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * @author zhengjy
 * @since 2018/12/26
 * Description:转发消息请求
 */
public class ForwardRequest implements Serializable {

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
     * 消息id数组
     */
    private List<String> logArray;
    /**
     * 要发送的好友id数组
     */
    private List<String> targetUsers;
    /**
     * 要发送的群id数组
     */
    private List<String> targetRooms;

    public ForwardRequest(String sourceId, int type, int forwardType, List<String> logArray) {
        this.sourceId = sourceId;
        this.type = type;
        this.forwardType = forwardType;
        this.logArray = logArray;
    }

    public ForwardRequest(PreForwardRequest request) {
        this.sourceId = request.getSourceId();
        this.type = request.getType();
        this.forwardType = request.getForwardType();
        this.logArray = new ArrayList<>();
        for (ChatMessage msg : request.getLogArray()) {
            this.logArray.add(msg.logId);
        }
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

    public List<String> getLogArray() {
        return logArray;
    }

    public void setLogArray(List<String> logArray) {
        this.logArray = logArray;
    }

    public List<String> getTargetUsers() {
        return targetUsers;
    }

    public void setTargetUsers(List<String> targetUsers) {
        this.targetUsers = targetUsers;
    }

    public List<String> getTargetRooms() {
        return targetRooms;
    }

    public void setTargetRooms(List<String> targetRooms) {
        this.targetRooms = targetRooms;
    }
}
