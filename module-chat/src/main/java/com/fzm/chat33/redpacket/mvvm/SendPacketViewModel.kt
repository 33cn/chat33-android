package com.fzm.chat33.redpacket.mvvm

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.fuzamei.common.ext.request
import com.fuzamei.common.ext.result
import com.fuzamei.common.ext.start
import com.fzm.chat33.core.repo.SettingRepository
import com.fzm.chat33.core.request.SendRedPacketRequest
import com.fzm.chat33.core.response.SendRedPacketResponse
import com.fzm.chat33.core.response.StateResponse
import com.fzm.chat33.core.source.RedPacketDataSource
import com.fuzamei.componentservice.app.LoadingViewModel
import com.fzm.chat33.core.request.SendRewardPacketRequest
import javax.inject.Inject

/**
 * @author zhengjy
 * @since 2019/10/10
 * Description:
 */
class SendPacketViewModel @Inject constructor(
        private val dataSource: RedPacketDataSource,
        private val settingRepository: SettingRepository
): LoadingViewModel() {

    private val _sendRedPacket by lazy { MutableLiveData<SendRedPacketResponse>() }
    val sendRedPacket : LiveData<SendRedPacketResponse>
        get() = _sendRedPacket

    private val _sendRewardPacket by lazy { MutableLiveData<Any>() }
    val sendRewardPacket : LiveData<Any>
        get() = _sendRewardPacket

    private val _isSetPayPassword by lazy { MutableLiveData<StateResponse>() }
    val isSetPayPassword : LiveData<StateResponse>
        get() = _isSetPayPassword

    fun sendRedPacket(request: SendRedPacketRequest) {
        start {
            loading()
        }.request{
            dataSource.sendRedPacket(request)
        }.result ({
            _sendRedPacket.value = it
        }, {
            _sendRedPacket.value = null
        }, {
            dismiss()
        })
    }

    fun isSetPayPassword() {
        start {
            loading()
        }.request{
            settingRepository.isSetPayPassword()
        }.result (onSuccess = {
            _isSetPayPassword.value = it
        }, onComplete = {
            dismiss()
        })
    }

    fun sendRewardPacket(request: SendRewardPacketRequest) {
        start {
            loading()
        }.request {
            dataSource.messageReward(request)
        }.result(onSuccess = {
            _sendRewardPacket.value = it
        }, onComplete = {
            dismiss()
        })
    }

    fun sendRewardPacketToUser(request: SendRewardPacketRequest) {
        start {
            loading()
        }.request {
            dataSource.userReward(request)
        }.result(onSuccess = {
            _sendRewardPacket.value = it
        }, onComplete = {
            dismiss()
        })
    }
}

