package com.fzm.chat33.core.db.bean;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.annotation.NonNull;
import android.text.TextUtils;

import com.fzm.chat33.core.global.Chat33Const;
import com.fzm.chat33.core.global.UserInfo;

import java.io.Serializable;

/**
 * @author zhengjy
 * @since 2018/10/29
 * Description:用户信息缓存一般情况保存的是:
 * 1：不在群数据库里
 * 2：不在好友数据库里
 * 3：不在群成员数据库里
 */
@Entity(tableName = "info_cache", primaryKeys = {"id", "channelType"})
public class InfoCacheBean implements Serializable {

    private int channelType;
    @NonNull
    private String id;
    private String nickname;
    private String avatar;
    private String remark;
    /**
     * 是否是认证用户
     */
    private int identification;
    /**
     * 用户认证信息
     */
    public String identificationInfo;

    @Ignore
    public InfoCacheBean(int channelType, @NonNull String id, String nickname, String avatar, int identification) {
        this.channelType = channelType;
        this.id = id;
        this.nickname = nickname;
        this.avatar = avatar;
        this.identification = identification;
    }

    @Ignore
    public InfoCacheBean(FriendBean bean) {
        this.channelType = Chat33Const.CHANNEL_FRIEND;
        this.id = bean.getId();
        this.nickname = bean.getDisplayName();
        this.avatar = bean.getAvatar();
        this.identification = bean.getIdentification();
        this.identificationInfo = bean.getIdentificationInfo();
    }

    @Ignore
    public InfoCacheBean(RoomInfoBean bean) {
        this.channelType = Chat33Const.CHANNEL_ROOM;
        this.id = bean.getId();
        this.nickname = bean.getName();
        this.avatar = bean.getAvatar();
        this.identification = bean.getIdentification();
        this.identificationInfo = bean.getIdentificationInfo();
    }

    @Ignore
    public InfoCacheBean(RoomUserBean bean) {
        this.channelType = Chat33Const.CHANNEL_FRIEND;
        this.id = bean.getId();
        this.nickname = bean.getDisplayName();
        this.avatar = bean.getAvatar();
        this.identification = bean.getIdentification();
        this.identificationInfo = bean.getIdentificationInfo();
    }

    @Ignore
    public InfoCacheBean(UserInfo info) {
        this.channelType = Chat33Const.CHANNEL_FRIEND;
        this.id = info.id;
        this.nickname = info.username;
        this.avatar = info.avatar;
        this.identification = info.identification;
        this.identificationInfo = info.identificationInfo;
    }

    @Ignore
    public InfoCacheBean(SenderInfo info) {
        this.channelType = Chat33Const.CHANNEL_FRIEND;
        this.id = info.id;
        this.nickname = info.nickname;
        this.avatar = info.avatar;
    }

    public InfoCacheBean(int channelType, @NonNull String id, String nickname, String avatar, String remark, int identification, String identificationInfo) {
        this.channelType = channelType;
        this.id = id;
        this.nickname = nickname;
        this.avatar = avatar;
        this.remark = remark;
        this.identification = identification;
        this.identificationInfo = identificationInfo;
    }

    public String getKey() {
        return channelType + "-" + id;
    }

    public int getChannelType() {
        return channelType;
    }

    public void setChannelType(int channelType) {
        this.channelType = channelType;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNickname() {
        return nickname;
    }

    public String getDisplayName() {
        return TextUtils.isEmpty(remark) ? nickname : remark;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
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
}
