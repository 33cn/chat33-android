package com.fzm.chat33.core.source

import com.fuzamei.common.net.Result
import com.fzm.chat33.core.global.UserInfo

/**
 * @author zhengjy
 * @since 2019/10/08
 * Description:
 */
interface UserDataSource {

    /**
     * 上传推送的设备标识符
     */
    suspend fun uploadDeviceToken(deviceToken: String): Result<Any>

    /**
     * token登录
     */
    suspend fun login(type: Int): Result<UserInfo>

    /**
     * 退出登录
     */
    suspend fun logout(): Result<Any>

    /**
     * 通讯首次上链成功记录
     */
    suspend fun backupChain(): Result<Any>

    /**
     * 获取用户信息
     */
    suspend fun getUserInfo(id: String): Result<UserInfo>
}