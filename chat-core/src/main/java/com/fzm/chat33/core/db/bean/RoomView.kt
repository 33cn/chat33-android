package com.fzm.chat33.core.db.bean

import androidx.room.DatabaseView
import java.io.Serializable

@DatabaseView("SELECT room_list.id, room_list.name, room_list.name AS remark, room_list.avatar, room_list.identification, room_list.onTop, room_list.noDisturbing  FROM room_list " +
        "UNION " +
        "SELECT info_cache.id, info_cache.nickname AS name, info_cache.remark, info_cache.avatar, info_cache.identification, 2 AS onTop, 2 AS noDisturbing FROM info_cache WHERE channelType = 2"
)
data class RoomView(
        var id: String,
        var name: String?,
        var remark: String?,
        var avatar: String?,
        var identification: String?,
        // 是否置顶
        var onTop: Int,
        var noDisturbing: Int
) : Serializable