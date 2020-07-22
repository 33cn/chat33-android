package com.fzm.chat33.core.logic

import android.annotation.SuppressLint
import android.content.Context
import android.media.RingtoneManager
import android.os.PowerManager
import android.text.TextUtils
import android.util.Log
import com.baidu.crabsdk.CrabSDK
import com.fuzamei.common.bus.LiveBus
import com.fuzamei.common.executor.AppExecutors
import com.fuzamei.common.utils.*
import com.fuzamei.componentservice.app.BusEvent
import com.fuzamei.componentservice.config.AppPreference
import com.fuzamei.componentservice.config.AppConfig
import com.fzm.chat33.core.Chat33
import com.fzm.chat33.core.R
import com.fzm.chat33.core.bean.MessageState
import com.fzm.chat33.core.bean.NotificationBean
import com.fzm.chat33.core.bean.SyncSignal
import com.fzm.chat33.core.consts.PraiseAction.ACTION_LIKE
import com.fzm.chat33.core.consts.PraiseAction.ACTION_REWARD
import com.fzm.chat33.core.consts.SocketCode
import com.fzm.chat33.core.db.ChatDatabase
import com.fzm.chat33.core.db.bean.*
import com.fzm.chat33.core.db.bean.ChatMessage.Type.*
import com.fzm.chat33.core.db.dao.ChatMessageDao
import com.fzm.chat33.core.event.BaseChatEvent
import com.fzm.chat33.core.event.NewMessageEvent
import com.fzm.chat33.core.event.NewMessageListEvent
import com.fzm.chat33.core.event.NotificationEvent
import com.fzm.chat33.core.exception.ChatSocketException
import com.fzm.chat33.core.global.Chat33Const.*
import com.fzm.chat33.core.global.LoginInfoDelegate
import com.fzm.chat33.core.global.UserInfo
import com.fzm.chat33.core.logic.MessageDispatcher.Companion.syncing
import com.fzm.chat33.core.manager.CipherException
import com.fzm.chat33.core.manager.CipherManager
import com.fzm.chat33.core.manager.MessageManager
import com.fzm.chat33.core.net.socket.ChatSocket
import com.fzm.chat33.core.provider.InfoProvider
import com.fzm.chat33.core.provider.SyncInfoStrategy
import com.fzm.chat33.core.request.CheckMessageRequest
import com.fzm.chat33.core.request.SyncRoomKeyRequest
import com.fzm.chat33.core.response.MsgSocketResponse
import com.fzm.chat33.core.source.LocalContactDataSource
import com.fzm.chat33.core.utils.UserInfoPreference
import com.google.gson.Gson
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import kotlinx.coroutines.*
import org.kodein.di.Kodein
import org.kodein.di.conf.global
import org.kodein.di.generic.instance
import java.lang.NullPointerException
import java.lang.RuntimeException
import java.util.*
import java.util.concurrent.locks.ReentrantLock
import javax.inject.Inject
import javax.inject.Singleton

/**
 * @author zhengjy
 * @since 2019/07/29
 * Description:消息处理器
 */
@Singleton
class MessageHandler @Inject constructor(
        private val context: Context,
        private val chatSocket: ChatSocket,
        private val loginInfoDelegate: LoginInfoDelegate,
        private val localContactDataSource: LocalContactDataSource
): LoginInfoDelegate by loginInfoDelegate {

    private fun chatDao() = ChatDatabase.getInstance().chatMessageDao()
    private fun messageDao() = ChatDatabase.getInstance().recentMessageDao()
    private fun roomKeyDao() = ChatDatabase.getInstance().roomKeyDao()
    private fun roomDao() = ChatDatabase.getInstance().roomsDao()
    private val gson: Gson by Kodein.global.instance()

    private val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager

    private var lastNotificationTime = 0L

    private var mDisposable: Disposable? = null

    /**
     * 消息确认到了当前最新，需要降低确认频率
     */
    private var ackToCurrent = false

    /**
     * 已经开始了消息确认请求流程
     */
    private var alreadyAck = false

    val batchMessage: PublishSubject<MsgSocketResponse> = PublishSubject.create()

    companion object {

        private val LOCK = ReentrantLock()

        private const val TAG = "MessageHandler"
    }

    fun dispose() {
        mDisposable?.dispose()
    }

    /**
     * 处理单条消息
     *
     * @param msg       收到的消息
     * @param callback  处理完消息之后的回调
     */
    @SuppressLint("CheckResult")
    fun handleMessage(msg: MsgSocketResponse, callback: (BaseChatEvent) -> Unit) {
        Observable.just(1)
                .subscribeOn(Schedulers.from(AppExecutors.databaseThreadPool()))
                .flatMap {
                    // 检查返回消息是否正常
                    val item = parseMessage(msg)
                    if (TextUtils.isEmpty(item.senderId) || TextUtils.isEmpty(item.receiveId)) {
                        // 如果消息来自聊天室则没有最近消息
                        // 或者消息发送、接收者为空
                        throw RuntimeException()
                    }
                    val id = item.getTargetId(getUserId())
                    if (localContactDataSource.isLocalBlock(id) && !item.isSentType) {
                        Log.d(TAG, "来自黑名单消息被数据库忽略，用户id：${id}")
                        item.ignoreInHistory = 1
                    }
                    processMessage(item)
                    // 将消息存入数据库
                    if (item.shouldSave()) {
                        if (Chat33.getLocalCache().localPathMap.containsKey(item.msgId)) {
                            item.msg.localPath = Chat33.getLocalCache().localPathMap[item.msgId]
                            Chat33.getLocalCache().localPathMap.remove(item.msgId)
                        }
                        item.messageState = MessageState.SEND_SUCCESS
                        if (item.msgType == TEXT) {
                            if (item.msg.aitList != null) {
                                for (aitId in item.msg.aitList) {
                                    if (aitId == getUserId() || aitId == "-1") {
                                        item.beAit = true
                                    }
                                }
                            }
                        } else if (item.msgType == FORWARD) {
                            handleForwardMessageContent(item)
                        } else if (item.msgType == TRANSFER) {
                            item.msg.recordId = item.msg.recordId.split(",".toRegex())[if (item.isSentType) 0 else 1]
                        }
                        if (item.isSentType && !item.msgId.isNullOrEmpty()) {
                            // 先删除本地logId等于msgId的消息
                            chatDao().deleteMessage(item.channelType, item.msgId)
                        }
                        chatDao().insert(item)
                    }
                    if (!syncing) {
                        // 如果没有正在进行消息同步，则更新最新消息时间
                        LogUtils.d("最新消息datetime:${ToolUtils.formatLogTime(item.sendTime)}")
                        val last = UserInfoPreference.getInstance().getLongPref(UserInfoPreference.LATEST_MSG_TIME, 0)
                        if (item.sendTime > last) {
                            UserInfoPreference.getInstance().setLongPref(UserInfoPreference.LATEST_MSG_TIME, item.sendTime)
                        }
                    }
                    LOCK.lock()
                    val oldMsg = messageDao().getRecentMsgById(item.channelType, id)
                    val count = oldMsg?.number ?: 0
                    val oldPraise = oldMsg?.praise ?: RecentMessage.PraiseNum()
                    if (!item.notChangeRecent()) {
                        // 通知中type=15,19不影响会话列表
                        LOCK.unlock()
                        Observable.just(item)
                    } else {
                        var sticky = 2
                        var disturb = 2
                        var address: String? = null
                        var deadline: Long = 0
                        if (item.channelType == CHANNEL_ROOM) {
                            localContactDataSource.getLocalRoomById(id)?.apply {
                                sticky = onTop
                                disturb = noDisturbing
                                address = depositAddress
                                deadline = disableDeadline
                            }
                        } else {
                            localContactDataSource.getLocalFriendById(id)?.apply {
                                sticky = onTop
                                disturb = noDisturbing
                                address = depositAddress
                            }
                        }
                        val previous = messageDao().getRecentMsgById(item.channelType, id)
                        val message: RecentMessage?
                        val currentId = AppPreference.CURRENT_TARGET_ID
                        if (item.isSentType) {
                            // 如果是自己账号的消息或焚毁消息，则不改变新消息红点数目
                            // 如果是个有赞赏操作的通知
                            val praise = when (item.msg.action) {
                                ACTION_LIKE -> oldPraise.apply { like() }
                                ACTION_REWARD -> oldPraise.apply { reward() }
                                else -> oldPraise
                            }
                            val lastLog = if (item.msg.action.isNullOrEmpty()) {
                                RecentMessage.LastLogBean(item)
                            } else {
                                // 如果收到了点赞通知，则显示原先的消息，如果原先没有消息，则显示空文本
                                previous?.lastLog ?: RecentMessage.LastLogBean().apply {
                                    this.msgType = TEXT
                                    this.msg = ChatFile.newText("")
                                }
                            }
                            message = if (currentId == id) {
                                RecentMessage(id, address, deadline, count, sticky, disturb, false,
                                        false, praise, lastLog)
                            } else {
                                RecentMessage(id, address, deadline, count, sticky, disturb, false,
                                        if (previous?.beAit() == true) true else item.beAit, praise, lastLog)
                            }
                        } else {
                            if (disturb != 1 && item.msgType != NOTIFICATION && currentId != id) {
                                if (ActivityUtils.isBackground() || !powerManager.isScreenOn) {
                                    // 当应用在后台时，发送通知栏通知
                                    val title: String = if (item.channelType == CHANNEL_ROOM) {
                                        localContactDataSource.getLocalRoomById(id)?.displayName ?: ""
                                    } else {
                                        localContactDataSource.getLocalFriendById(id)?.displayName ?: ""
                                    }
                                    var content = when(item.msgType) {
                                        SYSTEM -> context.getString(R.string.core_msg_type1) + item.msg.content
                                        TEXT -> if (item.msg.content == null) context.getString(R.string.core_msg_type11) else item.msg.content
                                        AUDIO -> context.getString(R.string.core_msg_type2)
                                        IMAGE -> context.getString(R.string.core_msg_type3)
                                        RED_PACKET -> context.getString(R.string.core_msg_type4) + (item.msg.redBagRemark ?:"")
                                        VIDEO -> context.getString(R.string.core_msg_type5)
                                        NOTIFICATION -> context.getString(R.string.core_msg_type6) + if (item.msg.content == null) "" else item.msg.content
                                        FORWARD -> context.getString(R.string.core_msg_type7)
                                        FILE -> context.getString(R.string.core_msg_type12) + item.msg.fileName
                                        TRANSFER -> if (item.isSentType) {
                                            context.getString(R.string.core_msg_type8) + context.getString(R.string.core_msg_type_transfer_out)
                                        } else {
                                            context.getString(R.string.core_msg_type8) + context.getString(R.string.core_msg_type_transfer_in)
                                        }
                                        RECEIPT -> if (item.isSentType) {
                                            context.getString(R.string.core_msg_type9) + context.getString(R.string.core_msg_type_receipt_out)
                                        } else {
                                            context.getString(R.string.core_msg_type9) + context.getString(R.string.core_msg_type_receipt_in)
                                        }
                                        INVITATION -> if (item.isSentType) {
                                            context.getString(R.string.core_msg_type14)
                                        } else {
                                            context.getString(R.string.core_msg_type13)
                                        }
                                        else -> ""
                                    }
                                    if (item.isSnap == 1) {
                                        content = context.getString(R.string.core_msg_type10)
                                    }
                                    val key = item.senderId
                                    val friendBean = localContactDataSource.getLocalFriendById(key)
                                    val from = if (friendBean != null && !TextUtils.isEmpty(friendBean.remark)) {
                                        "${friendBean.displayName}: "
                                    } else if (Chat33.loadRoomUserFromCache(item.receiveId, item.senderId) != null) {
                                        "${Chat33.loadRoomUserFromCache(item.receiveId, item.senderId)?.displayName}: "
                                    } else {
                                        "${item.senderInfo.nickname}: "
                                    }
                                    if (CHANNEL_FRIEND == item.channelType) {
                                        callback(NotificationEvent(msg.eventType, msg, NotificationBean(id, title, content, item.channelType, count + 1)))
                                    } else {
                                        callback(NotificationEvent(msg.eventType, msg, NotificationBean(id, title, from + content, item.channelType, count + 1)))
                                    }
                                } else if (System.currentTimeMillis() - lastNotificationTime > 1000L) {
                                    // 消息通知间隔大于1s，才发出提示音
                                    val uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
                                    RingtoneManager.getRingtone(Chat33.getContext(), uri).play()
                                }
                                lastNotificationTime = System.currentTimeMillis()
                            }
                            message = when {
                                item.ignoreInHistory == 1 || !item.shouldSave() -> {
                                    previous?.lastLog?.let {
                                        RecentMessage(id, address, deadline, count, sticky, disturb, false,
                                                if (previous.beAit()) true else item.beAit, oldPraise, it)
                                    }
                                }
                                currentId == id -> {
                                    // 如果正在当前聊天界面内，则不计算新消息
                                    RecentMessage(id, address, deadline, 0, sticky, disturb, false,
                                            false, oldPraise, RecentMessage.LastLogBean(item))
                                }
                                else -> {
                                    RecentMessage(id, address, deadline, count + 1, sticky, disturb, false,
                                            if (previous?.beAit() == true) true else item.beAit, oldPraise, RecentMessage.LastLogBean(item))
                                }
                            }
                        }
                        message?.let {
                            messageDao().insert(it)
                        }
                        LOCK.unlock()
                        Observable.just(item)
                    }
                }
                .doOnError {
                    Log.e(TAG, it.message ?: "handle message error")
                    if (LOCK.isLocked) {
                        LOCK.unlock()
                    }
                }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ message ->
                    callback(NewMessageEvent(msg.eventType, msg, message))
                }, { }, { })
    }

    /**
     * 处理批量消息
     *
     * @param callback      处理完消息之后的回调
     */
    @SuppressLint("CheckResult")
    fun handleBatchMessage(callback: (BaseChatEvent) -> Unit) {
        mDisposable = batchMessage
                .observeOn(Schedulers.from(AppExecutors.databaseThreadPool()))
                .subscribeOn(Schedulers.from(AppExecutors.databaseThreadPool()))
//                .concatMapDelayError {msgSocketResponse ->
                .concatMap { msgSocketResponse ->
                    val eventType = msgSocketResponse.eventType
                    if (msgSocketResponse.list.isNullOrEmpty() && msgSocketResponse.complete) {
                        // complete为true代表此次批量推送完成
                        return@concatMap Observable.just(SyncSignal(eventType, msgSocketResponse, true))
                    }
                    msgSocketResponse.list.lastOrNull()?.let {
                        LogUtils.d("batch最新消息datetime:${ToolUtils.formatLogTime(it.datetime)}")
                        val last = UserInfoPreference.getInstance().getLongPref(UserInfoPreference.LATEST_MSG_TIME, 0)
                        if (it.datetime > last) {
                            UserInfoPreference.getInstance().setLongPref(UserInfoPreference.LATEST_MSG_TIME, it.datetime)
                        }
                    }
                    val chatMessages = ArrayList<ChatMessage>()
                    for (response in msgSocketResponse.list) {
                        val item = response.newChatMessageDb()
                        val targetId = item.getTargetId(getUserId())
                        if (localContactDataSource.isLocalBlock(targetId) && !item.isSentType) {
                            Log.d(TAG, "来自黑名单的批量消息被数据库忽略，用户id：${targetId}")
                            item.ignoreInHistory = 1
                        }
                        if (eventType == MSG_NEW_DEVICE_PUSH) {
                            // 新设备只处理普通通知
                            handleNormalNotification(item)
                        } else {
                            processMessage(item)
                        }
                        if (item.msgType == TEXT) {
                            if (item.msg.aitList != null) {
                                for (id in item.msg.aitList) {
                                    if (id == getUserId() || id == "-1") {
                                        item.beAit = true
                                    }
                                }
                            }
                        }
                        if (item.shouldSave()) {
                            if (item.msgType == FORWARD) {
                                handleForwardMessageContent(item)
                            } else if (item.msgType == TRANSFER) {
                                item.msg.recordId = item.msg.recordId
                                        .split(",".toRegex())[if (item.isSentType) 0 else 1]
                            } else if (item.msgType == RECEIPT) {
                                if (!TextUtils.isEmpty(item.msg.recordId)) {
                                    item.msg.recordId = item.msg.recordId
                                            .split(",".toRegex())[if (item.isSentType) 1 else 0]
                                }
                            }
                            item.messageState = MessageState.SEND_SUCCESS
                            if (item.isSentType && !item.msgId.isNullOrEmpty()) {
                                chatDao().deleteMessage(item.channelType, item.msgId)
                            }
                            if (eventType == MSG_ACK_PUSH) {
                                // 消息确认推送过来的消息，不处理阅后即焚消息
                                if (item.isSnap != 1) {
                                    chatMessages.add(item)
                                }
                            } else {
                                chatMessages.add(item)
                            }
                        }
                    }
                    // 消息插入聊天消息表
                    chatDao().insert(chatMessages)
                    AndroidSchedulers.mainThread().createWorker().schedule {
                        callback(NewMessageListEvent(eventType, null, chatMessages))
                    }
                    val recentMessageMap = HashMap<String, RecentMessage>(256, 0.8f)
                    LOCK.lock()
                    for (i in chatMessages.indices) {
                        val msg = chatMessages[i]
                        val id = msg.getTargetId(getUserId())
                        val showUnread = !msg.isRead
                        val count: Int
                        var sticky = 2
                        var disturb = 2
                        var address: String? = null
                        var deadline: Long = 0
                        if (localContactDataSource.isLocalBlock(id) && !msg.isSentType) {
                            Log.d(TAG, "来自黑名单的批量消息被消息列表忽略，用户id：${id}")
                            continue
                        }
                        // 查询用户配置：置顶，免打扰
                        if (msg.channelType == CHANNEL_ROOM) {
                            localContactDataSource.getLocalRoomById(id)?.apply {
                                sticky = onTop
                                disturb = noDisturbing
                                address = depositAddress
                                deadline = disableDeadline
                            }
                        } else {
                            localContactDataSource.getLocalFriendById(id)?.apply {
                                sticky = onTop
                                disturb = noDisturbing
                                address = depositAddress
                            }
                        }
                        val recentMsg = recentMessageMap["${msg.channelType}-$id"] ?: messageDao().getRecentMsgById(msg.channelType, id)
                        var latest: ChatMessage? = null
                        var initPraise: RecentMessage.PraiseNum
                        if (recentMsg == null) {
                            if (msg.shouldSave()) {
                                latest = msg
                            }
                            count = 0
                            initPraise = RecentMessage.PraiseNum()
                        } else {
                            if (recentMessageMap.containsKey("${msg.channelType}-$id")) {
                                val temp = recentMessageMap["${msg.channelType}-$id"]
                                if (msg.sendTime > temp?.lastLog?.datetime ?: 0) {
                                    if (msg.shouldSave()) {
                                        latest = msg
                                    }
                                }
                                count = temp?.number ?: 0
                                initPraise = temp?.praise ?: RecentMessage.PraiseNum()
                            } else {
                                if (msg.sendTime > recentMsg.lastLog.datetime) {
                                    if (msg.shouldSave()) {
                                        latest = msg
                                    }
                                }
                                count = recentMsg.number
                                initPraise = recentMsg.praise
                            }
                        }
                        // 如果是个有赞赏操作的通知
                        val praise = when (msg.msg.action) {
                            ACTION_LIKE -> initPraise.apply { like() }
                            ACTION_REWARD -> initPraise.apply { reward() }
                            else -> initPraise
                        }
                        when {
                            msg.ignoreInHistory == 1 -> {
                                // 消息被忽略则不影响会话列表
                                null
                            }
                            latest != null -> {
                                // latest不为空表示latest这条消息是其会话的最新消息
                                RecentMessage(id, address, deadline, if (showUnread) count + 1 else count,
                                        sticky, disturb, false, latest.beAit, praise, RecentMessage.LastLogBean(latest))
                            }
                            else -> {
                                // latest为空则只需要修改未读数量，不用修改最新消息
                                // 如果收到了点赞通知，则显示原先的消息，如果原先没有消息，则显示空文本
                                RecentMessage(id, address, deadline, if (showUnread && msg.shouldSave()) count + 1 else count,
                                        sticky, disturb, false, msg.beAit, praise, recentMsg?.lastLog
                                        ?: RecentMessage.LastLogBean().apply {
                                            this.msgType = TEXT
                                            this.msg = ChatFile.newText("")
                                        })
                            }
                        }?.let {
                            recentMessageMap["${msg.channelType}-$id"] = it
                        }
                    }
                    val recentMessages = ArrayList<RecentMessage>()
                    for ((_, value) in recentMessageMap) {
                        recentMessages.add(value)
                    }
                    messageDao().insert(recentMessages)
                    LOCK.unlock()
                    return@concatMap Observable.just(SyncSignal(eventType, msgSocketResponse,  false))
                }
                .doOnError {
                    Log.e(TAG, it.message ?: "handle batch message error")
                    if (LOCK.isLocked) {
                        LOCK.unlock()
                    }
                }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    Log.d(TAG, "eventType:${it.eventType}, complete:${it.complete}")
                    if (it.complete) {
                        when (it.eventType) {
                            MSG_SYNC_GROUP_KEY_END -> {
                                // 群密钥推送完成
                                UserInfoPreference.getInstance().setLongPref(UserInfoPreference.OLDEST_MSG_TIME, 0)
                            }
                            MSG_NORMAL_PUSH_END -> {
                                // 消息推送完成
                                syncing = false
                                requestGroupKey()
                                if (!alreadyAck) {
                                    alreadyAck = true
                                    checkMessage(0)
                                }
                            }
                            MSG_ACK_PUSH_END -> {
                                // 消息确认完成，开始下一次消息确认，取消息确认的end作为下一次确认的开始
                                UserInfoPreference.getInstance().setLongPref(UserInfoPreference.SYNC_MSG_TIME, it.msg.end)
                                checkMessage(it.msg.end)
                            }
                        }
                    }
                }, { }, { })
    }

    /**
     * 向服务端确认消息完整性
     * 如果上一次已经确认到最新，则延迟15s进行确认
     *
     * 如果出现了频繁向服务端发出确认消息请求，则可能是[handleBatchMessage]被调用了多次
     *
     */
    private fun checkMessage(begin: Long) = GlobalScope.launch(Dispatchers.Main) {
        if (ackToCurrent) {
            delay(15_000)
        }
        val start = if (begin == 0L) {
            UserInfoPreference.getInstance().getLongPref(UserInfoPreference.SYNC_MSG_TIME, System.currentTimeMillis())
        } else {
            begin
        }
        val end = if (start + 20_000L > System.currentTimeMillis()) {
            ackToCurrent = true
            -1
        } else {
            ackToCurrent = false
            start + 20_000L
        }
        val count = withContext(Dispatchers.IO) {
            chatDao().getChatMessages(start, if (end == -1L) Long.MAX_VALUE else end)
        }
        val request = CheckMessageRequest(start, end, count)
        chatSocket.send(gson.toJson(request))
    }

    /**
     * 向服务端请求群密钥列表
     *
     */
    private fun requestGroupKey() {
        val oldest = UserInfoPreference.getInstance().getLongPref(UserInfoPreference.OLDEST_MSG_TIME, java.lang.Long.MAX_VALUE)
        if (oldest != 0L) {
            chatSocket.send(gson.toJson(SyncRoomKeyRequest(oldest)))
        }
    }

    fun handleGroupKey(messages: List<MsgSocketResponse>) {
        // 更新群密钥消息
        RoomUtils.run {
            val roomKeys = mutableListOf<RoomKey>()
            for (msg in messages) {
                val item = msg.newChatMessageDb()
                if (item.receiveId != getUserId()) {
                    // 只处理发给自己的密钥通知
                    continue
                }
                val roomKey = if (CipherManager.hasDHKeyPair()) {
                    val key = CipherManager.decryptString(item.msg.key, item.msg.fromKey, CipherManager.getPrivateKey())
                    if (key == item.msg.key) {
                        RoomKey(item.msg.roomId, item.msg.kid, item.msg.key, item.msg.fromKey)
                    } else {
                        RoomKey(item.msg.roomId, item.msg.kid, key)
                    }
                } else {
                    RoomKey(item.msg.roomId, item.msg.kid, item.msg.key, item.msg.fromKey)
                }
                roomKeys.add(roomKey)
            }
            roomKeyDao().insert(roomKeys)
        }
    }

    /**
     * 解析消息整体结构，将服务端返回的消息结构转换为本地消息结构
     *
     * @param msg   服务端返回的消息结构
     */
    @Throws(Exception::class)
    private fun parseMessage(msg: MsgSocketResponse): ChatMessage {
        return when (msg.code) {
            SocketCode.SUCCESS -> msg.newChatMessageDb()
            SocketCode.FRIEND_REJECT -> {
                // 查找被拒收的消息
                val message = chatDao().getMessageByMsgId(msg.msgId)
                        ?: throw NullPointerException()
                val chatFile = ChatFile().apply {
                    chatFileType = NOTIFICATION
                    logId = message.logId
                    type = FRIEND_REJECT_MSG
                    content = ChatSocketException(msg.code).message
                }
                MessageManager.cancelMessage(msg.msgId)
                // 本地显示一条被拒收的通知
                ChatMessage.create(message.receiveId, message.channelType,
                        NOTIFICATION, 2, chatFile).apply {
                    messageState = MessageState.SEND_FAIL
                }
            }
            else -> {
                // 错误信息提示
                val err = ChatSocketException(msg.code).message
                ShowUtils.showToast(err)
                throw RuntimeException(err)
            }
        }
    }

    /**
     * 解密消息，解析通知
     *
     * @param item  待处理的消息
     */
    private fun processMessage(item: ChatMessage) {
        when (item.msgType) {
            NOTIFICATION, RED_PACKET, TRANSFER, RECEIPT, FORWARD -> {
                // 这些消息类型不能点赞
            }
            else -> {
                // 普通类型消息，且不是ChatRowForwardText类型的消息，手动添加点赞详情信息
                if (item.praise == null && item.msg.forwardType != 1) {
                    item.praise = RewardDetail()
                }
            }
        }
        // 消息解密
        if (item.channelType == CHANNEL_FRIEND) {
            decryptFriendMessage(item)
        } else if (item.channelType == CHANNEL_ROOM) {
            decryptGroupMessage(item)
        }
        // 解析通知
        if (item.msgType == NOTIFICATION) {
            handleNotification(item)
        }
    }

    /**
     * 好友消息解密
     *
     * @param item  待解密的消息
     */
    private fun decryptFriendMessage(item: ChatMessage) {
        if (!TextUtils.isEmpty(item.msg.encryptedMsg)) {
            item.encrypted = 1
            if (!TextUtils.isEmpty(item.decryptPublicKey) && !TextUtils.isEmpty(CipherManager.getPrivateKey())) {
                try {
                    decryptFriend(item, gson)
                } catch (e: Exception) {
                    CrabSDK.uploadException(CipherException(getUserId(),
                            currentUser.value?.publicKey, item.decryptPublicKey, e))
                }
            }
        }
    }

    /**
     * 群消息解密
     *
     * @param item  待解密的消息
     */
    private fun decryptGroupMessage(item: ChatMessage) {
        if (!TextUtils.isEmpty(item.msg.encryptedMsg)) {
            item.encrypted = 1
            if (item.msg.kid != null) {
                try {
                    decryptGroup(item, gson)
                } catch (e: Exception) {
                    CrabSDK.uploadException(CipherException(getUserId(),
                            currentUser.value?.publicKey, item.msg.kid, e))
                }
            }
        }
    }

    /**
     * 拼接处理合并转发消息的content字段
     *
     * @param item  待解析的通知消息
     */
    private fun handleForwardMessageContent(item: ChatMessage) {
        val builder = StringBuilder()
        item.msg.sourceLog?.forEach {
            builder.append("${it.senderInfo.nickname}:")
            when (it.msgType) {
                SYSTEM, TEXT -> builder.append(it.msg.content)
                AUDIO -> builder.append(context.getString(R.string.core_msg_type2))
                IMAGE -> builder.append(context.getString(R.string.core_msg_type3))
                RED_PACKET -> builder.append(context.getString(R.string.core_msg_type4))
                VIDEO -> builder.append(context.getString(R.string.core_msg_type5))
                FILE -> builder.append("${context.getString(R.string.core_msg_type12)}${it.msg.fileName}")
                FORWARD -> builder.append(context.getString(R.string.core_msg_type7))
                TRANSFER -> builder.append(context.getString(R.string.core_msg_type8))
                RECEIPT -> builder.append(context.getString(R.string.core_msg_type9))
                INVITATION -> builder.append(context.getString(R.string.core_msg_type15))
            }
            builder.append("\n")
        }
        item.msg.content = builder.toString()
        if (item.msg.sourceChannel == CHANNEL_ROOM) {
            // 尝试解密转发的sourceName，通常情况下是明文，不需要解密
            try {
                val pair = item.msg.sourceName.split(AppConfig.ENC_INFIX)
                if (pair.size == 3) {
                    val roomKey = ChatDatabase.getInstance().roomKeyDao().getRoomKeyById(pair[2], pair[1])
                    item.msg.sourceName = CipherManager.decryptSymmetric(pair[0], roomKey.keySafe)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    /**
     * 解析通知内容
     *
     * @param item  待解析的通知消息
     */
    private fun handleNormalNotification(item: ChatMessage) {
        if (TextUtils.isEmpty(item.msg.operator) && TextUtils.isEmpty(item.msg.target) && TextUtils.isEmpty(item.msg.key)) {
            // 兼容旧版本消息的显示
            if (item.msg.content == null) {
                if (item.msg.type == 16) {
                    if (item.senderId == UserInfo.getInstance().id) {
                        item.msg.content = context.getString(R.string.core_screenshots_from_me)
                    } else {
                        item.msg.content = context.getString(R.string.core_screenshots_from_other)
                    }
                }
            }
            return
        }
        when (item.msg.type) {
            REVOKE_MSG -> {
                if (item.msg.operator == getUserId()) {
                    item.msg.content = context.getString(R.string.core_revoke_by_self)
                } else {
                    val operator = InfoProvider.getInstance()
                            .strategy(SyncInfoStrategy(item, item.msg.operator, true))
                            .get(FriendBean::class.java)?.displayName
                    item.msg.content = context.getString(R.string.core_revoke_by_other, operator)
                }
                chatDao().deleteMessage(item.channelType, item.msg.logId)
            }
            CREATE_GROUP -> if (TextUtils.isEmpty(item.msg.target)) {
                if (item.msg.operator == getUserId()) {
                    item.msg.content = context.getString(R.string.core_create_group_by_self)
                } else {
                    val operator = InfoProvider.getInstance()
                            .strategy(SyncInfoStrategy(item, item.msg.operator, true))
                            .get(FriendBean::class.java)?.displayName
                    item.msg.content = context.getString(R.string.core_create_group_by_other, operator)
                }
            } else {
                if (item.msg.operator == getUserId()) {
                    val target = InfoProvider.getInstance()
                            .strategy(SyncInfoStrategy(item, item.msg.target, false))
                            .get(FriendBean::class.java)?.displayName
                    val pair = item.msg.roomName.split(AppConfig.ENC_INFIX)
                    item.msg.content = if (pair.size == 3) {
                        try {
                            val roomKey = roomKeyDao().getRoomKeyById(pair[2], pair[1])
                            item.msg.roomName = CipherManager.decryptSymmetric(pair[0], roomKey.keySafe)
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                        context.getString(R.string.core_add_group_for_other, target, item.msg.roomName)
                    } else {
                        context.getString(R.string.core_add_group_for_other, target, item.msg.roomName)
                    }
                } else {
                    val operator = InfoProvider.getInstance()
                            .strategy(SyncInfoStrategy(item, item.msg.operator, true))
                            .get(FriendBean::class.java)?.displayName
                    val pair = item.msg.roomName.split(AppConfig.ENC_INFIX)
                    item.msg.content = if (pair.size == 3) {
                        try {
                            val roomKey = roomKeyDao().getRoomKeyById(pair[2], pair[1])
                            item.msg.roomName = CipherManager.decryptSymmetric(pair[0], roomKey.keySafe)
                        } catch (e: Exception) {
                            item.msg.roomName = item.msg.roomName.substring(0, 20)
                            e.printStackTrace()
                        }
                        context.getString(R.string.core_add_group_by_other, operator, item.msg.roomName)
                    } else {
                        context.getString(R.string.core_add_group_by_other, operator, item.msg.roomName)
                    }
                }
            }
            EXIT_GROUP -> {
                val name = InfoProvider.getInstance()
                        .strategy(SyncInfoStrategy(item, item.msg.operator, true))
                        .get(FriendBean::class.java)?.displayName
                item.msg.content = context.getString(R.string.core_group_remove_friend_by_self, name)
            }
            KICK_OUT -> {
                val name = InfoProvider.getInstance()
                        .strategy(SyncInfoStrategy(item, item.msg.target, true))
                        .get(FriendBean::class.java)?.displayName
                item.msg.content = context.getString(R.string.core_group_remove_friend_by_other, name)
            }
            JOIN_GROUP -> {
                val name = InfoProvider.getInstance()
                        .strategy(SyncInfoStrategy(item, item.msg.operator, true))
                        .get(FriendBean::class.java)?.displayName
                item.msg.content = context.getString(R.string.core_group_add_friend, name)
            }
            DISMISS_GROUP -> item.msg.content = context.getString(R.string.core_group_dissovle)
            FRIEND_IN_GROUP -> if (item.msg.operator == getUserId()) {
                val name = InfoProvider.getInstance()
                        .strategy(SyncInfoStrategy(item, item.msg.target, false))
                        .get(FriendBean::class.java)?.displayName
                item.msg.content = context.getString(R.string.core_add_friend_by_self, name)
            } else if (item.msg.target == getUserId()) {
                val name = InfoProvider.getInstance()
                        .strategy(SyncInfoStrategy(item, item.msg.operator, true))
                        .get(FriendBean::class.java)?.displayName
                item.msg.content = context.getString(R.string.core_add_friend_by_other, name)
            }
            DELETE_FRIEND -> if (item.msg.operator == getUserId()) {
                item.msg.content = context.getString(R.string.core_remove_friend_by_self)
            } else {
                item.msg.content = context.getString(R.string.core_remove_friend_by_other)
            }
            CHANGE_GROUP_OWNER -> if (item.msg.target == getUserId()) {
                item.msg.content = context.getString(R.string.core_group_set_master_to_me)
            } else {
                val target = InfoProvider.getInstance()
                        .strategy(SyncInfoStrategy(item, item.msg.target, true))
                        .get(FriendBean::class.java)?.displayName
                item.msg.content = context.getString(R.string.core_group_set_master_to_other, target)
            }
            CHANGE_GROUP_ADMIN -> if (item.msg.target == getUserId()) {
                item.msg.content = context.getString(R.string.core_group_set_admin_to_me)
            } else {
                val target = InfoProvider.getInstance()
                        .strategy(SyncInfoStrategy(item, item.msg.target, true))
                        .get(FriendBean::class.java)?.displayName
                item.msg.content = context.getString(R.string.core_group_set_admin_to_other, target)
            }
            CHANGE_GROUP_NAME -> {
                if (item.msg.operator == getUserId()) {
                    val pair = item.msg.roomName.split(AppConfig.ENC_INFIX)
                    item.msg.content = if (pair.size == 3) {
                        try {
                            val roomKey = roomKeyDao().getRoomKeyById(pair[2], pair[1])
                            item.msg.roomName = CipherManager.decryptSymmetric(pair[0], roomKey.keySafe)
                        } catch (e: Exception) {
                            item.msg.roomName = item.msg.roomName.substring(0, 20)
                            e.printStackTrace()
                        }
                        context.getString(R.string.core_change_group_name_by_self, item.msg.roomName)
                    } else {
                        context.getString(R.string.core_change_group_name_by_self, item.msg.roomName)
                    }
                } else {
                    val operator = InfoProvider.getInstance()
                            .strategy(SyncInfoStrategy(item, item.msg.operator, true))
                            .get(FriendBean::class.java)?.displayName
                    val pair = item.msg.roomName.split(AppConfig.ENC_INFIX)
                    item.msg.content = if (pair.size == 3) {
                        try {
                            val roomKey = roomKeyDao().getRoomKeyById(pair[2], pair[1])
                            item.msg.roomName = CipherManager.decryptSymmetric(pair[0], roomKey.keySafe)
                        } catch (e: Exception) {
                            item.msg.roomName = item.msg.roomName.substring(0, 20)
                            e.printStackTrace()
                        }
                        context.getString(R.string.core_change_group_name_by_other, operator, item.msg.roomName)
                    } else {
                        context.getString(R.string.core_change_group_name_by_other, operator, item.msg.roomName)
                    }
                }
                roomDao().updateName(item.msg.roomName, item.receiveId)
                LiveBus.of(BusEvent::class.java).contactsRefresh().setValue(2)
            }
            RECEIVE_RED_PACKET -> if (item.msg.operator == getUserId()) {
                val name = InfoProvider.getInstance()
                        .strategy(SyncInfoStrategy(item, item.msg.owner, false))
                        .get(FriendBean::class.java)?.displayName
                if (item.msg.owner == getUserId()) {
                    item.msg.content = context.getString(R.string.core_red_packet_receive_by_self)
                } else {
                    item.msg.content = context.getString(R.string.core_red_packet_receive_from_other, name)
                }
            } else {
                val name = InfoProvider.getInstance()
                        .strategy(SyncInfoStrategy(item, item.msg.operator, true))
                        .get(FriendBean::class.java)?.displayName
                item.msg.content = context.getString(R.string.core_red_packet_receive_by_other, name)
            }
            BECOME_FRIEND -> item.msg.content = context.getString(R.string.core_add_friend_success)
            MUTE_IN_GROUP -> {
                val operator = if (item.msg.operator == "2")
                    context.getString(R.string.core_tips_group_admin)
                else
                    context.getString(R.string.core_tips_group_master)
                when (item.msg.mutedType) {
                    1 -> item.msg.content = context.getString(R.string.core_speak_setting_all_enable, operator)
                    2, 3 -> {
                        val names = item.msg.names
                        val mode = if (item.msg.mutedType == 2 && item.msg.opt == 1)
                            context.getString(R.string.core_speak_disable)
                        else
                            context.getString(R.string.core_speak_enable)
                        if (names != null) {
                            val builder = StringBuilder()
                            for (i in names.take(6).indices) {
                                builder.append(names[i])
                                if (i != names.size - 1) {
                                    builder.append(context.getString(R.string.core_slight_pause))
                                }
                            }
                            if (names.size > 6) {
                                builder.append(context.getString(R.string.core_people_count_etc, names.size, mode))
                            } else {
                                builder.append(context.getString(R.string.core_people_count, names.size, mode))
                            }
                            item.msg.content = builder.toString()
                        }
                    }
                    4 -> item.msg.content = context.getString(R.string.core_speak_setting_all_disable, operator)
                }
            }
            SNAP_DESTROY -> // 阅后即焚焚毁消息
                RoomUtils.run {
                    // 删除还没开始读的阅后即焚消息（因为其他端已读）
                    if (!ChatMessageDao.visibleSnapMsg.contains("${item.channelType}-${item.msg.logId}")) {
                        chatDao().deleteUnreadSnapMessage(item.channelType, item.msg.logId)
                    }
                }
            SCREEN_SHOT -> if (item.senderId == getUserId()) {
                item.msg.content = context.getString(R.string.core_screenshots_from_me)
            } else {
                item.msg.content = context.getString(R.string.core_screenshots_from_other)
            }
            INVITE_GROUP -> {
                val names = item.msg.names
                if (names != null) {
                    val builder = StringBuilder()
                    for (i in names.take(6).indices) {
                        builder.append(names[i])
                        if (i != names.size - 1) {
                            builder.append(context.getString(R.string.core_slight_pause))
                        }
                    }
                    if (names.size > 6) {
                        builder.append(context.getString(R.string.core_invite_join_etc, names.size))
                    } else {
                        builder.append(context.getString(R.string.core_invite_join, names.size))
                    }
                    item.msg.content = builder.toString()
                }
            }
            RECEIPT_SUCCESS -> {
                // recordId的组成结构："FromId,ToId"，分别代表转出id和转入id
                if (item.msg.operator == getUserId()) {
                    item.msg.recordId = item.msg.recordId.split(",".toRegex())[0]
                    item.msg.content = context.getString(R.string.core_receipt_from_me)
                } else {
                    item.msg.recordId = item.msg.recordId.split(",".toRegex())[1]
                    item.msg.content = context.getString(R.string.core_receipt_from_other)
                }
                chatDao().updateRecordId(item.msg.logId, item.msg.recordId)
            }
            GROUP_REJECT_MSG -> {
                val name = InfoProvider.getInstance()
                        .strategy(SyncInfoStrategy(item, item.msg.target, true))
                        .get(FriendBean::class.java)?.displayName
                item.msg.content = context.getString(R.string.core_reject_join_group, name)
            }
            FRIEND_REJECT_MSG -> {
                // 被拒收的消息状态修改为发送失败
                chatDao().updateMessageState(item.channelType, item.logId, MessageState.SEND_FAIL)
            }
            PRAISE_MESSAGE -> {
                // 更新赞赏详情
                val temp = chatDao().getMessageByIdSync(item.msg.logId, item.channelType)
                if (temp != null) {
                    chatDao().updatePraise(item.msg.logId, item.channelType, temp.praise.apply {
                        // 点赞不能取消（只增不减）
                        if (item.msg.like > like) {
                            like = item.msg.like
                        }
                        // 打赏不能取消（只增不减）
                        if (item.msg.reward > reward) {
                            reward = item.msg.reward
                        }
                        if (item.msg.operator == getUserId()) {
                            when (item.msg.action) {
                                ACTION_LIKE -> like()
                                ACTION_REWARD -> reward()
                            }
                        }
                    })
                }
            }
        }
    }

    private fun handleNotification(item: ChatMessage) {
        handleNormalNotification(item)
        if (item.msg.type == UPDATE_GROUP_KEY) {
            if (item.receiveId != getUserId()) {
                // 只处理发给自己的密钥通知
                return
            }
            // 更新群密钥消息
            val roomKey = if (CipherManager.hasDHKeyPair()) {
                val key = CipherManager.decryptString(item.msg.key, item.msg.fromKey, CipherManager.getPrivateKey())
                if (key == item.msg.key) {
                    RoomKey(item.msg.roomId, item.msg.kid, item.msg.key, item.msg.fromKey)
                } else {
                    RoomKey(item.msg.roomId, item.msg.kid, key)
                }
            } else {
                RoomKey(item.msg.roomId, item.msg.kid, item.msg.key, item.msg.fromKey)
            }
            roomKeyDao().insert(roomKey)
        }
    }
}

@Throws(Exception::class)
fun decryptFriend(item: ChatMessage, gson: Gson): ChatMessage {
    val fromKey = item.msg.fromKey
    val toKey = item.msg.toKey
    val chatFile = CipherManager.decryptString(item.msg.encryptedMsg!!, item.decryptPublicKey, CipherManager.getPrivateKey())
    item.msg = gson.fromJson(chatFile, ChatFile::class.java)
    item.msg.fromKey = fromKey
    item.msg.toKey = toKey
    return item
}

@Throws(Exception::class)
fun decryptGroup(item: ChatMessage, gson: Gson): ChatMessage {
    val roomKey = ChatDatabase.getInstance().roomKeyDao().getRoomKeyById(item.receiveId, item.msg.kid)
    val chatFile = CipherManager.decryptSymmetric(item.msg.encryptedMsg!!, roomKey.keySafe)
    val kid = item.msg.kid
    item.msg = gson.fromJson(chatFile, ChatFile::class.java)
    item.msg.kid = kid
    return item
}

/**
 * 获取消息会话的对象id（和你聊天的人或者群id）
 *
 */
fun ChatMessage.getTargetId(selfId: String?): String {
    return if (channelType == CHANNEL_ROOM) {
        receiveId
    } else {
        if (senderId == selfId) {
            receiveId
        } else {
            senderId
        }
    }
}