package com.fzm.chat33.core.manager

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.text.TextUtils
import android.util.Log
import com.fuzamei.common.utils.RoomUtils
import com.fuzamei.common.utils.run
import com.fuzamei.componentservice.config.AppConfig
import com.fzm.chat33.core.Chat33
import com.fzm.chat33.core.bean.MessageState
import com.fzm.chat33.core.db.ChatDatabase
import com.fzm.chat33.core.db.bean.ChatMessage
import com.fzm.chat33.core.db.bean.ChatMessage.Type.*
import com.fzm.chat33.core.db.dao.ChatMessageDao
import com.fzm.chat33.core.event.NewMessageEvent
import com.fzm.chat33.core.global.Chat33Const
import com.fzm.chat33.core.global.EventReceiver
import com.fzm.chat33.core.global.LoginInfoDelegate
import com.fzm.chat33.core.net.socket.ChatSocket
import com.fzm.chat33.core.request.RedPacketRequest
import com.fzm.chat33.core.request.chat.*
import com.fzm.chat33.core.source.LocalContactDataSource
import com.google.gson.Gson
import io.reactivex.disposables.Disposable
import io.reactivex.functions.Consumer
import org.kodein.di.Kodein
import org.kodein.di.conf.global
import org.kodein.di.generic.instance
import java.util.concurrent.DelayQueue
import java.util.concurrent.Delayed
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject

/**
 * @author zhengjy
 * @since 2019/07/26
 * Description:
 */
class MessageManager @Inject constructor(
        loginInfo: LoginInfoDelegate,
        private val localData: LocalContactDataSource,
        private val socket: ChatSocket
) : LoginInfoDelegate by loginInfo {

    companion object {
        /**
         * 等待确认消息队列
         */
        private val waitingAckQueue: DelayQueue<SendingMessage> = DelayQueue()

        /**
         * 发送消息缓存
         */
        private val sendingMessage: HashMap<String, SendingMessage> = HashMap(8)

        private var worker: Thread? = null

        private val mainHandler by lazy { Handler(Looper.getMainLooper()) }

        private val eventReceiver = EventReceiver {
            when (it.eventType) {
                0 -> {
                    // 收到消息之后，如果发送消息缓存中包含这条消息，则状态设置为发送成功并执行回调
                    if (it is NewMessageEvent) {
                        val item = it.message
                        val msg = sendingMessage[item.msgId]
                        msg?.message?.messageState = MessageState.SEND_SUCCESS
                        msg?.callback?.invoke(true, item)
                        Log.d("MessageManager", "Message sent success")
                    }
                }
            }
        }

        /**
         * 消息发送Handler
         */
        private var sendHandler: SendHandler? = null

        /**
         * MessageManager是否处于活跃状态
         * 防止更换帐号时，不同账号之间消息混乱
         */
        private var active = AtomicBoolean(false)

        /**
         * 移除发送队列中的消息，调用该方法则视为消息发送失败
         *
         * @param msgId 消息msgId
         */
        @JvmStatic
        fun cancelMessage(msgId: String) {
            sendingMessage.remove(msgId)
        }

        /**
         * 发送队列中是否包含指定消息
         *
         * @param msgId 消息msgId
         */
        @JvmStatic
        fun containsMessage(msgId: String): Boolean {
            return sendingMessage.containsKey(msgId)
        }

        /**
         * 让发送次数已经为3的消息，再发送一次
         */
        @JvmStatic
        fun resendOneMore() {
            for (entry in sendingMessage) {
                if (entry.value.sendTimes == 3) {
                    sendHandler?.sendMessage(Message.obtain().apply {
                        data = Bundle().apply { putString("content", entry.value.content) }
                    })
                }
            }
        }

        /**
         * 清除队列中的所有消息
         */
        @JvmStatic
        fun reset() {
            worker?.interrupt()
            worker = null
            active.set(false)
            waitingAckQueue.clear()
            sendingMessage.clear()
        }
    }

    /**
     * 加密群聊密钥
     */
    private var groupKey: String = ""

    /**
     * 加密群聊密钥id
     */
    private var groupKid: String = ""

    /**
     * 好友加密公钥
     */
    private var publicKey: String = ""

    /**
     * 是否开始加密模式
     */
    private var encryptMode: Boolean = false

    private lateinit var targetId: String

    private var channel: Int = 0
    private val gson: Gson by Kodein.global.instance()

    private var roomKeyDisposable: Disposable? = null
    private var roomInfoDisposable: Disposable? = null
    private var userInfoDisposable: Disposable? = null

    private val chatMessageDao: ChatMessageDao
        get() = ChatDatabase.getInstance().chatMessageDao()

    init {
        if (worker == null) {
            worker = Thread {
                try {
                    while (!Thread.interrupted()) {
                        active.set(true)
                        Chat33.registerEventReceiver(eventReceiver)
                        val msg = waitingAckQueue.take()
                        msg.run()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                } finally {
                    active.set(false)
                    worker = null
                }
            }
            worker?.start()
        }
        sendHandler = SendHandler(socket)
    }

    fun setChatTarget(channel: Int, targetId: String, async: Boolean) {
        this.channel = channel
        this.targetId = targetId
        loadTargetInfo(async)
    }

    fun setChatTarget(channel: Int, targetId: String) {
        setChatTarget(channel, targetId, true)
    }

    fun dispose() {
        roomKeyDisposable?.dispose()
        roomInfoDisposable?.dispose()
        userInfoDisposable?.dispose()
    }

    private fun loadTargetInfo(async: Boolean) {
        if (async) {
            if (channel == Chat33Const.CHANNEL_ROOM) {
                roomInfoDisposable = ChatDatabase.getInstance().roomsDao().getRoomById(targetId).run(Consumer {
                    encryptMode = it.encrypt == 1
                    if (encryptMode) {
                        updateGroupKey()
                    }
                })
            } else {
                userInfoDisposable = ChatDatabase.getInstance().friendsDao().getFriendById(targetId).run(Consumer {
                    publicKey = it.publicKey
                })
            }
        } else {
            if (channel == Chat33Const.CHANNEL_ROOM) {
                localData.getLocalRoomById(targetId)?.also {
                    encryptMode = it.encrypt == 1
                    if (encryptMode) {
                        val roomKey = ChatDatabase.getInstance().roomKeyDao().getLatestKey(targetId)
                        if (roomKey != null) {
                            groupKey = roomKey.keySafe
                            groupKid = roomKey.kid
                        }
                    }
                }
            } else {
                localData.getLocalFriendById(targetId)?.also {
                    publicKey = it.publicKey
                }
            }
        }
    }

    /**
     * 更新群的会话密钥
     */
    private fun updateGroupKey() {
        roomKeyDisposable = ChatDatabase.getInstance().roomKeyDao().loadLatestKey(targetId).run(Consumer {
            groupKey = it.keySafe
            groupKid = it.kid
        })
    }

    /**
     * 发送消息后，立刻调用[dispose]
     */
    fun enqueueAndDispose(message: ChatMessage) {
        enqueue(message, true) { _, _ -> }
    }

    fun enqueue(message: ChatMessage) {
        enqueue(message, false) { _, _ -> }
    }

    fun enqueue(sentMsg: ChatMessage, dispose: Boolean, callback: (Boolean, ChatMessage) -> Unit) {
        val messageRequest = MessageRequest()
        messageRequest.eventType = 0
        messageRequest.msgId = sentMsg.msgId
        messageRequest.channelType = sentMsg.channelType
        messageRequest.targetId = targetId
        messageRequest.isSnap = sentMsg.isSnap
        messageRequest.msgType = sentMsg.msgType

        if (isLogin()) {
            if (!TextUtils.isEmpty(currentUser.value?.avatar)) {
                sentMsg.senderInfo.avatar = currentUser.value?.avatar
            }

            sentMsg.senderInfo.userLevel = currentUser.value?.user_level ?: 0
            sentMsg.senderInfo.nickname = currentUser.value?.username
        }

        when (messageRequest.msgType) {
            SYSTEM -> {
                messageRequest.msg = TextRequest().apply {
                    content = sentMsg.msg.content
                }
            }
            TEXT -> {
                val textRequest = TextRequest().apply {
                    content = sentMsg.msg.content
                    aitList = sentMsg.msg.aitList
                }
                messageRequest.msg = getMessageRequest(textRequest, sentMsg)
            }
            AUDIO -> {
                val mediaRequest = MediaRequest().apply {
                    mediaUrl = sentMsg.msg.mediaUrl
                    time = sentMsg.msg.duration
                }
                messageRequest.msg = getMessageRequest(mediaRequest, sentMsg)
            }
            IMAGE -> {
                val imageRequest = ImageRequest().apply {
                    imageUrl = sentMsg.msg.imageUrl
                    height = sentMsg.msg.height
                    width = sentMsg.msg.width
                }
                messageRequest.msg = getMessageRequest(imageRequest, sentMsg)
            }
            RED_PACKET -> {
                val redPacketRequest = RedPacketRequest().apply {
                    coin = sentMsg.msg.coin
                    coinName = sentMsg.msg.coinName
                    packetId = sentMsg.msg.packetId
                    packetUrl = sentMsg.msg.packetUrl
                    packetMode = sentMsg.msg.packetMode
                    packetType = sentMsg.msg.packetType
                    remark = sentMsg.msg.redBagRemark
                }
                messageRequest.msg = redPacketRequest
            }
            VIDEO -> {
                val videoRequest = VideoRequest().apply {
                    mediaUrl = sentMsg.msg.mediaUrl
                    time = sentMsg.msg.duration
                    height = sentMsg.msg.height
                    width = sentMsg.msg.width
                }
                messageRequest.msg = getMessageRequest(videoRequest, sentMsg)
            }
            FILE -> {
                val fileRequest = FileRequest().apply {
                    fileUrl = sentMsg.msg.fileUrl
                    fileName = sentMsg.msg.fileName
                    fileSize = sentMsg.msg.fileSize
                    md5 = sentMsg.msg.md5
                }
                messageRequest.msg = getMessageRequest(fileRequest, sentMsg)
            }
            TRANSFER -> {
                val transactionRequest = TransactionRequest().apply {
                    coinName = sentMsg.msg.coinName
                    amount = sentMsg.msg.amount
                    recordId = sentMsg.msg.recordId
                }
                messageRequest.msg = transactionRequest
            }
            RECEIPT -> {
                val receiptRequest = ReceiptRequest().apply {
                    coinName = sentMsg.msg.coinName
                    amount = sentMsg.msg.amount
                }
                messageRequest.msg = receiptRequest
            }
            else -> messageRequest.msg = sentMsg.msg
        }
        sentMsg.messageState = MessageState.SENDING
        sentMsg.sendTime = System.currentTimeMillis()

        val text = gson.toJson(messageRequest)
        val msg = SendingMessage(sentMsg, text, callback)
        sendingMessage[sentMsg.msgId] = msg
        waitingAckQueue.put(msg)
        Log.d("MessageManager", text)
        socket.send(text)
        RoomUtils.run {
            chatMessageDao.insert(sentMsg)
        }
        if (dispose) {
            dispose()
        }
    }

    /**
     * 根据当前设置确定是否进行加密
     *
     * @param request
     * @param sentMsg
     * @return
     */
    private fun getMessageRequest(request: Any, sentMsg: ChatMessage): Any {
        if (!AppConfig.APP_ENCRYPT) {
            return request
        }
        val result: BaseChatRequest
        if (channel == Chat33Const.CHANNEL_FRIEND) {
            if (!TextUtils.isEmpty(publicKey) && !TextUtils.isEmpty(CipherManager.getPrivateKey())) {
                result = BaseChatRequest()
                result.fromKey = CipherManager.getPublicKey()
                result.toKey = publicKey
                try {
                    result.encryptedMsg = CipherManager.encryptString(gson.toJson(request), publicKey, CipherManager.getPrivateKey())
                    sentMsg.encrypted = 1
                } catch (e: Exception) {
                    return request
                }
            } else {
                return request
            }
        } else {
            // 暂时保留非加密群
            if (encryptMode) {
                result = BaseChatRequest()
                result.kid = groupKid
                try {
                    result.encryptedMsg = CipherManager.encryptSymmetric(gson.toJson(request), groupKey)
                    sentMsg.encrypted = 1
                } catch (e: Exception) {
                    return request
                }

            } else {
                return request
            }
        }
        return result
    }

    class SendingMessage(val message: ChatMessage, val content: String, val callback: (Boolean, ChatMessage) -> Unit) : Delayed, Runnable {

        var sendTimes = 1

        override fun run() {
            if (!active.get()) {
                return
            }
            if (message.messageState == MessageState.SEND_SUCCESS) {
                // 如果发送状态为发送成功，则直接修改数据库，不进入下面逻辑
                sendingMessage.remove(message.msgId)
                updateMessageState()
                return
            }
            when (sendTimes) {
                1, 2 -> {
                    if (sendingMessage.containsKey(message.msgId)) {
                        message.sendTime = System.currentTimeMillis()
                        sendTimes++
                        waitingAckQueue.put(this)
                        content.sendToTarget()
                        Log.d("MessageManager", "${if (sendTimes == 2) "2nd" else "3rd"} attempt to send")
                    } else {
                        // 外部调用cancelMessage()视为该消息发送失败
                        message.messageState = MessageState.SEND_FAIL
                        updateMessageState()
                    }
                }
                3 -> {
                    if (sendingMessage.containsKey(message.msgId)) {
                        sendingMessage.remove(message.msgId)
                        Log.d("MessageManager", "Failed after trying to send 3 times")
                        mainHandler.post { callback(false, message) }
                    }
                    message.messageState = MessageState.SEND_FAIL
                    updateMessageState()
                }
            }
        }

        private fun updateMessageState() {
            RoomUtils.run {
                ChatDatabase.getInstance().chatMessageDao()
                        .updateMessageState(message.channelType, message.logId, message.messageState)
            }
        }

        override fun compareTo(other: Delayed?): Int {
            if (other == null || other !is SendingMessage) return 1
            if (other === this) return 0
            return when {
                this.message.sendTime > other.message.sendTime -> 1
                this.message.sendTime == other.message.sendTime -> 0
                else -> -1
            }
        }

        override fun getDelay(unit: TimeUnit): Long {
            return unit.convert(message.sendTime + 10_000L - System.currentTimeMillis(), TimeUnit.MILLISECONDS)
        }

        private fun String.sendToTarget() {
            sendHandler?.sendMessage(Message.obtain().apply {
                data = Bundle().apply { putString("content", this@sendToTarget) }
            })
        }
    }

    class SendHandler(private val socket: ChatSocket) : Handler() {
        override fun handleMessage(msg: Message?) {
            val message = msg?.data?.getString("content", "")
            if (!message.isNullOrEmpty()) {
                socket.send(message)
            }
        }
    }
}