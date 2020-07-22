package com.fzm.chat33.core.bean.param

import com.fzm.chat33.core.db.ChatDatabase
import com.fzm.chat33.core.db.bean.ChatMessage
import com.fzm.chat33.core.global.Chat33Const
import java.io.Serializable

/**
 * @author zhengjy
 * @since 2019/12/18
 * Description:文件解密参数
 */
class DecryptParams : Serializable {

    val channelType: Int
        get() {
            return when {
                message != null -> message!!.channelType
                publicKey != null -> Chat33Const.CHANNEL_FRIEND
                else -> Chat33Const.CHANNEL_FRIEND
            }
        }
    val friendKey: String
        get() {
            return when {
                message != null -> message!!.decryptPublicKey
                publicKey != null -> publicKey!!
                else -> ""
            }
        }
    val groupKey: String
        get() {
            return if (message != null) {
                ChatDatabase.getInstance().roomKeyDao().getRoomKeyById(message!!.receiveId, message?.msg?.kid ?: "0")?.keySafe ?: ""
            } else {
                ""
            }
        }

    private var message: ChatMessage? = null
    private var publicKey: String? = null

    constructor(message: ChatMessage) {
        this.message = message
    }

    constructor(publicKey: String) {
        this.publicKey = publicKey
    }
}

fun ChatMessage.toDecParams(): DecryptParams {
    return DecryptParams(this)
}

fun String.toDecParams(): DecryptParams {
    return DecryptParams(this)
}