package com.fzm.chat33.core.bean

import com.fzm.chat33.core.db.bean.ChatMessage
import java.io.Serializable

/**
 * @author zhengjy
 * @since 2019/09/17
 * Description:
 */
data class SearchResult(
        /**
         * 搜索结果类型
         * 0: 好友  1：群组  2：聊天记录
         */
        var searchScope: Int,
        /**
         * 搜索关键字
         */
        var keywords: String?,
        /**
         * 对象id
         */
        var targetId: String?,
        /**
         * 头像显示
         */
        var avatar: String?,
        /**
         * 主标题
         */
        var title: String?,
        /**
         * 副标题
         */
        var subTitle: String?,
        /**
         * 聊天记录列表
         */
        var chatLogs: List<ChatMessage>?
) : Serializable {
        data class Wrapper(
                var keywords: String?,
                var list: List<SearchResult>?
        ) : Serializable
}

class SearchScope {
        companion object {
                const val FRIEND = 0
                const val GROUP = 1
                const val CHATLOG = 2
                const val ALL = 3
        }
}