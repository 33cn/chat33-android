package com.fzm.chat33.core.bean;

import java.io.Serializable;
import java.util.List;

/**
 * @author zhengjy
 * @since 2018/10/19
 * Description:
 */
public class ApplyInfoBean implements Serializable {
    /**
     * senderInfo : {"id":"1123","name":"用户1","avatar":"http://...../***.jpg","position":"产品"}
     * receiveInfo : {"id":"1123","name":"用户1","avatar":"http://...../***.jpg","position":"产品"}
     * id : 123
     * type : 1(1 群 2 好友)
     * applyReason : 申请理由
     * status : 1(1:等待验证 2:已拒绝 3:已同意)
     * datetime : 1676764266167
     */

    private InfoBean senderInfo;
    private InfoBean receiveInfo;
    private String id;
    private int type;
    private String applyReason;
    private int status;
    private long datetime;
    private String source;

    public InfoBean getSenderInfo() {
        return senderInfo;
    }

    public void setSenderInfo(InfoBean senderInfo) {
        this.senderInfo = senderInfo;
    }

    public InfoBean getReceiveInfo() {
        return receiveInfo;
    }

    public void setReceiveInfo(InfoBean receiveInfo) {
        this.receiveInfo = receiveInfo;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getApplyReason() {
        return applyReason;
    }

    public void setApplyReason(String applyReason) {
        this.applyReason = applyReason;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public long getDatetime() {
        return datetime;
    }

    public void setDatetime(long datetime) {
        this.datetime = datetime;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public static class InfoBean implements Serializable {
        /**
         * id : 1123
         * name : 用户1
         * avatar : http://...../***.jpg
         * position : 产品
         */

        private String id;
        private String markId;
        private String name;
        private String avatar;
        private String position;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getMarkId() {
            return markId;
        }

        public void setMarkId(String markId) {
            this.markId = markId;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getAvatar() {
            return avatar;
        }

        public void setAvatar(String avatar) {
            this.avatar = avatar;
        }

        public String getPosition() {
            return position;
        }

        public void setPosition(String position) {
            this.position = position;
        }
    }

    public static class Wrapper implements Serializable {
        public List<ApplyInfoBean> applyList;
        public String nextId;
        public int totalNumber;
        // 显示判断使用
        public transient boolean clearData;
    }
}
