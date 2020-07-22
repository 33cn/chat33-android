package com.fzm.chat33.core.source

import com.fuzamei.common.net.Result
import com.fzm.chat33.core.bean.DepositSMS
import com.fzm.chat33.core.bean.SettingInfoBean
import com.fzm.chat33.core.bean.param.AddQuestionParam
import com.fzm.chat33.core.request.PayPasswordRequest
import com.fzm.chat33.core.response.BoolResponse
import com.fzm.chat33.core.response.StateResponse

/**
 * @author zhengjy
 * @since 2019/09/16
 * Description:
 */
interface SettingDataSource {

    /**
     * 获取用户设置相关信息
     *
     */
    suspend fun getSettingInfo(): Result<SettingInfoBean>

    /**
     * 用户编辑头像
     */
    suspend fun editAvatar(channelType: Int?, id: String?, avatar: String): Result<Any>

    /**
     * 用户修改昵称
     */
    suspend fun editName(channelType: Int?, id: String?, name: String): Result<Any>

    /**
     * 设置添加好友是否需要验证
     *
     * @param map
     */
    suspend fun setAddVerify(needVerify: Int): Result<Any>

    /**
     * 设置添加好友是否需要回答问题
     *
     * @param param
     */
    suspend fun setAddQuestion(param: AddQuestionParam): Result<Any>

    /**
     * 设置被邀请入群需要确认
     *
     * @param map
     */
    suspend fun setInviteConfirm(needConfirm: Int): Result<Any>

    /**
     * 验证问题是否回答正确
     *
     * @param map
     */
    suspend fun checkAnswer(map: Map<String, Any>): Result<BoolResponse>

    /**
     * 获取是否设置支付密码
     *
     * @return
     */
    suspend fun isSetPayPassword(): Result<StateResponse>

    /**
     * 发送短信
     */
    suspend fun sendSMS(
            area: String,
            mobile: String,
            codetype: String,
            param: String,
            extend_param: String,
            businessId: String,
            ticket: String
    ): Result<DepositSMS>

    /**
     * 语音验证码
     */
    suspend fun sendVoiceCode(
            area: String,
            mobile: String,
            codetype: String,
            param: String,
            businessId: String,
            ticket: String
    ): Result<DepositSMS>

    /**
     * 验证码预校验
     */
    suspend fun verifyCode(
            area: String,
            mobile: String,
            email: String,
            /**
             * 验证码类型
             * 修改设置支付密码："reset_pay_password"
             */
            codetype: String,
            /**
             * sms/email/voice
             */
            type: String,
            code: String
    ): Result<Any>

    /**
     * 修改，设置支付密码
     *
     * @return
     */
    suspend fun setPayPassword(request: PayPasswordRequest): Result<Any>

    /**
     * 校验支付密码
     *
     * @return
     */
    suspend fun checkPayPassword(payPassword: String): Result<Any>

    /**
     * 上传加密助记词和公钥
     *
     * @param publicKey     聊天公钥
     * @param privateKey    加密后的助记词
     * @return
     */
    suspend fun uploadSecretKey(publicKey: String, privateKey: String): Result<Any>
}