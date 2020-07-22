package com.fzm.chat33.main.mvvm

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.fuzamei.common.ext.request
import com.fuzamei.common.ext.result
import com.fuzamei.common.ext.start
import com.fuzamei.componentservice.app.LoadingViewModel
import com.fzm.chat33.core.bean.PraiseBean
import com.fzm.chat33.core.bean.PraiseDetail
import com.fzm.chat33.core.consts.PraiseAction
import com.fzm.chat33.core.global.Chat33Const
import com.fzm.chat33.core.logic.decryptFriend
import com.fzm.chat33.core.logic.decryptGroup
import com.fzm.chat33.core.repo.ChatRepository
import com.google.gson.Gson
import kotlinx.coroutines.*
import org.kodein.di.Kodein
import org.kodein.di.conf.global
import org.kodein.di.generic.instance
import javax.inject.Inject

/**
 * @author yll
 * @since 2019/11/19
 * Description:赞赏列表
 */
class MessagePraiseViewModel @Inject constructor(
        private val chatRepository: ChatRepository
) : LoadingViewModel() {

    private val gson: Gson by Kodein.global.instance()

    private val _praiseDetail by lazy { MutableLiveData<PraiseDetail>() }
    val praiseDetail: LiveData<PraiseDetail>
        get() = _praiseDetail

    private val _praiseList by lazy { MutableLiveData<PraiseBean.Wrapper>() }
    val praiseList: LiveData<PraiseBean.Wrapper>
        get() = _praiseList

    private val _praise by lazy { MutableLiveData<Any>() }
    val praise: LiveData<Any>
        get() = _praise

    private val _cancelPraise by lazy { MutableLiveData<Any>() }
    val cancelPraise: LiveData<Any>
        get() = _cancelPraise

    fun praiseDetails(channelType: Int, logId: String) {
        request {
            chatRepository.praiseDetails(channelType, logId)
        }.result {
            launch {
                val result = withContext(Dispatchers.IO) {
                    try {
                        if (it.log.msg.encryptedMsg.isNullOrEmpty()) {
                            it.log
                        } else {
                            when (channelType) {
                                Chat33Const.CHANNEL_ROOM -> decryptGroup(it.log, gson)
                                Chat33Const.CHANNEL_FRIEND -> decryptFriend(it.log, gson)
                                else -> it.log
                            }
                        }
                    } catch (e: Exception) {
                        it.log
                    }
                }
                _praiseDetail.value = it.apply { log = result }
            }
        }
    }

    fun praiseDetailList(channelType: Int, startId: String?, logId: String) {
        request {
            chatRepository.praiseDetailList(channelType, startId, logId)
        }.result({
            _praiseList.value = it
        }, {
            _praiseList.value = null
        })
    }

    fun praise(channelType: Int, logId: String) {
        start {
            loading()
        }.request {
            chatRepository.messageLike(channelType, logId, PraiseAction.ACTION_LIKE)
        }.result(onSuccess = {
            _praise.value = it
        }, onComplete = {
            dismiss()
        })
    }
}