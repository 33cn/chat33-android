package com.fzm.chat33.core.source.impl

import com.fuzamei.common.net.Result
import com.fuzamei.componentservice.config.AppConfig
import com.fuzamei.componentservice.ext.apiCall
import com.fzm.chat33.core.bean.*
import com.fzm.chat33.core.bean.param.PraiseRankingParam
import com.fzm.chat33.core.global.Chat33Const
import com.fzm.chat33.core.net.ApiService
import com.fzm.chat33.core.net.api.ChatService
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
import com.fzm.chat33.core.source.ChatDataSource
import javax.inject.Inject

/**
 * @author zhengjy
 * @since 2019/09/16
 * Description:
 */
class NetChatDataSource @Inject constructor(
        private val service: ChatService
) : ChatDataSource {

    override suspend fun revokeMessage(logId: String, type: Int): Result<Any> {
        val map = mapOf("logId" to logId, "type" to type)
        return apiCall { service.revokeMessage(map) }
    }

    override suspend fun revokeFile(logs: List<String>, type: Int): Result<StateResponse> {
        val map = mapOf("logs" to logs.toTypedArray(), "type" to type)
        return apiCall { service.revokeFile(map) }
    }

    override suspend fun forwardMessage(request: ForwardRequest): Result<StateResponse> {
        return apiCall { service.forwardMessage(request) }
    }

    override suspend fun forwardEncryptMessage(request: EncryptForwardRequest): Result<StateResponse> {
        return apiCall { service.forwardEncryptMessage(request) }
    }

    override suspend fun readSnapMessage(logId: String, type: Int): Result<Any> {
        val map = mapOf("logId" to logId, "type" to type)
        return apiCall { service.readSnapMessage(map) }
    }

    override suspend fun withdraw(request: WithdrawRequest): Result<WithdrawResponse> {
        return apiCall { service.withdraw(request) }
    }

    override suspend fun getChatLogHistory(channelType: Int, request: ChatLogHistoryRequest): Result<ChatListResponse> {
        val url = when (channelType) {
            Chat33Const.CHANNEL_GROUP -> ChatService.get_group_chat_log
            Chat33Const.CHANNEL_ROOM -> ChatService.get_room_chat_log
            Chat33Const.CHANNEL_FRIEND -> ChatService.get_private_chat_log
            else -> ""
        }
        val map = mapOf(
                "id" to request.id,
                "startId" to request.startId,
                "number" to request.number
        )
        return apiCall { service.getChatLogHistory(url, map) }
    }

    override suspend fun getChatFileHistory(channelType: Int, request: ChatFileHistoryRequest): Result<ChatListResponse> {
        val url = when (channelType) {
            Chat33Const.CHANNEL_ROOM -> ChatService.get_room_chat_file
            Chat33Const.CHANNEL_FRIEND -> ChatService.get_private_chat_file
            else -> ""
        }
        val map = mutableMapOf(
                "id" to request.id,
                "startId" to request.startId,
                "number" to request.number
        )
        if (request.owner != null) {
            map["owner"] = request.owner
        }
        if (request.query != null) {
            map["query"] = request.query
        }
        return apiCall { service.getChatLogHistory(url, map) }
    }

    override suspend fun getChatMediaHistory(channelType: Int, request: ChatFileHistoryRequest): Result<ChatListResponse> {
        val url = when (channelType) {
            Chat33Const.CHANNEL_ROOM -> ChatService.get_room_chat_media
            Chat33Const.CHANNEL_FRIEND -> ChatService.get_private_chat_media
            else -> ""
        }
        val map = mutableMapOf(
                "id" to request.id,
                "startId" to request.startId,
                "number" to request.number
        )
        if (request.owner != null) {
            map["owner"] = request.owner
        }
        if (request.query != null) {
            map["query"] = request.query
        }
        return apiCall { service.getChatLogHistory(url, map) }
    }

    override suspend fun setMutedSingle(roomId: String, userId: String, deadline: Long): Result<Any> {
        val map = mutableMapOf(
                "roomId" to roomId,
                "userId" to userId,
                "deadline" to deadline
        )
        return apiCall { service.setMutedSingle(map) }
    }

    override suspend fun receiveRedPacket(request: ReceiveRedPacketRequest): Result<ReceiveRedPacketResponse> {
        return apiCall { service.receiveRedPacket(request) }
    }

    override suspend fun hasRelationship(channelType: Int, id: String): Result<RelationshipBean> {
        var url = ""
        val map = mutableMapOf<String, Any>()
        if (channelType == Chat33Const.CHANNEL_ROOM) {
            url = ApiService.in_group
            map["roomId"] = id
        } else if (channelType == Chat33Const.CHANNEL_FRIEND) {
            url = ApiService.is_friend
            map["friendId"] = id
        }
        return apiCall { service.hasRelationship(url, map) }
    }

    override suspend fun praiseList(channelType: Int, targetId: String, startId: String?): Result<PraiseBean.Wrapper> {
        val map = mutableMapOf(
                "channelType" to channelType,
                "targetId" to targetId,
                "number" to AppConfig.PAGE_SIZE
        )
        if(!startId.isNullOrEmpty()) {
            map["startId"] = startId
        }
        return apiCall { service.praiseList(map) }
    }

    override suspend fun praiseDetails(channelType: Int, logId: String): Result<PraiseDetail> {
        val map = mapOf(
                "channelType" to channelType,
                "logId" to logId
        )
        return apiCall { service.praiseDetails(map) }
    }

    override suspend fun praiseDetailList(channelType: Int, startId: String?, logId: String): Result<PraiseBean.Wrapper> {
        val map = mutableMapOf(
                "channelType" to channelType,
                "logId" to logId,
                "number" to AppConfig.PAGE_SIZE
        )
        if(!startId.isNullOrEmpty()) {
            map["startId"] = startId
        }
        return apiCall { service.praiseDetailList(map) }
    }

    override suspend fun messageLike(channelType: Int, logId: String, action: String): Result<Any> {
        val map = mapOf(
                "channelType" to channelType,
                "logId" to logId,
                "action" to action
        )
        return apiCall { service.messageLike(map) }
    }

    override suspend fun praiseRanking(param: PraiseRankingParam): Result<PraiseRank.Wrapper> {
        return apiCall { service.praiseRanking(param) }
    }

    override suspend fun praiseRankingHistory(page: Int, size: Int): Result<PraiseRankHistory.Wrapper> {
        val map = mapOf("page" to page, "size" to size)
        return apiCall { service.praiseRankingHistory(map) }
    }
}