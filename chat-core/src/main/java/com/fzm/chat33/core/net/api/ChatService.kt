package com.fzm.chat33.core.net.api

import com.fuzamei.common.net.rxjava.HttpResult
import com.fuzamei.componentservice.config.AppConfig
import com.fzm.chat33.core.bean.*
import com.fzm.chat33.core.bean.param.PraiseRankingParam
import com.fzm.chat33.core.request.ReceiveRedPacketRequest
import com.fzm.chat33.core.request.WithdrawRequest
import com.fzm.chat33.core.request.chat.EncryptForwardRequest
import com.fzm.chat33.core.request.chat.ForwardRequest
import com.fzm.chat33.core.response.ChatListResponse
import com.fzm.chat33.core.response.ReceiveRedPacketResponse
import com.fzm.chat33.core.response.StateResponse
import com.fzm.chat33.core.response.WithdrawResponse
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Url

/**
 * @author zhengjy
 * @since 2019/09/16
 * Description:
 */
interface ChatService {

    /**
     * 撤回指定的一条消息
     *
     * @param map  logId    消息id
     * type     消息类型:1：群消息；2：好友消息
     */
    @JvmSuppressWildcards
    @POST("chat/chat33/RevokeMessage")
    suspend fun revokeMessage(@Body map: Map<String, Any>): HttpResult<Any>

    /**
     * 撤回指定文件
     *
     * @param map  logs    消息id
     * type    消息类型:1：群文件；2：好友文件
     */
    @JvmSuppressWildcards
    @POST("chat/chat33/RevokeFiles")
    suspend fun revokeFile(@Body map: Map<String, Any>): HttpResult<StateResponse>

    /**
     * 转发消息
     *
     * @param request  转发请求参数
     */
    @POST("chat/chat33/forward")
    suspend fun forwardMessage(@Body request: ForwardRequest): HttpResult<StateResponse>

    /**
     * 加密转发消息
     *
     * @param request  加密转发请求参数
     */
    @POST("chat/chat33/encryptForward")
    suspend fun forwardEncryptMessage(@Body request: EncryptForwardRequest): HttpResult<StateResponse>

    @JvmSuppressWildcards
    @POST("chat/chat33/readSnapMsg")
    suspend fun readSnapMessage(@Body map: Map<String, Any>): HttpResult<Any>

    /**
     * 托管账户提币（转账）
     *
     * @param request
     * @return
     */
    @POST("chat/pay/payment")
    suspend fun withdraw(@Body request: WithdrawRequest): HttpResult<WithdrawResponse>

    /**
     * 拉取服务端聊天记录或者文件记录
     *
     * @param url   请求地址
     * @param map   请求参数
     */
    @JvmSuppressWildcards
    @POST
    suspend fun getChatLogHistory(@Url url: String, @Body map: Map<String, Any>): HttpResult<ChatListResponse>

    /**
     * 单个成员禁言设置
     *
     * @param map
     */
    @JvmSuppressWildcards
    @POST("chat/room/setMutedSingle")
    suspend fun setMutedSingle(@Body map: Map<String, Any>): HttpResult<Any>

    /**
     * 收取红包
     *
     * @param request
     * @return
     */
    @POST("chat/red-packet/receive-entry")
    suspend fun receiveRedPacket(@Body request: ReceiveRedPacketRequest): HttpResult<ReceiveRedPacketResponse>

    @JvmSuppressWildcards
    @POST
    suspend fun hasRelationship(@Url url: String, @Body map: Map<String, Any>): HttpResult<RelationshipBean>

    /**
     * 赞赏列表
     *
     * @param map
     */
    @JvmSuppressWildcards
    @POST("chat/praise/list")
    suspend fun praiseList(@Body map: Map<String, Any>): HttpResult<PraiseBean.Wrapper>

    /**
     * 消息赞赏详情
     *
     * @param map
     */
    @JvmSuppressWildcards
    @POST("chat/praise/details")
    suspend fun praiseDetails(@Body map: Map<String, Any>): HttpResult<PraiseDetail>

    /**
     * 消息赞赏列表
     *
     * @param map
     */
    @JvmSuppressWildcards
    @POST("chat/praise/detailList")
    suspend fun praiseDetailList(@Body map: Map<String, Any>): HttpResult<PraiseBean.Wrapper>

    /**
     * 消息点赞
     *
     * @param map
     */
    @JvmSuppressWildcards
    @POST("chat/praise/like")
    suspend fun messageLike(@Body map: Map<String, Any>): HttpResult<Any>

    /**
     * 获取指定周的赞赏排行
     *
     * @param param
     */
    @POST("chat/praise/leaderboard")
    suspend fun praiseRanking(@Body param: PraiseRankingParam): HttpResult<PraiseRank.Wrapper>

    /**
     * 获取赞赏排行历史记录
     *
     * @param map
     */
    @JvmSuppressWildcards
    @POST("chat/praise/leaderboardHistory")
    suspend fun praiseRankingHistory(@Body map: Map<String, Any>): HttpResult<PraiseRankHistory.Wrapper>

    companion object {

        /**
         * 消息记录接口地址
         */
        val get_group_chat_log = AppConfig.CHAT_BASE_URL + "chat/group/getGroupChatHistory"
        val get_room_chat_log = AppConfig.CHAT_BASE_URL + "chat/room/chatLog"
        val get_private_chat_log = AppConfig.CHAT_BASE_URL + "chat/friend/chatLog"

        /**
         * 文件记录接口地址
         */
        val get_room_chat_file = AppConfig.CHAT_BASE_URL + "chat/room/historyFiles"
        val get_room_chat_media = AppConfig.CHAT_BASE_URL + "chat/room/historyPhotos"
        val get_private_chat_file = AppConfig.CHAT_BASE_URL + "chat/friend/historyFiles"
        val get_private_chat_media = AppConfig.CHAT_BASE_URL + "chat/friend/historyPhotos"
    }
}