package com.fzm.chat33.core.net.socket

import android.annotation.SuppressLint
import com.fzm.chat33.core.net.WebSocketService

/**
 * @author zhengjy
 * @since 2019/09/05
 * Description:
 */
@Deprecated(message = "准备以依赖注入的方式提供ChatSocket")
class SocketServiceProvider {

    private object SocketServiceHolder {
        @SuppressLint("StaticFieldLeak")
        val chatSocket: ChatSocket = WebSocketService()
    }

    companion object {
        @JvmStatic
        fun provide(): ChatSocket {
            return SocketServiceHolder.chatSocket
        }
    }

}