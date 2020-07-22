package com.fzm.chat33.core.response;

import android.text.TextUtils;

import com.fuzamei.componentservice.config.AppConfig;
import com.fzm.chat33.core.db.bean.ReceiverInfo;
import com.fzm.chat33.core.db.bean.ChatMessage;
import com.fzm.chat33.core.db.bean.RewardDetail;
import com.fzm.chat33.core.db.bean.SenderInfo;
import com.fzm.chat33.core.db.bean.ChatFile;
import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * 创建日期：2018/7/27 on 15:07
 * 描述:
 * 作者:wdl
 */
public class MsgSocketResponse extends BaseResponse {
    public String msgId;
    public String fromId;
    public int channelType;
    public int isSnap;
    public SenderInfo senderInfo;
    public String targetId;
    public int user_level;
    public int msgType;
    public String logId;
    public ChatFile msg;
    public long datetime;
    public int eventType;
    // 离线消息
    public List<MsgSocketResponse> list;
    // 确认消息的开始和结束时间
    public long begin;
    public long end;
    public RewardDetail praise;

    public int code;
    public String content;
    public String remark;
    public String avatar;
    public String uid;

    // 同步序列结束标志
    public boolean complete;

    // 聊天消息是否已读
    public boolean isRead;

    // eventType:25 禁言相关
    // eventType:11 封群通知
    @SerializedName(value = "deadline", alternate = {"disableDeadline"})
    public long deadline;

    // eventType:20 入群通知
    public String roomId;
    public String userId;

    // eventType:20 入群通知
    @SerializedName(value = "receiveInfo")
    public ReceiverInfo receiverInfo;
    public String id;
    public int type;
    public String applyReason;
    // 1:等待验证 2:已拒绝 3:已同意
    public int status;

    // eventType:34 用户公钥
    public String publicKey;

    public ChatMessage newChatMessageDb() {
        ChatMessage message = new ChatMessage(logId, msgId, channelType, fromId, targetId, msgType, datetime, isSnap, isRead, msg);
        if (TextUtils.isEmpty(msgId)) {
            message.setMsgId(logId);//红包时直接发消息过来，没有msgid  只有logid
        }
        if (praise != null) {
            message.praise = praise;
        } else {
            message.praise = new RewardDetail();
        }
        if (senderInfo != null) {
            message.senderInfo = senderInfo;
        } else {
            message.senderInfo = new SenderInfo();
        }
        return message;
    }

    public boolean isSentType() {
        if (TextUtils.isEmpty(fromId)) {
            return false;
        }
        return fromId.equals(AppConfig.MY_ID);
    }
}
