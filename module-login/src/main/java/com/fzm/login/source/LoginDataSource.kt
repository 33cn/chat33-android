package com.fzm.login.source

import com.fuzamei.common.net.Result
import com.fzm.login.model.bean.ChatLogin

/**
 * @author zhengjy
 * @since 2020/02/11
 * Description:
 */
interface LoginDataSource {

    /**
     * 登录方法
     *
     * @param phone 手机号
     * @param code  验证码
     */
    suspend fun login(phone: String, code: String): Result<ChatLogin>

    /**
     * 发送验证码
     *
     * @param phone 手机号
     */
    suspend fun sendCode(phone: String): Result<Any>
}