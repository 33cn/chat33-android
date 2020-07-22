package com.fzm.chat33.core.net.api

import com.fuzamei.common.net.rxjava.HttpResult
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
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

/**
 * @author zhengjy
 * @since 2019/09/16
 * Description:
 */
interface RedPacketService {

    /**
     * 获取红包币种信息
     *
     * @return
     */
    @POST("chat/red-packet/balance")
    suspend fun packetBalance(): HttpResult<RedPacketCoin.Wrapper>

    /**
     * 获取红包币种信息
     *
     * @return
     */
    @POST("chat/red-packet/balance")
    fun packetBalanceSync(): HttpResult<RedPacketCoin.Wrapper>

    /**
     * 发送红包
     *
     * @param request
     * @return
     */
    @POST("chat/red-packet/send")
    suspend fun sendRedPacket(@Body request: SendRedPacketRequest): HttpResult<SendRedPacketResponse>

    /**
     * 收取红包
     *
     * @param request
     * @return
     */
    @POST("chat/red-packet/receive-entry")
    suspend fun receiveRedPacket(@Body request: ReceiveRedPacketRequest): HttpResult<ReceiveRedPacketResponse>

    /**
     * 红包详情
     *
     * @param map
     * @return
     */
    @JvmSuppressWildcards
    @POST("chat/red-packet/detail")
    suspend fun redPacketInfo(@Body map: Map<String, Any>): HttpResult<RedPacketInfoResponse>

    /**
     * 获取红包领取用户列表
     *
     * @param map
     * @return
     */
    @JvmSuppressWildcards
    @POST("chat/red-packet/receiveDetail")
    suspend fun redPacketReceiveList(@Body map: Map<String, Any>): HttpResult<RedPacketReceiveInfo.Wrapper>

    /**
     * 红包领取记录
     *
     * @param request
     * @return
     */
    @POST("chat/red-packet/statistic")
    suspend fun redPacketRecord(@Body request: RedPacketRecordRequest): HttpResult<RedPacketRecord>

    /**
     * 消息打赏接口
     *
     * @param request
     * @return
     */
    @POST("chat/praise/reward")
    suspend fun messageReward(@Body request: SendRewardPacketRequest): HttpResult<Any>

    /**
     * 用户打赏接口
     *
     * @param request
     * @return
     */
    @POST("chat/praise/rewardUser")
    suspend fun userReward(@Body request: SendRewardPacketRequest): HttpResult<Any>
}