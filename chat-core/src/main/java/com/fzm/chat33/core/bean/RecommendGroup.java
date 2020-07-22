package com.fzm.chat33.core.bean;

import java.io.Serializable;
import java.util.List;

/**
 * @author zhengjy
 * @since 2019/07/01
 * Description:推荐群
 */
public class RecommendGroup implements Serializable {

    private String id;
    private String markId;
    private String name;
    private String avatar;
    private String masterId;
    private int canAddFriend;
    private int joinPermission;
    private int recordPermission;
    private int encrypt;
    /**
     * 是否被选中
     */
    public boolean selected = false;

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

    public String getMasterId() {
        return masterId;
    }

    public void setMasterId(String masterId) {
        this.masterId = masterId;
    }

    public int getCanAddFriend() {
        return canAddFriend;
    }

    public void setCanAddFriend(int canAddFriend) {
        this.canAddFriend = canAddFriend;
    }

    public int getJoinPermission() {
        return joinPermission;
    }

    public void setJoinPermission(int joinPermission) {
        this.joinPermission = joinPermission;
    }

    public int getRecordPermission() {
        return recordPermission;
    }

    public void setRecordPermission(int recordPermission) {
        this.recordPermission = recordPermission;
    }

    public int getEncrypt() {
        return encrypt;
    }

    public void setEncrypt(int encrypt) {
        this.encrypt = encrypt;
    }

    public static class Wrapper implements Serializable {
        public int nextTimes;
        public List<RecommendGroup> roomList;
    }
}
