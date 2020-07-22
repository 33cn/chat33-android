package com.fzm.chat33.core.db.bean;

import androidx.room.ColumnInfo;
import androidx.room.Embedded;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.annotation.NonNull;

import android.os.CountDownTimer;
import android.text.TextUtils;

import com.fuzamei.componentservice.config.AppConfig;
import com.fzm.chat33.core.response.BaseResponse;
import com.fzm.chat33.core.bean.MessageState;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import org.jetbrains.annotations.NotNull;

import java.util.UUID;

import static com.fzm.chat33.core.global.Chat33Const.PRAISE_MESSAGE;
import static com.fzm.chat33.core.global.Chat33Const.SNAP_DESTROY;
import static com.fzm.chat33.core.global.Chat33Const.UPDATE_GROUP_KEY;

/**
 * 类名：ChatMessageDb 说明：单人或群聊天消息
 * 信息：对方user/group
 */
@Entity(tableName = "chat_message", primaryKeys = {"logId", "channelType"})
public class ChatMessage extends BaseResponse implements Cloneable {

    //字段：mtype [1:text,2:html,3:image,4:audio,5:file]
    public static class Type {
        public static final int SYSTEM = 0;
        public static final int TEXT = 1;
        public static final int AUDIO = 2;
        public static final int IMAGE = 3;
        public static final int RED_PACKET = 4;
        public static final int VIDEO = 5;
        public static final int NOTIFICATION = 6;
        public static final int CLEAR_LOVE = 7;
        public static final int FORWARD = 8;
        public static final int FILE = 9;
        public static final int TRANSFER = 10;
        public static final int RECEIPT = 11;
        public static final int INVITATION = 12;
    }

    @Ignore
    public transient boolean isSelected = false;

    @Ignore
    public transient CountDownTimer timer;

    /**
     * 历史文件记录中显示时占的格数
     */
    @Ignore
    public transient int offset = 1;

    @NonNull
    @Expose
    public String logId;

    @Expose
    private String msgId;

    // 消息是否是加密消息
    @Expose
    public int encrypted;

    // 是否为阅后即焚消息，1：是 2：否
    public int isSnap;
    // 是否已展开消息
    public int snapVisible = 0;
    // 消息是否开始倒计时
    public int snapCounting = 0;
    // 消息销毁时间
    public long destroyTime;

    // 查询历史记录时不包含在内
    // 0:表示正常  1:表示不包含在历史记录
    public int ignoreInHistory;

    @Ignore
    public boolean isRead;

    @Expose
    public int channelType;

    @Expose
    @SerializedName(value = "senderId", alternate = {"fromId"})
    public String senderId;

    @Expose
    @SerializedName("from_gid")
    @Deprecated
    public String fromGId;

    @Expose
    @SerializedName(value = "receiveId", alternate = {"targetId"})
    public String receiveId;

    @Embedded
    @Expose
    public ChatFile msg;

    /**
     * 此条消息被人@
     */
    @Ignore
    public boolean beAit;

    public int messageState = MessageState.SEND_SUCCESS;//0代表发送成功，1代表发送中，2代表发送失败

    //消息内容类型[1:text,2:audio,3:image,4:hongbao,5:file]
    @Expose
    @SerializedName("msgType")
    public int msgType;

    @Expose
    @SerializedName("datetime")
    public long sendTime;

    /**
     * 在聊天列表是否显示消息时间
     */
    public transient boolean showTime;

    @Embedded
    @Expose
    public SenderInfo senderInfo;

    /**
     * 点赞打赏详情
     */
    @ColumnInfo(name = "praise", typeAffinity = ColumnInfo.TEXT)
    public RewardDetail praise;

    /**
     * 当前需要的转发消息的位置，从1开始，0为无
     */
    @Ignore
    public int briefPos;

    @NonNull
    public String getLogId() {
        return logId;
    }

    public void setLogId(@NonNull String logId) {
        this.logId = logId;
    }

    public String getMsgId() {
        return msgId;
    }

    public void setMsgId(String msgId) {
        this.msgId = msgId;
    }

    public boolean isSentType() {
        if (TextUtils.isEmpty(senderId)) {
            return false;
        }
        return senderId.equals(AppConfig.MY_ID);
    }

    public boolean shouldSave() {
        if (msgType != Type.NOTIFICATION) {
            return true;
        } else {
            return msg.type != SNAP_DESTROY
                    && msg.type != UPDATE_GROUP_KEY
                    && msg.type != PRAISE_MESSAGE;
        }
    }

    /**
     * 不需要改变会话列表的消息
     */
    public boolean notChangeRecent() {
        if (msgType != Type.NOTIFICATION) {
            return true;
        } else {
            return msg.type != SNAP_DESTROY
                    && msg.type != UPDATE_GROUP_KEY;
        }
    }

    public String getDecryptPublicKey() {
        if (isSentType()) {
            return msg.toKey;
        } else {
            return msg.fromKey;
        }
    }

    public static ChatMessage create(String receiveId, int channelType, int msgType, int isSnap, ChatFile msg) {
        ChatMessage message = new ChatMessage();
        message.msgId = UUID.randomUUID().toString();
        message.logId = message.msgId;
        message.channelType = channelType;
        message.senderId = AppConfig.MY_ID;
        message.receiveId = receiveId;
        message.msgType = msgType;
        message.sendTime = System.currentTimeMillis();
        message.isSnap = isSnap;
        message.msg = msg;
        message.messageState = MessageState.SENDING;
        message.senderInfo = new SenderInfo();
        message.praise = new RewardDetail();
        return message;
    }

    public ChatMessage() {
    }

    @Ignore
    public ChatMessage(@NotNull String logId, String msgId, int channelType, String senderId,
                       String receiveId, int msgType, long sendTime, int isSnap, boolean isRead, ChatFile msg) {
        this.logId = logId;
        this.msgId = msgId;
        this.channelType = channelType;
        this.senderId = senderId;
        this.receiveId = receiveId;
        this.msgType = msgType;
        this.sendTime = sendTime;
        this.isSnap = isSnap;
        this.isRead = isRead;
        this.msg = msg;
    }

    @Override
    public ChatMessage clone() throws CloneNotSupportedException {
        // 暂时只需浅拷贝 briefPos
        return (ChatMessage) super.clone();
    }
}
