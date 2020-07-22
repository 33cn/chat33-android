package com.fzm.chat33.core.bean;

import java.io.Serializable;
import java.util.List;

/**
 * @author zhengjy
 * @since 2018/10/17
 * Description:聊天室列表
 */
public class ChatGroupBean implements Serializable {
    /**
     * groupId : 1
     * groupName : 说正事专用群10
     * createTime : 1533643540345
     * avatar : http://zb-chat.oss-cn-shanghai.aliyuncs.com/manage/1001/group_img/1536977783213.png
     * description :
     * openTime : 0
     * closeTime : 0
     * status : 1
     * totalNumber : 0
     * userNumber : 0
     * visitorNumber : 0
     */

    private String groupId;
    private String groupName;
    private long createTime;
    private String avatar;
    private String description;
    private long openTime;
    private long closeTime;
    private int status;
    private int totalNumber;
    private int userNumber;
    private int visitorNumber;

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(long createTime) {
        this.createTime = createTime;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public long getOpenTime() {
        return openTime;
    }

    public void setOpenTime(long openTime) {
        this.openTime = openTime;
    }

    public long getCloseTime() {
        return closeTime;
    }

    public void setCloseTime(long closeTime) {
        this.closeTime = closeTime;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public int getTotalNumber() {
        return totalNumber;
    }

    public void setTotalNumber(int totalNumber) {
        this.totalNumber = totalNumber;
    }

    public int getUserNumber() {
        return userNumber;
    }

    public void setUserNumber(int userNumber) {
        this.userNumber = userNumber;
    }

    public int getVisitorNumber() {
        return visitorNumber;
    }

    public void setVisitorNumber(int visitorNumber) {
        this.visitorNumber = visitorNumber;
    }

    public static class Wrapper implements Serializable {
        public List<ChatGroupBean> groups;
    }
}
