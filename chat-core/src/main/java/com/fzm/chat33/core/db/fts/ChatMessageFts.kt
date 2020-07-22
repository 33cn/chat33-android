package com.fzm.chat33.core.db.fts

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.Fts4
import androidx.room.FtsOptions
import com.fzm.chat33.core.bean.MessageState
import com.fzm.chat33.core.db.bean.ChatFile
import com.fzm.chat33.core.db.bean.ChatMessage
import com.fzm.chat33.core.db.bean.SenderInfo
import java.io.Serializable

/**
 * @author zhengjy
 * @since 2019/09/20
 * Description:用于全文搜索的聊天消息虚表
 */
@Entity(tableName = "message_fts")
@Fts4(
        // 使用微信的mmicu分词器
        tokenizer = "mmicu",
        contentEntity = ChatMessage::class,
        notIndexed = ["snapVisible", "snapCounting", "destroyTime", "imageUrl", "mediaUrl", "localPath",
                "height", "width", "roomAvatar", "encryptedMsg", "recordId", "coinName", "packetId",
                "packetUrl", "fileUrl", "fileSize", "md5", "like", "reward", "action", "packetMode"],
        order = FtsOptions.Order.DESC
)
data class ChatMessageFts(

        var logId: String,

        var msgId: String?,

        // 消息是否是加密消息
        var encrypted: Int = 0,

        // 是否为阅后即焚消息，1：是 2：否
        var isSnap: Int = 0,
        // 是否已展开消息
        var snapVisible: Int = 0,
        // 消息是否开始倒计时
        var snapCounting: Int = 0,
        // 消息销毁时间
        var destroyTime: Long = 0,

        // 查询历史记录时不包含在内
        // 0:表示正常  1:表示不包含在历史记录
        var ignoreInHistory: Int = 0,

        var channelType: Int = 0,

        var senderId: String?,

        @Deprecated("")
        var fromGId: String?,

        var receiveId: String?,

        @Embedded
        var msg: ChatFile?,

        var messageState: Int = MessageState.SEND_SUCCESS,

        var msgType: Int = 0,

        var sendTime: Long = 0,

        @Embedded
        var senderInfo: SenderInfo?
) : Serializable