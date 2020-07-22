package com.fzm.chat33.core.source

import com.fuzamei.common.net.Result
import com.fzm.chat33.core.bean.RedPacketCoin
import com.fzm.chat33.core.bean.RedPacketReceiveInfo
import com.fzm.chat33.core.bean.RedPacketRecord
import com.fzm.chat33.core.request.ReceiveRedPacketRequest
import com.fzm.chat33.core.request.RedPacketRecordRequest
import com.fzm.chat33.core.request.SendRedPacketRequest
import com.fzm.chat33.core.request.SendRewardPacketRequest
import com.fzm.chat33.core.response.ReceiveRedPacketResponse
import com.fzm.chat33.core.response.RedPacketInfoResponse
import com.fzm.chat33.core.response.SendRedPacketResponse

/**
 * @author zhengjy
 * @since 2019/09/16
 * Description:
 */
interface RedPacketDataSource {

    /**
     * 获取红包币种信息
     *
     * @return
     */
    suspend fun packetBalance(): Result<RedPacketCoin.Wrapper>

    /**
     * 获取红包币种信息
     *
     * @return
     */
    suspend fun packetBalanceSync(): Result<RedPacketCoin.Wrapper>

    /**
     * 发送红包
     *
     * @param request
     * @return
     */
    suspend fun sendRedPacket(request: SendRedPacketRequest): Result<SendRedPacketResponse>

    /**
     * 收取红包
     *
     * @param request
     * @return
     */
    suspend fun receiveRedPacket(request: ReceiveRedPacketRequest): Result<ReceiveRedPacketResponse>

    /**
     * 红包详情
     *
     * @param packetId
     * @return
     */
    suspend fun redPacketInfo(packetId: String): Result<RedPacketInfoResponse>

    /**
     * 获取红包领取用户列表
     *
     * @param packetId
     * @return
     */
    suspend fun redPacketReceiveList(packetId: String): Result<RedPacketReceiveInfo.Wrapper>

    /**
     * 红包领取记录
     *
     * @param request
     * @return
     */
    suspend fun redPacketRecord(request: RedPacketRecordRequest): Result<RedPacketRecord>

    /**
     * 消息打赏接口
     *
     * @param request
     * @return
     */
    suspend fun messageReward(request: SendRewardPacketRequest): Result<Any>

    /**
     * 用户打赏接口
     *
     * @param request
     * @return
     */
    suspend fun userReward(request: SendRewardPacketRequest): Result<Any>
}