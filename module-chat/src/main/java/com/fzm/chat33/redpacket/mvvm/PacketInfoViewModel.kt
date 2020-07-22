package com.fzm.chat33.redpacket.mvvm

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.fuzamei.common.ext.request
import com.fuzamei.common.ext.result
import com.fuzamei.common.ext.start
import com.fzm.chat33.core.bean.RedPacketReceiveInfo
import com.fzm.chat33.core.response.RedPacketInfoResponse
import com.fzm.chat33.core.source.RedPacketDataSource
import com.fuzamei.componentservice.app.LoadingViewModel
import javax.inject.Inject

/**
 * @author zhengjy
 * @since 2019/10/10
 * Description:
 */
class PacketInfoViewModel @Inject constructor(
        private val dataSource: RedPacketDataSource
): LoadingViewModel() {

    private val _redPacketInfo by lazy { MutableLiveData<RedPacketInfoResponse>() }
    val redPacketInfo : LiveData<RedPacketInfoResponse>
        get() = _redPacketInfo

    private val _redPacketReceiveList by lazy { MutableLiveData<RedPacketReceiveInfo.Wrapper>() }
    val redPacketReceiveList : LiveData<RedPacketReceiveInfo.Wrapper>
        get() = _redPacketReceiveList

    fun redPacketInfo(packetId: String) {
        start {
            loading()
        }.request{
            dataSource.redPacketInfo(packetId)
        }.result (onSuccess = {
            _redPacketInfo.value = it
        }, onComplete = {
            dismiss()
        })
    }

    fun redPacketReceiveList(packetId: String) {
        request{
            dataSource.redPacketReceiveList(packetId)
        }.result {
            _redPacketReceiveList.value = it
        }
    }
}

