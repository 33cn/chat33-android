package com.fzm.chat33.core.source.impl

import com.fuzamei.common.net.Result
import com.fuzamei.common.net.rxjava.HttpResult
import com.fuzamei.componentservice.ext.apiCall
import com.fzm.chat33.core.bean.GroupNotice
import com.fzm.chat33.core.bean.RecommendGroup
import com.fzm.chat33.core.bean.RelationshipBean
import com.fzm.chat33.core.bean.ResultList
import com.fzm.chat33.core.bean.param.CreateGroupParam
import com.fzm.chat33.core.bean.param.EditRoomUserParam
import com.fzm.chat33.core.bean.param.JoinGroupParam
import com.fzm.chat33.core.db.bean.RoomInfoBean
import com.fzm.chat33.core.db.bean.RoomListBean
import com.fzm.chat33.core.db.bean.RoomUserBean
import com.fzm.chat33.core.net.api.GroupService
import com.fzm.chat33.core.response.StateResponse
import com.fzm.chat33.core.source.GroupDataSource
import retrofit2.Call
import javax.inject.Inject

/**
 * @author zhengjy
 * @since 2019/10/10
 * Description:
 */
class NetGroupDataSource @Inject constructor(
        private val service: GroupService
) : GroupDataSource {

    override suspend fun getRoomList(type: Int): Result<RoomListBean.Wrapper> {
        val map = mapOf("type" to type)
        return apiCall { service.getRoomList(map) }
    }

    override suspend fun roomStickyOnTop(roomId: String, stickyOnTop: Int): Result<Any> {
        val map = mapOf("roomId" to roomId, "stickyOnTop" to stickyOnTop)
        return apiCall { service.roomStickyOnTop(map) }
    }

    override suspend fun roomNoDisturb(roomId: String, setNoDisturbing: Int): Result<Any> {
        val map = mapOf("roomId" to roomId, "setNoDisturbing" to setNoDisturbing)
        return apiCall { service.roomNoDisturb(map) }
    }

    override suspend fun isInRoom(roomId: String): Result<RelationshipBean> {
        val map = mapOf("roomId" to roomId)
        return apiCall { service.isInRoom(map) }
    }

    override suspend fun getGroupNoticeList(roomId: String, startId: String?, number: Int): Result<GroupNotice.Wrapper> {
        val map = mutableMapOf("roomId" to roomId, "number" to number)
        if (startId != null) {
            map["startId"] = startId
        }
        return apiCall { service.getGroupNoticeList(map) }
    }

    override suspend fun inviteUsers(param: EditRoomUserParam): Result<StateResponse> {
        return apiCall { service.inviteUsers(param) }
    }

    override suspend fun kickOutUsers(param: EditRoomUserParam): Result<Any> {
        return apiCall { service.kickOutUsers(param) }
    }

    override suspend fun recommendGroups(times: Int): Result<RecommendGroup.Wrapper> {
        val map = mapOf("times" to times)
        return apiCall { service.recommendGroups(map) }
    }

    override suspend fun batchJoinRoomApply(rooms: List<String>): Result<ResultList> {
        val map = mapOf("rooms" to rooms.toTypedArray())
        return apiCall { service.batchJoinRoomApply(map) }
    }

    override suspend fun joinRoomApply(param: JoinGroupParam): Result<Any> {
        return apiCall { service.joinRoomApply(param) }
    }

    override suspend fun dealJoinRoomApply(roomId: String, userId: String, agree: Int): Result<Any> {
        val map = mapOf("roomId" to roomId, "userId" to userId, "agree" to agree)
        return apiCall { service.dealJoinRoomApply(map) }
    }

    override suspend fun setRoomUserLevel(roomId: String, userId: String, level: Int): Result<Any> {
        val map = mapOf("roomId" to roomId, "userId" to userId, "level" to level)
        return apiCall { service.setRoomUserLevel(map) }
    }

    override suspend fun setPermission(roomId: String,
                                       canAddFriend: Int,
                                       joinPermission: Int,
                                       recordPermission: Int
    ): Result<Any> {
        val map = mutableMapOf<String, Any>()
        map["roomId"] = roomId
        if (canAddFriend != 0) {
            map["canAddFriend"] = canAddFriend
        }
        if (joinPermission != 0) {
            map["joinPermission"] = joinPermission
        }
        if (recordPermission != 0) {
            map["recordPermission"] = recordPermission
        }
        return apiCall { service.setPermission(map) }
    }

    override suspend fun getRoomInfo(roomId: String): Result<RoomInfoBean> {
        val map = mapOf("roomId" to roomId)
        return apiCall { service.getRoomInfo(map) }
    }

    override suspend fun getRoomUsers(roomId: String): Result<RoomUserBean.Wrapper> {
        val map = mapOf("roomId" to roomId)
        return apiCall { service.getRoomUsers(map) }
    }

    override fun getRoomUsersCall(roomId: String): Call<HttpResult<RoomUserBean.Wrapper>> {
        val map = mapOf("roomId" to roomId)
        return service.getRoomUsersCall(map)
    }

    override suspend fun getRoomUserInfo(roomId: String, userId: String): Result<RoomUserBean> {
        val map = mapOf("roomId" to roomId, "userId" to userId)
        return apiCall { service.getRoomUserInfo(map) }
    }

    override suspend fun createRoom(param: CreateGroupParam): Result<RoomInfoBean> {
        return apiCall { service.createRoom(param) }
    }

    override suspend fun deleteRoom(roomId: String): Result<Any> {
        val map = mapOf("roomId" to roomId)
        return apiCall { service.deleteRoom(map) }
    }

    override suspend fun quitRoom(roomId: String): Result<Any> {
        val map = mapOf("roomId" to roomId)
        return apiCall { service.quitRoom(map) }
    }

    override suspend fun setMemberNickname(roomId: String, nickname: String): Result<Any> {
        val map = mapOf("roomId" to roomId, "nickname" to nickname)
        return apiCall { service.setMemberNickname(map) }
    }

    override suspend fun publishNotice(roomId: String, content: String): Result<Any> {
        val map = mapOf("roomId" to roomId, "content" to content)
        return apiCall { service.publishNotice(map) }
    }

    override suspend fun setMutedList(roomId: String, listType: Int, users: List<String>?, deadline: Long): Result<Any> {
        val map : MutableMap<String, Any> = mutableMapOf("roomId" to roomId, "listType" to listType)
        if (users != null) {
            map["users"] = users.toTypedArray()
        }
        if (deadline != -1L) {
            map["deadline"] = deadline
        }
        return apiCall { service.setMutedList(map) }
    }

    override suspend fun setMutedSingle(roomId: String, userId: String, deadline: Long): Result<Any> {
        val map = mapOf("roomId" to roomId, "userId" to userId, "deadline" to deadline)
        return apiCall { service.setMutedSingle(map) }
    }

}