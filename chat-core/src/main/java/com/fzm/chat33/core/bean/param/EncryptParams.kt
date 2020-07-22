package com.fzm.chat33.core.bean.param

import com.fzm.chat33.core.bean.ChatTarget
import com.fzm.chat33.core.db.ChatDatabase
import com.fzm.chat33.core.db.bean.ChatMessage
import com.fzm.chat33.core.global.Chat33Const
import com.fzm.chat33.core.global.LoginInfoDelegate
import com.fzm.chat33.core.logic.getTargetId
import com.fzm.chat33.core.source.LocalContactDataSource
import org.kodein.di.conf.KodeinGlobalAware
import org.kodein.di.generic.instance
import java.io.Serializable

/**
 * @author zhengjy
 * @since 2019/12/18
 * Description:文件加密参数
 */
class EncryptParams : Serializable, KodeinGlobalAware {

    private val localData: LocalContactDataSource by instance()
    private val loginDelegate: LoginInfoDelegate by instance()

    val channelType: Int
        get() {
            return when {
                message != null -> message!!.channelType
                target != null -> target!!.channelType
                publicKey != null -> Chat33Const.CHANNEL_FRIEND
                else -> Chat33Const.CHANNEL_FRIEND
            }
        }
    val friendKey: String
        get() {
            if (publicKey != null) {
                return publicKey!!
            }
            val targetId = when {
                message != null -> message!!.getTargetId(loginDelegate.getUserId())
                target != null -> target!!.targetId
                else -> ""
            }
            val friend = localData.getLocalFriendById(targetId)
            return if (friend != null) {
                friend.publicKey ?: ""
            } else {
                ""
            }
        }
    val groupKey: String
        get() {
            return when {
                message != null -> {
                    val targetId = message!!.getTargetId(loginDelegate.getUserId())
                    ChatDatabase.getInstance().roomKeyDao().getLatestKey(targetId)?.keySafe ?: ""
                }
                target != null -> ChatDatabase.getInstance().roomKeyDao().getLatestKey(target!!.targetId)?.keySafe ?: ""
                else -> ""
            }
        }

    private var message: ChatMessage? = null
    private var target: ChatTarget? = null
    private var publicKey: String? = null

    constructor(message: ChatMessage) {
        this.message = message
    }

    constructor(target: ChatTarget) {
        this.target = target
    }

    constructor(publicKey: String) {
        this.publicKey = publicKey
    }
}

fun ChatMessage.toEncParams(): EncryptParams {
    return EncryptParams(this)
}

fun ChatTarget.toEncParams(): EncryptParams {
    return EncryptParams(this)
}

fun String.toEncParams(): EncryptParams {
    return EncryptParams(this)
}
