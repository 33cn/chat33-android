package com.fzm.chat33.core.source.impl

import com.fuzamei.common.net.Result
import com.fuzamei.componentservice.ext.apiCall
import com.fzm.chat33.core.global.UserInfo
import com.fzm.chat33.core.net.api.UserService
import com.fzm.chat33.core.source.UserDataSource
import javax.inject.Inject

/**
 * @author zhengjy
 * @since 2019/10/08
 * Description:
 */
class NetUserDataSource @Inject constructor(
        private val service: UserService
) : UserDataSource {

    override suspend fun uploadDeviceToken(deviceToken: String): Result<Any> {
        val map = mapOf("deviceToken" to deviceToken)
        return apiCall { service.setDeviceToken(map) }
    }

    override suspend fun login(type: Int): Result<UserInfo> {
        val map = mapOf("type" to type)
        return apiCall { service.login(map) }
    }

    override suspend fun logout(): Result<Any> {
        return apiCall { service.logout() }
    }

    override suspend fun backupChain(): Result<Any> {
        return apiCall { service.backupChain() }
    }

    override suspend fun getUserInfo(id: String): Result<UserInfo> {
        val map = mapOf("id" to id)
        return apiCall { service.getUserInfo(map) }
    }
}