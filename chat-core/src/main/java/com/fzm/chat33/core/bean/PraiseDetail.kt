package com.fzm.chat33.core.bean

import com.fzm.chat33.core.db.bean.ChatMessage
import java.io.Serializable

/**
 * 创建日期：2019/11/20
 * 描述:
 * 作者:yll
 */
data class PraiseDetail(
        var log: ChatMessage,
        var state: Int,
        var praiseNumber: Int,
        var reward: Double
) : Serializable