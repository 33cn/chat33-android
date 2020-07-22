package com.fzm.chat33.core.di

import com.fzm.chat33.core.repo.*
import org.kodein.di.Kodein
import org.kodein.di.generic.bind
import org.kodein.di.generic.instance
import org.kodein.di.generic.provider
import org.kodein.di.generic.singleton

/**
 * @author zhengjy
 * @since 2019/12/04
 * Description:
 */
fun coreRepoModule() = Kodein.Module("CoreRepoModule") {
    bind<ChatRepository>() with singleton { ChatRepository(instance()) }
    bind<ContactsRepository>() with singleton { ContactsRepository(instance(), instance(), instance(), instance(), instance(), instance(), instance()) }
    bind<MainRepository>() with singleton { MainRepository(instance(), instance(), instance(), instance()) }
    bind<PromoteRepository>() with provider { PromoteRepository(instance()) }
    bind<SearchRepository>() with singleton { SearchRepository(instance(), instance()) }
    bind<SettingRepository>() with singleton { SettingRepository(instance(), instance()) }
}