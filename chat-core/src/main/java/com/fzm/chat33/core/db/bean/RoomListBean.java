package com.fzm.chat33.core.db.bean;

import androidx.annotation.NonNull;
import android.text.TextUtils;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import com.fzm.chat33.core.global.Chat33Const;
import com.fzm.chat33.core.utils.PinyinUtils;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;

/**
 * @author zhengjy
 * @since 2018/10/19
 * Description:聊天群组列表
 */
@Entity(tableName = "room_list")
public class RoomListBean implements Contact, Serializable {

    /**
     * id : 1123
     * name : 群聊1
     * avatar : http://...../***.jpg
     * noDisturbing : 1          //1：开启了免打扰，2：关闭
     * commonlyUsed : 1          //1：普通 2 常用
     * onTop : 1                 //1：置顶 2 不置顶
     */

    @PrimaryKey
    @NonNull
    private String id;
    private String name;
    private String avatar;
    private int noDisturbing;
    private int commonlyUsed;
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
    private long disableDeadline;
    @Ignore
    public String letter;
    private String searchKey;

    @Ignore
    public RoomListBean(@NonNull String id, String name, String avatar, int identification, String identificationInfo) {
        this.id = id;
        this.name = name;
        this.avatar = avatar;
        this.identification = identification;
        this.identificationInfo = identificationInfo;
    }

    @Ignore
    public RoomListBean(RoomInfoBean bean) {
        this.id = bean.getId();
        this.name = bean.getName();
        this.avatar = bean.getAvatar();
        this.noDisturbing = bean.getNoDisturbing();
        this.onTop = bean.getOnTop();
        this.encrypt = bean.getEncrypt();
        this.identification = bean.getIdentification();
    }

    public RoomListBean(@NonNull String id, String name, String avatar, int noDisturbing, int onTop, int encrypt, int identification) {
        this.id = id;
        this.name = name;
        this.avatar = avatar;
        this.noDisturbing = noDisturbing;
        this.onTop = onTop;
        this.encrypt = encrypt;
        this.identification = identification;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        letter = null;
        this.name = name;
    }

    @Override
    public String getAvatar() {
        return avatar;
    }

    @Override
    public String getDisplayName() {
        return name;
    }

    @Override
    public int channelType() {
        return Chat33Const.CHANNEL_ROOM;
    }

    @Override
    public String getKey() {
        return channelType() + "-" + id;
    }

    @Override
    public String getDepositAddress() {
        return null;
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

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public int getNoDisturbing() {
        return noDisturbing;
    }

    public void setNoDisturbing(int noDisturbing) {
        this.noDisturbing = noDisturbing;
    }

    @Override
    public boolean isNoDisturb() {
        return noDisturbing == 1;
    }

    @Override
    public boolean isStickyTop() {
        return onTop == 1;
    }

    public int getCommonlyUsed() {
        return commonlyUsed;
    }

    public void setCommonlyUsed(int commonlyUsed) {
        this.commonlyUsed = commonlyUsed;
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

    public long getDisableDeadline() {
        return disableDeadline;
    }

    public void setDisableDeadline(long disableDeadline) {
        this.disableDeadline = disableDeadline;
    }

    public String getSearchKey() {
        if(!PinyinUtils.isContainChinese(name)) return name;
        StringBuilder firstSpell = new StringBuilder();
        StringBuilder pingYin = new StringBuilder();
        if(!TextUtils.isEmpty(name)) {
            int index = 0;
            while (index < name.length()) {
                firstSpell.append(" ");
                pingYin.append(" ");
                String subName = name.substring(index);
                for(int i = 0; i < subName.length(); i++) {
                    String charPingYin = PinyinUtils.getCharPingYin(subName.charAt(i));
                    if(!TextUtils.equals("#", charPingYin)) {
                        firstSpell.append(charPingYin.substring(0, 1));
                        pingYin.append(charPingYin);
                    }
                }
                index ++;
            }

        }
        return firstSpell.append(pingYin).toString();
    }

    public void setSearchKey(String searchKey) {
        this.searchKey = searchKey;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        RoomListBean that = (RoomListBean) o;

        if (noDisturbing != that.noDisturbing) return false;
        if (commonlyUsed != that.commonlyUsed) return false;
        if (onTop != that.onTop) return false;
        if (identification != that.identification) return false;
        if (encrypt != that.encrypt) return false;
        if (disableDeadline != that.disableDeadline) return false;
        if (!id.equals(that.id)) return false;
        if (!Objects.equals(name, that.name)) return false;
        if (!Objects.equals(avatar, that.avatar)) return false;
        if (!Objects.equals(identificationInfo, that.identificationInfo))
            return false;
        if (!Objects.equals(letter, that.letter)) return false;
        return Objects.equals(searchKey, that.searchKey);
    }

    @Override
    public int hashCode() {
        int result = id.hashCode();
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (avatar != null ? avatar.hashCode() : 0);
        result = 31 * result + noDisturbing;
        result = 31 * result + commonlyUsed;
        result = 31 * result + onTop;
        result = 31 * result + identification;
        result = 31 * result + (identificationInfo != null ? identificationInfo.hashCode() : 0);
        result = 31 * result + encrypt;
        result = 31 * result + (int) (disableDeadline ^ (disableDeadline >>> 32));
        result = 31 * result + (letter != null ? letter.hashCode() : 0);
        result = 31 * result + (searchKey != null ? searchKey.hashCode() : 0);
        return result;
    }

    public static class Wrapper {
        public List<RoomListBean> roomList;
    }
}
