package com.fzm.chat33.core.net.socket

/**
 * @author zhengjy
 * @since 2019/09/04
 * Description:通信连接接口
 */
interface ChatSocket {

    /**
     * 注册socket消息回调
     */
    fun register(listener: ChatSocketListener)

    /**
     * 解除socket消息回调
     */
    fun unregister(listener: ChatSocketListener)

    /**
     * 注册socket状态变化回调
     */
    fun addSocketStateChangeListener(listener: SocketStateChangeListener)

    /**
     * 解除socket状态变化回调
     */
    fun removeSocketStateChangeListener(listener: SocketStateChangeListener)

    /**
     * 建立连接
     */
    fun connect()

    /**
     * 断开连接
     */
    fun disconnect()

    /**
     * 发送字符串消息
     */
    fun send(message: String)

    /**
     * 发送字节数组消息
     */
    fun send(message: ByteArray)

    /**
     * 连接是否存活
     */
    fun isAlive(): Boolean
}