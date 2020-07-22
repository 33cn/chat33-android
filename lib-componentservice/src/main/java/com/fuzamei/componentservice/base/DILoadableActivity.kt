package com.fuzamei.componentservice.base

import android.os.Bundle
import org.kodein.di.AnyKodeinContext
import org.kodein.di.Kodein
import org.kodein.di.KodeinContext
import org.kodein.di.KodeinTrigger
import org.kodein.di.conf.KodeinGlobalAware
import org.kodein.di.conf.global
import org.kodein.di.jxinject.jx

/**
 * @author zhengjy
 * @since 2019/12/04
 * Description:提供注入容器的[LoadableActivity]
 */
abstract class DILoadableActivity: LoadableActivity(), KodeinGlobalAware {

    override val kodein: Kodein get() = Kodein.global

    override val kodeinContext: KodeinContext<*> get() = AnyKodeinContext

    override val kodeinTrigger: KodeinTrigger? get() = null

    override fun onCreate(savedInstanceState: Bundle?) {
        kodein.jx.inject(this)
        super.onCreate(savedInstanceState)
    }
}