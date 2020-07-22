package com.fzm.login.di

import android.content.Context
import com.alibaba.android.arouter.facade.annotation.Route
import com.alibaba.android.arouter.facade.template.IProvider
import com.fuzamei.componentservice.app.AppRoute
import com.fuzamei.componentservice.di.BaseInjectors
import org.kodein.di.Kodein
import org.kodein.di.conf.global

/**
 * @author zhengjy
 * @since 2019/02/11
 * Description:
 */
@Route(path = AppRoute.LOGIN_INJECTOR)
class LoginInjectors : IProvider {

    init {
        Kodein.global.apply {
            BaseInjectors
            // 登录模块
            addImport(loginModule())
        }
    }

    override fun init(context: Context?) {

    }
}