package com.fzm.chat33.core.db.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.fzm.chat33.core.db.bean.BriefChatLog;
import com.fzm.chat33.core.db.bean.ChatMessage;
import com.fzm.chat33.core.db.bean.RewardDetail;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Maybe;
import io.reactivex.Single;

/**
 * @author zhengjy
 * @since 2018/10/27
 * Description:
 */
@Dao
public interface ChatMessageDao {

    /**
     * 当前设备正在读或已经读完的阅后即焚消息，防止收到焚毁通知后被删除
     */
    List<String> visibleSnapMsg = new ArrayList<>();

    @Query("SELECT * FROM chat_message WHERE channelType=:channelType AND receiveId=:id " +
            "AND sendTime < :timeStamp AND ignoreInHistory=0 ORDER BY sendTime DESC LIMIT :number")
    Maybe<List<ChatMessage>> getGroupChatLogLocal(int channelType, String id, long timeStamp, int number);

    @Query("SELECT * FROM chat_message WHERE channelType=:channelType AND receiveId=:id " +
            "AND sendTime < :timeStamp AND ignoreInHistory=0 AND snapCounting=0 AND snapVisible=0 ORDER BY sendTime DESC LIMIT :number")
    List<ChatMessage> getGroupNormalChatLogLocal(int channelType, String id, long timeStamp, int number);

    @Query("SELECT * FROM chat_message WHERE channelType=3 AND (senderId=:id OR receiveId=:id) " +
            "AND sendTime < :timeStamp AND ignoreInHistory=0 ORDER BY sendTime DESC LIMIT :number")
    Maybe<List<ChatMessage>> getPrivateChatLogLocal(String id, long timeStamp, int number);

    @Query("SELECT * FROM chat_message WHERE channelType=3 AND (senderId=:id OR receiveId=:id) " +
            "AND sendTime < :timeStamp AND ignoreInHistory=0 AND snapCounting=0 AND snapVisible=0 ORDER BY sendTime DESC LIMIT :number")
    List<ChatMessage> getPrivateNormalChatLogLocal(String id, long timeStamp, int number);

    @Query("SELECT * FROM chat_message WHERE channelType=2 AND receiveId=:id AND sendTime >= " +
            "(SELECT sendTime FROM chat_message WHERE channelType=2 AND logId=:logId) " +
            "AND ignoreInHistory=0 ORDER BY sendTime DESC")
    Maybe<List<ChatMessage>> getGroupChatLogFromId(String id, String logId);

    @Query("SELECT * FROM chat_message WHERE channelType=3 AND (senderId=:id OR receiveId=:id) " +
            "AND sendTime >= (SELECT sendTime FROM chat_message WHERE channelType=3 AND logId=:logId) " +
            "AND ignoreInHistory=0 ORDER BY sendTime DESC")
    Maybe<List<ChatMessage>> getPrivateChatLogFromId(String id, String logId);

    @Query("SELECT * FROM chat_message WHERE logId=:logId AND channelType=:channelType")
    Maybe<ChatMessage> mayGetMessageById(String logId, int channelType);

    @Query("SELECT * FROM chat_message WHERE logId=:logId AND channelType=:channelType")
    Single<ChatMessage> getMessageById(String logId, int channelType);

    @Query("SELECT * FROM chat_message WHERE logId=:logId AND channelType=:channelType")
    ChatMessage getMessageByIdSync(String logId, int channelType);

    @Query("SELECT * FROM chat_message WHERE msgId=:msgId")
    ChatMessage getMessageByMsgId(String msgId);

    @Query("SELECT count(*) FROM chat_message WHERE sendTime >= :start AND sendTime < :end AND messageState=0")
    Integer getChatMessages(long start, long end);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    Long insert(ChatMessage chatMessage);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    List<Long> insert(List<ChatMessage> chatMessages);

    @Query("UPDATE chat_message SET ignoreInHistory=1 WHERE channelType=:channelType AND logId=:logId")
    void fakeDeleteMessage(int channelType, String logId);

    @Query("DELETE FROM chat_message WHERE channelType=:channelType AND logId=:logId AND snapVisible=0")
    void deleteUnreadSnapMessage(int channelType, String logId);

    @Query("DELETE FROM chat_message WHERE channelType=:channelType AND logId=:logId")
    void deleteMessage(int channelType, String logId);

    @Query("DELETE FROM chat_message WHERE channelType=3 AND (senderId=:id OR receiveId=:id)")
    void deletePrivateMessage(String id);

    @Query("DELETE FROM chat_message WHERE channelType=2 AND receiveId=:id")
    void deleteGroupMessage(String id);

    @Query("UPDATE chat_message SET messageState=:state WHERE channelType=:channelType AND logId=:logId")
    void updateMessageState(int channelType, String logId, int state);

    @Query("UPDATE chat_message SET recordId=:recordId WHERE channelType=3 AND logId=:logId")
    void updateRecordId(String logId, String recordId);

    @Query("UPDATE chat_message SET ignoreInHistory=0 WHERE channelType=:channelType AND logId=:logId")
    void updateIgnore(int channelType, String logId);

    @Query("UPDATE chat_message SET destroyTime=:destroyTime WHERE channelType=:channelType AND logId=:logId")
    void updateDestroyTime(long destroyTime, int channelType, String logId);

    @Query("UPDATE chat_message SET snapVisible=:snapVisible WHERE channelType=:channelType AND logId=:logId")
    void updateVisible(int snapVisible, int channelType, String logId);

    @Query("UPDATE chat_message SET snapCounting=:snapCounting WHERE channelType=:channelType AND logId=:logId")
    void updateCounting(int snapCounting, int channelType, String logId);

    @Query("UPDATE chat_message SET redPacketStatus=:status WHERE packetId=:packetId")
    void updateRedPacketStatus(int status, String packetId);

    @Query("UPDATE chat_message SET sourceLog=:sourceLog WHERE logId=:logId AND channelType=:channelType")
    void updateSourceLog(String logId, int channelType, List<BriefChatLog> sourceLog);

    @Query("UPDATE chat_message SET praise=:praise WHERE logId=:logId AND channelType=:channelType")
    void updatePraise(String logId, int channelType, RewardDetail praise);
}
