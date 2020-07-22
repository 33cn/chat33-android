package com.fzm.chat33.core.bean

import java.io.Serializable

/**
 * @author zhengjy
 * @since 2019/11/08
 * Description:Socket关闭时返回的信息
 */
open class ServerTips(
        val time: Long,
        val device: String,
        val way: Int
) : Serializable {

    companion object {
        const val PATTERN = "yyyy-MM-dd HH:mm"
    }
}