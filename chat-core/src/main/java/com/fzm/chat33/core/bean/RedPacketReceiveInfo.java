package com.fzm.chat33.core.bean;

import java.io.Serializable;
import java.util.List;

/**
 * @author zhengjy
 * @since 2019/03/15
 * Description:
 */
public class RedPacketReceiveInfo implements Serializable {
    /**
     * userId : 1
     * userName : 嘎嘎嘎
     * userAvatar : http://zb-chat.oss-cn-shanghai.aliyuncs.com/chat33/user/avatar/1540865084115_1.png
     * coinId : 12
     * coinName : YCC
     * amount : 1
     * createdAt : 0
     * status : 2
     * failMessage :
     */

    private String userId;
    private String userName;
    private String userAvatar;
    private int coinId;
    private String coinName;
    private double amount;
    /**
     * 领取时间，unix时间戳
     */
    private long createdAt;
    /**
     * 领取状态：1:正在入账 2：入账成功 3：入账失败 4：用户token失效
     */
    private int status;
    /**
     * 仅当为入账失败状态时才会返回
     */
    private String failMessage;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserAvatar() {
        return userAvatar;
    }

    public void setUserAvatar(String userAvatar) {
        this.userAvatar = userAvatar;
    }

    public int getCoinId() {
        return coinId;
    }

    public void setCoinId(int coinId) {
        this.coinId = coinId;
    }

    public String getCoinName() {
        return coinName;
    }

    public void setCoinName(String coinName) {
        this.coinName = coinName;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getFailMessage() {
        return failMessage;
    }

    public void setFailMessage(String failMessage) {
        this.failMessage = failMessage;
    }

    public static class Wrapper {
        public List<RedPacketReceiveInfo> rows;
    }
}
