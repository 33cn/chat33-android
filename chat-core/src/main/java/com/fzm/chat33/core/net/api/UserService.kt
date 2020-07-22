package com.fzm.chat33.core.net.api

import com.fuzamei.common.net.rxjava.HttpResult
import com.fzm.chat33.core.global.UserInfo
import retrofit2.http.Body
import retrofit2.http.POST

/**
 * @author zhengjy
 * @since 2019/10/09
 * Description:
 */
interface UserService {

    /**
     * 上传推送的设备标识符
     */
    @JvmSuppressWildcards
    @POST("chat/user/set-device-token")
    suspend fun setDeviceToken(@Body map: Map<String, Any>): HttpResult<Any>

    /**
     * token登录
     */
    @POST("chat/user/tokenLogin")
    suspend fun login(): HttpResult<UserInfo>

    /**
     * token登录
     */
    @JvmSuppressWildcards
    @POST("chat/user/tokenLogin")
    suspend fun login(@Body map: Map<String, Any>): HttpResult<UserInfo>

    /**
     * 退出登录
     */
    @POST("chat/user/logout")
    suspend fun logout(): HttpResult<Any>

    /**
     * 通讯首次上链成功记录
     */
    @POST("chat/user/isChain")
    suspend fun backupChain(): HttpResult<Any>

    /**
     * 查看用户详情
     *
     * @param map id 用户id
     */
    @JvmSuppressWildcards
    @POST("chat/user/userInfo")
    suspend fun getUserInfo(@Body map: Map<String, Any>): HttpResult<UserInfo>
}