package com.fzm.chat33.core.db.bean;

import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.room.Embedded;
import androidx.room.Entity;

import com.fuzamei.common.callback.Unique;
import com.fuzamei.componentservice.config.AppConfig;

import java.io.Serializable;
import java.util.List;

/**
 * @author zhengjy
 * @since 2018/10/17
 * Description:
 */
@Entity(tableName = "recent_message", primaryKeys = {"channelType", "id"})
public class RecentMessage implements Serializable, Unique {
    /**
     * id : 1123
     * number : 213
     * lastLog : {"logId":"11","channelType":"1","senderId":"1","receiveId":"9","msgType":1,"msg":{"msg":"文本消息"},"datetime":1530238075000,"senderInfo":{"nickname":"昵称","avatar":"http://xxx/xxx/xxx.jpg"}}
     */

    @NonNull
    private String id;
    private String depositAddress;
    private int number;
    private boolean isDeleted;
    private int stickyTop;
    private int noDisturb;
    // 1:启用加密  2:关闭加密
    @Deprecated
    private int encrypt;
    // 1:有@消息  2:无@消息
    private boolean beAit;
    // 封群截至时间
    private long disableDeadline;
    @Embedded(prefix = "recent_")
    @NonNull
    private PraiseNum praise;
    @Embedded
    @NonNull
    private LastLogBean lastLog;

    public RecentMessage() {

    }

    public RecentMessage(@NonNull String id, String address, long deadline, int number, int stickyTop,
                         int noDisturb, boolean isDeleted, boolean beAit, @NonNull PraiseNum praise,
                         @NonNull LastLogBean lastLog) {
        this.id = id;
        this.depositAddress = address;
        this.disableDeadline = deadline;
        this.number = number;
        this.stickyTop = stickyTop;
        this.noDisturb = noDisturb;
        this.isDeleted = isDeleted;
        this.beAit = beAit;
        this.praise = praise;
        this.lastLog = lastLog;
    }

    @NonNull
    public String getId() {
        return id;
    }

    public void setDepositAddress(String depositAddress) {
        this.depositAddress = depositAddress;
    }

    public String getDepositAddress() {
        return depositAddress;
    }

    public void setId(@NonNull String id) {
        this.id = id;
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    public boolean isDeleted() {
        return isDeleted;
    }

    public void setDeleted(boolean deleted) {
        isDeleted = deleted;
    }

    public int getStickyTop() {
        return stickyTop == 0 ? 2 : stickyTop;
    }

    public void setStickyTop(int stickyTop) {
        this.stickyTop = stickyTop;
    }

    public int getNoDisturb() {
        return noDisturb == 0 ? 2 : noDisturb;
    }

    public void setNoDisturb(int noDisturb) {
        this.noDisturb = noDisturb;
    }

    public boolean beAit() {
        return beAit;
    }

    public void setBeAit(boolean beAit) {
        this.beAit = beAit;
    }

    @Deprecated
    public int getEncrypt() {
        return encrypt;
    }

    @Deprecated
    public void setEncrypt(int encrypt) {
        this.encrypt = encrypt;
    }

    public long getDisableDeadline() {
        return disableDeadline;
    }

    public void setDisableDeadline(long disableDeadline) {
        this.disableDeadline = disableDeadline;
    }

    @NonNull
    public PraiseNum getPraise() {
        return praise;
    }

    public void setPraise(PraiseNum praise) {
        this.praise = praise;
    }

    @NonNull
    public LastLogBean getLastLog() {
        return lastLog;
    }

    public void setLastLog(@NonNull LastLogBean lastLog) {
        this.lastLog = lastLog;
    }

    public static class LastLogBean implements Serializable {
        /**
         * logId : 11
         * channelType : 1
         * fromId : 1
         * targetId : 9
         * msgType : 1
         * msg : {"msg":"文本消息"}
         * datetime : 1530238075000
         * senderInfo : {"nickname":"昵称","avatar":"http://xxx/xxx/xxx.jpg"}
         */

        private String logId;
        private int channelType;
        private String fromId;
        private String targetId;
        private int msgType;
        private int isSnap;
        @Embedded
        private ChatFile msg;
        private long datetime;
        @Embedded
        private SenderInfo senderInfo;

        public LastLogBean(){

        }

        public LastLogBean(ChatMessage chatMessage) {
            this.logId = chatMessage.logId;
            this.channelType = chatMessage.channelType;
            this.fromId = chatMessage.senderId;
            this.targetId = chatMessage.receiveId;
            this.msgType = chatMessage.msgType;
            this.isSnap = chatMessage.isSnap;
            this.msg = chatMessage.msg;
            this.datetime = chatMessage.sendTime;
            this.senderInfo = chatMessage.senderInfo;
        }

        public String getLogId() {
            return logId;
        }

        public void setLogId(String logId) {
            this.logId = logId;
        }

        public int getChannelType() {
            return channelType;
        }

        public void setChannelType(int channelType) {
            this.channelType = channelType;
        }

        public String getFromId() {
            return fromId;
        }

        public void setFromId(String fromId) {
            this.fromId = fromId;
        }

        public String getTargetId() {
            return targetId;
        }

        public void setTargetId(String targetId) {
            this.targetId = targetId;
        }

        public int getMsgType() {
            return msgType;
        }

        public void setMsgType(int msgType) {
            this.msgType = msgType;
        }

        public int getIsSnap() {
            return isSnap;
        }

        public void setIsSnap(int isSnap) {
            this.isSnap = isSnap;
        }

        public ChatFile getMsg() {
            return msg;
        }

        public void setMsg(ChatFile msg) {
            this.msg = msg;
        }

        public long getDatetime() {
            return datetime;
        }

        public void setDatetime(long datetime) {
            this.datetime = datetime;
        }

        public SenderInfo getSenderInfo() {
            return senderInfo;
        }

        public void setSenderInfo(SenderInfo senderInfo) {
            this.senderInfo = senderInfo;
        }

        public boolean isSentType() {
            if (TextUtils.isEmpty(fromId)) {
                return false;
            }
            return fromId.equals(AppConfig.MY_ID);
        }

        @Override
        public int hashCode() {
            return logId.hashCode();
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        RecentMessage message = (RecentMessage) o;

        return id.equals(message.id);
    }

    @Override
    public int hashCode() {
        int result = id.hashCode();
        result = 31 * result + (depositAddress != null ? depositAddress.hashCode() : 0);
        result = 31 * result + number;
        result = 31 * result + (isDeleted ? 1 : 0);
        result = 31 * result + stickyTop;
        result = 31 * result + noDisturb;
        result = 31 * result + encrypt;
        result = 31 * result + (int) (disableDeadline ^ (disableDeadline >>> 32));
        result = 31 * result + lastLog.hashCode();
        return result;
    }

    public static class PraiseNum implements Serializable {
        public int like;
        public int reward;

        public void like() {
            like++;
        }

        public void reward() {
            reward++;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            PraiseNum praiseNum = (PraiseNum) o;

            if (like != praiseNum.like) return false;
            return reward == praiseNum.reward;
        }

        @Override
        public int hashCode() {
            int result = like;
            result = 31 * result + reward;
            return result;
        }
    }

    public static class Wrapper implements Serializable {
        public List<RecentMessage> infos;
    }
}
