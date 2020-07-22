package com.fzm.chat33.core.bean;

import com.fzm.chat33.core.db.bean.RoomInfoBean;
import com.fzm.chat33.core.db.bean.FriendBean;

import java.io.Serializable;

/**
 * @author zhengjy
 * @since 2018/10/25
 * Description:
 */
public class UidSearchBean implements Serializable {
    /**
     * type : 1
     * roomInfo : {"id":"123","name":"群名称","avatar":"群头像","canAddFriend":1,"joinPermission":1}
     * userInfo : {"id":"1123","name":"用户1","avatar":"http://...../***.jpg","position":"产品","remark":"好友备注，不是好友为空"}
     */

    //1：群 2：用户
    private int type;
    private RoomInfoBean roomInfo;
    private FriendBean userInfo;

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public RoomInfoBean getRoomInfo() {
        return roomInfo;
    }

    public void setRoomInfo(RoomInfoBean roomInfo) {
        this.roomInfo = roomInfo;
    }

    public FriendBean getUserInfo() {
        return userInfo;
    }

    public void setUserInfo(FriendBean userInfo) {
        this.userInfo = userInfo;
    }
}
