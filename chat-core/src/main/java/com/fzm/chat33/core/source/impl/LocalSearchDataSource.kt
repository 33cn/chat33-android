package com.fzm.chat33.core.source.impl

import com.fzm.chat33.core.bean.ChatTarget
import com.fzm.chat33.core.db.ChatDatabase
import com.fzm.chat33.core.db.bean.ChatMessage
import com.fzm.chat33.core.db.bean.FriendBean
import com.fzm.chat33.core.db.bean.RoomContact
import com.fzm.chat33.core.db.bean.RoomListBean
import com.fzm.chat33.core.global.Chat33Const
import com.fzm.chat33.core.source.SearchDataSource
import java.util.*
import javax.inject.Inject

/**
 * @author zhengjy
 * @since 2019/09/17
 * Description:
 */
class LocalSearchDataSource @Inject constructor() : SearchDataSource {

    private fun friendDao() = ChatDatabase.getInstance().friendsDao()
    private fun groupDao() = ChatDatabase.getInstance().roomsDao()
    private fun roomUserDao() = ChatDatabase.getInstance().roomUserDao()
    private fun ftsSearchDao() = ChatDatabase.getInstance().ftsSearchDao()

    override fun searchFriends(keywords: String): List<FriendBean> {
        return friendDao().searchFriends(getLikeKey(keywords))
    }

    override fun searchFriendsWithBlocked(keywords: String): List<FriendBean> {
        return friendDao().searchFriendsWithBlocked(getLikeKey(keywords))
    }

    override fun searchGroups(keywords: String): List<RoomListBean> {
        return groupDao().searchGroups(getLikeKey(keywords))
    }

    override fun searchChatLogs(keywords: String): List<ChatMessage> {
        return ftsSearchDao().searchChatLogs(getMatchKey(keywords))
    }

    override fun searchChatLogsByTarget(keywords: String, target: ChatTarget): List<ChatMessage> {
        val result = mutableListOf<ChatMessage>()
        if (target.channelType == Chat33Const.CHANNEL_ROOM) {
            ftsSearchDao().searchGroupChatLogs(target.targetId, keywords)?.let {
                result.addAll(it)
            }
        } else {
            ftsSearchDao().searchFriendChatLogs(target.targetId, keywords)?.let {
                result.addAll(it)
            }
        }
        return result
    }

    override fun searchRoomContacts(roomId: String, keywords: String): List<RoomContact> {
        return roomUserDao().searchRoomContacts(roomId, keywords)
    }

    override fun searchRoomContactsByLevel(roomId: String, memberLevel: Int, keywords: String): List<RoomContact> {
        return roomUserDao().searchRoomContactsByLevel(roomId, memberLevel, keywords)
    }

    /**
     * 转义特殊字符，防止SQL注入
     */
    private fun getLikeKey(keywords: String): String {
        return keywords.replace("[", "/[")
                .replace("]", "/]")
                .replace("^", "/^")
                .replace("%", "/%")
                .replace("_", "/_")
                .replace("/", "//")
                .replace("'", "/'")
                .replace("&", "/&")
                .replace("(", "/(")
                .replace(")", "/)")
    }

    /**
     * 转义特殊字符，防止SQL注入
     */
    private fun getMatchKey(keywords: String): String {
        return keywords
                .replace("\"", "")
                .replace("'", "\"'\"")
                .replace("*", "\"*\"")
                .replace("(", "\"(\"")
                .replace(")", "\")\"")
                .toLowerCase(Locale.CHINESE)
    }
}