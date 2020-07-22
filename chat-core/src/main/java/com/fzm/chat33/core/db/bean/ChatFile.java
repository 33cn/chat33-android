package com.fzm.chat33.core.db.bean;

import androidx.room.ColumnInfo;
import androidx.room.Ignore;
import androidx.annotation.Nullable;
import android.text.Spannable;
import android.text.TextUtils;

import com.fzm.chat33.core.response.BaseResponse;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class ChatFile extends BaseResponse {

    @Ignore
    @Expose(serialize = false, deserialize = false)
    private int chatFileType;

    public transient boolean downloading;

    private String imageUrl;

    private String mediaUrl;
    // 语音消息是否已读
    public boolean isRead;

    @Expose(serialize = false, deserialize = false)
    @SerializedName("unused")
    private String name;

    //文件的本地路径
    private String localPath;
    @SerializedName("time")
    private float duration;
    private int height;
    private int width;

    // 通知消息的类型
    public int type;
    // 通知相关的群名
    public String roomName;
    // 通知操作发起用户
    public String operator;
    // 通知操作影响用户
    public String target;
    // 红包发送用户
    public String owner;
    // 被拉入群聊的用户的昵称
    public List<String> names;
    // 1.全员发言 2.黑名单 3.白名单 4.全员禁言
    public int mutedType;
    // 仅在黑名单模式有效：1.禁言 2.解禁
    public int opt;
    // 撤回消息的logId
    // 被好友拒收的消息logId
    // 收款成功的消息logId
    @ColumnInfo(name = "delete_logId")
    public String logId;
    // 更新加密群聊会话密钥
    // 不需要存数据库
    public String roomId;
    @Ignore
    public String key;
    // 邀请消息
    public String markId;
    public String inviterId;
    @ColumnInfo(name = "roomAvatar")
    public String avatar;
    public String identificationInfo;

    /**
     * 全文搜索时匹配词在搜索字段中的偏移位置
     */
    public String matchOffsets;

    public String content;
    @Ignore
    public transient Spannable linkContent;
    @Ignore
    @Expose(serialize = false, deserialize = false)
    public boolean expand = false;

    // 端到端加密
    @Expose
    public String kid;
    @Expose
    @Nullable
    public String encryptedMsg;
    @Nullable
    public String fromKey;
    @Nullable
    public String toKey;

    // 转账收款
    // 交易记录id
    public String recordId;
    public String amount;

    // 打赏消息通知
    public int like;
    public int reward;
    /**
     * @see com.fzm.chat33.core.consts.PraiseAction
     */
    public String action;

    // 红包
    // 币种：1 CNY 2 BTC 3 BTY 4 ETH   5 ETC 7 SC 8 ZEC 9 BTS 10 LTC 11 BCC 12 YCC 15 USDT 17 DCR
    public int coin;
    public String coinName;
    public String packetId;
    public String packetUrl;
    //红包类型。1手气红包，2 推广红包
    public int packetType;
    // 红包模式，0：普通红包模式 1：消息红包模式
    public int packetMode;
    //红包备注
    @SerializedName("remark")
    public String redBagRemark;
    //红包参数
    public boolean isOpened;
    // 1:生效中 2:已领取完 3:过期已退回 4:已完成
    public int redPacketStatus;
    /**
     * 消息@对象列表
     */
    public List<String> aitList;

    // 文件
    public String fileUrl;
    @SerializedName("name")
    public String fileName;
    @SerializedName("size")
    public long fileSize;
    public String md5;

    // 转发消息
    @SerializedName("channelType")
    public int sourceChannel;
    /**
     * 1逐条转发 2合并转发
     */
    public int forwardType;
    @SerializedName("fromName")
    public String sourceName;
    public String forwardUserName;
    @SerializedName("data")
    @ColumnInfo(name = "sourceLog", typeAffinity = ColumnInfo.TEXT)
    public List<BriefChatLog> sourceLog;

    public ChatFile() {
    }

    public ChatFile(int chatFileType) {
        this.chatFileType = chatFileType;
    }

    public int getChatFileType() {
        return chatFileType;
    }

    public void setChatFileType(int chatFileType) {
        this.chatFileType = chatFileType;
    }

    public String getLocalOrNetUrl() {
        if (!TextUtils.isEmpty(localPath)) {
            return localPath;
        } else {
            if (!TextUtils.isEmpty(imageUrl)) {
                return imageUrl;
            } else if (!TextUtils.isEmpty(mediaUrl)) {
                return mediaUrl;
            } else {
                return "";
            }
        }
    }

    public String getImageUrl() {
        if (imageUrl == null) {
            return "";
        }
        return imageUrl;
    }

    public String getMediaUrl() {
        if (mediaUrl == null) {
            return "";
        }
        return mediaUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public void setMediaUrl(String mediaUrl) {
        this.mediaUrl = mediaUrl;
    }

    @Deprecated
    public String getName() {
        return name;
    }

    @Deprecated
    public void setName(String name) {
        this.name = name;
    }

    public String getLocalPath() {
        return localPath;
    }

    public void setLocalPath(String localPath) {
        this.localPath = localPath;
    }

    public float getDuration() {
        return duration;
    }

    public void setDuration(float duration) {
        this.duration = duration;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    private static ChatFile newChatFile(int type, String image_url, String media_url, int duration, String localPath, int height, int width) {
        ChatFile chatFile = new ChatFile();
        chatFile.setChatFileType(type);
        chatFile.imageUrl = image_url;
        chatFile.mediaUrl = media_url;
        chatFile.setDuration(duration);
        chatFile.setLocalPath(localPath);
        chatFile.setHeight(height);
        chatFile.setWidth(width);
        return chatFile;
    }

    public static ChatFile newFile(/*String url, */String localPath, String fileName, long fileSize, String md5) {
        ChatFile chatFile = new ChatFile();
        chatFile.setChatFileType(ChatMessage.Type.FILE);
        chatFile.setLocalPath(localPath);
//        chatFile.fileUrl = url;
        chatFile.fileName = fileName;
        chatFile.fileSize = fileSize;
        chatFile.md5 = md5;
        return chatFile;
    }

    public static ChatFile newText(String text) {
        ChatFile chatFile = new ChatFile();
        chatFile.setChatFileType(ChatMessage.Type.TEXT);
        chatFile.content = text;
        return chatFile;
    }

    public static ChatFile newAudio(String url, int duration, String localPath) {
        return newChatFile(ChatMessage.Type.AUDIO, "", url, duration, localPath, 0, 0);
    }

    public static ChatFile newImage(String url, String localPath, int height, int width) {
        return newChatFile(ChatMessage.Type.IMAGE, url, "", 0, localPath, height, width);
    }

    public static ChatFile newImage(String localPath, int height, int width) {
        return newChatFile(ChatMessage.Type.IMAGE, "", "", 0, localPath, height, width);
    }

    public static ChatFile newVideo(/*String url, */int duration, String localPath, int height, int width) {
        return newChatFile(ChatMessage.Type.VIDEO, "", "", duration, localPath, height, width);
    }

    public static ChatFile newRedPacket(int coin, String packetId, String packetUrl, int packetType, String redBagRemark) {
        ChatFile chatFile = new ChatFile();
        chatFile.coin = coin;
        chatFile.packetId = packetId;
        chatFile.packetUrl = packetUrl;
        chatFile.packetType = packetType;
        chatFile.redBagRemark = redBagRemark;
        return chatFile;
    }

}