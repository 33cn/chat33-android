package com.fzm.chat33.main.mvvm

import android.text.TextUtils
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.fuzamei.common.ext.request
import com.fuzamei.common.ext.result
import com.fuzamei.common.ext.start
import com.fuzamei.common.utils.run
import com.fuzamei.componentservice.app.LoadingViewModel
import com.fuzamei.componentservice.config.AppConfig
import com.fzm.chat33.core.Chat33
import com.fzm.chat33.core.bean.ChatTarget
import com.fzm.chat33.core.bean.param.DecryptParams
import com.fzm.chat33.core.bean.param.toDecParams
import com.fzm.chat33.core.bean.param.toEncParams
import com.fzm.chat33.core.db.ChatDatabase
import com.fzm.chat33.core.db.bean.*
import com.fzm.chat33.core.db.bean.ChatMessage.Type.*
import com.fzm.chat33.core.global.Chat33Const.CHANNEL_FRIEND
import com.fzm.chat33.core.global.Chat33Const.CHANNEL_ROOM
import com.fzm.chat33.core.global.LoginInfoDelegate
import com.fzm.chat33.core.manager.*
import com.fzm.chat33.core.net.OssModel
import com.fzm.chat33.core.request.chat.*
import com.fzm.chat33.core.response.StateResponse
import com.fzm.chat33.core.repo.ChatRepository
import com.fzm.chat33.core.repo.ContactsRepository
import com.google.gson.Gson
import io.reactivex.disposables.Disposable
import io.reactivex.functions.Consumer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.kodein.di.Kodein
import org.kodein.di.conf.global
import org.kodein.di.generic.instance
import java.io.File
import java.util.*
import javax.inject.Inject
import kotlin.collections.ArrayList

/**
 * @author zhengjy
 * @since 2019/09/03
 * Description:
 */
class ContactSelectViewModel @Inject constructor(
        private val repository: ChatRepository,
        private val manager: MessageManager,
        private val contactsRepository: ContactsRepository
) : LoadingViewModel(), LoginInfoDelegate by manager {

    private val gson: Gson by Kodein.global.instance()

    private val _forwardResult by lazy { MutableLiveData<StateResponse>() }
    val forwardResult: LiveData<StateResponse>
        get() = _forwardResult

    private val _forwardSingle by lazy { MutableLiveData<Any>() }
    val forwardSingle: LiveData<Any>
        get() = _forwardSingle

    val updateFriend: LiveData<List<FriendBean>>
        get() = contactsRepository.updateFriend

    val updateBlocked: LiveData<List<FriendBean>>
        get() = contactsRepository.updateBlocked

    val updateRoom: LiveData<List<RoomListBean>>
        get() = contactsRepository.updateRoom

    private val recentFriendList = ArrayList<RecentContact>()
    private val recentRoomList = ArrayList<RecentContact>()

    private val _recentList by lazy { MutableLiveData<List<RecentContact>>() }
    val recentList: LiveData<List<RecentContact>>
        get() = _recentList

    private val _searchContactList by lazy { MutableLiveData<List<Contact>>() }
    val searchContactList: LiveData<List<Contact>>
        get() = _searchContactList

    /**
     * 客户端直接转发单条消息
     *
     * @param params    解密参数
     * @param chatFile  消息内容
     * @param friendIds 转发好友id列表
     * @param groupIds  转发群id列表
     */
    fun forwardSingleMessage(params: DecryptParams?, chatFile: ChatFile, friendIds: List<String>,
                             groupIds: List<String>) = launch(Dispatchers.Main) {
        loading()
        launch(Dispatchers.IO) {
            for (i in friendIds.indices) {
                sendMessage(params, CHANNEL_FRIEND, friendIds[i], chatFile)
            }
            for (i in groupIds.indices) {
                sendMessage(params, CHANNEL_ROOM, groupIds[i], chatFile)
            }
        }
        _forwardSingle.value = null
        dismiss()
    }

    private suspend fun sendMessage(params: DecryptParams?, channelType: Int, targetId: String, chatFile: ChatFile) {
        val sentMsg = ChatMessage.create(targetId, channelType, chatFile.chatFileType, 2, chatFile)
        val file = when {
            !chatFile.localPath.isNullOrEmpty() -> File(chatFile.localPath)
            sentMsg.msgType == IMAGE -> DownloadManager.downloadTemp(chatFile.imageUrl)
            sentMsg.msgType == VIDEO -> DownloadManager.downloadTemp(chatFile.mediaUrl)
            sentMsg.msgType == FILE -> DownloadManager.downloadTemp(chatFile.fileUrl)
            else -> null
        }
        if (file != null) {
            if (params != null) {
                FileEncryption.decrypt(params, file.toByteArray())?.toCacheFile(file.path)?.apply {
                    val url = OssModel.getInstance().uploadMedia(sentMsg.toEncParams(), path, sentMsg.msgType)
                    when (sentMsg.msgType) {
                        IMAGE -> sentMsg.msg.imageUrl = url
                        VIDEO -> sentMsg.msg.mediaUrl = url
                        FILE -> sentMsg.msg.fileUrl = url
                    }
                }
            } else {
                val url = OssModel.getInstance().uploadMedia(sentMsg.toEncParams(), file.path, sentMsg.msgType)
                when (sentMsg.msgType) {
                    IMAGE -> sentMsg.msg.imageUrl = url
                    VIDEO -> sentMsg.msg.mediaUrl = url
                    FILE -> sentMsg.msg.fileUrl = url
                }
            }
        }
        sentMsg.channelType = channelType
        if (!AppConfig.FILE_ENCRYPT) {
            if (sentMsg.msgType == VIDEO || sentMsg.msgType == FILE) {
                // 如果是转发文件，则把本地路径带上
                Chat33.getLocalCache().localPathMap[sentMsg.msgId] = chatFile.localPath
            }
        }
        manager.setChatTarget(channelType, targetId, false)
        manager.enqueueAndDispose(sentMsg)
    }

    /**
     * 请求服务端转发普通消息
     *
     * @param preForward    预转发请求参数
     * @param friendIds     转发好友id列表
     * @param groupIds      转发群id列表
     */
    fun forwardMessage(preForward: PreForwardRequest, friendIds: List<String>, groupIds: List<String>) {
        val request = ForwardRequest(preForward)
        start {
            loading()
        }.request {
            request.targetUsers = friendIds
            request.targetRooms = groupIds
            repository.forwardMessage(request)
        }.result({
            _forwardResult.value = it
        }, {
            _forwardResult.value = null
        }, {
            dismiss()
        })
    }

    /**
     * 将加密消息转发给服务端，让其分发给目标用户或群
     *
     * @param preForward    预转发请求参数
     * @param friendIds     转发好友id列表
     * @param groupIds      转发群id列表
     */
    fun forwardEncryptMessage(preForward: PreForwardRequest, friendIds: List<String>, groupIds: List<String>) {
        start {
            loading()
        }.request {
            val request = EncryptForwardRequest(preForward.forwardType)
            if (request.type == 1) {
                // 逐条转发
                for (i in friendIds.indices) {
                    request.userLogs.add(encryptSingle(friendIds[i], CHANNEL_FRIEND, preForward))
                }
                for (i in groupIds.indices) {
                    request.roomLogs.add(encryptSingle(groupIds[i], CHANNEL_ROOM, preForward))
                }
            } else {
                // 合并转发
                for (i in friendIds.indices) {
                    request.userLogs.add(encryptBatch(friendIds[i], CHANNEL_FRIEND, preForward))
                }
                for (i in groupIds.indices) {
                    request.roomLogs.add(encryptBatch(groupIds[i], CHANNEL_ROOM, preForward))
                }
            }
            repository.forwardEncryptMessage(request)
        }.result({
            _forwardResult.value = it
        }, {
            _forwardResult.value = null
        }, {
            dismiss()
        })
    }

    /**
     * 逐条转发，依次加密发给某个用户或群的每一条消息
     *
     * @param target        转发对象id
     * @param channel       转发对象类型（群，好友）
     * @param preForward    预转发请求参数
     *
     * @return  用户发给指定用户或群的加密消息
     */
    private suspend fun encryptSingle(target: String, channel: Int, preForward: PreForwardRequest): EncryptForwardRequest.TargetMessage {
        val list: MutableList<EncryptForwardRequest.Message> = mutableListOf()
        for (log in preForward.logArray) {
            val message = EncryptForwardRequest.Message(log.msgType)
            val content: Any = when (message.msgType) {
                SYSTEM, TEXT -> TextRequest(preForward).apply {
                    content = log.msg.content
                }
                IMAGE -> ImageRequest(preForward).apply {
                    imageUrl = log.msg.imageUrl
                    height = log.msg.height
                    width = log.msg.width
                }
                VIDEO -> VideoRequest(preForward).apply {
                    mediaUrl = log.msg.mediaUrl
                    time = log.msg.duration
                    height = log.msg.height
                    width = log.msg.width
                }
                FILE -> FileRequest(preForward).apply {
                    fileUrl = log.msg.fileUrl
                    fileName = log.msg.fileName
                    fileSize = log.msg.fileSize
                    md5 = log.msg.md5
                }
                else -> BaseChatRequest(preForward)
            }
            val file = when (log.msgType) {
                IMAGE -> DownloadManager.downloadTemp(log.msg.imageUrl)
                VIDEO -> DownloadManager.downloadTemp(log.msg.mediaUrl)
                FILE -> DownloadManager.downloadTemp(log.msg.fileUrl)
                else -> null
            }
            if (file != null) {
                FileEncryption.decrypt(log.toDecParams(), file.toByteArray())?.toCacheFile(file.path)?.apply {
                    val url = OssModel.getInstance().uploadMedia(ChatTarget(channel, target).toEncParams(), path, message.msgType)
                    when (log.msgType) {
                        IMAGE -> (content as ImageRequest).imageUrl = url
                        VIDEO -> (content as VideoRequest).mediaUrl = url
                        FILE -> (content as FileRequest).fileUrl = url
                    }
                }
            }
            when (message.msgType) {
                RED_PACKET, TRANSFER, RECEIPT -> message.msg = content
                else -> message.msg = getMessageRequest(content, target, channel)
            }
            list.add(message)
        }
        return EncryptForwardRequest.TargetMessage(target, list)
    }

    /**
     * 合并转发，统一加密发给某个用户或群的所有消息
     *
     * @param target        转发对象id
     * @param channel       转发对象类型（群，好友）
     * @param preForward    预转发请求参数
     *
     * @return  用户发给指定用户或群的加密消息
     */
    private suspend fun encryptBatch(target: String, channel: Int, preForward: PreForwardRequest): EncryptForwardRequest.TargetMessage {
        val list = mutableListOf<BaseChatRequest.SourceChatLog>()
        for (log in preForward.logArray) {
            list.add(BaseChatRequest.SourceChatLog().apply {
                msgType = log.msgType
                logId = log.logId
                datetime = log.sendTime
                msg = when (msgType) {
                    SYSTEM, TEXT -> TextRequest().apply {
                        content = log.msg.content
                    }
                    IMAGE -> ImageRequest().apply {
                        imageUrl = log.msg.imageUrl
                        height = log.msg.height
                        width = log.msg.width
                    }
                    VIDEO -> VideoRequest().apply {
                        mediaUrl = log.msg.mediaUrl
                        time = log.msg.duration
                        height = log.msg.height
                        width = log.msg.width
                    }
                    FILE -> FileRequest().apply {
                        fileUrl = log.msg.fileUrl
                        fileName = log.msg.fileName
                        fileSize = log.msg.fileSize
                        md5 = log.msg.md5
                    }
                    else -> BaseChatRequest()
                }
                senderInfo = log.senderInfo
            })
            val file = when (log.msgType) {
                IMAGE -> DownloadManager.downloadTemp(log.msg.imageUrl)
                VIDEO -> DownloadManager.downloadTemp(log.msg.mediaUrl)
                FILE -> DownloadManager.downloadTemp(log.msg.fileUrl)
                else -> null
            }
            if (file != null) {
                FileEncryption.decrypt(log.toDecParams(), file.toByteArray())?.toCacheFile(file.path)?.apply {
                    val url = OssModel.getInstance().uploadMedia(ChatTarget(channel, target).toEncParams(), path, log.msgType)
                    when (log.msgType) {
                        IMAGE -> (list.last().msg as ImageRequest).imageUrl = url
                        VIDEO -> (list.last().msg as VideoRequest).mediaUrl = url
                        FILE -> (list.last().msg as FileRequest).fileUrl = url
                    }
                }
            }
        }
        val request = BaseChatRequest(preForward).apply {
            data = list
        }
        val message = EncryptForwardRequest.Message(FORWARD).apply {
            msg = getMessageRequest(request, target, channel)
        }
        return EncryptForwardRequest.TargetMessage(target, Collections.singletonList(message))
    }

    /**
     * 加密具体的消息内容
     *
     * @param request   待加密的内容
     * @param target    消息发送对象id
     * @param channel   消息发送对象channelType
     *
     * @return  加密之后的消息内容对象
     */
    private fun getMessageRequest(request: Any, target: String, channel: Int): Any {
        val result: BaseChatRequest
        if (channel == CHANNEL_FRIEND) {
            val publicKey = contactsRepository.getLocalFriendById(target)?.publicKey
            if (!TextUtils.isEmpty(publicKey) && !TextUtils.isEmpty(CipherManager.getPrivateKey())) {
                result = BaseChatRequest()
                result.fromKey = CipherManager.getPublicKey()
                result.toKey = publicKey
                try {
                    result.encryptedMsg = CipherManager.encryptString(gson.toJson(request), publicKey, CipherManager.getPrivateKey())
                } catch (e: Exception) {
                    return request
                }
            } else {
                return request
            }
        } else {
            val room = contactsRepository.getLocalRoomById(target)
            if (room != null && room.encrypt == 1) {
                val roomKey = ChatDatabase.getInstance().roomKeyDao().getLatestKey(target)
                result = BaseChatRequest()
                result.kid = roomKey.kid
                try {
                    result.encryptedMsg = CipherManager.encryptSymmetric(gson.toJson(request), roomKey.keySafe)
                } catch (e: Exception) {
                    return request
                }
            } else {
                return request
            }
        }
        return result
    }

    private var friendDisposable: Disposable? = null
    private var roomDisposable: Disposable? = null

    fun getRecentFriendList() {
        friendDisposable = ChatDatabase.getInstance().recentMessageDao().recentFriend.run(Consumer { recentMessages ->
            recentFriendList.clear()
            recentFriendList.addAll(recentMessages)
            changeRecentList()
        })
    }

    fun getRecentRoomList() {
        roomDisposable = ChatDatabase.getInstance().recentMessageDao().recentRoom.run(Consumer { recentMessages ->
            recentRoomList.clear()
            recentRoomList.addAll(recentMessages)
            changeRecentList()
        })
    }

    private fun changeRecentList() {
        val dataList = ArrayList<RecentContact>()
        dataList.addAll(recentFriendList)
        dataList.addAll(recentRoomList)
        _recentList.value = dataList
    }

    fun searchContact(keywords: String) {
        launch {
            _searchContactList.value = withContext(Dispatchers.IO) {
                contactsRepository.searchContacts(keywords)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        friendDisposable?.dispose()
        roomDisposable?.dispose()
    }
}