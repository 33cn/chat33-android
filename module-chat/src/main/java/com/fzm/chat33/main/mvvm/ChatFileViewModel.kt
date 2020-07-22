package com.fzm.chat33.main.mvvm

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.fuzamei.common.ext.request
import com.fuzamei.common.ext.result
import com.fuzamei.common.ext.start
import com.fuzamei.common.net.rxjava.ApiException
import com.fuzamei.componentservice.app.LoadingViewModel
import com.fzm.chat33.base.mvvm.data.RxLiveData
import com.fzm.chat33.core.db.bean.ChatMessage
import com.fzm.chat33.core.global.Chat33Const
import com.fzm.chat33.core.manager.MessageManager
import com.fzm.chat33.core.request.ChatFileHistoryRequest
import com.fzm.chat33.core.response.ChatListResponse
import com.fzm.chat33.core.response.StateResponse
import com.fzm.chat33.core.source.ChatDataSource
import com.fzm.chat33.global.AppConst
import javax.inject.Inject

/**
 * @author zhengjy
 * @since 2019/03/04
 * Description:
 */
class ChatFileViewModel @Inject constructor(
        private val manager: MessageManager,
        private val chatDataSource: ChatDataSource
) : LoadingViewModel() {

    private val _chatFiles by lazy { MutableLiveData<ChatFileResult>() }
    val chatFiles: LiveData<ChatFileResult>
        get() = _chatFiles

    private val _chatMedias by lazy { MutableLiveData<ChatFileResult>() }
    val chatMedias: LiveData<ChatFileResult>
        get() = _chatMedias

    private val _revokeResponse by lazy { MutableLiveData<RevokeFileResult>() }
    val revokeResponse: LiveData<RevokeFileResult>
        get() = _revokeResponse

    val disableDelete by lazy { RxLiveData<Boolean>().apply { value = false } }

    val logIds by lazy { arrayListOf<String>() }
    val fileLogIds by lazy { arrayListOf<String>() }
    val mediaLogIds by lazy { arrayListOf<String>() }
    val messageItems by lazy { arrayListOf<ChatMessage>() }

    // 列表是否是可选择模式
    var selectable: Boolean = false

    var targetId: String? = null
        private set
    var channelType: Int = 0
        private set

    fun setChatTarget(channel: Int, targetId: String?) {
        this.channelType = channel
        this.targetId = targetId
        manager.setChatTarget(channel, targetId!!)
    }

    fun getChatFileList(nextLog: String, channelType: Int) {
        val request = ChatFileHistoryRequest().apply {
            this.id = targetId
            this.startId = nextLog
            this.number = AppConst.PAGE_SIZE
        }
        request{
            chatDataSource.getChatFileHistory(channelType, request)
        }.result (onSuccess = {
            _chatFiles.value = ChatFileResult(it, null)
        }, onError = {
            _chatFiles.value = ChatFileResult(null, it)
        })
    }

    fun getUserChatFileList(userId: String?, nextLog: String) {
        val request = ChatFileHistoryRequest().apply {
            this.id = targetId
            this.owner = userId
            this.startId = nextLog
            this.number = AppConst.PAGE_SIZE
        }
        request {
            chatDataSource.getChatFileHistory(channelType, request)
        }.result (onSuccess = {
            _chatFiles.value = ChatFileResult(it, null)
        }, onError = {
            _chatFiles.value = ChatFileResult(null, it)
        })
    }

    fun searchChatFiles(query: String?, nextLog: String) {
        val request = ChatFileHistoryRequest().apply {
            this.id = targetId
            this.startId = nextLog
            this.query = query
            this.number = AppConst.PAGE_SIZE
        }
        request {
            chatDataSource.getChatFileHistory(channelType, request)
        }.result (onSuccess = {
            _chatFiles.value = ChatFileResult(it, null)
        }, onError = {
            _chatFiles.value = ChatFileResult(null, it)
        })
    }

    fun getChatMediaList(nextLog: String, channelType: Int) {
        val request = ChatFileHistoryRequest().apply {
            this.id = targetId
            this.startId = nextLog
            this.number = AppConst.PAGE_SIZE
        }
        request {
            chatDataSource.getChatMediaHistory(channelType, request)
        }.result (onSuccess = {
            _chatMedias.value = ChatFileResult(it, null)
        }, onError = {
            _chatMedias.value = ChatFileResult(null, it)
        })
    }

    fun sendMessage(sentMsg: ChatMessage) {
        manager.enqueue(sentMsg)
    }

    /**
     * 撤回文件消息
     */
    fun revokeFiles() {
        start {
            loading()
        }.request{
            chatDataSource.revokeFile(fileLogIds, if (channelType == Chat33Const.CHANNEL_ROOM) 1 else 2)
        }.result(onSuccess = {
            _revokeResponse.value = RevokeFileResult(it, null)
        }, onError = {
            _revokeResponse.value = RevokeFileResult(null, it)
        }, onComplete = {
            dismiss()
        })
    }

    override fun onCleared() {
        super.onCleared()
        manager.dispose()
    }
}

data class ChatFileResult(
        val chatFileResponse: ChatListResponse?,
        val apiException: ApiException?
)

data class RevokeFileResult(
        val stateResponse: StateResponse?,
        val apiException: ApiException?
)