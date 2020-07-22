package com.fzm.chat33.core.db.dao;

import androidx.room.Dao;
import androidx.room.Query;

import com.fzm.chat33.core.db.bean.ChatMessage;

import java.util.List;

/**
 * @author zhengjy
 * @since 2019/09/20
 * Description:Fts本地搜索接口
 */
@Dao
public interface FtsSearchDao {

    @Query("SELECT *, snippet(message_fts, '<\u200b>', '</\u200b>', '…', -1, 64) AS matchOffsets " +
            "FROM message_fts WHERE ignoreInHistory=0 AND snapCounting=0 AND snapVisible=0 " +
            "AND content MATCH :keywords ORDER BY sendTime DESC")
    List<ChatMessage> searchChatLogs(String keywords);

    @Query("SELECT *, snippet(message_fts, '<\u200b>', '</\u200b>', '…', -1, 64) AS matchOffsets " +
            "FROM message_fts WHERE channelType=2 AND receiveId=:id AND ignoreInHistory=0 " +
            "AND snapCounting=0 AND snapVisible=0 AND content MATCH :keywords ORDER BY sendTime DESC")
    List<ChatMessage> searchGroupChatLogs(String id, String keywords);

    @Query("SELECT *, snippet(message_fts, '<\u200b>', '</\u200b>', '…', -1, 64) AS matchOffsets " +
            "FROM message_fts WHERE channelType=3 AND (senderId=:id OR receiveId=:id) " +
            "AND ignoreInHistory=0 AND snapCounting=0 AND snapVisible=0 AND content MATCH :keywords ORDER BY sendTime DESC")
    List<ChatMessage> searchFriendChatLogs(String id, String keywords);
}
