package com.fzm.chat33.main.mvvm

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.fuzamei.common.ext.request
import com.fuzamei.common.ext.result
import com.fuzamei.common.utils.RoomUtils
import com.fuzamei.componentservice.app.LoadingViewModel
import com.fzm.chat33.core.bean.PraiseBean
import com.fzm.chat33.core.db.ChatDatabase
import com.fzm.chat33.core.db.bean.RoomContact
import com.fzm.chat33.core.repo.ChatRepository
import com.fzm.chat33.core.repo.ContactsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * @author yll
 * @since 2019/11/19
 * Description:赞赏列表
 */
class ChatPraiseViewModel @Inject constructor(
        private val chatRepository: ChatRepository
) : LoadingViewModel() {

    private val _praiseList by lazy { MutableLiveData<PraiseBean.Wrapper>() }
    val praiseList: LiveData<PraiseBean.Wrapper>
        get() = _praiseList


    fun getChatPraises(channelType: Int, targetId: String, startId: String?) {
        request {
            chatRepository.praiseList(channelType, targetId, startId)
        }.result({
            _praiseList.value = it
        }, {
            _praiseList.value = null
        })
    }

    fun clearPraise(channelType: Int, targetId: String) {
        RoomUtils.run(Runnable {
            ChatDatabase.getInstance().recentMessageDao().clearPraise(channelType, targetId)
        })
    }
}