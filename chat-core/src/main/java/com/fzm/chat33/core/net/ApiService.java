package com.fzm.chat33.core.net;

import com.fuzamei.common.net.rxjava.HttpResponse;
import com.fuzamei.common.net.rxjava.HttpResult;
import com.fuzamei.componentservice.config.AppConfig;
import com.fzm.chat33.core.bean.AdInfoBean;
import com.fzm.chat33.core.bean.ApplyInfoBean;
import com.fzm.chat33.core.bean.ChatGroupBean;
import com.fzm.chat33.core.bean.ConditionReward;
import com.fzm.chat33.core.bean.DepositSMS;
import com.fzm.chat33.core.bean.GroupNotice;
import com.fzm.chat33.core.bean.ModuleState;
import com.fzm.chat33.core.bean.PromoteBriefInfo;
import com.fzm.chat33.core.bean.PromoteReward;
import com.fzm.chat33.core.bean.RecommendGroup;
import com.fzm.chat33.core.bean.RedPacketCoin;
import com.fzm.chat33.core.bean.RedPacketReceiveInfo;
import com.fzm.chat33.core.bean.RedPacketRecord;
import com.fzm.chat33.core.bean.ResultList;
import com.fzm.chat33.core.bean.RoomSessionKeys;
import com.fzm.chat33.core.bean.SettingInfoBean;
import com.fzm.chat33.core.bean.param.AddFriendParam;
import com.fzm.chat33.core.bean.param.AddQuestionParam;
import com.fzm.chat33.core.bean.param.ChatGroupParam;
import com.fzm.chat33.core.bean.param.CreateGroupParam;
import com.fzm.chat33.core.bean.param.EditRoomUserParam;
import com.fzm.chat33.core.bean.param.JoinGroupParam;
import com.fzm.chat33.core.request.WithdrawRequest;
import com.fzm.chat33.core.response.SendRedPacketResponse;
import com.fzm.chat33.core.db.bean.RoomInfoBean;
import com.fzm.chat33.core.db.bean.RoomUserBean;
import com.fzm.chat33.core.request.PayPasswordRequest;
import com.fzm.chat33.core.request.ReceiveRedPacketRequest;
import com.fzm.chat33.core.request.RedPacketRecordRequest;
import com.fzm.chat33.core.request.SendRedPacketRequest;
import com.fzm.chat33.core.request.chat.ForwardRequest;
import com.fzm.chat33.core.bean.RelationshipBean;
import com.fzm.chat33.core.bean.UidSearchBean;
import com.fzm.chat33.core.bean.UnreadNumber;
import com.fzm.chat33.core.global.UserInfo;
import com.fzm.chat33.core.db.bean.FriendBean;
import com.fzm.chat33.core.db.bean.RoomListBean;
import com.fzm.chat33.core.response.BoolResponse;
import com.fzm.chat33.core.response.ReceiveRedPacketResponse;
import com.fzm.chat33.core.response.RedPacketInfoResponse;
import com.fzm.chat33.core.response.StateResponse;
import com.fzm.chat33.core.response.ChatListResponse;
import com.fzm.chat33.core.response.WithdrawResponse;

import java.util.Map;

import io.reactivex.Observable;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.Url;

import static com.fuzamei.common.retrofiturlmanager.RetrofitUrlManager.DOMAIN_NAME_HEADER;

/**
 * @author zhengjy
 * @since 2018/10/17
 * Description:
 */
public interface ApiService {

    String friend_sticky_top = AppConfig.CHAT_BASE_URL + "chat/friend/stickyOnTop";
    String room_sticky_top = AppConfig.CHAT_BASE_URL + "chat/room/stickyOnTop";

    String friend_dnd = AppConfig.CHAT_BASE_URL + "chat/friend/setNoDisturbing";
    String room_dnd = AppConfig.CHAT_BASE_URL + "chat/room/setNoDisturbing";

    String group_avatar = AppConfig.CHAT_BASE_URL + "chat/group/editAvatar";
    String room_avatar = AppConfig.CHAT_BASE_URL + "chat/room/setAvatar";
    String my_avatar = AppConfig.CHAT_BASE_URL + "chat/user/editAvatar";

    String group_name = AppConfig.CHAT_BASE_URL + "chat/group/editGroupName";
    String room_name = AppConfig.CHAT_BASE_URL + "chat/room/setName";
    String my_name = AppConfig.CHAT_BASE_URL + "chat/user/editNickname";

    String is_friend = AppConfig.CHAT_BASE_URL + "chat/friend/isFriend";
    String in_group = AppConfig.CHAT_BASE_URL + "chat/room/userIsInRoom";

    /**
     * 消息记录接口地址
      */
    String getGroupChatLog = AppConfig.CHAT_BASE_URL + "chat/group/getGroupChatHistory";
    String getRoomChatLog = AppConfig.CHAT_BASE_URL + "chat/room/chatLog";
    String getPrivateChatLog = AppConfig.CHAT_BASE_URL + "chat/friend/chatLog";

    /**
     * 文件记录接口地址
     */
    String getRoomChatFile = AppConfig.CHAT_BASE_URL + "chat/room/historyFiles";
    String getRoomChatMedia = AppConfig.CHAT_BASE_URL + "chat/room/historyPhotos";
    String getPrivateChatFile = AppConfig.CHAT_BASE_URL + "chat/friend/historyFiles";
    String getPrivateChatMedia = AppConfig.CHAT_BASE_URL + "chat/friend/historyPhotos";

    /*------------------------------------------------用户相关------------------------------------------------*/

    /**
     * 上传推送的设备标识符
     */
    @POST("chat/user/set-device-token")
    Observable<HttpResult<Object>> setDeviceToken(@Body Map<String, Object> map);

    /**
     * token登录
     */
    @POST("chat/user/tokenLogin")
    Observable<HttpResult<UserInfo>> login();

    /**
     * token登录
     */
    @POST("chat/user/tokenLogin")
    Observable<HttpResult<UserInfo>> login(@Body Map<String, Object> map);

    /**
     * 退出登录
     */
    @POST("chat/user/logout")
    Observable<HttpResult<Object>> logout();

    /**
     * 获取用户设置相关信息
     *
     */
    @POST("chat/user/userConf")
    Observable<HttpResult<SettingInfoBean>> getSettingInfo();

    /**
     * 获取用户推广概况信息
     *
     */
    @POST("chat/user/invite-statistics")
    Observable<HttpResult<PromoteBriefInfo>> getPromoteBriefInfo();

    /**
     * 获取用户邀请推广奖励记录
     *
     */
    @POST("chat/user/single-invite-info")
    Observable<HttpResult<PromoteReward.Wrapper>> getPromoteRewardList(@Body Map<String, Object> map);

    /**
     * 获取用户条件推广奖励记录
     *
     */
    @POST("chat/user/accumulate-invite-info")
    Observable<HttpResult<ConditionReward.Wrapper>> getConditionRewardList(@Body Map<String, Object> map);

    /**
     * 用户修改昵称
     */
    @POST
    Observable<HttpResult<Object>> editName(@Url String url, @Body Map<String, Object> map);

    /**
     * 用户编辑头像
     */
    @POST
    Observable<HttpResult<Object>> editAvatar(@Url String url, @Body Map<String, Object> map);

    /**
     * 精确搜索用户或群组
     */
    @POST("chat/chat33/search")
    Observable<HttpResult<UidSearchBean>> searchByUid(@Body Map<String, Object> map);

    /**
     * 撤回指定的一条消息
     *
     * @param map  logId    消息id
     *             type     消息类型:1：群消息；2：好友消息
     */
    @POST("chat/chat33/RevokeMessage")
    Observable<HttpResult<Object>> revokeMessage(@Body Map<String, Object> map);

    /**
     * 撤回指定文件
     *
     * @param map  logs    消息id
     *             type    消息类型:1：群文件；2：好友文件
     */
    @POST("chat/chat33/RevokeFiles")
    Observable<HttpResult<StateResponse>> revokeFile(@Body Map<String, Object> map);

    /**
     * 转发消息
     *
     * @param request  转发请求参数
     */
    @POST("chat/chat33/forward")
    Observable<HttpResult<StateResponse>> forwardMessage(@Body ForwardRequest request);

    @POST()
    Observable<HttpResult<RelationshipBean>> hasRelationship(@Url String url, @Body Map<String, Object> map);

    /**
     * 拉取服务端聊天记录或者文件记录
     *
     * @param url   请求地址
     * @param map   请求参数
     */
    @POST()
    Observable<HttpResult<ChatListResponse>> getChatLogHistory(@Url String url, @Body Map<String, Object> map);

    @POST("chat/chat33/readSnapMsg")
    Observable<HttpResult<Object>> readSnapMessage(@Body Map<String, Object> map);

    /**
     * 获取指定时间之前的所有群会话密钥
     *
     * @param map
     */
    @POST("chat/chat33/roomSessionKey")
    Observable<HttpResult<RoomSessionKeys>> getRoomSessionKeys(@Body Map<String, Object> map);

    /*------------------------------------------------设置相关------------------------------------------------*/

    /**
     * 设置好友或群置顶
     *
     * @param map
     */
    @POST
    Observable<HttpResult<Object>> stickyOnTop(@Url String url, @Body Map<String, Object> map);

    /**
     * 设置好友或群免打扰
     *
     * @param map
     */
    @POST
    Observable<HttpResult<Object>> setDND(@Url String url, @Body Map<String, Object> map);

    /**
     * 设置添加好友是否需要验证
     *
     * @param map
     */
    @POST("chat/friend/confirm")
    Observable<HttpResult<Object>> setAddVerify(@Body Map<String, Object> map);

    /**
     * 设置添加好友是否需要回答问题
     *
     * @param param
     */
    @POST("chat/friend/question")
    Observable<HttpResult<Object>> setAddQuestion(@Body AddQuestionParam param);

    /**
     * 设置被邀请入群需要确认
     *
     * @param map
     */
    @POST("chat/user/set-invite-confirm")
    Observable<HttpResult<Object>> setInviteConfirm(@Body Map<String, Object> map);

    /**
     * 验证问题是否回答正确
     *
     * @param map
     */
    @POST("chat/friend/checkAnswer")
    Observable<HttpResult<BoolResponse>> checkAnswer(@Body Map<String, Object> map);

    /**
     * 启动次数统计
     *
     */
    @POST("chat/open")
    Observable<HttpResult<Object>> startStatistics();

    /**
     * 获取启动页广告信息
     *
     */
    @POST("chat/chat33/getAdvertisement")
    Observable<HttpResult<AdInfoBean>> getSplashAdInfo();

    /**
     * 获取模块启用状态
     *
     */
    @POST("chat/public/module-state")
    Observable<HttpResult<ModuleState.Wrapper>> getModuleState();

    /*------------------------------------------------聊天室相关------------------------------------------------*/

    /**
     * 获取聊天室列表
     *
     * @param param
     */
    @POST("chat/group/list")
    Observable<HttpResult<ChatGroupBean.Wrapper>> getChatGroupList(@Body ChatGroupParam param);

    /*------------------------------------------------群相关------------------------------------------------*/

    /**
     * 获取群组列表
     *
     * @param map  type 群聊类型（是否常用）
     *             1：普通，2：常用  3：全部
     */
    @POST("chat/room/list")
    Observable<HttpResult<RoomListBean.Wrapper>> getRoomList(@Body Map<String, Object> map);

    /**
     * 获取群公告列表
     *
     * @param map  roomId   群id
     *             startId  当前起始记录id
     *             number   获取记录数
     */
    @POST("chat/room/systemMsgs")
    Observable<HttpResult<GroupNotice.Wrapper>> getGroupNoticeList(@Body Map<String, Object> map);

    /**
     * 邀请入群
     *
     * @param param roomId    群id
     *              users     受邀请人id数组
     */
    @POST("chat/room/joinRoomInvite")
    Observable<HttpResult<StateResponse>> inviteUsers(@Body EditRoomUserParam param);

    /**
     * 踢出群
     *
     * @param param roomId    群id
     *              users     被踢人id数组
     */
    @POST("chat/room/kickOut")
    Observable<HttpResult<Object>> kickOutUsers(@Body EditRoomUserParam param);

    /**
     * 推荐群聊
     *
     * @param map times     推荐群聊批次
     */
    @POST("chat/room/recommend")
    Observable<HttpResult<RecommendGroup.Wrapper>> recommendGroups(@Body Map<String, Object> map);

    /**
     * 批量申请入群
     *
     * @param map rooms     群id数组
     */
    @POST("chat/room/batchJoinRoomApply")
    Observable<HttpResult<ResultList>> batchJoinRoomApply(@Body Map<String, Object> map);

    /**
     * 申请入群
     *
     * @param param roomId          群id
     *              id              入群人id
     *              applyReason     申请理由
     */
    @POST("chat/room/joinRoomApply")
    Observable<HttpResult<Object>> joinRoomApply(@Body JoinGroupParam param);

    /**
     * 处理入群申请
     *
     * @param map id        申请人id
     *            roomId        群id
     *            agree         是否同意，1：同意  2：拒绝
     */
    @POST("chat/room/joinRoomApprove")
    Observable<HttpResult<Object>> dealJoinRoomApply(@Body Map<String, Object> map);

    /**
     * 群内用户等级设置
     *
     * @param map roomId 群id
     *            id 用户id
     *            level  群内用户等级:1.普通用户;2.管理员;3.群主
     */
    @POST("chat/room/setLevel")
    Observable<HttpResult<Object>> setRoomUserLevel(@Body Map<String, Object> map);

    /**
     * 管理员设置群
     *
     * @param map roomId            群id
     *            canAddFriend      可否添加好友
     *            joinPermission    进群权限设置,1：需要审批，2：不需要审批，3：禁止加群
     */
    @POST("chat/room/setPermission")
    Observable<HttpResult<Object>> setPermission(@Body Map<String, Object> map);

    /**
     * 查看群信息
     *
     * @param map roomId 群id
     */
    @POST("chat/room/info")
    Observable<HttpResult<RoomInfoBean>> getRoomInfo(@Body Map<String, Object> map);

    /**
     * 查看群信息
     *
     * @param map roomId 群id
     */
    @POST("chat/room/info")
    Call<HttpResult<RoomInfoBean>> getRoomInfoSync(@Body Map<String, Object> map);

    /**
     * 获取群成员列表
     *
     * @param map roomId 群id
     */
    @POST("chat/room/userList")
    Observable<HttpResult<RoomUserBean.Wrapper>> getRoomUsers(@Body Map<String, Object> map);

    /**
     * 获取群成员列表
     *
     * @param map roomId 群id
     */
    @POST("chat/room/userList")
    Call<HttpResult<RoomUserBean.Wrapper>> getRoomUsersCall(@Body Map<String, Object> map);

    /**
     * 获取群成员详情
     *
     * @param map roomId 群id
     *            userId 用户id
     */
    @POST("chat/room/userInfo")
    Observable<HttpResult<RoomUserBean>> getRoomUserInfo(@Body Map<String, Object> map);

    /**
     * 创建群
     *
     * @param param 群id
     */
    @POST("chat/room/create")
    Observable<HttpResult<RoomInfoBean>> createRoom(@Body CreateGroupParam param);

    /**
     * 删除群
     *
     * @param map roomId 群id
     */
    @POST("chat/room/delete")
    Observable<HttpResult<Object>> deleteRoom(@Body Map<String, Object> map);

    /**
     * 成员退出群
     *
     * @param map roomId 群id
     */
    @POST("chat/room/loginOut")
    Observable<HttpResult<Object>> quitRoom(@Body Map<String, Object> map);

    /**
     * 群内昵称设置
     */
    @POST("chat/room/setMemberNickname")
    Observable<HttpResult<Object>> setMemberNickname(@Body Map<String, Object> map);

    /**
     * 群内发布公告
     */
    @POST("chat/room/sendSystemMsgs")
    Observable<HttpResult<Object>> publishNotice(@Body Map<String, Object> map);

    /**
     * 设置群内禁言名单
     * <tbody>
     * <tr>
     * <td>roomId</td>
     * <td>群id</td>
     * <td>string</td>
     * <td>必填</td>
     * <td></td>
     * </tr>
     * <tr>
     * <td>listType</td>
     * <td>列表类型</td>
     * <td>int</td>
     * <td>必填</td>
     * <td>1：全员发言 2：黑名单 3：白名单 4：全员禁言</td>
     * </tr>
     * <tr>
     * <td>users</td>
     * <td>成员id</td>
     * <td>string[]</td>
     * <td>非必填</td>
     * <td>listType 为 2或3时需要</td>
     * </tr>
     * <tr>
     * <td>deadline</td>
     * <td>解禁时间</td>
     * <td>datetime</td>
     * <td>非必填</td>
     * <td>黑名单时必填，永远为2200/1/1 00:00:00（7258089600000）</td>
     * </tr>
     * </tbody>
     */
    @POST("chat/room/setMutedList")
    Observable<HttpResult<Object>> setMutedList(@Body Map<String, Object> map);

    /**
     * 单个成员禁言设置
     *
     * @param map
     */
    @POST("chat/room/setMutedSingle")
    Observable<HttpResult<Object>> setMutedSingle(@Body Map<String, Object> map);

    /*------------------------------------------------好友相关------------------------------------------------*/

    /**
     * 获取所有未处理的好友请求和入群请求
     */
    @POST("chat/chat33/unreadApplyNumber")
    Observable<HttpResult<UnreadNumber>> getUnreadApplyNumber();

    /**
     * 好友申请列表
     *
     * @param map datetime  最早一条时间
     *            number    数量
     */
    @POST("chat/chat33/applyList")
    Observable<HttpResult<ApplyInfoBean.Wrapper>> getFriendsApplyList(@Body Map<String, Object> map);

    /**
     * 好友申请处理
     *
     * @param map id       对方id
     *            agree    是否同意 1：同意 2：拒绝
     */
    @POST("chat/friend/response")
    Observable<HttpResult<Object>> dealFriendRequest(@Body Map<String, Object> map);

    /**
     * 查看好友详情
     *
     * @param map id 对方id
     */
    @POST("chat/user/userInfo")
    Observable<HttpResult<FriendBean>> getUserInfo(@Body Map<String, Object> map);

    /**
     * 查看好友详情(同步请求)
     *
     * @param map id 对方id
     */
    @POST("chat/user/userInfo")
    Call<HttpResult<FriendBean>> getUserInfoSync(@Body Map<String, Object> map);

    /**
     * 修改好友备注
     *
     * @param map id        对方id
     *            nickname    备注
     */
    @POST("chat/friend/setRemark")
    Observable<HttpResult<Object>> setFriendRemark(@Body Map<String, Object> map);

    /**
     * 修改好友详细备注
     */
    @POST("chat/friend/setExtRemark")
    Observable<HttpResult<Object>> setFriendExtRemark(@Body Map<String, Object> map);

    /***************************************钱包、新版红包相关接口**********************************************/

    /**
     * 发送短信
     */
    @Headers({DOMAIN_NAME_HEADER + AppConfig.DEPOSIT_URL_NAME})
    @FormUrlEncoded
    @POST("v1/send/sms")
    Observable<HttpResponse<DepositSMS>> sendSMS(
            @Field("area") String area,
            @Field("mobile") String mobile,
            @Field("codetype") String codetype,
            @Field("param") String param,
            @Field("extend_param") String extend_param,
            @Field("businessId") String businessId,
            @Field("ticket") String ticket
    );

    /**
     * 语音验证码
     */
    @Headers({DOMAIN_NAME_HEADER + AppConfig.DEPOSIT_URL_NAME})
    @FormUrlEncoded
    @POST("v1/send/voice")
    Observable<HttpResponse<DepositSMS>> sendVoiceCode(
            @Field("area") String area,
            @Field("mobile") String mobile,
            @Field("codetype") String codetype,
            @Field("param") String param,
            @Field("businessId") String businessId,
            @Field("ticket") String ticket
    );

    /**
     * 验证码预校验
     */
    @Headers({DOMAIN_NAME_HEADER + AppConfig.DEPOSIT_URL_NAME})
    @FormUrlEncoded
    @POST("v1/send/pre-validate")
    Observable<HttpResponse<Object>> verifyCode(
            @Field("area") String area,
            @Field("mobile") String mobile,
            @Field("email") String email,
            /**
             * 验证码类型
             * 修改设置支付密码："reset_pay_password"
             */
            @Field("codetype") String codetype,
            /**
             * sms/email/voice
             */
            @Field("type") String type,
            @Field("code") String code
    );

    /**
     * 获取是否设置支付密码
     *
     * @return
     */
    @POST("chat/user/isSetPayPwd")
    Observable<HttpResult<StateResponse>> isSetPayPassword();

    /**
     * 修改，设置支付密码
     *
     * @return
     */
    @POST("chat/user/setPayPwd")
    Observable<HttpResult<Object>> setPayPassword(@Body PayPasswordRequest request);

    /**
     * 校验支付密码
     *
     * @return
     */
    @POST("chat/user/checkPayPwd")
    Observable<HttpResult<Object>> checkPayPassword(@Body Map<String, Object> map);

    /**
     * 获取红包币种信息
     *
     * @return
     */
    @POST("chat/red-packet/balance")
    Observable<HttpResult<RedPacketCoin.Wrapper>> packetBalance();

    /**
     * 获取红包币种信息
     *
     * @return
     */
    @POST("chat/red-packet/balance")
    Call<HttpResult<RedPacketCoin.Wrapper>> packetBalanceSync();

    /**
     * 发送红包
     *
     * @param request
     * @return
     */
    @POST("chat/red-packet/send")
    Observable<HttpResult<SendRedPacketResponse>> sendRedPacket(@Body SendRedPacketRequest request);

    /**
     * 收取红包
     *
     * @param request
     * @return
     */
    @POST("chat/red-packet/receive-entry")
    Observable<HttpResult<ReceiveRedPacketResponse>> receiveRedPacket(@Body ReceiveRedPacketRequest request);

    /**
     * 红包详情
     *
     * @param map
     * @return
     */
    @POST("chat/red-packet/detail")
    Observable<HttpResult<RedPacketInfoResponse>> redPacketInfo(@Body Map<String, Object> map);

    /**
     * 获取红包领取用户列表
     *
     * @param map
     * @return
     */
    @POST("chat/red-packet/receiveDetail")
    Observable<HttpResult<RedPacketReceiveInfo.Wrapper>> redPacketReceiveList(@Body Map<String, Object> map);

    /**
     * 红包领取记录
     *
     * @param request
     * @return
     */
    @POST("chat/red-packet/statistic")
    Observable<HttpResult<RedPacketRecord>> redPacketRecord(@Body RedPacketRecordRequest request);

    /**
     * 托管账户提币（转账）
     *
     * @param request
     * @return
     */
    @POST("chat/pay/payment")
    Observable<HttpResult<WithdrawResponse>> withdraw(@Body WithdrawRequest request);
}
