package com.fzm.chat33.core.repo

import com.fzm.chat33.core.bean.SearchResult
import com.fzm.chat33.core.bean.SearchScope.Companion.ALL
import com.fzm.chat33.core.bean.SearchScope.Companion.CHATLOG
import com.fzm.chat33.core.bean.SearchScope.Companion.FRIEND
import com.fzm.chat33.core.bean.SearchScope.Companion.GROUP
import com.fzm.chat33.core.bean.ChatTarget
import com.fzm.chat33.core.db.ChatDatabase
import com.fzm.chat33.core.db.bean.ChatMessage
import com.fzm.chat33.core.global.Chat33Const
import com.fzm.chat33.core.global.LoginInfoDelegate
import com.fzm.chat33.core.logic.getTargetId
import javax.inject.Inject
import javax.inject.Singleton

/**
 * @author zhengjy
 * @since 2019/09/17
 * Description:
 */
@Singleton
class SearchRepository @Inject constructor(
        private val contactData: ContactsRepository,
        private val loginInfoDelegate: LoginInfoDelegate
) : LoginInfoDelegate by loginInfoDelegate {

    private fun friendsDao() = ChatDatabase.getInstance().friendsDao()
    private fun groupsDao() = ChatDatabase.getInstance().roomsDao()

    suspend fun searchDataScoped(keywords: String, scope: Int): SearchResult.Wrapper {
        val searchResult = mutableListOf<SearchResult>()
        when (scope) {
            FRIEND -> searchFriends(keywords, searchResult)
            GROUP -> searchGroups(keywords, searchResult)
            CHATLOG -> searchChatLogs(keywords, searchResult)
            ALL -> {
                searchFriends(keywords, searchResult)
                searchGroups(keywords, searchResult)
                searchChatLogs(keywords, searchResult)
            }
        }
        return SearchResult.Wrapper(keywords, searchResult)
    }

    fun searchChatLogs(keywords: String, target: ChatTarget): List<ChatMessage> {
        return contactData.searchChatLogsByTarget(keywords, target)
    }

    private fun searchFriends(keywords: String, searchResult: MutableList<SearchResult>) {
        val friends = contactData.searchFriends(keywords)
        friends.forEach {
            val result = if (it.remark.isNullOrEmpty()) {
                SearchResult(FRIEND, keywords, it.id, it.avatar, it.name, null, null)
            } else {
                SearchResult(FRIEND, keywords, it.id, it.avatar, it.remark, it.name, null)
            }
            searchResult.add(result)
        }
    }

    private fun searchGroups(keywords: String, searchResult: MutableList<SearchResult>) {
        val groups = contactData.searchGroups(keywords)
        groups.forEach {
            val result = SearchResult(GROUP, keywords, it.id, it.avatar, it.name, null, null)
            searchResult.add(result)
        }
    }

    private suspend fun searchChatLogs(keywords: String, searchResult: MutableList<SearchResult>) {
        val chatLogs = contactData.searchChatLogs(keywords)
        chatLogs.groupBy {
            ChatTarget(it.channelType, it.getTargetId(getUserId()))
        }.forEach {
            val target = it.key
            if (target.channelType == Chat33Const.CHANNEL_ROOM) {
                val local = groupsDao().getRoomFromView(target.targetId)
                val result = if (local != null) {
                    SearchResult(CHATLOG, keywords, local.id, local.avatar, local.name, null, it.value)
                } else {
                    val data = contactData.getRoomInfo(target.targetId).dataOrNull()
                    if (data != null) {
                        SearchResult(CHATLOG, keywords, data.id, data.avatar, data.name, null, it.value)
                    } else {
                        SearchResult(CHATLOG, keywords, target.targetId, "", "", null, it.value)
                    }
                }
                searchResult.add(result)
            } else {
                val local = friendsDao().getFriendFromView(target.targetId)
                val result = if (local != null) {
                    SearchResult(CHATLOG, keywords, local.id, local.avatar, local.name, null, it.value)
                } else {
                    val data = contactData.getUserInfo(target.targetId).dataOrNull()
                    if (data != null) {
                        SearchResult(CHATLOG, keywords, data.id, data.avatar, data.name, null, it.value)
                    } else {
                        SearchResult(CHATLOG, keywords, target.targetId, "", "", null, it.value)
                    }
                }
                searchResult.add(result)
            }
        }
    }
}