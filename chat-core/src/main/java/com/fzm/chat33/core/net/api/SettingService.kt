package com.fzm.chat33.core.net.api

import com.fuzamei.common.net.rxjava.HttpResponse
import com.fuzamei.common.net.rxjava.HttpResult
import com.fuzamei.common.retrofiturlmanager.RetrofitUrlManager
import com.fuzamei.componentservice.config.AppConfig
import com.fzm.chat33.core.bean.DepositSMS
import com.fzm.chat33.core.bean.SettingInfoBean
import com.fzm.chat33.core.bean.param.AddQuestionParam
import com.fzm.chat33.core.request.PayPasswordRequest
import com.fzm.chat33.core.response.StateResponse
import io.reactivex.Observable
import retrofit2.http.*

/**
 * @author zhengjy
 * @since 2019/09/16
 * Description:
 */
interface SettingService {

    /**
     * 获取用户设置相关信息
     *
     */
    @POST("chat/user/userConf")
    suspend fun getSettingInfo(): HttpResult<SettingInfoBean>

    /**
     * 用户编辑头像
     */
    @JvmSuppressWildcards
    @POST
    suspend fun editAvatar(@Url url: String, @Body map: Map<String, Any>): HttpResult<Any>

    /**
     * 用户修改昵称
     */
    @JvmSuppressWildcards
    @POST
    suspend fun editName(@Url url: String, @Body map: Map<String, Any>): HttpResult<Any>

    /**
     * 设置添加好友是否需要验证
     *
     * @param map
     */
    @JvmSuppressWildcards
    @POST("chat/friend/confirm")
    suspend fun setAddVerify(@Body map: Map<String, Any>): HttpResult<Any>

    /**
     * 设置添加好友是否需要回答问题
     *
     * @param param
     */
    @POST("chat/friend/question")
    suspend fun setAddQuestion(@Body param: AddQuestionParam): HttpResult<Any>

    /**
     * 设置被邀请入群需要确认
     *
     * @param map
     */
    @JvmSuppressWildcards
    @POST("chat/user/set-invite-confirm")
    suspend fun setInviteConfirm(@Body map: Map<String, Any>): HttpResult<Any>

//    /**
//     * 验证问题是否回答正确
//     *
//     * @param map
//     */
//    @JvmSuppressWildcards
//    @POST("chat/friend/checkAnswer")
//    fun checkAnswer(@Body map: Map<String, Any>): Observable<HttpResult<BoolResponse>>

    /**
     * 获取是否设置支付密码
     *
     * @return
     */
    @JvmSuppressWildcards
    @POST("chat/user/isSetPayPwd")
    suspend fun isSetPayPassword(): HttpResult<StateResponse>

    /**
     * 发送短信
     */
    @Headers(RetrofitUrlManager.DOMAIN_NAME_HEADER + AppConfig.DEPOSIT_URL_NAME)
    @FormUrlEncoded
    @POST("v1/send/sms")
    suspend fun sendSMS(
            @Field("area") area: String,
            @Field("mobile") mobile: String,
            @Field("codetype") codetype: String,
            @Field("param") param: String,
            @Field("extend_param") extend_param: String,
            @Field("businessId") businessId: String,
            @Field("ticket") ticket: String
    ): HttpResponse<DepositSMS>

    /**
     * 语音验证码
     */
    @Headers(RetrofitUrlManager.DOMAIN_NAME_HEADER + AppConfig.DEPOSIT_URL_NAME)
    @FormUrlEncoded
    @POST("v1/send/voice")
    suspend fun sendVoiceCode(
            @Field("area") area: String,
            @Field("mobile") mobile: String,
            @Field("codetype") codetype: String,
            @Field("param") param: String,
            @Field("businessId") businessId: String,
            @Field("ticket") ticket: String
    ): HttpResponse<DepositSMS>

    /**
     * 验证码预校验
     */
    @Headers(RetrofitUrlManager.DOMAIN_NAME_HEADER + AppConfig.DEPOSIT_URL_NAME)
    @FormUrlEncoded
    @POST("v1/send/pre-validate")
    suspend fun verifyCode(
            @Field("area") area: String,
            @Field("mobile") mobile: String,
            @Field("email") email: String,
            /**
             * 验证码类型
             * 修改设置支付密码："reset_pay_password"
             */
            @Field("codetype") codetype: String,
            /**
             * sms/email/voice
             */
            @Field("type") type: String,
            @Field("code") code: String
    ): HttpResponse<Any>

    /**
     * 修改，设置支付密码
     *
     * @return
     */
    @POST("chat/user/setPayPwd")
    suspend fun setPayPassword(@Body request: PayPasswordRequest): HttpResult<Any>

    /**
     * 校验支付密码
     *
     * @return
     */
    @JvmSuppressWildcards
    @POST("chat/user/checkPayPwd")
    suspend fun checkPayPassword(@Body map: Map<String, Any>): HttpResult<Any>

    /**
     * 上传聊天公钥和加密助记词
     *
     * @return
     */
    @JvmSuppressWildcards
    @POST("chat/chat33/uploadSecretKey")
    suspend fun uploadSecretKey(@Body map: Map<String, Any>): HttpResult<Any>

    companion object {
        val group_avatar = AppConfig.CHAT_BASE_URL + "chat/group/editAvatar"
        val room_avatar = AppConfig.CHAT_BASE_URL + "chat/room/setAvatar"
        val my_avatar = AppConfig.CHAT_BASE_URL + "chat/user/editAvatar"

        val group_name = AppConfig.CHAT_BASE_URL + "chat/group/editGroupName"
        val room_name = AppConfig.CHAT_BASE_URL + "chat/room/setName"
        val my_name = AppConfig.CHAT_BASE_URL + "chat/user/editNickname"
    }
}