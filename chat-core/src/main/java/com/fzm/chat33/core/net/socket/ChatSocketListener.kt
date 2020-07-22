package com.fzm.chat33.core.net.socket

import com.fuzamei.common.net.rxjava.ApiException
import com.fzm.chat33.core.response.MsgSocketResponse

/**
 * @author zhengjy
 * @since 2019/09/04
 * Description:连接消息与状态回调
 */
interface ChatSocketListener {

    /**
     * 收到连接消息
     *
     * @param msg   消息结构
     */
    fun onCall(msg: MsgSocketResponse?)

    /**
     * 连接建立回调
     */
    fun onOpen()

    /**
     * 连接关闭回调
     *
     * @param e 连接关闭原因
     */
    fun onClose(e: ApiException)
}
