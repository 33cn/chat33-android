package com.fzm.chat33.core.exception

import com.fzm.chat33.core.Chat33
import com.fzm.chat33.core.R
import com.fzm.chat33.core.consts.SocketCode
import java.lang.RuntimeException

/**
 * @author zhengjy
 * @since 2019/10/10
 * Description:聊天socket通信中的错误码
 */
class ChatSocketException(
        private val errorCode: Int,
        errorMessage: String
) : RuntimeException(errorMessage) {

    /**
     * 动态指定message
     * @param errorCode
     */
    constructor(errorCode: Int): this(errorCode, getExceptionMessage(errorCode))

    fun getErrorCode(): Int {
        return errorCode
    }

    companion object {
        /**
         * 由于服务器传递过来的错误信息直接给用户看的话，用户未必能够理解
         * 需要根据错误码对错误信息进行一个转换，在显示给用户
         *
         * @param code
         * @return
         */
        private fun getExceptionMessage(code: Int): String {
            return when (code) {
                SocketCode.FRIEND_REJECT -> Chat33.getContext().getString(R.string.core_error_friend_reject)
                else -> Chat33.getContext().getString(R.string.core_error_unknown)
            }
        }
    }
}