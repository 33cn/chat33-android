package com.fzm.chat33.core.source.impl

import com.fuzamei.common.net.Result
import com.fuzamei.componentservice.ext.apiCall
import com.fuzamei.componentservice.ext.apiCall2
import com.fzm.chat33.core.bean.DepositSMS
import com.fzm.chat33.core.bean.SettingInfoBean
import com.fzm.chat33.core.bean.param.AddQuestionParam
import com.fzm.chat33.core.global.Chat33Const
import com.fzm.chat33.core.net.ApiService
import com.fzm.chat33.core.net.api.SettingService
import com.fzm.chat33.core.request.PayPasswordRequest
import com.fzm.chat33.core.response.BoolResponse
import com.fzm.chat33.core.response.StateResponse
import com.fzm.chat33.core.source.SettingDataSource
import javax.inject.Inject

/**
 * @author zhengjy
 * @since 2019/09/16
 * Description:
 */
class NetSettingDataSource @Inject constructor(
        private val service: SettingService
) : SettingDataSource {

    override suspend fun getSettingInfo(): Result<SettingInfoBean> {
        return apiCall { service.getSettingInfo() }
    }

    override suspend fun editAvatar(channelType: Int?, id: String?, avatar: String): Result<Any> {
        val map = mutableMapOf<String, Any>()
        val url = when(channelType) {
            Chat33Const.CHANNEL_GROUP -> {
                map["groupId"] = id!!
                ApiService.group_avatar
            }
            Chat33Const.CHANNEL_ROOM -> {
                map["roomId"] = id!!
                ApiService.room_avatar
            }
            else -> {
                ApiService.my_avatar
            }
        }

        map["avatar"] = avatar
        return apiCall { service.editAvatar(url, map) }
    }

    override suspend fun editName(channelType: Int?, id: String?, name: String): Result<Any> {
        val map = mutableMapOf<String, Any>()
        val url = when (channelType) {
            Chat33Const.CHANNEL_GROUP -> {
                map["groupId"] = id!!
                map["groupName"] = name
                SettingService.group_name
            }
            Chat33Const.CHANNEL_ROOM -> {
                map["roomId"] = id!!
                map["name"] = name
                SettingService.room_name
            }
            else -> {
                map["nickname"] = name
                SettingService.my_name
            }
        }
        return apiCall { service.editName(url, map) }
    }

    override suspend fun setAddVerify(needVerify: Int): Result<Any> {
        val map = mapOf("tp" to needVerify)
        return apiCall { service.setAddVerify(map) }
    }

    override suspend fun setAddQuestion(param: AddQuestionParam): Result<Any> {
        return apiCall { service.setAddQuestion(param) }
    }

    override suspend fun setInviteConfirm(needConfirm: Int): Result<Any> {
        val map = mapOf("needConfirmInvite" to needConfirm)
        return apiCall { service.setInviteConfirm(map) }
    }

    override suspend fun checkAnswer(map: Map<String, Any>): Result<BoolResponse> {
        throw NotImplementedError()
    }

    override suspend fun isSetPayPassword(): Result<StateResponse> {
        return apiCall{ service.isSetPayPassword() }
    }

    override suspend fun sendSMS(area: String, mobile: String, codetype: String, param: String, extend_param: String, businessId: String, ticket: String): Result<DepositSMS> {
        return apiCall2 { service.sendSMS(area, mobile, codetype, param, extend_param, businessId, ticket) }
    }

    override suspend fun sendVoiceCode(area: String, mobile: String, codetype: String, param: String, businessId: String, ticket: String): Result<DepositSMS> {
        return apiCall2 { service.sendVoiceCode(area, mobile, codetype, param, businessId, ticket) }
    }

    override suspend fun verifyCode(area: String, mobile: String, email: String, codetype: String, type: String, code: String): Result<Any> {
        return apiCall2 { service.verifyCode(area, mobile, email, codetype, type, code) }
    }

    override suspend fun setPayPassword(request: PayPasswordRequest): Result<Any> {
        return apiCall { service.setPayPassword(request) }
    }

    override suspend fun checkPayPassword(payPassword: String): Result<Any> {
        val map = mapOf("payPassword" to payPassword)
        return apiCall { service.checkPayPassword(map) }
    }

    override suspend fun uploadSecretKey(publicKey: String, privateKey: String): Result<Any> {
        val map = mapOf("publicKey" to publicKey, "privateKey" to privateKey)
        return apiCall { service.uploadSecretKey(map) }
    }
}