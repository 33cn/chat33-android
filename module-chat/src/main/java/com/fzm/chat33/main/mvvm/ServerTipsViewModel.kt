package com.fzm.chat33.main.mvvm

import com.fuzamei.componentservice.app.LoadingViewModel
import com.fzm.chat33.core.global.LoginInfoDelegate
import javax.inject.Inject

/**
 * @author zhengjy
 * @since 2019/10/17
 * Description:
 */
class ServerTipsViewModel @Inject constructor(
        private val loginInfoDelegate: LoginInfoDelegate
) : LoadingViewModel(), LoginInfoDelegate by loginInfoDelegate