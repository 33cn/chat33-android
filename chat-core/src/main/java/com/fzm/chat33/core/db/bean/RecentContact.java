package com.fzm.chat33.core.db.bean;

import android.text.TextUtils;

import androidx.room.Ignore;

import com.fzm.chat33.core.utils.PinyinUtils;

import java.io.Serializable;

/**
 * 仅查询出最近联系人相关字段
 */
public class RecentContact implements Contact, Serializable {
    private String id;
    private int channelType;
    private String name;
    private String remark;
    private String avatar;
    /**
     * 用户公钥，只有好友才有
     */
    private String depositAddress;
    private int noDisturb;
    private int stickyTop;
    /**
     * 用户认证信息(非实名认证)
     */
    private String identificationInfo;
    /**
     * 最近一条消息时间
     */
    private long datetime;
    @Ignore
    public String letter;

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public String getId() {
        return id;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    @Override
    public String getAvatar() {
        return avatar;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public String getRemark() {
        return remark;
    }

    @Override
    public String getDisplayName() {
        return TextUtils.isEmpty(remark) ? name : remark;
    }

    public void setChannelType(int channelType) {
        this.channelType = channelType;
    }

    @Override
    public int channelType() {
        return channelType;
    }

    @Override
    public String getKey() {
        return channelType() + "-" + id;
    }

    public void setDepositAddress(String depositAddress) {
        this.depositAddress = depositAddress;
    }

    @Override
    public String getDepositAddress() {
        return depositAddress;
    }

    @Override
    public String getFirstChar() {
        String first;
        if (!TextUtils.isEmpty(name)) {
            first = name.substring(0, 1);
        } else {
            first = "#";
        }
        return first;
    }

    @Override
    public String getFirstLetter() {
        return getLetters().substring(0, 1);
    }

    @Override
    public String getLetters() {
        if (letter != null) {
            return letter;
        }
        //汉字转换成拼音
        String pinyin;
        if (!TextUtils.isEmpty(name)) {
            pinyin = PinyinUtils.getPingYin(name);
        } else {
            pinyin = "#";
        }
        String sortString = pinyin.substring(0, 1).toUpperCase();
        if (sortString.matches("[A-Z]")) {
            letter = pinyin.toUpperCase();
            return letter;
        } else {
            letter = "#";
            return letter;
        }
    }

    @Override
    public int priority() {
        return 0;
    }

    public void setNoDisturb(int noDisturb) {
        this.noDisturb = noDisturb;
    }

    public int getNoDisturb() {
        return noDisturb == 0 ? 2 : noDisturb;
    }

    @Override
    public boolean isNoDisturb() {
        return noDisturb == 1;
    }

    @Override
    public boolean isStickyTop() {
        return stickyTop == 1;
    }

    public int getStickyTop() {
        return stickyTop == 0 ? 2 : stickyTop;
    }

    public void setStickyTop(int stickyTop) {
        this.stickyTop = stickyTop;
    }

    @Override
    public String  getIdentificationInfo() {
        return identificationInfo;
    }

    public void setIdentificationInfo(String identificationInfo) {
        this.identificationInfo = identificationInfo;
    }

    public boolean isIdentified() {
        return !TextUtils.isEmpty(identificationInfo);
    }

    public void setDatetime(long datetime) {
        this.datetime = datetime;
    }

    public long getDatetime() {
        return datetime;
    }
}
