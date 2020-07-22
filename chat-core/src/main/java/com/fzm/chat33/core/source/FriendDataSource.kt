package com.fzm.chat33.core.source

import com.fuzamei.common.net.Result
import com.fuzamei.common.net.rxjava.HttpResult
import com.fzm.chat33.core.bean.ApplyInfoBean
import com.fzm.chat33.core.bean.RelationshipBean
import com.fzm.chat33.core.bean.UidSearchBean
import com.fzm.chat33.core.bean.param.AddFriendParam
import com.fzm.chat33.core.bean.param.EditExtRemarkParam
import com.fzm.chat33.core.db.bean.FriendBean
import com.fzm.chat33.core.response.BoolResponse
import com.fzm.chat33.core.response.StateResponse
import retrofit2.Call
import java.util.Date

/**
 * @author zhengjy
 * @since 2019/09/16
 * Description:
 */
interface FriendDataSource {

    /**
     * 精确搜索用户或群组
     */
    suspend fun searchByUid(markId: String): Result<UidSearchBean>

    /**
     * 通过地址查询用户信息
     */
    suspend fun getUserByAddress(address: String): Result<FriendBean>

    /**
     * 通过地址批量查询用户信息
     */
    suspend fun getUsersByAddress(address: List<String>): Result<FriendBean.Wrapper>

    /**
     * 设置好友置顶
     */
    suspend fun friendStickyOnTop(id: String, stickyOnTop: Int): Result<Any>

    /**
     * 设置好友免打扰
     */
    suspend fun friendNoDisturb(id: String, setNoDisturbing: Int): Result<Any>

    suspend fun isFriend(friendId: String): Result<RelationshipBean>

    /**
     * 加入黑名单列表
     *
     * @param userId  用户id
     */
    @Deprecated("使用合约接口")
    suspend fun blockUser(userId: String): Result<Any>

    /**
     * 移出黑名单列表
     *
     * @param userId  用户id
     */
    @Deprecated("使用合约接口")
    suspend fun unblockUser(userId: String): Result<Any>

    /**
     * 获取黑名单列表
     */
    suspend fun getBlackList(): Result<FriendBean.Wrapper>

    /**
     * 获取好友列表
     *
     * @param type  好友类型（是否常用）
     * 1：普通，2：常用  3：全部
     */
    suspend fun getFriendList(type: Int, date: Date?, number: Int): Result<FriendBean.Wrapper>

    /**
     * 添加好友申请
     *
     * @param param
     */
    @Deprecated("use ContractDataSource to add friends")
    suspend fun addFriend(param: AddFriendParam): Result<StateResponse>

    /**
     * 好友申请列表
     *
     * @param id        最早一条的id
     * @param number    数量
     */
    suspend fun getFriendsApplyList(id: String?, number: Int): Result<ApplyInfoBean.Wrapper>

    /**
     * 好友申请处理
     *
     * @param id       对方id
     * @param agree    是否同意 1：同意 2：拒绝
     */
    suspend fun dealFriendRequest(id: String, agree: Int): Result<Any>

    /**
     * 删除好友
     *
     * @param id 对方id
     */
    @Deprecated("use ContractDataSource to delete friends")
    suspend fun deleteFriend(id: String): Result<Any>

    /**
     * 查看好友详情
     *
     * @param id 对方id
     */
    suspend fun getUserInfo(id: String): Result<FriendBean>

    /**
     * 查看好友详情(同步请求)
     *
     * @param id 对方id
     */
    fun getUserInfoSync(id: String): Call<HttpResult<FriendBean>>

    /**
     * 修改好友备注
     *
     * @param id        对方id
     * @param remark  备注
     */
    suspend fun setFriendRemark(id: String, remark: String): Result<Any>

    /**
     * 修改好友详细备注
     *
     * @param param     请求参数
     */
    suspend fun setFriendExtRemark(param: EditExtRemarkParam): Result<Any>

    /**
     * 验证问题是否回答正确
     *
     * @param friendId     对方id
     * @param answer  答案
     */
    suspend fun checkAnswer(friendId: String, answer: String): Result<BoolResponse>
}