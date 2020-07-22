package com.fzm.chat33.core.bean;

import java.io.Serializable;
import java.util.List;

/**
 * @author zhengjy
 * @since 2018/11/29
 * Description:
 */
public class GroupNotice implements Serializable {
    /**
     * logId : 1123
     * content : 群聊1
     * datetime : 213123123
     */

    private String logId;
    private String senderName;
    private String content;
    private long datetime;

    public String getLogId() {
        return logId;
    }

    public void setLogId(String logId) {
        this.logId = logId;
    }

    public String getSenderName() {
        return senderName;
    }

    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public long getDatetime() {
        return datetime;
    }

    public void setDatetime(long datetime) {
        this.datetime = datetime;
    }

    public static class Wrapper implements Serializable {
        public int number;
        public List<GroupNotice> list;
        public String nextLog;
    }
}
