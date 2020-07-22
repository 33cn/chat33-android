package com.fzm.chat33.main.mvvm

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.fuzamei.common.base.mvvm.SingleLiveData
import com.fuzamei.common.ext.rawResult
import com.fuzamei.common.ext.request
import com.fuzamei.common.ext.result
import com.fuzamei.common.ext.start
import com.fuzamei.common.net.Result
import com.fuzamei.componentservice.app.LoadingViewModel
import com.fzm.chat33.core.bean.ApplyInfoBean
import com.fzm.chat33.core.bean.RelationshipBean
import com.fzm.chat33.core.global.Chat33Const
import com.fzm.chat33.core.repo.ContactsRepository
import javax.inject.Inject

/**
 * @author zhengjy
 * @since 2019/10/23
 * Description:
 */
class NewFriendViewModel @Inject constructor(
        private val repository: ContactsRepository
): LoadingViewModel() {

    private val _applyList by lazy { MutableLiveData<Result<ApplyInfoBean.Wrapper>>() }
    val applyList: LiveData<Result<ApplyInfoBean.Wrapper>>
        get() = _applyList

    private val _friendRequest by lazy { MutableLiveData<Triple<Int, Int, String>>() }
    val friendRequest: LiveData<Triple<Int, Int, String>>
        get() = _friendRequest

    private val _groupRequest by lazy { MutableLiveData<Triple<Int, Int, String>>() }
    val groupRequest: LiveData<Triple<Int, Int, String>>
        get() = _groupRequest

    fun hasRelationship(channelType: Int, id: String): LiveData<RelationshipBean> {
        val result = SingleLiveData<RelationshipBean>()
        start {
            loading()
        }.request {
            if (channelType == Chat33Const.CHANNEL_ROOM) {
                repository.isInRoom(id)
            } else {
                repository.isFriend(id)
            }
        }.result(onSuccess = {
            result.value = it
        }, onComplete = {
            dismiss()
        })
        return result
    }

    fun getFriendsApplyList(id: String?, number: Int, initLoad: Boolean) {
        start {
            if (initLoad) {
                loading()
            }
        }.request {
            repository.getFriendsApplyList(id, number)
        }.rawResult({
            it.dataOrNull()?.let { wrapper ->
                wrapper.clearData = id == null
            }
            _applyList.value = it
        }, {
            dismiss()
        })
    }

    fun dealFriendRequest(id: String, agree: Int, position: Int) {
        start {
            loading()
        }.request {
            repository.dealFriendRequest(id, agree)
        }.result(onSuccess = {
            _friendRequest.value = Triple(agree, position, id)
        }, onComplete = {
            dismiss()
        })
    }

    fun dealJoinRoomApply(roomId: String, userId: String, agree: Int, position: Int) {
        start {
            loading()
        }.request {
            repository.dealJoinRoomApply(roomId, userId, agree)
        }.result(onSuccess = {
            _groupRequest.value = Triple(agree, position, roomId)
        }, onComplete = {
            dismiss()
        })
    }
}