package com.fzm.login.model.bean

import java.io.Serializable

/**
 * @author zhengjy
 * @since 2020/02/11
 * Description:
 */
data class ChatLogin(
        var token: String,
        /**
         * 0：注册成功
         * 1：登录成功
         */
        var type: Int
) : Serializable