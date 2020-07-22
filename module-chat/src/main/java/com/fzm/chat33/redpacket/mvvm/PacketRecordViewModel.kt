package com.fzm.chat33.redpacket.mvvm

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.fuzamei.common.ext.request
import com.fuzamei.common.ext.result
import com.fzm.chat33.base.mvvm.data.SingleLiveEvent
import com.fzm.chat33.core.bean.RedPacketRecord
import com.fzm.chat33.core.request.RedPacketRecordRequest
import com.fzm.chat33.core.source.RedPacketDataSource
import com.fuzamei.componentservice.app.LoadingViewModel
import javax.inject.Inject

/**
 * @author zhengjy
 * @since 2019/03/11
 * Description:红包收发历史记录ViewModel
 */
class PacketRecordViewModel @Inject constructor(
        private val dataSource: RedPacketDataSource
): LoadingViewModel() {

    /**
     * 表示选中筛选的币种数量
     */
    var coinTypeNum: Int = 0

    /**
     * send列表的请求结果
     */
    private val _sendRedPacketRecord by lazy { MutableLiveData<RedPacketRecord>() }
    val sendRedPacketRecord: LiveData<RedPacketRecord>
        get() = _sendRedPacketRecord

    /**
     * receive列表的请求结果
     */
    private val _receiveRedPacketRecord by lazy { MutableLiveData<RedPacketRecord>() }
    val receiveRedPacketRecord: LiveData<RedPacketRecord>
        get() = _receiveRedPacketRecord

    /**
     * 用于切换筛选条件时，清空send列表
     */
    private val _clearSendList = SingleLiveEvent<Any>()
    val clearSendList: LiveData<Any>
        get() = _clearSendList

    /**
     * 用于切换筛选条件时，清空receive列表
     */
    private val _clearReceiveList = SingleLiveEvent<Any>()
    val clearReceiveList: LiveData<Any>
        get() = _clearReceiveList

    /**
     * send列表请求参数
     */
    private var sendRequest = RedPacketRecordRequest().apply {
        this.operation = 1
        this.pageNum = 0
    }

    /**
     * receive列表请求参数
     */
    private var receiveRequest = RedPacketRecordRequest().apply {
        this.operation = 2
        this.pageNum = 0
    }

    fun clearList() {
        _clearSendList.call()
        _clearReceiveList.call()
    }

    fun changeCoin(coin: Int) {
        sendRequest.coinId = coin
        receiveRequest.coinId = coin
        resetPage()
    }

    private fun resetPage() {
        sendRequest.pageNum = 0
        receiveRequest.pageNum = 0
    }

    fun setDate(start: Long, end: Long) {
        sendRequest.startTime = start
        sendRequest.endTime = end
        receiveRequest.startTime = start
        receiveRequest.endTime = end
    }

    fun obtainRequest(type: Int): RedPacketRecordRequest {
        return if (type == 1) {
            sendRequest
        } else {
            receiveRequest
        }
    }

    fun requestRedPacketRecords(type: Int) {
        request {
            dataSource.redPacketRecord(obtainRequest(type))
        }.result(onSuccess = {
            if (type == 1)
                _sendRedPacketRecord.value = it
            else
                _receiveRedPacketRecord.value = it
        }, onError = {
            if (type == 1)
                _sendRedPacketRecord.value = null
            else
                _receiveRedPacketRecord.value = null
        })
    }
}