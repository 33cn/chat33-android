package com.fzm.chat33.di

import com.alibaba.android.arouter.launcher.ARouter
import com.fuzamei.componentservice.app.AppRoute
import org.kodein.di.Kodein
import org.kodein.di.conf.global

/**
 * @author zhengjy
 * @since 2019/12/04
 * Description:
 */
object Injectors {

    @JvmStatic
    fun init() {
        Kodein.global.apply {
            ARouter.getInstance().build(AppRoute.LOGIN_INJECTOR).navigation()
            ARouter.getInstance().build(AppRoute.WORK_INJECTOR).navigation()
            // 聊天ViewModel模块
            addImport(viewModelFactoryModule())
        }
    }
}