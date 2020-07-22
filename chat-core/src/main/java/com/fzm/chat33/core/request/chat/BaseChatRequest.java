package com.fzm.chat33.core.request.chat;

import com.fzm.chat33.core.Chat33;
import com.fzm.chat33.core.R;
import com.fzm.chat33.core.db.bean.FriendBean;
import com.fzm.chat33.core.db.bean.RoomListBean;
import com.fzm.chat33.core.db.bean.SenderInfo;
import com.fzm.chat33.core.request.BaseRequest;

import java.io.Serializable;
import java.util.List;

import static com.fzm.chat33.core.global.Chat33Const.CHANNEL_FRIEND;
import static com.fzm.chat33.core.global.Chat33Const.CHANNEL_ROOM;

/**
 * @author zhengjy
 * @since 2019/05/21
 * Description:
 */
public class BaseChatRequest extends BaseRequest {

    //----------------------加密消息----------------------//
    /**
     * 群聊中的消息密钥id
     */
    public String kid;

    /**
     * 加密消息内容
     */
    public String encryptedMsg;

    /**
     * 消息发送方的公钥
     */
    public String fromKey;

    /**
     * 消息接收方的公钥
     */
    public String toKey;

    //----------------------转发消息----------------------//
    /**
     * 转发消息来源
     */
    public Integer channelType;
    /**
     * 1逐条转发 2合并转发
     */
    public Integer forwardType;
    /**
     * 转发来源名称
     */
    public String fromName;
    /**
     * 转发者昵称
     */
    public String forwardUserName;
    /**
     * 批量转发列表
     */
    public List<SourceChatLog> data;

    public BaseChatRequest() {

    }

    public BaseChatRequest(PreForwardRequest request) {
        channelType = request.getType() == 1 ? CHANNEL_ROOM : CHANNEL_FRIEND;
        forwardType = request.getForwardType();
        if (channelType == CHANNEL_ROOM) {
            RoomListBean bean = Chat33.loadRoomFromCache(request.getSourceId());
            if (bean != null) {
                fromName = bean.getName();
            } else {
                fromName = Chat33.getContext().getString(R.string.core_name_unknown);
            }
        } else {
            FriendBean bean = Chat33.loadFriendFromCache(request.getSourceId());
            if (bean != null) {
                fromName = bean.getName();
            } else {
                fromName = Chat33.getContext().getString(R.string.core_name_unknown);
            }
        }
        forwardUserName = request.getForwardUsername();
    }

    public static class SourceChatLog implements Serializable {
        public int msgType;
        public String logId;
        public long datetime;
        public Object msg;
        public SenderInfo senderInfo;

    }
}
