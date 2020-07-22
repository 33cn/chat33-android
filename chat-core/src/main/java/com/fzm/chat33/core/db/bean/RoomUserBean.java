package com.fzm.chat33.core.db.bean;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.text.TextUtils;

import com.fzm.chat33.core.utils.PinyinUtils;

import java.io.Serializable;
import java.util.List;

/**
 * @author zhengjy
 * @since 2018/10/25
 * Description:群成员bean
 */
@Entity(
        tableName = "room_user",
        primaryKeys = {"roomId", "id"}
)
public class RoomUserBean implements Serializable, Sortable {
    /**
     * id : 1123
     * nickname : 群聊1
     * roomNickname : 群聊2
     * avatar : http://...../***.jpg
     * memberLevel : 1
     */

    @NonNull
    public String roomId;
    @NonNull
    private String id;
    private String nickname;
    private String roomNickname;
    private String avatar;
    private int memberLevel;
    private int roomMutedType;//1 全员发言 2黑名单 3 白名单 4 全员禁言
    private int mutedType;//1 不采用 2 黑名单 3 白名单
    private long deadline;
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

    // 0:无操作  1:邀请  2:移除
    @Ignore
    public int operation;
    @Ignore
    public String letter;
    private String searchKey;

    public RoomUserBean() {

    }

    public RoomUserBean(int operation) {
        this.operation = operation;
    }

    @NonNull
    public String getId() {
        return id;
    }

    public void setId(@NonNull String id) {
        this.id = id;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        letter = null;
        this.nickname = nickname;
    }

    public String getRoomNickname() {
        return roomNickname;
    }

    public void setRoomNickname(String roomNickname) {
        letter = null;
        this.roomNickname = roomNickname;
    }

    public String getDisplayName() {
        return TextUtils.isEmpty(roomNickname) ? nickname : roomNickname;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public int getMemberLevel() {
        return memberLevel;
    }

    public void setMemberLevel(int memberLevel) {
        this.memberLevel = memberLevel;
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

    public String getSearchKey() {
        String displayName = TextUtils.isEmpty(roomNickname) ? nickname : roomNickname;
        if(!PinyinUtils.isContainChinese(displayName)) return displayName;
        StringBuffer firstSpell = new StringBuffer();
        StringBuffer pingYin = new StringBuffer();
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
        if (!TextUtils.isEmpty(roomNickname)) {
            first = roomNickname.substring(0, 1);
        } else if (!TextUtils.isEmpty(nickname)) {
            first = nickname.substring(0, 1);
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
        if (!TextUtils.isEmpty(roomNickname)) {
            pinyin = PinyinUtils.getPingYin(roomNickname);
        } else if (!TextUtils.isEmpty(nickname)) {
            pinyin = PinyinUtils.getPingYin(nickname);
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
        return memberLevel;
    }

    public static class Wrapper implements Serializable {
        public List<RoomUserBean> userList;
    }
}
