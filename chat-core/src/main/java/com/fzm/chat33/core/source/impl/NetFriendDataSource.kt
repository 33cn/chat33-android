package com.fzm.chat33.core.source.impl

import com.fuzamei.common.net.Result
import com.fuzamei.common.net.rxjava.HttpResult
import com.fuzamei.componentservice.ext.apiCall
import com.fzm.chat33.core.bean.ApplyInfoBean
import com.fzm.chat33.core.bean.RelationshipBean
import com.fzm.chat33.core.bean.UidSearchBean
import com.fzm.chat33.core.bean.param.AddFriendParam
import com.fzm.chat33.core.bean.param.EditExtRemarkParam
import com.fzm.chat33.core.db.bean.FriendBean
import com.fzm.chat33.core.net.api.FriendService
import com.fzm.chat33.core.response.BoolResponse
import com.fzm.chat33.core.response.StateResponse
import com.fzm.chat33.core.source.FriendDataSource
import retrofit2.Call
import java.util.*
import javax.inject.Inject
import kotlin.collections.HashMap
import kotlin.collections.mapOf
import kotlin.collections.set

/**
 * @author zhengjy
 * @since 2019/10/09
 * Description:
 */
class NetFriendDataSource @Inject constructor(
        private val service: FriendService
) : FriendDataSource {

    override suspend fun searchByUid(markId: String): Result<UidSearchBean> {
        val map = mapOf("markId" to markId)
        return apiCall { service.searchByUid(map) }
    }

    override suspend fun getUserByAddress(address: String): Result<FriendBean> {
        val map = mapOf("uid" to address)
        return apiCall { service.getUserByAddress(map) }
    }

    override suspend fun getUsersByAddress(address: List<String>): Result<FriendBean.Wrapper> {
        val map = mapOf("uids" to address)
        return apiCall { service.getUsersByAddress(map) }
    }

    override suspend fun friendStickyOnTop(id: String, stickyOnTop: Int): Result<Any> {
        val map = mapOf("id" to id, "stickyOnTop" to stickyOnTop)
        return apiCall { service.friendStickyOnTop(map) }
    }

    override suspend fun friendNoDisturb(id: String, setNoDisturbing: Int): Result<Any> {
        val map = mapOf("id" to id, "setNoDisturbing" to setNoDisturbing)
        return apiCall { service.friendNoDisturb(map) }
    }

    override suspend fun isFriend(friendId: String): Result<RelationshipBean> {
        val map = mapOf("friendId" to friendId)
        return apiCall { service.isFriend(map) }
    }

    override suspend fun blockUser(userId: String): Result<Any> {
        val map = mapOf("userId" to userId)
        return apiCall { service.blockUser(map) }
    }

    override suspend fun unblockUser(userId: String): Result<Any> {
        val map = mapOf("userId" to userId)
        return apiCall { service.unblockUser(map) }
    }

    override suspend fun getBlackList(): Result<FriendBean.Wrapper> {
        return apiCall { service.getBlackList() }
    }
    
    override suspend fun getFriendList(type: Int, date: Date?, number: Int): Result<FriendBean.Wrapper> {
        val map = HashMap<String, Any>()
        map["type"] = type
        if (date != null) {
            map["time"] = date.time
        }
        if (number != -1) {
            map["number"] = number
        }
        return apiCall { service.getFriendList(map) }
    }

    override suspend fun addFriend(param: AddFriendParam): Result<StateResponse> {
        return apiCall { service.addFriend(param) }
    }

    override suspend fun getFriendsApplyList(id: String?, number: Int): Result<ApplyInfoBean.Wrapper> {
        val map = mutableMapOf<String, Any>("number" to number)
        if (id != null) {
            map["id"] = id
        }
        return apiCall { service.getFriendsApplyList(map) }
    }

    override suspend fun dealFriendRequest(id: String, agree: Int): Result<Any> {
        val map = mapOf("id" to id, "agree" to agree)
        return apiCall { service.dealFriendRequest(map) }
    }

    override suspend fun deleteFriend(id: String): Result<Any> {
        val map = mapOf("id" to id)
        return apiCall { service.deleteFriend(map) }
    }

    override suspend fun getUserInfo(id: String): Result<FriendBean> {
        val map = mapOf("id" to id)
        return apiCall { service.getUserInfo(map) }
    }

    override fun getUserInfoSync(id: String): Call<HttpResult<FriendBean>> {
        val map = mapOf("id" to id)
        return service.getUserInfoSync(map)
    }

    override suspend fun setFriendRemark(id: String, remark: String): Result<Any> {
        val map = mapOf("id" to id, "remark" to remark)
        return apiCall { service.setFriendRemark(map) }
    }

    override suspend fun setFriendExtRemark(param: EditExtRemarkParam): Result<Any> {
        val map = mapOf(
                "id" to param.id,
                "remark" to param.remark,
                "telephones" to param.telephones,
                "description" to param.description,
                "pictures" to param.pictures,
                "encrypt" to param.encrypt
        )
        return apiCall { service.setFriendExtRemark(map) }
    }

    override suspend fun checkAnswer(friendId: String, answer: String): Result<BoolResponse> {
        val map = mapOf(
                "friendId" to friendId,
                "answer" to answer
        )
        return apiCall { service.checkAnswer(map) }
    }
}