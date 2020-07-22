package com.fzm.chat33.core.bean

import java.io.Serializable

/**
 * @author zhengjy
 * @since 2019/09/17
 * Description:聊天对象
 */
data class ChatTarget(
        var channelType: Int,
        var targetId: String
) :Serializable {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ChatTarget

        if (channelType != other.channelType) return false
        if (targetId != other.targetId) return false

        return true
    }

    override fun hashCode(): Int {
        var result = channelType
        result = 31 * result + targetId.hashCode()
        return result
    }
}