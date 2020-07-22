package com.fuzamei.componentservice.di

import com.fuzamei.common.retrofiturlmanager.RetrofitUrlManager
import com.fuzamei.componentservice.app.Chat33Interceptor
import com.fuzamei.componentservice.config.AppConfig
import com.fuzamei.componentservice.ext.createOkHttpClient
import com.fuzamei.componentservice.ext.createRetrofit
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import org.kodein.di.Kodein
import org.kodein.di.generic.bind
import org.kodein.di.generic.instance
import org.kodein.di.generic.singleton
import retrofit2.Retrofit

/**
 * @author zhengjy
 * @since 2019/11/05
 * Description:
 */
fun netModule() = Kodein.Module("NetModule") {
    bind<RetrofitUrlManager>(tag = "mainUrlManager") with singleton {
        RetrofitUrlManager.getUrlManager().apply {
            putDomain(AppConfig.BASE_URL_NAME, AppConfig.CHAT_BASE_URL)
            putDomain(AppConfig.DEPOSIT_URL_NAME, AppConfig.DEPOSIT_BASE_URL)
            startAdvancedModel(AppConfig.CHAT_BASE_URL)
        }
    }
    bind<Interceptor>(tag = "mainInterceptor") with singleton { Chat33Interceptor() }
    bind<OkHttpClient>(tag = "mainClient") with singleton {
        createOkHttpClient(instance(tag = "mainUrlManager"), instance(tag = "mainInterceptor"), instance(tag = "encryptInterceptor"))
    }
    bind<OkHttpClient>(tag = "downloadClient") with singleton {
        createOkHttpClient(instance(tag = "mainUrlManager"))
    }
    bind<Retrofit>(tag = "mainRetrofit") with singleton {
        createRetrofit(instance(tag = "mainClient"), AppConfig.CHAT_BASE_URL)
    }
    bind<OkHttpClient>(tag = "contractClient") with singleton {
        createOkHttpClient(null)
    }
    bind<Retrofit>(tag = "contractRetrofit") with singleton {
        createRetrofit(instance(tag = "contractClient"), AppConfig.CONTRACT_BASE_URL)
    }
}