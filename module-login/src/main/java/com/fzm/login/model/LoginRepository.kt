package com.fzm.login.model

import com.fzm.login.source.LoginDataSource

/**
 * @author zhengjy
 * @since 2019/08/09
 * Description:
 */
class LoginRepository(
        private val dataSource: LoginDataSource
): LoginDataSource by dataSource