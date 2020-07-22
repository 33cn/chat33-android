package com.fzm.chat33.core.net.api

import com.fuzamei.common.net.rxjava.HttpResult
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
import com.fzm.chat33.core.response.StateResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

/**
 * @author zhengjy
 * @since 2019/09/16
 * Description:
 */
interface GroupService {

    /**
     * 获取群组列表
     *
     * @param map  type 群聊类型（是否常用）
     * 1：普通，2：常用  3：全部
     */
    @JvmSuppressWildcards
    @POST("chat/room/list")
    suspend fun getRoomList(@Body map: Map<String, Any>): HttpResult<RoomListBean.Wrapper>

    /**
     * 设置群置顶
     *
     * @param map
     */
    @JvmSuppressWildcards
    @POST("chat/room/stickyOnTop")
    suspend fun roomStickyOnTop(@Body map: Map<String, Any>): HttpResult<Any>

    /**
     * 设置群免打扰
     *
     * @param map
     */
    @JvmSuppressWildcards
    @POST("chat/room/setNoDisturbing")
    suspend fun roomNoDisturb(@Body map: Map<String, Any>): HttpResult<Any>

    @JvmSuppressWildcards
    @POST("chat/room/userIsInRoom")
    suspend fun isInRoom(@Body map: Map<String, Any>): HttpResult<RelationshipBean>

    /**
     * 获取群公告列表
     *
     * @param map  roomId   群id
     * startId  当前起始记录id
     * number   获取记录数
     */
    @JvmSuppressWildcards
    @POST("chat/room/systemMsgs")
    suspend fun getGroupNoticeList(@Body map: Map<String, Any>): HttpResult<GroupNotice.Wrapper>

    /**
     * 邀请入群
     *
     * @param param roomId    群id
     * users     受邀请人id数组
     */
    @POST("chat/room/joinRoomInvite")
    suspend fun inviteUsers(@Body param: EditRoomUserParam): HttpResult<StateResponse>

    /**
     * 踢出群
     *
     * @param param roomId    群id
     * users     被踢人id数组
     */
    @POST("chat/room/kickOut")
    suspend fun kickOutUsers(@Body param: EditRoomUserParam): HttpResult<Any>

    /**
     * 推荐群聊
     *
     * @param map times     推荐群聊批次
     */
    @JvmSuppressWildcards
    @POST("chat/room/recommend")
    suspend fun recommendGroups(@Body map: Map<String, Any>): HttpResult<RecommendGroup.Wrapper>

    /**
     * 批量申请入群
     *
     * @param map rooms     群id数组
     */
    @JvmSuppressWildcards
    @POST("chat/room/batchJoinRoomApply")
    suspend fun batchJoinRoomApply(@Body map: Map<String, Any>): HttpResult<ResultList>

    /**
     * 申请入群
     *
     * @param param roomId          群id
     * id              入群人id
     * applyReason     申请理由
     */
    @POST("chat/room/joinRoomApply")
    suspend fun joinRoomApply(@Body param: JoinGroupParam): HttpResult<Any>

    /**
     * 处理入群申请
     *
     * @param map id        申请人id
     * roomId        群id
     * agree         是否同意，1：同意  2：拒绝
     */
    @JvmSuppressWildcards
    @POST("chat/room/joinRoomApprove")
    suspend fun dealJoinRoomApply(@Body map: Map<String, Any>): HttpResult<Any>

    /**
     * 群内用户等级设置
     *
     * @param map roomId 群id
     * id 用户id
     * level  群内用户等级:1.普通用户;2.管理员;3.群主
     */
    @JvmSuppressWildcards
    @POST("chat/room/setLevel")
    suspend fun setRoomUserLevel(@Body map: Map<String, Any>): HttpResult<Any>

    /**
     * 管理员设置群
     *
     * @param map roomId            群id
     * canAddFriend      可否添加好友
     * joinPermission    进群权限设置,1：需要审批，2：不需要审批，3：禁止加群
     */
    @JvmSuppressWildcards
    @POST("chat/room/setPermission")
    suspend fun setPermission(@Body map: Map<String, Any>): HttpResult<Any>

    /**
     * 查看群信息
     *
     * @param map roomId 群id
     */
    @JvmSuppressWildcards
    @POST("chat/room/info")
    suspend fun getRoomInfo(@Body map: Map<String, Any>): HttpResult<RoomInfoBean>

    /**
     * 获取群成员列表
     *
     * @param map roomId 群id
     */
    @JvmSuppressWildcards
    @POST("chat/room/userList")
    suspend fun getRoomUsers(@Body map: Map<String, Any>): HttpResult<RoomUserBean.Wrapper>

    /**
     * 获取群成员列表
     *
     * @param map roomId 群id
     */
    @JvmSuppressWildcards
    @POST("chat/room/userList")
    fun getRoomUsersCall(@Body map: Map<String, Any>): Call<HttpResult<RoomUserBean.Wrapper>>

    /**
     * 获取群成员详情
     *
     * @param map roomId 群id
     * userId 用户id
     */
    @JvmSuppressWildcards
    @POST("chat/room/userInfo")
    suspend fun getRoomUserInfo(@Body map: Map<String, Any>): HttpResult<RoomUserBean>

    /**
     * 创建群
     *
     * @param param 群id
     */
    @POST("chat/room/create")
    suspend fun createRoom(@Body param: CreateGroupParam): HttpResult<RoomInfoBean>

    /**
     * 删除群
     *
     * @param map roomId 群id
     */
    @JvmSuppressWildcards
    @POST("chat/room/delete")
    suspend fun deleteRoom(@Body map: Map<String, Any>): HttpResult<Any>

    /**
     * 成员退出群
     *
     * @param map roomId 群id
     */
    @JvmSuppressWildcards
    @POST("chat/room/loginOut")
    suspend fun quitRoom(@Body map: Map<String, Any>): HttpResult<Any>

    /**
     * 群内昵称设置
     */
    @JvmSuppressWildcards
    @POST("chat/room/setMemberNickname")
    suspend fun setMemberNickname(@Body map: Map<String, Any>): HttpResult<Any>

    /**
     * 群内发布公告
     */
    @JvmSuppressWildcards
    @POST("chat/room/sendSystemMsgs")
    suspend fun publishNotice(@Body map: Map<String, Any>): HttpResult<Any>

    /**
     * 设置群内禁言名单
     * <tbody>
     * <tr>
     * <td>roomId</td>
     * <td>群id</td>
     * <td>string</td>
     * <td>必填</td>
     * <td></td>
    </tr> *
     * <tr>
     * <td>listType</td>
     * <td>列表类型</td>
     * <td>int</td>
     * <td>必填</td>
     * <td>1：全员发言 2：黑名单 3：白名单 4：全员禁言</td>
    </tr> *
     * <tr>
     * <td>users</td>
     * <td>成员id</td>
     * <td>string[]</td>
     * <td>非必填</td>
     * <td>listType 为 2或3时需要</td>
    </tr> *
     * <tr>
     * <td>deadline</td>
     * <td>解禁时间</td>
     * <td>datetime</td>
     * <td>非必填</td>
     * <td>黑名单时必填，永远为2200/1/1 00:00:00（7258089600000）</td>
    </tr> *
    </tbody> *
     */
    @JvmSuppressWildcards
    @POST("chat/room/setMutedList")
    suspend fun setMutedList(@Body map: Map<String, Any>): HttpResult<Any>

    /**
     * 单个成员禁言设置
     *
     * @param map
     */
    @JvmSuppressWildcards
    @POST("chat/room/setMutedSingle")
    suspend fun setMutedSingle(@Body map: Map<String, Any>): HttpResult<Any>
}