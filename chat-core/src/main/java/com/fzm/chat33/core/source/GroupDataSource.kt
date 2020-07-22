package com.fzm.chat33.core.source

import com.fuzamei.common.net.Result
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

/**
 * @author zhengjy
 * @since 2019/09/16
 * Description:
 */
interface GroupDataSource {

    /**
     * 获取群组列表
     *
     * @param type 群聊类型（是否常用）
     * 1：普通，2：常用  3：全部
     */
    suspend fun getRoomList(type: Int): Result<RoomListBean.Wrapper>

    /**
     * 设置群置顶
     */
    suspend fun roomStickyOnTop(roomId: String, stickyOnTop: Int): Result<Any>

    /**
     * 设置群免打扰
     */
    suspend fun roomNoDisturb(roomId: String, setNoDisturbing: Int): Result<Any>

    suspend fun isInRoom(roomId: String): Result<RelationshipBean>

    /**
     * 获取群公告列表
     *
     * @param roomId    群id
     * @param startId   当前起始记录id
     * @param number    获取记录数
     */
    suspend fun getGroupNoticeList(roomId: String, startId: String?, number: Int): Result<GroupNotice.Wrapper>

    /**
     * 邀请入群
     *
     * @param param roomId    群id
     * users     受邀请人id数组
     */
    suspend fun inviteUsers(param: EditRoomUserParam): Result<StateResponse>

    /**
     * 踢出群
     *
     * @param param roomId    群id
     * users     被踢人id数组
     */
    suspend fun kickOutUsers(param: EditRoomUserParam): Result<Any>

    /**
     * 推荐群聊
     *
     * @param times     推荐群聊批次
     */
    suspend fun recommendGroups(times: Int): Result<RecommendGroup.Wrapper>

    /**
     * 批量申请入群
     *
     * @param rooms     群id数组
     */
    suspend fun batchJoinRoomApply(rooms: List<String>): Result<ResultList>

    /**
     * 申请入群
     *
     * @param param roomId          群id
     * id              入群人id
     * applyReason     申请理由
     */
    suspend fun joinRoomApply(param: JoinGroupParam): Result<Any>

    /**
     * 处理入群申请
     *
     * @param roomId    群id
     * @param userId    申请人id
     * @param agree     是否同意，1：同意  2：拒绝
     */
    suspend fun dealJoinRoomApply(roomId: String, userId: String, agree: Int): Result<Any>

    /**
     * 群内用户等级设置
     *
     * @param roomId    群id
     * @param userId    用户id
     * @param level     群内用户等级:1.普通用户;2.管理员;3.群主
     */
    suspend fun setRoomUserLevel(roomId: String, userId: String, level: Int): Result<Any>

    /**
     * 管理员设置群
     *
     * @param roomId            群id
     * @param canAddFriend      可否添加好友
     * @param joinPermission    进群权限设置,1：需要审批，2：不需要审批，3：禁止加群
     * @param recordPermission  是否新成员查看历史记录
     */
    suspend fun setPermission(
            roomId: String,
            canAddFriend: Int,
            joinPermission: Int,
            recordPermission: Int
    ): Result<Any>

    /**
     * 查看群信息
     *
     * @param roomId 群id
     */
    suspend fun getRoomInfo(roomId: String): Result<RoomInfoBean>

    /**
     * 获取群成员列表
     *
     * @param roomId 群id
     */
    suspend fun getRoomUsers(roomId: String): Result<RoomUserBean.Wrapper>

    /**
     * 获取群成员列表
     *
     * @param roomId 群id
     */
    fun getRoomUsersCall(roomId: String): Call<HttpResult<RoomUserBean.Wrapper>>

    /**
     * 获取群成员详情
     *
     * @param roomId 群id
     * @param userId 用户id
     */
    suspend fun getRoomUserInfo(roomId: String, userId: String): Result<RoomUserBean>

    /**
     * 创建群
     *
     * @param param 群id
     */
    suspend fun createRoom(param: CreateGroupParam): Result<RoomInfoBean>

    /**
     * 删除群
     *
     * @param roomId 群id
     */
    suspend fun deleteRoom(roomId: String): Result<Any>

    /**
     * 成员退出群
     *
     * @param roomId 群id
     */
    suspend fun quitRoom(roomId: String): Result<Any>

    /**
     * 群内昵称设置
     *
     * @param roomId    群id
     * @param nickname  群昵称
     */
    suspend fun setMemberNickname(roomId: String, nickname: String): Result<Any>

    /**
     * 群内发布公告
     *
     * @param roomId    群id
     * @param content   公告内容
     */
    suspend fun publishNotice(roomId: String, content: String): Result<Any>

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
    suspend fun setMutedList(
            roomId: String,
            listType: Int,
            users: List<String>?,
            deadline: Long
    ): Result<Any>

    /**
     * 单个成员禁言设置
     */
    suspend fun setMutedSingle(roomId: String, userId: String, deadline: Long): Result<Any>
}