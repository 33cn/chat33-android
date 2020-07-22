package com.fzm.chat33.core.source.impl

import com.fuzamei.common.net.Result
import com.fuzamei.componentservice.ext.apiCall
import com.fzm.chat33.core.bean.RedPacketCoin
import com.fzm.chat33.core.bean.RedPacketReceiveInfo
import com.fzm.chat33.core.bean.RedPacketRecord
import com.fzm.chat33.core.net.api.RedPacketService
import com.fzm.chat33.core.request.ReceiveRedPacketRequest
import com.fzm.chat33.core.request.RedPacketRecordRequest
import com.fzm.chat33.core.request.SendRedPacketRequest
import com.fzm.chat33.core.request.SendRewardPacketRequest
import com.fzm.chat33.core.response.ReceiveRedPacketResponse
import com.fzm.chat33.core.response.RedPacketInfoResponse
import com.fzm.chat33.core.response.SendRedPacketResponse
import com.fzm.chat33.core.source.RedPacketDataSource
import javax.inject.Inject

/**
 * @author Yaoll
 * @since 2019/11/1
 * Description:
 */
class NetRedPacketDataSource @Inject constructor(
        private val dataSource: RedPacketService
) : RedPacketDataSource {

    override suspend fun packetBalance(): Result<RedPacketCoin.Wrapper> {
        return apiCall { dataSource.packetBalance() }
    }

    override suspend fun packetBalanceSync(): Result<RedPacketCoin.Wrapper> {
        return apiCall { dataSource.packetBalanceSync() }
    }

    override suspend fun sendRedPacket(request: SendRedPacketRequest): Result<SendRedPacketResponse> {
        return apiCall { dataSource.sendRedPacket(request) }
    }

    override suspend fun receiveRedPacket(request: ReceiveRedPacketRequest): Result<ReceiveRedPacketResponse> {
        return apiCall { dataSource.receiveRedPacket(request) }
    }

    override suspend fun redPacketInfo(packetId: String): Result<RedPacketInfoResponse> {
        val map = mapOf("packetId" to packetId)
        return apiCall { dataSource.redPacketInfo(map) }
    }

    override suspend fun redPacketReceiveList(packetId: String): Result<RedPacketReceiveInfo.Wrapper> {
        val map = mapOf("packetId" to packetId)
        return apiCall { dataSource.redPacketReceiveList(map) }
    }

    override suspend fun redPacketRecord(request: RedPacketRecordRequest): Result<RedPacketRecord> {
        return apiCall { dataSource.redPacketRecord(request) }
    }

    override suspend fun messageReward(request: SendRewardPacketRequest): Result<Any> {
        return apiCall { dataSource.messageReward(request) }
    }

    override suspend fun userReward(request: SendRewardPacketRequest): Result<Any> {
        return apiCall { dataSource.userReward(request) }
    }

}