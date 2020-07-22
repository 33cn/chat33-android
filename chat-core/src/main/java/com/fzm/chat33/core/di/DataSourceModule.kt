package com.fzm.chat33.core.di

import com.fzm.chat33.core.global.LoginInfoDelegate
import com.fzm.chat33.core.global.LoginInfoDelegateImpl
import com.fzm.chat33.core.net.api.*
import com.fzm.chat33.core.source.*
import com.fzm.chat33.core.source.impl.*
import org.kodein.di.Kodein
import org.kodein.di.generic.bind
import org.kodein.di.generic.instance
import org.kodein.di.generic.provider
import org.kodein.di.generic.singleton
import retrofit2.Retrofit

/**
 * @author zhengjy
 * @since 2019/09/16
 * Description:
 */
fun dataSourceModule() = Kodein.Module("DataSourceModule") {
    bind<PromoteDataSource>() with provider { NetPromoteDataSource(instance<Retrofit>(tag = "mainRetrofit").create(PromoteService::class.java)) }
    bind<SettingDataSource>() with singleton { NetSettingDataSource(instance<Retrofit>(tag = "mainRetrofit").create(SettingService::class.java)) }
    bind<ChatDataSource>() with singleton { NetChatDataSource(instance<Retrofit>(tag = "mainRetrofit").create(ChatService::class.java)) }
    bind<UserDataSource>() with singleton { NetUserDataSource(instance<Retrofit>(tag = "mainRetrofit").create(UserService::class.java)) }
    bind<GeneralDataSource>() with singleton { NetGeneralDataSource(instance<Retrofit>(tag = "mainRetrofit").create(GeneralService::class.java)) }
    bind<FriendDataSource>() with singleton { NetFriendDataSource(instance<Retrofit>(tag = "mainRetrofit").create(FriendService::class.java)) }
    bind<GroupDataSource>() with singleton { NetGroupDataSource(instance<Retrofit>(tag = "mainRetrofit").create(GroupService::class.java)) }
    bind<LocalContactDataSource>() with provider { DatabaseLocalContactDataSource.get() }
    bind<SearchDataSource>() with singleton { LocalSearchDataSource() }
    bind<RedPacketDataSource>() with singleton { NetRedPacketDataSource(instance<Retrofit>(tag = "mainRetrofit").create(RedPacketService::class.java)) }
    bind<ContractDataSource>() with singleton { NetContractDataSource(instance<Retrofit>(tag = "contractRetrofit").create(ContractService::class.java)) }
    bind<LoginInfoDelegate>() with singleton { LoginInfoDelegateImpl(instance(), instance()) }
}