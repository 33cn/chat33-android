package com.fzm.chat33.core.source

import com.fzm.chat33.core.bean.ChatTarget
import com.fzm.chat33.core.db.bean.ChatMessage
import com.fzm.chat33.core.db.bean.FriendBean
import com.fzm.chat33.core.db.bean.RoomContact
import com.fzm.chat33.core.db.bean.RoomListBean

/**
 * @author zhengjy
 * @since 2019/09/17
 * Description:
 */
interface SearchDataSource {

    fun searchFriends(keywords: String): List<FriendBean>

    fun searchFriendsWithBlocked(keywords: String): List<FriendBean>

    fun searchGroups(keywords: String): List<RoomListBean>

    fun searchChatLogs(keywords: String): List<ChatMessage>

    fun searchChatLogsByTarget(keywords: String, target: ChatTarget): List<ChatMessage>

    fun searchRoomContacts(roomId: String, keywords: String): List<RoomContact>

    fun searchRoomContactsByLevel(roomId: String, memberLevel: Int, keywords: String): List<RoomContact>
}