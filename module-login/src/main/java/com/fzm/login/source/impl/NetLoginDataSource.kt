package com.fzm.login.source.impl

import com.fuzamei.common.net.Result
import com.fuzamei.componentservice.ext.apiCall
import com.fzm.login.model.bean.ChatLogin
import com.fzm.login.net.LoginService
import com.fzm.login.source.LoginDataSource

/**
 * @author zhengjy
 * @since 2020/02/11
 * Description:
 */
class NetLoginDataSource(
        private val service: LoginService
) : LoginDataSource {

    override suspend fun login(phone: String, code: String): Result<ChatLogin> {
        val map = mapOf("phone" to phone, "code" to code)
        return apiCall { service.login(map) }
    }

    override suspend fun sendCode(phone: String): Result<Any> {
        val map = mapOf("phone" to phone)
        return apiCall { service.sendCode(map) }
    }
}