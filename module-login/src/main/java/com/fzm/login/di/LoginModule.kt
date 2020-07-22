package com.fzm.login.di

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.fuzamei.componentservice.di.Chat33ViewModelFactory
import com.fzm.login.LoginViewModel
import com.fzm.login.model.LoginRepository
import com.fzm.login.net.LoginService
import com.fzm.login.source.LoginDataSource
import com.fzm.login.source.impl.NetLoginDataSource
import org.kodein.di.Kodein
import org.kodein.di.generic.bind
import org.kodein.di.generic.instance
import org.kodein.di.generic.provider
import org.kodein.di.generic.singleton
import retrofit2.Retrofit
import javax.inject.Provider

/**
 * @author zhengjy
 * @since 2019/02/11
 * Description:
 */
fun loginModule() = Kodein.Module("LoginModule") {
    bind<LoginDataSource>() with singleton { NetLoginDataSource(instance<Retrofit>(tag = "mainRetrofit").create(LoginService::class.java)) }
    bind<LoginRepository>() with provider { LoginRepository(instance()) }
    bind<Map<Class<out ViewModel>, Provider<ViewModel>>>(tag = "loginViewModel") with singleton {
        mutableMapOf<Class<out ViewModel>, Provider<ViewModel>>().apply {
            put(LoginViewModel::class.java, Provider { LoginViewModel(instance()) })
        }
    }
    bind<ViewModelProvider.Factory>(tag = "login") with singleton { Chat33ViewModelFactory(instance(tag = "loginViewModel")) }
}