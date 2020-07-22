package com.fzm.chat33.core.logic

import android.annotation.SuppressLint
import android.text.TextUtils
import android.util.Log
import com.alibaba.android.arouter.launcher.ARouter
import com.fuzamei.common.net.rxjava.ApiException
import com.fuzamei.common.utils.LogUtils
import com.fuzamei.common.utils.RoomUtils
import com.fuzamei.componentservice.app.AppRoute
import com.fzm.chat33.core.bean.MultipleLogin
import com.fzm.chat33.core.bean.UpdateWords
import com.fzm.chat33.core.db.ChatDatabase
import com.fzm.chat33.core.db.bean.RoomListBean
import com.fzm.chat33.core.event.BaseChatEvent
import com.fzm.chat33.core.global.EventReceiver
import com.fzm.chat33.core.response.MsgSocketResponse
import com.fzm.chat33.core.consts.SocketCode
import java.util.*

import com.fzm.chat33.core.global.Chat33Const.*
import com.fzm.chat33.core.global.LoginInfoDelegate
import com.fzm.chat33.core.net.socket.ChatSocket
import com.fzm.chat33.core.net.socket.ChatSocketListener
import com.fzm.chat33.core.repo.ContactsRepository
import com.google.gson.Gson
import org.kodein.di.Kodein
import org.kodein.di.conf.global
import org.kodein.di.generic.instance
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject
import javax.inject.Singleton

/**
 * @author zhengjy
 * @since 2019/08/30
 * Description:
 */
@Singleton
class MessageDispatcher @Inject constructor(
        private val messageHandler: MessageHandler,
        private val contactData: ContactsRepository,
        private val loginInfoDelegate: LoginInfoDelegate,
        private val chatSocket: ChatSocket
) : ChatSocketListener, LoginInfoDelegate by loginInfoDelegate {

    private val TAG = "MessageDispatcher"

    private val INITIALIZE = AtomicBoolean(false)

    private fun messageDao() = ChatDatabase.getInstance().recentMessageDao()

    private val gson: Gson by Kodein.global.instance()

    fun start() {
        if (!INITIALIZE.getAndSet(true)) {
            // 防止重复调用
            messageHandler.handleBatchMessage { onReceiveEvent(it) }
            Log.d(TAG, "dispatcher start")
        }
        chatSocket.register(this)
        chatSocket.connect()
    }

    override fun onCall(msg: MsgSocketResponse?) {
        if (msg == null) return
        when (msg.eventType) {
            MSG_NORMAL_MESSAGE -> {
                messageHandler.handleMessage(msg) { onReceiveEvent(it) }
            }
            MSG_OTHER_LOGIN -> {
                // 在其他终端登录
                onReceiveEvent(BaseChatEvent(msg.eventType, msg))
            }
            MSG_BANNED_USER -> {
                // 帐号被封禁
                onReceiveEvent(BaseChatEvent(msg.eventType, msg))
            }
            MSG_BANNED_GROUP -> {
                // 群组被封禁
                RoomUtils.run {
                    // 更新数据库
                    messageDao().changeDisableDeadline(msg.roomId, msg.deadline)
                    ChatDatabase.getInstance().roomsDao().changeDisableDeadline(msg.roomId, msg.deadline)
                }
                onReceiveEvent(BaseChatEvent(msg.eventType, msg))
            }
            MSG_ENTER_GROUP -> {
                // 入群通知：1.创建群 2.被邀请者入群 3.直接入群回复
                contactData.getRoomInfoSync(msg.roomId).observeForever {
                    it.dataOrNull()?.let { roomInfoBean ->
                        if (roomInfoBean.memberLevel == 3) {
                            // 如果自己是群主，则直接打开群聊
                            ARouter.getInstance()
                                    .build(AppRoute.CHAT)
                                    .withBoolean("isGroupChat", true)
                                    .withInt("channelType", CHANNEL_ROOM)
                                    .withString("targetName", roomInfoBean.name)
                                    .withString("targetId", roomInfoBean.id)
                                    .navigation()
                        }
                        RoomUtils.run {
                            if (!TextUtils.isEmpty(roomInfoBean.id)) {
                                val listBean = RoomListBean(roomInfoBean)
                                ChatDatabase.getInstance().roomsDao().insert(listBean)
                                messageDao().markDelete(false, CHANNEL_ROOM, roomInfoBean.id)
                            }
                        }
                    }
                }
                onReceiveEvent(BaseChatEvent(msg.eventType, msg))
            }
            MSG_EXIT_GROUP -> {
                // 退群通知
                RoomUtils.run {
                    // 更新数据库
                    if (msg.type == 1) {
                        messageDao().deleteMessage(CHANNEL_ROOM, msg.roomId)
                    } else if (msg.type == 2) {
                        messageDao().changeSticky(msg.roomId, 2)
                        messageDao().changeDisturb(msg.roomId, 2)
                        messageDao().markDelete(true, CHANNEL_ROOM, msg.roomId)
                    }
                    ChatDatabase.getInstance().roomsDao().delete(msg.roomId)
                }
                onReceiveEvent(BaseChatEvent(msg.eventType, msg))
            }
            MSG_DISMISS_GROUP -> {
                // 解散群通知
                RoomUtils.run {
                    // 更新数据库
                    messageDao().changeSticky(msg.roomId, 2)
                    messageDao().changeDisturb(msg.roomId, 2)
                    messageDao().markDelete(true, CHANNEL_ROOM, msg.roomId)
                    ChatDatabase.getInstance().roomsDao().delete(msg.roomId)
                }
                onReceiveEvent(BaseChatEvent(msg.eventType, msg))
            }
            MSG_GROUP_REQUEST ->
                // 入群请求和回复通知
                onReceiveEvent(BaseChatEvent(msg.eventType, msg))
            MSG_GROUP_MUTE ->
                // 群中被禁言、解禁通知
                onReceiveEvent(BaseChatEvent(msg.eventType, msg))
            MSG_SYNC_GROUP_KEY -> {
                // 同步会话密钥
                messageHandler.handleGroupKey(msg.list)
                onReceiveEvent(BaseChatEvent(msg.eventType, msg))
            }
            MSG_SYNC_GROUP_KEY_END -> {
                // 会话密钥同步完成
                msg.complete = true
                messageHandler.batchMessage.onNext(msg)
            }
            MSG_ADD_FRIEND ->
                // 添加好友消息通知
                onReceiveEvent(BaseChatEvent(msg.eventType, msg))
            MSG_DELETE_FRIEND -> {
                // 删除好友消息通知
                RoomUtils.run {
                    // 更新数据库
                    messageDao().changeSticky(msg.senderInfo.id, 2)
                    messageDao().changeDisturb(msg.senderInfo.id, 2)
                    messageDao().markDelete(true, CHANNEL_FRIEND, msg.senderInfo.id)
                    ChatDatabase.getInstance().friendsDao().delete(msg.senderInfo.id)
                }
                onReceiveEvent(BaseChatEvent(msg.eventType, msg))
            }
            MSG_UPDATE_FRIEND_KEY -> {
                // 好友公钥更新
                RoomUtils.run { ChatDatabase.getInstance().friendsDao().changePublicKey(msg.userId, msg.publicKey) }
                onReceiveEvent(BaseChatEvent(msg.eventType, msg))
            }
            MSG_NEW_DEVICE_PUSH -> {
                // 换设备或卸载重装，批量接收最近消息
                LogUtils.d(TAG, "已读推送:" + msg.list.size)
                for (response in msg.list) {
                    response.isRead = true
                }
                messageHandler.batchMessage.onNext(msg)
            }
            MSG_OFFLINE_PUSH -> {
                // 离线未读消息批量推送
                LogUtils.d(TAG, "未读推送:" + msg.list.size)
                for (response in msg.list) {
                    response.isRead = false
                }
                messageHandler.batchMessage.onNext(msg)
            }
            MSG_NORMAL_PUSH_END -> {
                // 同步成功消息
                msg.complete = true
                messageHandler.batchMessage.onNext(msg)
            }
            MSG_FORWARD_PUSH -> {
                // 逐条转发批量推送
                LogUtils.d(TAG, "逐条转发推送:" + msg.list.size)
                val responses = msg.list
                if (responses != null && responses.size > 0) {
                    val showUnread = responses[0].targetId == getUserId()
                    for (response in msg.list) {
                        response.isRead = !showUnread
                    }
                }
                messageHandler.batchMessage.onNext(msg)
            }
            MSG_ACK_PUSH -> {
                // 消息确认，发现丢失后推送
                for (response in msg.list) {
                    response.isRead = response.isSentType
                }
                messageHandler.batchMessage.onNext(msg)
            }
            MSG_ACK_PUSH_END -> {
                msg.complete = true
                messageHandler.batchMessage.onNext(msg)
            }
        }
    }

    override fun onOpen() {

    }

    override fun onClose(e: ApiException) {
        when (e.errorCode) {
            SocketCode.SOCKET_OTHER_LOGIN -> {
                val tips = gson.fromJson(e.message, MultipleLogin::class.java)
                onReceiveEvent(BaseChatEvent(e.errorCode,
                        MsgSocketResponse().apply { content = tips.toString() }))
            }
            SocketCode.SOCKET_OTHER_UPDATE_WORDS -> {
                val tips = gson.fromJson(e.message, UpdateWords::class.java)
                onReceiveEvent(BaseChatEvent(e.errorCode,
                        MsgSocketResponse().apply { content = tips.toString() }))
            }
        }
    }

    private fun onReceiveEvent(event: BaseChatEvent) {
        for (receiver in mEventReceiver) {
            receiver.onReceiveEvent(event)
        }
    }

    private fun dispose() {
        chatSocket.unregister(this)
        mEventReceiver.clear()
        messageHandler.dispose()
    }

    companion object {

        @SuppressLint("StaticFieldLeak")
        private var dispatcher: MessageDispatcher? = null

        /**
         * "正在同步消息"标志
         */
        @JvmStatic
        var syncing = false

        @JvmStatic
        fun reset() {
            mEventReceiver.clear()
            dispatcher?.dispose()
            dispatcher = null
        }

        private val mEventReceiver: LinkedList<EventReceiver> by lazy { LinkedList<EventReceiver>() }

        @JvmStatic
        fun addEventReceiver(eventReceiver: EventReceiver) {
            if (!mEventReceiver.contains(eventReceiver)) {
                mEventReceiver.add(eventReceiver)
            }
        }

        @JvmStatic
        fun hasEventReceiver(eventReceiver: EventReceiver): Boolean {
            return mEventReceiver.contains(eventReceiver)
        }

        @JvmStatic
        fun removeEventReceiver(eventReceiver: EventReceiver) {
            mEventReceiver.remove(eventReceiver)
        }
    }
}