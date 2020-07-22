package com.fuzamei.componentservice.di

import com.fuzamei.common.FzmFramework
import org.kodein.di.Kodein
import org.kodein.di.conf.KodeinGlobalAware
import org.kodein.di.conf.global
import org.kodein.di.jxinject.jxInjectorModule

/**
 * @author zhengjy
 * @since 2019/12/05
 * Description:
 */
object BaseInjectors : KodeinGlobalAware {

    init {
        Kodein.global.mutable = true
        // 兼容java代码和旧的@Inject注解
        Kodein.global.addImport(jxInjectorModule)
        Kodein.global.addImport(commonModule(FzmFramework.context))
        Kodein.global.addImport(netModule())
    }
}