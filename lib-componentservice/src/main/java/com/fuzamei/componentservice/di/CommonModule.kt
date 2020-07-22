package com.fuzamei.componentservice.di

import android.content.Context
import com.google.gson.Gson
import org.kodein.di.Kodein
import org.kodein.di.generic.bind
import org.kodein.di.generic.singleton

/**
 * @author zhengjy
 * @since 2019/09/12
 * Description:公共通用依赖注入
 */
fun commonModule(application: Context) = Kodein.Module("CommonModule") {
    bind<Context>() with singleton { application }
    bind<Gson>() with singleton { Gson() }
}