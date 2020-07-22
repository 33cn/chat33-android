package com.fzm.chat33.core.di

import com.fzm.chat33.core.global.EncryptInterceptor
import com.fzm.chat33.core.logic.MessageDispatcher
import com.fzm.chat33.core.logic.MessageHandler
import com.fzm.chat33.core.manager.MessageManager
import com.fzm.chat33.core.net.RequestManager
import com.fzm.chat33.core.net.socket.ChatSocket
import com.fzm.chat33.core.net.socket.SocketServiceProvider
import org.kodein.di.Kodein
import org.kodein.di.generic.bind
import org.kodein.di.generic.instance
import org.kodein.di.generic.provider
import org.kodein.di.generic.singleton

/**
 * @author zhengjy
 * @since 2019/09/12
 * Description:网络请求相关类注入
 */
fun chatModule() = Kodein.Module("ChatModule") {
    bind<RequestManager>() with singleton { RequestManager.INS }
    bind<ChatSocket>() with provider { SocketServiceProvider.provide() }
    bind<MessageManager>() with provider { MessageManager(instance(), instance(), instance()) }
    bind<MessageHandler>() with singleton { MessageHandler(instance(), instance(), instance(), instance()) }
    bind<MessageDispatcher>() with singleton { MessageDispatcher(instance(), instance(), instance(), instance()) }
    bind<EncryptInterceptor>(tag = "encryptInterceptor") with singleton { EncryptInterceptor() }
}
