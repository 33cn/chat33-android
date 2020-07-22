package com.fzm.chat33.core.db.bean;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;
import androidx.annotation.NonNull;

import com.fzm.chat33.core.bean.GroupNotice;

import java.io.Serializable;
import java.util.List;

/**
 * @author zhengjy
 * @since 2018/10/25
 * Description:
 */
@Entity(tableName = "room_info")
public class RoomInfoBean implements Serializable {


    /**
     * id : 123
     * markId : 1231313
     * name : 群名称
     * avatar : 群头像
     * onlineNumber : 123
     * memberNumber : 123
     * noDisturbing : 1
     * canAddFriend : 1
     * joinPermission : 1
     * users : [{"id":"1123","nickname":"群聊1","roomNickname":"群聊2","avatar":"http://...../***.jpg","memberLevel":1}]
     */
    @PrimaryKey
    @NonNull
    private String id;
    private String markId;
    private String name;
    private String avatar;
    private int isMember;
    private int onlineNumber;
    private int memberNumber;
    private int recordPermission;
    private int noDisturbing;
    private int onTop;
    /**
     * 用户是否认证
     */
    private int identification;
    /**
     * 用户认证信息
     */
    public String identificationInfo;
    // 是否是加密群，1：是  2：不是
    private int encrypt;
    private int memberLevel;
    private int canAddFriend;
    private int joinPermission;
    private String roomNickname;
    private int managerNumber;
    private int mutedNumber;
    private int roomMutedType;//1 全员发言 2黑名单 3 白名单 4 全员禁言
    private int mutedType;//1 不采用 2 黑名单 3 白名单
    // 禁言截至时间
    private long deadline;
    // 封群截至时间
    private long disableDeadline;
    @Ignore
    private GroupNotice.Wrapper systemMsg;
    @Ignore
    private List<RoomUserBean> users;

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

    public int getIsMember() {
        return isMember;
    }

    public void setIsMember(int isMember) {
        this.isMember = isMember;
    }

    public int getOnlineNumber() {
        return onlineNumber;
    }

    public void setOnlineNumber(int onlineNumber) {
        this.onlineNumber = onlineNumber;
    }

    public int getMemberNumber() {
        return memberNumber;
    }

    public void setMemberNumber(int memberNumber) {
        this.memberNumber = memberNumber;
    }

    public int getRecordPermission() {
        return recordPermission;
    }

    public void setRecordPermission(int recordPermission) {
        this.recordPermission = recordPermission;
    }

    public int getNoDisturbing() {
        return noDisturbing;
    }

    public void setNoDisturbing(int noDisturbing) {
        this.noDisturbing = noDisturbing;
    }

    public int getOnTop() {
        return onTop;
    }

    public void setOnTop(int onTop) {
        this.onTop = onTop;
    }

    public boolean isIdentified() {
        return identification == 1;
    }

    public int getIdentification() {
        return identification;
    }

    public void setIdentification(int identification) {
        this.identification = identification;
    }

    public String getIdentificationInfo() {
        return identificationInfo;
    }

    public void setIdentificationInfo(String identificationInfo) {
        this.identificationInfo = identificationInfo;
    }

    public int getEncrypt() {
        return encrypt;
    }

    public void setEncrypt(int encrypt) {
        this.encrypt = encrypt;
    }

    public int getMemberLevel() {
        return memberLevel;
    }

    public void setMemberLevel(int memberLevel) {
        this.memberLevel = memberLevel;
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

    public String getRoomNickname() {
        return roomNickname;
    }

    public void setRoomNickname(String roomNickname) {
        this.roomNickname = roomNickname;
    }

    public int getManagerNumber() {
        return managerNumber;
    }

    public void setManagerNumber(int managerNumber) {
        this.managerNumber = managerNumber;
    }

    public void setJoinPermission(int joinPermission) {
        this.joinPermission = joinPermission;
    }

    public int getRoomMutedType() {
        return roomMutedType;
    }

    public void setRoomMutedType(int roomMutedType) {
        this.roomMutedType = roomMutedType;
    }

    public int getMutedType() {
        return mutedType;
    }

    public void setMutedType(int mutedType) {
        this.mutedType = mutedType;
    }

    public long getDeadline() {
        return deadline;
    }

    public void setDeadline(long deadline) {
        this.deadline = deadline;
    }

    public long getDisableDeadline() {
        return disableDeadline;
    }

    public void setDisableDeadline(long disableDeadline) {
        this.disableDeadline = disableDeadline;
    }

    public int getMutedNumber() {
        return mutedNumber;
    }

    public void setMutedNumber(int mutedNumber) {
        this.mutedNumber = mutedNumber;
    }

    public GroupNotice.Wrapper getSystemMsg() {
        return systemMsg;
    }

    public void setSystemMsg(GroupNotice.Wrapper systemMsg) {
        this.systemMsg = systemMsg;
    }

    public List<RoomUserBean> getUsers() {
        return users;
    }

    public void setUsers(List<RoomUserBean> users) {
        this.users = users;
    }
}
