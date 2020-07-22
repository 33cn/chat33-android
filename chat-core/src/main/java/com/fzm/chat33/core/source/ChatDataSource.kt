package com.fzm.chat33.core.source

import com.fuzamei.common.net.Result
import com.fzm.chat33.core.bean.*
import com.fzm.chat33.core.bean.param.PraiseRankingParam
import com.fzm.chat33.core.request.ChatFileHistoryRequest
import com.fzm.chat33.core.request.ChatLogHistoryRequest
import com.fzm.chat33.core.request.ReceiveRedPacketRequest
import com.fzm.chat33.core.request.WithdrawRequest
import com.fzm.chat33.core.request.chat.EncryptForwardRequest
import com.fzm.chat33.core.request.chat.ForwardRequest
import com.fzm.chat33.core.response.ChatListResponse
import com.fzm.chat33.core.response.ReceiveRedPacketResponse
import com.fzm.chat33.core.response.StateResponse
import com.fzm.chat33.core.response.WithdrawResponse

/**
 * @author zhengjy
 * @since 2019/09/16
 * Description:
 */
interface ChatDataSource {

    /**
     * 撤回指定的一条消息
     *
     * @param logId     消息id
     * @param type      消息类型:1：群消息；2：好友消息
     */
    suspend fun revokeMessage(logId: String, type: Int): Result<Any>

    /**
     * 撤回指定文件
     *
     * @param logs  消息id
     * @param type  消息类型:1：群文件；2：好友文件
     */
    suspend fun revokeFile(logs: List<String>, type: Int): Result<StateResponse>

    /**
     * 转发消息
     *
     * @param request  转发请求参数
     */
    suspend fun forwardMessage(request: ForwardRequest): Result<StateResponse>

    /**
     * 加密转发消息
     *
     * @param request  加密转发请求参数
     */
    suspend fun forwardEncryptMessage(request: EncryptForwardRequest): Result<StateResponse>

    suspend fun readSnapMessage(logId: String, type: Int): Result<Any>

    /**
     * 托管账户提币（转账）
     *
     * @param request
     * @return
     */
    suspend fun withdraw(request: WithdrawRequest): Result<WithdrawResponse>

    /**
     * 拉取服务端聊天记录
     *
     * @param channelType   记录类型
     * @param request       请求参数
     */
    suspend fun getChatLogHistory(channelType: Int, request: ChatLogHistoryRequest): Result<ChatListResponse>

    /**
     * 拉取服务端文件记录
     *
     * @param channelType   记录类型
     * @param request       请求参数
     */
    suspend fun getChatFileHistory(channelType: Int, request: ChatFileHistoryRequest): Result<ChatListResponse>

    /**
     * 拉取服务端图片视频等记录
     *
     * @param channelType   记录类型
     * @param request       请求参数
     */
    suspend fun getChatMediaHistory(channelType: Int, request: ChatFileHistoryRequest): Result<ChatListResponse>

    /**
     * 单个成员禁言设置
     *
     * @param roomId 禁言群组
     * @param userId 禁言用户
     * @param deadline 禁言时间 0为取消禁言
     */
    suspend fun setMutedSingle(roomId: String, userId: String, deadline: Long): Result<Any>

    /**
     * 收取红包
     *
     * @param request
     */
    suspend fun receiveRedPacket(request: ReceiveRedPacketRequest): Result<ReceiveRedPacketResponse>

    suspend fun hasRelationship(channelType: Int, id: String): Result<RelationshipBean>

    /**
     * 赞赏列表
     *
     * @param channelType 1: 聊天室（弃用）； 2：群组；3：好友
     * @param targetId 群或好友id
     * @param startId 从start_id的消息开始往前拉取记录，若id为空则拉取最近的消息
     */
    suspend fun praiseList(channelType: Int, targetId: String, startId: String?): Result<PraiseBean.Wrapper>

    /**
     * 消息赞赏详情
     *
     * @param channelType 1: 聊天室（弃用）； 2：群组；3：好友
     * @param logId 消息id
     */
    suspend fun praiseDetails(channelType: Int, logId: String): Result<PraiseDetail>

    /**
     * 消息赞赏列表
     *
     * @param channelType 1: 聊天室（弃用）； 2：群组；3：好友
     * @param startId 从start_id的消息开始往前拉取记录，若id为空则拉取最近的消息
     * @param logId 消息id
     */
    suspend fun praiseDetailList(channelType: Int, startId: String?, logId: String): Result<PraiseBean.Wrapper>

    /**
     * 消息点赞
     *
     * @param channelType   1: 聊天室（弃用）； 2：群组；3：好友
     * @param logId         消息id
     * @param action        操作类型："like":点赞，"cancel_like":取消点赞
     */
    suspend fun messageLike(channelType: Int, logId: String, action: String): Result<Any>

    /**
     * 赞赏排行
     *
     * @param param
     */
    suspend fun praiseRanking(param: PraiseRankingParam): Result<PraiseRank.Wrapper>

    /**
     * 赞赏排行历史记录
     *
     * @param page  分页页码
     * @param size  分页大小
     */
    suspend fun praiseRankingHistory(page: Int, size: Int): Result<PraiseRankHistory.Wrapper>
}