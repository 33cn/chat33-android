package com.fzm.chat33.core.net.api

import com.fuzamei.common.net.rxjava.HttpResult
import com.fuzamei.componentservice.config.AppConfig
import com.fzm.chat33.core.bean.ApplyInfoBean
import com.fzm.chat33.core.bean.RelationshipBean
import com.fzm.chat33.core.bean.UidSearchBean
import com.fzm.chat33.core.bean.param.AddFriendParam
import com.fzm.chat33.core.db.bean.FriendBean
import com.fzm.chat33.core.response.BoolResponse
import com.fzm.chat33.core.response.StateResponse
import io.reactivex.Observable
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Url

/**
 * @author zhengjy
 * @since 2019/09/16
 * Description:
 */
interface FriendService {

    /**
     * 精确搜索用户或群组
     */
    @JvmSuppressWildcards
    @POST("chat/chat33/search")
    suspend fun searchByUid(@Body map: Map<String, Any>): HttpResult<UidSearchBean>

    /**
     * 通过地址查询用户信息
     */
    @JvmSuppressWildcards
    @POST("chat/user/userInfoByUid")
    suspend fun getUserByAddress(@Body map: Map<String, Any>): HttpResult<FriendBean>

    /**
     * 通过地址批量查询用户信息
     */
    @JvmSuppressWildcards
    @POST("chat/user/usersInfo")
    suspend fun getUsersByAddress(@Body map: Map<String, Any>): HttpResult<FriendBean.Wrapper>

    /**
     * 设置好友置顶
     *
     * @param map
     */
    @JvmSuppressWildcards
    @POST("chat/friend/stickyOnTop")
    suspend fun friendStickyOnTop(@Body map: Map<String, Any>): HttpResult<Any>

    /**
     * 设置好友免打扰
     *
     * @param map
     */
    @JvmSuppressWildcards
    @POST("chat/friend/setNoDisturbing")
    suspend fun friendNoDisturb(@Body map: Map<String, Any>): HttpResult<Any>

    @JvmSuppressWildcards
    @POST("chat/friend/isFriend")
    suspend fun isFriend(@Body map: Map<String, Any>): HttpResult<RelationshipBean>

    /**
     * 加入黑名单列表
     *
     * @param map userId  用户id
     */
    @JvmSuppressWildcards
    @POST("chat/friend/block")
    suspend fun blockUser(@Body map: Map<String, Any>): HttpResult<Any>

    /**
     * 移出黑名单列表
     *
     * @param map userId  用户id
     */
    @JvmSuppressWildcards
    @POST("chat/friend/unblock")
    suspend fun unblockUser(@Body map: Map<String, Any>): HttpResult<Any>

    /**
     * 获取黑名单列表
     */
    @POST("chat/friend/blocked-list")
    suspend fun getBlackList(): HttpResult<FriendBean.Wrapper>

    /**
     * 获取好友列表
     *
     * @param map type  好友类型（是否常用）
     * 1：普通，2：常用  3：全部
     */
    @JvmSuppressWildcards
    @POST("chat/friend/list")
    suspend fun getFriendList(@Body map: Map<String, Any>): HttpResult<FriendBean.Wrapper>

    /**
     * 添加好友申请
     *
     * @param param
     */
    @POST("chat/friend/add")
    suspend fun addFriend(@Body param: AddFriendParam): HttpResult<StateResponse>

    /**
     * 好友申请列表
     *
     * @param map datetime  最早一条时间
     * number    数量
     */
    @JvmSuppressWildcards
    @POST("chat/chat33/applyList")
    suspend fun getFriendsApplyList(@Body map: Map<String, Any>): HttpResult<ApplyInfoBean.Wrapper>

    /**
     * 好友申请处理
     *
     * @param map id       对方id
     * agree    是否同意 1：同意 2：拒绝
     */
    @JvmSuppressWildcards
    @POST("chat/friend/response")
    suspend fun dealFriendRequest(@Body map: Map<String, Any>): HttpResult<Any>

    /**
     * 删除好友
     *
     * @param map id 对方id
     */
    @JvmSuppressWildcards
    @POST("chat/friend/delete")
    suspend fun deleteFriend(@Body map: Map<String, Any>): HttpResult<Any>

    /**
     * 查看好友详情
     *
     * @param map id 对方id
     */
    @JvmSuppressWildcards
    @POST("chat/user/userInfo")
    suspend fun getUserInfo(@Body map: Map<String, Any>): HttpResult<FriendBean>

    /**
     * 查看好友详情(同步请求)
     *
     * @param map id 对方id
     */
    @JvmSuppressWildcards
    @POST("chat/user/userInfo")
    fun getUserInfoSync(@Body map: Map<String, Any>): Call<HttpResult<FriendBean>>

    /**
     * 修改好友备注
     *
     * @param map id        对方id
     *            remark    备注
     */
    @JvmSuppressWildcards
    @POST("chat/friend/setRemark")
    suspend fun setFriendRemark(@Body map: Map<String, Any>): HttpResult<Any>

    /**
     * 修改好友详细备注
     *
     * @param map id            对方id
     *            remark        备注
     *            telephones    电话
     *            description   描述
     *            pictures      图片
     */
    @JvmSuppressWildcards
    @POST("chat/friend/setExtRemark")
    suspend fun setFriendExtRemark(@Body map: Map<String, Any>): HttpResult<Any>

    /**
     * 验证问题是否回答正确
     *
     * @param map
     */
    @JvmSuppressWildcards
    @POST("chat/friend/checkAnswer")
    suspend fun checkAnswer(@Body map: Map<String, Any>): HttpResult<BoolResponse>

    /**
     * 设置好友或群免打扰
     *
     * @param url
     * @param map
     */
    @JvmSuppressWildcards
    @POST
    suspend fun setDND(@Url url: String, @Body map: Map<String, Any>): HttpResult<Any>
}