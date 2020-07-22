package com.fzm.chat33.main.mvvm

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.fuzamei.common.ext.request
import com.fuzamei.common.ext.result
import com.fuzamei.common.ext.start
import com.fuzamei.componentservice.app.LoadingViewModel
import com.fzm.chat33.core.bean.param.AddFriendParam
import com.fzm.chat33.core.bean.param.JoinGroupParam
import com.fzm.chat33.core.repo.ContactsRepository
import com.fzm.chat33.core.response.StateResponse
import javax.inject.Inject

/**
 * @author zhengjy
 * @since 2019/10/10
 * Description:
 */
class AddVerifyViewModel @Inject constructor(
        private val repository: ContactsRepository
): LoadingViewModel() {

    private val _addFriend by lazy { MutableLiveData<StateResponse>() }
    val addFriend : LiveData<StateResponse>
        get() = _addFriend

    private val _joinRoomApply by lazy { MutableLiveData<Any>() }
    val joinRoomApply : LiveData<Any>
        get() = _joinRoomApply

    @Deprecated("没有好友验证这一步，因此不会调用到这个方法")
    fun addFriend(param: AddFriendParam) {
        start {
            loading()
        }.request{
            repository.addFriend(param)
        }.result (onSuccess = {
            _addFriend.value = it
        }, onComplete = {
            dismiss()
        })
    }

    fun joinRoomApply(param: JoinGroupParam) {
        start {
            loading()
        }.request{
            repository.joinRoomApply(param)
        }.result (onSuccess = {
            _joinRoomApply.value = it
        }, onComplete = {
            dismiss()
        })
    }
}