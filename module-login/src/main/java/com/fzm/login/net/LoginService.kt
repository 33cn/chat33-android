package com.fzm.login.net

import com.fuzamei.common.net.rxjava.HttpResult
import com.fzm.login.model.bean.ChatLogin
import retrofit2.http.Body
import retrofit2.http.POST

/**
 * @author zhengjy
 * @since 2020/02/11
 * Description:聊天服务登录服务
 */
interface LoginService {

    /**
     * 聊天服务登录接口
     */
    @JvmSuppressWildcards
    @POST("chat/user/phoneLogin")
    suspend fun login(@Body map: Map<String, Any>): HttpResult<ChatLogin>

    /**
     * 聊天服务发送短信服务
     */
    @JvmSuppressWildcards
    @POST("chat/user/sendCode")
    suspend fun sendCode(@Body map: Map<String, Any>): HttpResult<Any>
}