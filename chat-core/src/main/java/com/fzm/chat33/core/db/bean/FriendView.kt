package com.fzm.chat33.core.db.bean

import androidx.room.DatabaseView
import java.io.Serializable

@DatabaseView("SELECT friends.id, friends.name, friends.remark, friends.avatar, friends.identification, friends.onTop, friends.noDisturbing FROM friends " +
        "UNION " +
        "SELECT info_cache.id, info_cache.nickname AS name, info_cache.remark, info_cache.avatar, info_cache.identification, 2 AS onTop, 2 AS noDisturbing FROM info_cache WHERE channelType = 3"
)
data class FriendView(
        var id: String,
        var name: String?,
        var remark: String?,
        var avatar: String?,
        var identification: String?,
        // 是否置顶
        var onTop: Int,
        // 仅好友可看
        var noDisturbing: Int
) : Serializable