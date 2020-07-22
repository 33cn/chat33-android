package com.fzm.chat33.core.global;

import androidx.room.Entity;
import androidx.annotation.NonNull;
import androidx.room.Ignore;

import android.text.TextUtils;

import com.fuzamei.common.utils.RoomUtils;
import com.fuzamei.componentservice.config.AppConfig;
import com.fuzamei.componentservice.config.AppPreference;
import com.fzm.chat33.core.db.ChatDatabase;
import com.fzm.chat33.core.logic.MessageDispatcher;
import com.fzm.chat33.core.utils.UserInfoPreference;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.Objects;

/**
 * @author zhengjy
 * @since 2018/10/22
 * Description:登录用户信息
 */
@Entity(tableName = "user_info", primaryKeys = {"id"})
public class UserInfo implements Serializable, Cloneable {

    public String account;
    public String avatar;
    @NonNull
    public String id;
    public String uid;
    public String phone;
    /**
     * 是否设置支付密码 0:否，1：是
     */
    public int isSetPayPwd;
    /**
     * 0:游客，1：普通用户，2：客服
     */
    @Deprecated
    public int user_level;
    /**
     * 昵称
     */
    @SerializedName(value = "username", alternate = {"name"})
    public String username;
    /**
     * 是否实名 0:否，1：是
      */
    public int verified;
    public String token;
    public String position;
    /**
     * 通讯录是否已上链
     */
    public boolean isChain;
    public boolean firstLogin;
    public String depositAddress;
    /**
     * 用户密聊公钥
     */
    public String publicKey;
    /**
     * 用户是否认证
     */
    public int identification;
    /**
     * 用户认证信息
     */
    public String identificationInfo;
    /**
     * 邀请码
     */
    public String code;
    /**
     * 加密后的助记词
     */
    @Ignore
    public String privateKey;


    public static final UserInfo EMPTY_USER = new UserInfo();

    public UserInfo() {

    }

    private static class Holder {
        private static UserInfo sInstance = new UserInfo();
    }

    public static UserInfo getInstance() {
        return Holder.sInstance;
    }

    public String appendCode(String source) {
        return appendCode(source, false);
    }

    public String appendCode(String source, boolean first) {
        if (!TextUtils.isEmpty(code)) {
            if (first) {
                return source + "?code=" + code;
            } else {
                return source + "&code=" + code;
            }
        } else {
            return source;
        }
    }

    /**
     * app启动后第一次设置用户信息
     *
     * @param userInfo
     */
    public void setUserInfo0(UserInfo userInfo) {
        this.account = userInfo.account;
        this.avatar = userInfo.avatar;
        this.id = userInfo.id;
        this.uid = userInfo.uid;
        this.phone = userInfo.phone;
        this.isSetPayPwd = userInfo.isSetPayPwd;
        this.user_level = userInfo.user_level;
        this.username = userInfo.username;
        this.verified = userInfo.verified;
        this.token = userInfo.token;
        this.position = userInfo.position;
        this.firstLogin = userInfo.firstLogin;
        this.isChain = userInfo.isChain;
        this.depositAddress = userInfo.depositAddress;
        this.privateKey = userInfo.privateKey;
        this.publicKey = userInfo.publicKey;
        this.identification = userInfo.identification;
        this.identificationInfo = userInfo.identificationInfo;
        this.code = userInfo.code;
    }

    /**
     * app启动后续更新用户信息
     *
     * @param userInfo
     */
    public void setUserInfo1(UserInfo userInfo) {
        this.account = userInfo.account;
        this.avatar = userInfo.avatar;
        this.id = userInfo.id;
        this.uid = userInfo.uid;
        this.phone = userInfo.phone;
        this.user_level = userInfo.user_level;
        this.username = userInfo.username;
        this.verified = userInfo.verified;
        this.token = userInfo.token;
        this.position = userInfo.position;
        this.isChain = userInfo.isChain;
        this.depositAddress = userInfo.depositAddress;
        this.privateKey = userInfo.privateKey;
        this.publicKey = userInfo.publicKey;
        this.identification = userInfo.identification;
        this.identificationInfo = userInfo.identificationInfo;
        this.code = userInfo.code;
    }

    public void setUserInfo(UserInfo userInfo) {

        setUserInfo0(userInfo);

        RoomUtils.run(new Runnable() {
            @Override
            public void run() {
                ChatDatabase.getInstance().userInfoDao().insert(userInfo);
            }
        });
        AppPreference.INSTANCE.setUSER_ID(this.id);
        AppPreference.INSTANCE.setUSER_UID(this.uid);
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
        RoomUtils.run(new Runnable() {
            @Override
            public void run() {
                ChatDatabase.getInstance().userInfoDao().updateAvatar(id, avatar);
            }
        });
    }

    public void setIsSetPayPwd(int isSetPayPwd) {
        this.isSetPayPwd = isSetPayPwd;
        RoomUtils.run(new Runnable() {
            @Override
            public void run() {
                ChatDatabase.getInstance().userInfoDao().updateIsSetPwd(id, isSetPayPwd);
            }
        });
    }

    public void setUsername(String username) {
        this.username = username;
        RoomUtils.run(new Runnable() {
            @Override
            public void run() {
                ChatDatabase.getInstance().userInfoDao().updateName(id, username);
            }
        });
    }

    public void setVerified(int verified) {
        this.verified = verified;
        RoomUtils.run(new Runnable() {
            @Override
            public void run() {
                ChatDatabase.getInstance().userInfoDao().updateVerified(id, verified);
            }
        });
    }

    public void setFirstLogin(boolean firstLogin) {
        this.firstLogin = firstLogin;
        RoomUtils.run(new Runnable() {
            @Override
            public void run() {
                ChatDatabase.getInstance().userInfoDao().updateFirstLogin(id, firstLogin);
            }
        });
    }

    public boolean isIdentified() {
        return identification == 1;
    }

    public boolean isLogin() {
        return !TextUtils.isEmpty(id);
    }

    public boolean hasPublicKey() {
        return !TextUtils.isEmpty(publicKey);
    }

    @Deprecated
    public static void logout() {
        reset();
        AppPreference.INSTANCE.setUSER_ID("");
        AppPreference.INSTANCE.setUSER_UID("");
        AppPreference.INSTANCE.setTOKEN("");
        AppPreference.INSTANCE.setSESSION_KEY("");
        AppConfig.CHAT_SESSION = "";
        ChatDatabase.reset();
        UserInfoPreference.reset();
        MessageDispatcher.reset();
    }

    public static void reset() {
        try {
            // 克隆EMPTY_USER，防止其被修改
            Holder.sInstance = (UserInfo) EMPTY_USER.clone();
        } catch (CloneNotSupportedException e) {
            Holder.sInstance = new UserInfo();
        }
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        UserInfo userInfo = (UserInfo) o;

        if (isSetPayPwd != userInfo.isSetPayPwd) return false;
        if (user_level != userInfo.user_level) return false;
        if (verified != userInfo.verified) return false;
        if (firstLogin != userInfo.firstLogin) return false;
        if (isChain != userInfo.isChain) return false;
        if (identification != userInfo.identification) return false;
        if (!Objects.equals(account, userInfo.account)) return false;
        if (!Objects.equals(avatar, userInfo.avatar)) return false;
        if (!Objects.equals(id, userInfo.id)) return false;
        if (!Objects.equals(uid, userInfo.uid)) return false;
        if (!Objects.equals(phone, userInfo.phone)) return false;
        if (!Objects.equals(username, userInfo.username)) return false;
        if (!Objects.equals(token, userInfo.token)) return false;
        if (!Objects.equals(position, userInfo.position)) return false;
        if (!Objects.equals(depositAddress, userInfo.depositAddress))return false;
        if (!Objects.equals(publicKey, userInfo.publicKey)) return false;
        if (!Objects.equals(privateKey, userInfo.privateKey)) return false;
        if (!Objects.equals(identificationInfo, userInfo.identificationInfo)) return false;
        return Objects.equals(code, userInfo.code);
    }

    @Override
    public int hashCode() {
        int result = account != null ? account.hashCode() : 0;
        result = 31 * result + (avatar != null ? avatar.hashCode() : 0);
        result = 31 * result + (id != null ? id.hashCode() : 0);
        result = 31 * result + (uid != null ? uid.hashCode() : 0);
        result = 31 * result + (phone != null ? phone.hashCode() : 0);
        result = 31 * result + isSetPayPwd;
        result = 31 * result + user_level;
        result = 31 * result + (username != null ? username.hashCode() : 0);
        result = 31 * result + verified;
        result = 31 * result + (token != null ? token.hashCode() : 0);
        result = 31 * result + (position != null ? position.hashCode() : 0);
        result = 31 * result + (firstLogin ? 1 : 0);
        result = 31 * result + (isChain ? 1 : 0);
        result = 31 * result + (depositAddress != null ? depositAddress.hashCode() : 0);
        result = 31 * result + (publicKey != null ? publicKey.hashCode() : 0);
        result = 31 * result + (privateKey != null ? privateKey.hashCode() : 0);
        result = 31 * result + identification;
        result = 31 * result + (identificationInfo != null ? identificationInfo.hashCode() : 0);
        result = 31 * result + (code != null ? code.hashCode() : 0);
        return result;
    }
}
