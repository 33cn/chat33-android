package com.fzm.chat33.main.mvvm

import android.annotation.SuppressLint
import android.content.Context
import android.text.TextUtils
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.baidu.crabsdk.CrabSDK
import com.fuzamei.common.base.mvvm.SingleLiveData
import com.fuzamei.common.bus.LiveBus
import com.fuzamei.common.executor.AppExecutors
import com.fuzamei.common.ext.request
import com.fuzamei.common.ext.result
import com.fuzamei.common.ext.start
import com.fuzamei.common.net.Result
import com.fuzamei.common.net.rxjava.ApiException
import com.fuzamei.common.utils.RoomUtils
import com.fuzamei.common.utils.ShowUtils
import com.fuzamei.common.utils.ToolUtils
import com.fuzamei.common.utils.run
import com.fuzamei.componentservice.app.BusEvent
import com.fuzamei.componentservice.app.LoadingViewModel
import com.fuzamei.componentservice.config.AppConfig
import com.fzm.chat33.R
import com.fzm.chat33.core.bean.MessageState
import com.fzm.chat33.core.bean.RelationshipBean
import com.fzm.chat33.core.bean.param.toEncParams
import com.fzm.chat33.core.consts.PraiseAction.ACTION_LIKE
import com.fzm.chat33.core.consts.PraiseAction.ACTION_REWARD
import com.fzm.chat33.core.db.ChatDatabase
import com.fzm.chat33.core.db.bean.*
import com.fzm.chat33.core.db.dao.ChatMessageDao
import com.fzm.chat33.core.global.Chat33Const.*
import com.fzm.chat33.core.global.LoginInfoDelegate
import com.fzm.chat33.core.manager.CipherException
import com.fzm.chat33.core.manager.CipherManager
import com.fzm.chat33.core.manager.MessageManager
import com.fzm.chat33.core.net.OssModel
import com.fzm.chat33.core.provider.InfoProvider
import com.fzm.chat33.core.provider.SyncInfoStrategy
import com.fzm.chat33.core.repo.ContactsRepository
import com.fzm.chat33.core.request.ChatLogHistoryRequest
import com.fzm.chat33.core.request.ReceiveRedPacketRequest
import com.fzm.chat33.core.response.ReceiveRedPacketResponse
import com.fzm.chat33.core.source.ChatDataSource
import com.fzm.chat33.core.utils.UserInfoPreference
import com.fzm.chat33.utils.FileUtils
import com.google.gson.Gson
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.functions.Consumer
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.kodein.di.Kodein
import org.kodein.di.conf.global
import org.kodein.di.generic.instance
import top.zibin.luban.Luban
import top.zibin.luban.OnCompressListener
import java.io.File
import java.util.*
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject
import kotlin.collections.ArrayList

class ChatViewModel @Inject constructor(
        private val contactsRepository: ContactsRepository,
        private val loginInfoDelegate: LoginInfoDelegate,
        private val chatDataSource: ChatDataSource
): LoadingViewModel(), LoginInfoDelegate by loginInfoDelegate {

    private fun chatDao() = ChatDatabase.getInstance().chatMessageDao()
    private fun roomKeyDao() = ChatDatabase.getInstance().roomKeyDao()
    private fun roomDao() = ChatDatabase.getInstance().roomsDao()
    private val privateKey by lazy { CipherManager.getPrivateKey() }
    private val groupKeyCache by lazy { HashMap<String, RoomKey>() }
    private val gson: Gson by Kodein.global.instance()

    private val _chatLogResult by lazy { MutableLiveData<ChatLogResult>() }
    val chatLogResult: LiveData<ChatLogResult>
        get() = _chatLogResult

    private val _sendMessage by lazy { MutableLiveData<ChatMessageResult>() }
    val sendMessage: LiveData<ChatMessageResult>
        get() = _sendMessage

    private val _revokeMessage by lazy { MutableLiveData<Any>() }
    val revokeMessage: LiveData<Any>
        get() = _revokeMessage

    private val _setMutedSingle by lazy { MutableLiveData<Long>() }
    val setMutedSingle: LiveData<Long>
        get() = _setMutedSingle

    private val _receiveRedPacket by lazy { MutableLiveData<ReceiveRedPacketResult>() }
    val receiveRedPacket: LiveData<ReceiveRedPacketResult>
        get() = _receiveRedPacket

    private val _hasRelationship by lazy { MutableLiveData<HasRelationshipResult>() }
    val hasRelationship: LiveData<HasRelationshipResult>
        get() = _hasRelationship

    private val _getRoomInfo by lazy { MutableLiveData<GetRoomInfoResult>() }
    val getRoomInfo: LiveData<GetRoomInfoResult>
        get() = _getRoomInfo

    private val _getRoomUsers by lazy { MutableLiveData<GetRoomUsersResult>() }
    val getRoomUsers: LiveData<GetRoomUsersResult>
        get() = _getRoomUsers

    private val _msgLikeResult by lazy { MutableLiveData<Pair<String, String>>() }
    val msgLikeResult: LiveData<Pair<String, String>>
        get() = _msgLikeResult

    private val _notifyChanged by lazy { MutableLiveData<Any>() }
    val notifyChanged: LiveData<Any>
        get() = _notifyChanged

    private val notify = AtomicBoolean(false)

    @JvmOverloads
    fun notifyDataSetChanged(delay: Long = 0L) {
        if (notify.get()) {
            return
        }
        notify.set(true)
        launch {
            delay(delay)
            if (notify.get()) {
                _notifyChanged.value = null
                notify.set(false)
            }
        }
    }

    fun getChatLogHistory(context: Context, targetId: String, channelType: Int, startId: String, timeStamp: Long, size: Int) {

        //私聊群聊共用一套
        val request = ChatLogHistoryRequest()
        request.id = targetId
        request.startId = startId
        request.number = size

        if (channelType == CHANNEL_FRIEND && targetId == getUserId()) {
            _chatLogResult.value = ChatLogResult(ArrayList(), "-1")
            return
        }

        // 先从本地数据库中查询聊天记录
        if (channelType == CHANNEL_ROOM) {
            chatDao().getGroupChatLogLocal(channelType, targetId, timeStamp, size).run(Consumer {
                processMessage(context, targetId, channelType, request, it)
            })
        } else {
            chatDao().getPrivateChatLogLocal(targetId, timeStamp, size).run(Consumer {
                processMessage(context, targetId, channelType, request, it)
            })
        }
    }

    fun getChatLogFromId(context: Context, targetId: String, channelType: Int, logId: String) {
        if (channelType == CHANNEL_ROOM) {
            chatDao().getGroupChatLogFromId(targetId, logId).run(Consumer {
                processMessage(context, targetId, channelType, null, it)
            })
        } else {
            chatDao().getPrivateChatLogFromId(targetId, logId).run(Consumer {
                processMessage(context, targetId, channelType, null, it)
            })
        }
    }

    @SuppressLint("CheckResult")
    private fun processMessage(context: Context, targetId: String, channelType: Int, request: ChatLogHistoryRequest?, chatMessages: MutableList<ChatMessage>) {
        val noMoreChatLog = UserInfoPreference.getInstance()
                .getBooleanPref(UserInfoPreference.NO_MORE_CHAT_LOG + channelType + "-" + targetId, false)
        Observable.just(1)
                .subscribeOn(Schedulers.from(AppExecutors.databaseThreadPool()))
                .flatMap {
                    for (i in chatMessages.indices.reversed()) {
                        val message = chatMessages[i]
                        if (channelType == CHANNEL_ROOM) {
                            decryptGroupMessage(message)
                        } else if (channelType == CHANNEL_FRIEND) {
                            decryptFriendMessage(message)
                        }
                        if (message.msgType == ChatMessage.Type.NOTIFICATION) {
                            handleNotification(context, message)
                        }
                        if (message.messageState == MessageState.SENDING) {
                            // 如果数据库查出正在发送的消息，但发送队列里没有，则标记为发送失败
                            if (!MessageManager.containsMessage(message.msgId)) {
                                message.messageState = MessageState.SEND_FAIL
                                RoomUtils.run { chatDao().updateMessageState(message.channelType, message.msgId, message.messageState) }
                            }
                        }
                        val snapMsg = (message.isSnap == 1
                                && message.snapCounting == 1
                                && message.destroyTime <= System.currentTimeMillis())
                        val snapNotification = message.msgType == 6 && message.msg.type == 15
                        if (snapMsg || snapNotification) {
                            val channel = message.channelType
                            val logId = message.logId
                            RoomUtils.run { chatDao().deleteMessage(channel, logId) }
                            chatMessages.removeAt(i)
                        }
                    }
                    Observable.just(chatMessages)
                }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ messages ->
                    if (messages.size > 0) {
                        val nextLog = messages[messages.size - 1].logId
                        messages.reverse()
                        _chatLogResult.value = ChatLogResult(messages, nextLog)
                    } else if (!noMoreChatLog) {
                        if (request != null) {
                            getChatLogFromServer(context, channelType, request)
                        }
                    } else {
                        _chatLogResult.value = ChatLogResult(ArrayList(), "-1")
                    }
                }, {
                    _chatLogResult.value = ChatLogResult(chatMessages, "-1")
                })
    }

    /**
     * 从服务端获取聊天记录
     *
     * @param request   聊天记录请求参数
     */
    private fun getChatLogFromServer(context: Context, channelType: Int, request: ChatLogHistoryRequest) {
        request {
            chatDataSource.getChatLogHistory(channelType, request)
        }.result (onSuccess = {
            if (it.logs == null) {
                if ("-1" == it.nextLog) {
                    // 标记为无更早的消息记录
                    UserInfoPreference.getInstance().setBooleanPref(UserInfoPreference.NO_MORE_CHAT_LOG
                            + channelType + "" + request.id, true)
                }
                _chatLogResult.value = ChatLogResult(it.logs, it.nextLog)
                return@result
            }
            Observable.just(it.logs)
                    .subscribeOn(Schedulers.io())
                    .flatMap { chatMessages ->
                        if (chatMessages.size > 0) {
                            for (i in chatMessages.indices.reversed()) {
                                val message = chatMessages[i]
                                if (channelType == CHANNEL_ROOM) {
                                    decryptGroupMessage(message)
                                } else if (channelType == CHANNEL_FRIEND) {
                                    decryptFriendMessage(message)
                                }
                                if (message.msgType == ChatMessage.Type.NOTIFICATION) {
                                    handleNotification(context, message)
                                    if (message.msg.type == 15 || message.msg.type == 19) {
                                        chatMessages.removeAt(i)
                                    }
                                } else if (message.msgType == ChatMessage.Type.FORWARD) {
                                    handleForwardMessageContent(context, message)
                                } else if (message.msgType == ChatMessage.Type.TRANSFER) {
                                    message.msg.recordId = message.msg.recordId.split(",".toRegex())[if (message.isSentType) 0 else 1]
                                } else if (message.msgType == ChatMessage.Type.RECEIPT) {
                                    if (!TextUtils.isEmpty(message.msg.recordId)) {
                                        message.msg.recordId = message.msg.recordId.split(",".toRegex())[if (message.isSentType) 1 else 0]
                                    }
                                }
                            }
                        }
                        chatMessages.reverse()
                        Observable.just(chatMessages)
                    }
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({ chatMessages ->
                        for (message in chatMessages) {
                            chatDao().getMessageById(message.logId, message.channelType).run(Consumer {
                                RoomUtils.run { chatDao().updateIgnore(it.channelType, it.logId) }
                            }, Consumer {
                                RoomUtils.run { chatDao().insert(message) }
                            })
                        }
                        if ("-1" == it.nextLog) {
                            // 标记为无更早的消息记录
                            UserInfoPreference.getInstance().setBooleanPref(UserInfoPreference.NO_MORE_CHAT_LOG
                                    + channelType + "" + request.id, true)
                        }
                        _chatLogResult.value = ChatLogResult(chatMessages, it.nextLog)
                    }, { throwable ->
                        throwable.printStackTrace()
                        if ("-1" == it.nextLog) {
                            // 标记为无更早的消息记录
                            UserInfoPreference.getInstance().setBooleanPref(UserInfoPreference.NO_MORE_CHAT_LOG
                                    + channelType + "" + request.id, true)
                        }
                        _chatLogResult.value = ChatLogResult(ArrayList(), it.nextLog)
                    })
        }, onError = {
            //ShowUtils.showToastNormal(context, it.message)
            _chatLogResult.value = ChatLogResult(ArrayList(), request.startId)
        })
    }

    private fun decryptFriendMessage(item: ChatMessage) {
        if (!TextUtils.isEmpty(item.msg.encryptedMsg)) {
            item.encrypted = 1
            if (!TextUtils.isEmpty(item.decryptPublicKey) && !TextUtils.isEmpty(privateKey)) {
                try {
                    val fromKey = item.msg.fromKey
                    val toKey = item.msg.toKey
                    val chatFile = CipherManager.decryptString(item.msg.encryptedMsg!!, item.decryptPublicKey, privateKey)
                    item.msg = gson.fromJson(chatFile, ChatFile::class.java)
                    item.msg.fromKey = fromKey
                    item.msg.toKey = toKey
                    RoomUtils.run { chatDao().insert(item) }
                } catch (e: Exception) {
                    CrabSDK.uploadException(CipherException(getUserId(),
                            currentUser.value?.publicKey, item.decryptPublicKey, e))
                }
            }
        }
    }

    private fun decryptGroupMessage(item: ChatMessage) {
        if (!TextUtils.isEmpty(item.msg.encryptedMsg)) {
            item.encrypted = 1
            var decryptKey = ""
            if (item.msg.kid != null && !TextUtils.isEmpty(privateKey)) {
                try {
                    var roomKey: RoomKey?
                    roomKey = groupKeyCache[item.receiveId + "-" + item.msg.kid]
                    if (roomKey == null) {
                        roomKey = ChatDatabase.getInstance().roomKeyDao().getRoomKeyById(item.receiveId, item.msg.kid)
                        if (roomKey != null) {
                            groupKeyCache[item.receiveId + "-" + item.msg.kid] = roomKey
                        }
                    }
                    decryptKey = roomKey!!.keySafe
                    val kid = item.msg.kid
                    val chatFile = CipherManager.decryptSymmetric(item.msg.encryptedMsg!!, decryptKey)
                    item.msg = gson.fromJson(chatFile, ChatFile::class.java)
                    item.msg.kid = kid
                    RoomUtils.run { chatDao().insert(item) }
                } catch (e: Exception) {
                    CrabSDK.uploadException(CipherException(getUserId(),
                            currentUser.value?.publicKey, decryptKey, e))
                }
            }
        }
    }

    /**
     * 拼接处理合并转发消息的content字段
     *
     * @param item  待解析的通知消息
     */
    private fun handleForwardMessageContent(context: Context, item: ChatMessage) {
        val builder = StringBuilder()
        item.msg.sourceLog?.forEach {
            builder.append("${it.senderInfo.nickname}:")
            when (it.msgType) {
                ChatMessage.Type.SYSTEM, ChatMessage.Type.TEXT -> builder.append(it.msg.content)
                ChatMessage.Type.AUDIO -> builder.append(context.getString(R.string.core_msg_type2))
                ChatMessage.Type.IMAGE -> builder.append(context.getString(R.string.core_msg_type3))
                ChatMessage.Type.RED_PACKET -> builder.append(context.getString(R.string.core_msg_type4))
                ChatMessage.Type.VIDEO -> builder.append(context.getString(R.string.core_msg_type5))
                ChatMessage.Type.FILE -> builder.append("${context.getString(R.string.core_msg_type12)}${it.msg.fileName}")
                ChatMessage.Type.FORWARD -> builder.append(context.getString(R.string.core_msg_type7))
                ChatMessage.Type.TRANSFER -> builder.append(context.getString(R.string.core_msg_type8))
                ChatMessage.Type.RECEIPT -> builder.append(context.getString(R.string.core_msg_type9))
                ChatMessage.Type.INVITATION -> builder.append(context.getString(R.string.core_msg_type15))
            }
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

    private fun handleNotification(context: Context, item: ChatMessage) {
        if (TextUtils.isEmpty(item.msg.operator) && TextUtils.isEmpty(item.msg.target) && TextUtils.isEmpty(item.msg.key)) {
            // 兼容旧版本消息的显示
            if (item.msg.content == null) {
                if (item.msg.type == 16) {
                    if (item.senderId == getUserId()) {
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
                else context.getString(R.string.core_tips_group_master)
                when (item.msg.mutedType) {
                    1 -> item.msg.content = context.getString(R.string.core_speak_setting_all_enable, operator)
                    2, 3 -> {
                        val names = item.msg.names
                        val mode = if (item.msg.mutedType == 2 && item.msg.opt == 1)
                            context.getString(R.string.core_speak_disable)
                        else context.getString(R.string.core_speak_enable)
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
            UPDATE_GROUP_KEY -> {
                if (item.receiveId != getUserId()) {
                    // 只处理发给自己的密钥通知
                    return
                }
                // 更新群密钥消息
                val roomKey = if (CipherManager.hasDHKeyPair()) {
                    val key = CipherManager.decryptString(item.msg.key, item.msg.fromKey, privateKey)
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
            GROUP_REJECT_MSG -> {
                val name = InfoProvider.getInstance()
                        .strategy(SyncInfoStrategy(item, item.msg.target, true))
                        .get(FriendBean::class.java)?.displayName
                item.msg.content = context.getString(R.string.core_reject_join_group, name)
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

    fun sendResendMessage(message: ChatMessage) {
        _sendMessage.value = ChatMessageResult(message, true)
    }


    fun uploadImage(context: Context, imagePath: String, message: ChatMessage) {
        // TODO: 18-5-25  这里为了简单起见，没有判断file的类型
        if (!File(imagePath).exists() || imagePath.contains(AppConfig.ENC_PREFIX)) {
            ShowUtils.showToast(context, R.string.chat_error_upload_image_no_exist)
            return
        }
        if (imagePath.endsWith("gif")) {
            // gif图不压缩
            uploadImageInternal(context, File(imagePath), message)
        } else {
            Luban.with(context)
                    .load(imagePath)
                    .ignoreBy(100)
                    .setTargetDir(FileUtils.getImageCachePath(context))
                    .setCompressListener(object : OnCompressListener {
                        override fun onStart() {}

                        override fun onSuccess(file: File) {
                            if (!file.exists()) {
                                ShowUtils.showToast(context, R.string.chat_error_upload_image_zip)
                                return
                            }
                            val heightWidth = ToolUtils.getLocalImageHeightWidth(file.absolutePath)
                            if (heightWidth[0] <= 0 || heightWidth[1] <= 0) {
                                ShowUtils.showToast(context, R.string.chat_error_upload_image)
                                return
                            }
                            uploadImageInternal(context, file, message)
                        }

                        override fun onError(e: Throwable) {
                            ShowUtils.showToast(context, R.string.chat_error_upload_image_zip)
                            e.printStackTrace()
                        }
                    }).launch()
        }
    }

    private fun uploadImageInternal(context: Context, file: File, message: ChatMessage) {
        OssModel.getInstance().uploadMedia(message.toEncParams(), file.absolutePath, OssModel.PICTURE, object : OssModel.UpLoadCallBack {
            override fun onSuccess(url: String) {
                val heightWidth = ToolUtils.getLocalImageHeightWidth(file.absolutePath)
                if (heightWidth[0] <= 0 || heightWidth[1] <= 0) {
                    ShowUtils.showToast(context, R.string.chat_error_upload_image)
                } else {
                    message.msg.imageUrl = url
                    _sendMessage.value = ChatMessageResult(message, true)
                }
            }

            override fun onProgress(currentSize: Long, totalSize: Long) {

            }

            override fun onFailure(path: String) {
                ShowUtils.showToast(context, R.string.chat_error_upload_image)
            }
        })
    }

    fun uploadFile(context: Context, filePath: String, message: ChatMessage) {
        val file = File(filePath)
        if (!file.exists()) {
            ShowUtils.showToast(context, R.string.chat_error_upload_file_no_exist)
            return
        }
        if (FileUtils.getLength(filePath) > MAX_UPLOAD_FILE_SIZE) {
            ShowUtils.showToast(context, R.string.chat_error_upload_file_size)
            return
        }
        OssModel.getInstance().uploadMedia(message.toEncParams(), file.absolutePath, OssModel.FILE, object : OssModel.UpLoadCallBack {
            override fun onSuccess(url: String) {
                message.msg.fileUrl = url
                _sendMessage.value = ChatMessageResult(message, true)
            }

            override fun onProgress(currentSize: Long, totalSize: Long) {

            }

            override fun onFailure(path: String) {
                ShowUtils.showToast(context, R.string.chat_error_upload_file)
            }
        })
    }

    fun uploadAudio(context: Context, targetId: String, channelType: Int, isSnap: Int, seconds: Float, audioPath: String?) {
        if (audioPath == null) {
            ShowUtils.showToast(context, R.string.chat_error_upload_audio)
            return
        }
        val file = File(audioPath)
        if (!file.exists()) {
            ShowUtils.showToast(context, R.string.chat_error_upload_audio)
            return
        }
        val duration = seconds.toInt() + 1

        val chatFile = ChatFile.newAudio("", 0, audioPath)
        val message = ChatMessage.create(targetId, channelType, chatFile.chatFileType, isSnap, chatFile)
        OssModel.getInstance().uploadMedia(message.toEncParams(), file.absolutePath, OssModel.VOICE, object : OssModel.UpLoadCallBack {
            override fun onSuccess(url: String) {
                var durations = duration
                if (durations > 60) {
                    durations = 60
                }
                message.msg.mediaUrl = url
                message.msg.duration = durations.toFloat()
                _sendMessage.value = ChatMessageResult(message, false)
            }

            override fun onProgress(currentSize: Long, totalSize: Long) {

            }

            override fun onFailure(path: String) {
                ShowUtils.showToast(context, R.string.chat_error_upload_audio)
            }

        })
    }

    fun uploadVideo(context: Context, filePath: String, message: ChatMessage) {
        val file = File(filePath)
        if (!file.exists()) {
            ShowUtils.showToast(context, R.string.chat_error_upload_video_no_exist)
            return
        }
        if (FileUtils.getLength(filePath) > MAX_UPLOAD_FILE_SIZE) {
            ShowUtils.showToast(context, R.string.chat_error_upload_video_size)
            return
        }
        OssModel.getInstance().uploadMedia(message.toEncParams(), file.absolutePath, OssModel.VIDEO, object : OssModel.UpLoadCallBack {
            override fun onSuccess(url: String) {
                message.msg.mediaUrl = url
                _sendMessage.value = ChatMessageResult(message, true)
            }

            override fun onProgress(currentSize: Long, totalSize: Long) {

            }

            override fun onFailure(path: String) {
                ShowUtils.showToast(context, R.string.chat_error_upload_video)
            }
        })
    }

    fun isLocalBlock(id: String?): Boolean {
        return contactsRepository.isLocalBlock(id)
    }

    fun getLocalFriendById(id: String?) : FriendBean? {
        return contactsRepository.getLocalFriendById(id)
    }

    fun getLocalRoomById(id: String?) : RoomListBean? {
        return contactsRepository.getLocalRoomById(id)
    }

    fun revokeMessage(id: String, type: Int) {
        start {
            loading()
        }.request {
            chatDataSource.revokeMessage(id, type)
        }.result (onSuccess = {
            _revokeMessage.value = it
        }, onComplete = {dismiss()})
    }

    fun setMutedSingle(roomId: String, userId: String, deadline: Long){
        start {
            loading()
        }.request {
            chatDataSource.setMutedSingle(roomId, userId, deadline)
        }.result (onSuccess = {
            _setMutedSingle.value = deadline
        }, onComplete = {dismiss()})
    }

    fun receiveRedPacket(message: ChatMessage) {
        start {
            loading()
        }.request {
            val request = ReceiveRedPacketRequest()
            request.packetId = message.msg.packetId
            chatDataSource.receiveRedPacket(request)
        }.result (onSuccess = {
            _receiveRedPacket.value = ReceiveRedPacketResult(message, true, it, null)
        }, onError = {
            _receiveRedPacket.value = ReceiveRedPacketResult(message, false, null, it)
        }, onComplete = {dismiss()})
    }

    fun hasRelationship(channelType: Int, id: String, oldDeleted: Boolean) {
        request {
            when (channelType) {
                CHANNEL_ROOM -> chatDataSource.hasRelationship(channelType, id)
                CHANNEL_FRIEND -> {
                    // 好友关系从本地判断
                    Result.Success(RelationshipBean().apply {
                        isFriend = contactsRepository.isLocalFriend(id)
                    })
                }
                else -> Result.Error(ApiException(0))
            }
        }.result(onSuccess = {
            _hasRelationship.value = HasRelationshipResult(true, oldDeleted, it, null)
        }, onError = {
            _hasRelationship.value = HasRelationshipResult(false, oldDeleted, null, it)
        })
    }

    fun getRoomInfo(targetId: String) {
        request {
            contactsRepository.getRoomInfo(targetId)
        }.result (onSuccess = {
            RoomUtils.run(Runnable {
                ChatDatabase.getInstance().recentMessageDao().changeSticky(targetId, it.getOnTop())
                ChatDatabase.getInstance().recentMessageDao().changeDisturb(targetId, it.getNoDisturbing())
            })
            _getRoomInfo.value = GetRoomInfoResult(true, it)
        }, onError = {
            _getRoomInfo.value = GetRoomInfoResult(false, null)
        })
    }

    fun getRoomUsers(targetId: String) {
        request {
            contactsRepository.getRoomUsers(targetId)
        }.result{
            _getRoomUsers.value = GetRoomUsersResult(targetId, it)
        }
    }

    fun messageLike(channelType: Int, logId: String, action: String) {
        request {
            chatDataSource.messageLike(channelType, logId, action)
        }.result {
            _msgLikeResult.value = Pair(logId, action)
        }
    }

    /*-------------------------------ChatRowBase中的请求-------------------------------*/
    /**
     * 阅读阅后即焚消息
     */
    fun readSnapMessage(logId: String, type: Int): LiveData<Result<Any>> {
        val result = SingleLiveData<Result<Any>>()
        launch {
            result.value = withContext(Dispatchers.IO) {
                chatDataSource.readSnapMessage(logId, type)
            }
        }
        return result
    }

    /**
     * 点击群聊邀请消息时，判断是否在群中
     */
    fun hasRelationship(channelType: Int, id: String): LiveData<RelationshipBean> {
        loading()
        val result = SingleLiveData<RelationshipBean>()
        launch {
            result.value = withContext(Dispatchers.IO) {
                chatDataSource.hasRelationship(channelType, id).dataOrNull()
            }
            dismiss()
        }
        return result
    }

    /**
     * 获取群成员信息
     */
    fun getRoomUserInfo(roomId: String, userId: String): LiveData<Result<RoomUserBean>> {
        val result = SingleLiveData<Result<RoomUserBean>>()
        launch {
            result.value = withContext(Dispatchers.IO) {
                contactsRepository.getRoomUserInfo(roomId, userId)
            }
        }
        return result
    }

}

data class ChatLogResult (
        val chatList: List<ChatMessage>,
        val nextLogId: String
)

data class ChatMessageResult (
        val message: ChatMessage,
        val resend: Boolean
)

data class ReceiveRedPacketResult (
        val message: ChatMessage,
        val success: Boolean,
        val receiveRedPacketResponse: ReceiveRedPacketResponse?,
        val apiException: ApiException?
)

data class HasRelationshipResult (
        val success: Boolean,
        val oldDeleted: Boolean,
        val relationshipBean: RelationshipBean?,
        val apiException: ApiException?
)

data class GetRoomInfoResult (
        val success: Boolean,
        val roomInfoBean: RoomInfoBean?
)

data class GetRoomUsersResult (
        val targetId: String,
        val wrapper: RoomUserBean.Wrapper?
)