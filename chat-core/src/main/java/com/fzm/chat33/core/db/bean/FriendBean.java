package com.fzm.chat33.core.db.bean;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.text.TextUtils;

import com.fzm.chat33.core.global.Chat33Const;
import com.fzm.chat33.core.utils.PinyinUtils;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;

/**
 * @author zhengjy
 * @since 2018/10/17
 * Description:用户信息，好友类
 */
@Entity(tableName = "friends")
public class FriendBean implements Contact, Serializable {

    @PrimaryKey
    @NonNull
    private String id;
    private String name;
    private String sex;
    private String avatar;
    private String position;
    private int needConfirm;
    private int needAnswer;
    private String question;

    // 仅好友可看
    @SerializedName(value = "noDisturbing", alternate = {"DND"})
    private int noDisturbing;
    @SerializedName(value = "onTop", alternate = {"stickyOnTop"})
    private int onTop;
    // 1：开启加密 2：关闭加密
    @Deprecated
    private int encrypt;
    private long addTime;
    private String remark;
    private String source;
    private int isDelete;
    private int isBlocked;
    private int isFriend;
    private String mark_id;
    private int commonlyUsed;
    private String depositAddress;
    /**
     * 用户是否认证
     */
    private int identification;
    /**
     * 用户认证信息
     */
    public String identificationInfo;
    // 加密聊天公钥
    @Nullable
    private String publicKey;

    @ColumnInfo(name = "extRemark", typeAffinity = ColumnInfo.TEXT)
    private ExtRemark extRemark;

    // 以下部分仅管理员查看时返回
    @Ignore
    private List<String> tags;
    private String com_id;
    /**
     * uid即用户的地址
     * 用户的地址即uid
     */
    @SerializedName("uid")
    @ColumnInfo(name = "uid")
    private String address;
    private String account;
    private String username;
    private String phone;
    private String email;
    private int verified;
    private String user_level;
    @Deprecated
    private String description;
    @Ignore
    public String letter;
    private String searchKey;

    public FriendBean(@NonNull String id, String name, String avatar) {
        this.id = id;
        this.name = name;
        this.avatar = avatar;
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

    public int getNeedConfirm() {
        return needConfirm;
    }

    public void setNeedConfirm(int needConfirm) {
        this.needConfirm = needConfirm;
    }

    public int getNeedAnswer() {
        return needAnswer;
    }

    public void setNeedAnswer(int needAnswer) {
        this.needAnswer = needAnswer;
    }

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    @Override
    public String getDisplayName() {
        return TextUtils.isEmpty(remark) ? name : remark;
    }

    @Override
    public int channelType() {
        return Chat33Const.CHANNEL_FRIEND;
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

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        letter = null;
        this.remark = remark;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }


    public int getIsFriend() {
        return isFriend;
    }

    public void setIsFriend(int isFriend) {
        this.isFriend = isFriend;
    }

    public String getMark_id() {
        return mark_id;
    }

    public void setMark_id(String mark_id) {
        this.mark_id = mark_id;
    }

    public void setNoDisturbing(int noDisturbing) {
        this.noDisturbing = noDisturbing;
    }

    public int getNoDisturbing() {
        return noDisturbing;
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

    @Deprecated
    public int getEncrypt() {
        return encrypt;
    }

    @Deprecated
    public void setEncrypt(int encrypt) {
        this.encrypt = encrypt;
    }

    public ExtRemark getExtRemark() {
        return extRemark;
    }

    public void setExtRemark(ExtRemark extRemark) {
        this.extRemark = extRemark;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
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

    public String getPublicKey() {
        return publicKey;
    }

    public void setPublicKey(String publicKey) {
        this.publicKey = publicKey;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        letter = null;
        this.username = username;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public int getVerified() {
        return verified;
    }

    public void setVerified(int verified) {
        this.verified = verified;
    }

    @Deprecated
    public String getDescription() {
        return description;
    }

    @Deprecated
    public void setDescription(String description) {
        this.description = description;
    }

    public String getCom_id() {
        return com_id;
    }

    public void setCom_id(String com_id) {
        this.com_id = com_id;
    }

    public String getSex() {
        return sex;
    }

    public void setSex(String sex) {
        this.sex = sex;
    }

    public int getIsDelete() {
        return isDelete;
    }

    public void setIsDelete(int isDelete) {
        this.isDelete = isDelete;
    }

    public int getIsBlocked() {
        return isBlocked;
    }

    public void setIsBlocked(int isBlocked) {
        this.isBlocked = isBlocked;
    }

    public long getAddTime() {
        return addTime;
    }

    public void setAddTime(long addTime) {
        this.addTime = addTime;
    }

    public String getUser_level() {
        return user_level;
    }

    public void setUser_level(String user_level) {
        this.user_level = user_level;
    }

    public boolean isBlocked() {
        return isBlocked == 1;
    }

    public String getSearchKey() {
        String displayName = TextUtils.isEmpty(remark) ? name : remark;
        if(!PinyinUtils.isContainChinese(displayName)) return displayName;
        StringBuilder firstSpell = new StringBuilder();
        StringBuilder pingYin = new StringBuilder();
        if(!TextUtils.isEmpty(displayName)) {
            int index = 0;
            while (index < displayName.length()) {
                firstSpell.append(" ");
                pingYin.append(" ");
                String subName = displayName.substring(index);
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
    public String getFirstChar() {
        String first;
        if (!TextUtils.isEmpty(remark)) {
            first = remark.substring(0, 1);
        } else if (!TextUtils.isEmpty(name)) {
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
        if (!TextUtils.isEmpty(remark)) {
            pinyin = PinyinUtils.getPingYin(remark);
        } else if (!TextUtils.isEmpty(name)) {
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        FriendBean that = (FriendBean) o;

        if (needConfirm != that.needConfirm) return false;
        if (needAnswer != that.needAnswer) return false;
        if (noDisturbing != that.noDisturbing) return false;
        if (onTop != that.onTop) return false;
        if (encrypt != that.encrypt) return false;
        if (addTime != that.addTime) return false;
        if (isDelete != that.isDelete) return false;
        if (isBlocked != that.isBlocked) return false;
        if (isFriend != that.isFriend) return false;
        if (commonlyUsed != that.commonlyUsed) return false;
        if (identification != that.identification) return false;
        if (verified != that.verified) return false;
        if (!id.equals(that.id)) return false;
        if (!Objects.equals(name, that.name)) return false;
        if (!Objects.equals(sex, that.sex)) return false;
        if (!Objects.equals(avatar, that.avatar)) return false;
        if (!Objects.equals(position, that.position)) return false;
        if (!Objects.equals(question, that.question)) return false;
        if (!Objects.equals(remark, that.remark)) return false;
        if (!Objects.equals(source, that.source)) return false;
        if (!Objects.equals(mark_id, that.mark_id)) return false;
        if (!Objects.equals(depositAddress, that.depositAddress)) return false;
        if (!Objects.equals(identificationInfo, that.identificationInfo)) return false;
        if (!Objects.equals(publicKey, that.publicKey)) return false;
        if (!Objects.equals(extRemark, that.extRemark)) return false;
        if (!Objects.equals(tags, that.tags)) return false;
        if (!Objects.equals(com_id, that.com_id)) return false;
        if (!Objects.equals(address, that.address)) return false;
        if (!Objects.equals(account, that.account)) return false;
        if (!Objects.equals(username, that.username)) return false;
        if (!Objects.equals(phone, that.phone)) return false;
        if (!Objects.equals(email, that.email)) return false;
        if (!Objects.equals(user_level, that.user_level)) return false;
        if (!Objects.equals(description, that.description)) return false;
        if (!Objects.equals(letter, that.letter)) return false;
        return Objects.equals(searchKey, that.searchKey);
    }

    @Override
    public int hashCode() {
        int result = id.hashCode();
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (sex != null ? sex.hashCode() : 0);
        result = 31 * result + (avatar != null ? avatar.hashCode() : 0);
        result = 31 * result + (position != null ? position.hashCode() : 0);
        result = 31 * result + needConfirm;
        result = 31 * result + needAnswer;
        result = 31 * result + (question != null ? question.hashCode() : 0);
        result = 31 * result + noDisturbing;
        result = 31 * result + onTop;
        result = 31 * result + encrypt;
        result = 31 * result + (int) (addTime ^ (addTime >>> 32));
        result = 31 * result + (remark != null ? remark.hashCode() : 0);
        result = 31 * result + (source != null ? source.hashCode() : 0);
        result = 31 * result + isDelete;
        result = 31 * result + isBlocked;
        result = 31 * result + isFriend;
        result = 31 * result + (mark_id != null ? mark_id.hashCode() : 0);
        result = 31 * result + commonlyUsed;
        result = 31 * result + (depositAddress != null ? depositAddress.hashCode() : 0);
        result = 31 * result + identification;
        result = 31 * result + (identificationInfo != null ? identificationInfo.hashCode() : 0);
        result = 31 * result + (publicKey != null ? publicKey.hashCode() : 0);
        result = 31 * result + (extRemark != null ? extRemark.hashCode() : 0);
        result = 31 * result + (tags != null ? tags.hashCode() : 0);
        result = 31 * result + (com_id != null ? com_id.hashCode() : 0);
        result = 31 * result + (address != null ? address.hashCode() : 0);
        result = 31 * result + (account != null ? account.hashCode() : 0);
        result = 31 * result + (username != null ? username.hashCode() : 0);
        result = 31 * result + (phone != null ? phone.hashCode() : 0);
        result = 31 * result + (email != null ? email.hashCode() : 0);
        result = 31 * result + verified;
        result = 31 * result + (user_level != null ? user_level.hashCode() : 0);
        result = 31 * result + (description != null ? description.hashCode() : 0);
        result = 31 * result + (letter != null ? letter.hashCode() : 0);
        result = 31 * result + (searchKey != null ? searchKey.hashCode() : 0);
        return result;
    }

    public static class Wrapper implements Serializable {
        public List<FriendBean> userList;
    }
}
