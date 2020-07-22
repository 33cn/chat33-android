package com.fzm.chat33.di

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.fuzamei.componentservice.di.Chat33ViewModelFactory
import com.fzm.chat33.main.mvvm.*
import com.fzm.chat33.redpacket.mvvm.*
import org.kodein.di.Kodein
import org.kodein.di.generic.bind
import org.kodein.di.generic.instance
import org.kodein.di.generic.singleton
import javax.inject.Provider

/**
 * @author zhengjy
 * @since 2019/12/04
 * Description:
 */
fun viewModelFactoryModule() = Kodein.Module("MainViewModelModule") {
    bind<ViewModelProvider.Factory>() with singleton { Chat33ViewModelFactory(instance()) }
    bind<Map<Class<out ViewModel>, Provider<ViewModel>>>() with singleton {
        mutableMapOf<Class<out ViewModel>, Provider<ViewModel>>().apply {
            put(AddVerifyViewModel::class.java, Provider { AddVerifyViewModel(instance()) })
            put(AitSelectorViewModel::class.java, Provider { AitSelectorViewModel(instance()) })
            put(BlackListViewModel::class.java, Provider { BlackListViewModel(instance()) })
            put(BookFriendViewModel::class.java, Provider { BookFriendViewModel(instance()) })
            put(BookGroupViewModel::class.java, Provider { BookGroupViewModel(instance()) })
            put(ChatFileViewModel::class.java, Provider { ChatFileViewModel(instance(), instance()) })
            put(ChatPraiseViewModel::class.java, Provider { ChatPraiseViewModel(instance()) })
            put(ChatViewModel::class.java, Provider { ChatViewModel(instance(), instance(), instance()) })
            put(ContactSelectViewModel::class.java, Provider { ContactSelectViewModel(instance(), instance(), instance()) })
            put(EditUserRemarkViewModel::class.java, Provider { EditUserRemarkViewModel(instance()) })
            put(EncryptPasswordViewModel::class.java, Provider { EncryptPasswordViewModel(instance(), instance()) })
            put(GroupMemberViewModel::class.java, Provider { GroupMemberViewModel(instance()) })
            put(GroupViewModel::class.java, Provider { GroupViewModel(instance(), instance(), instance()) })
            put(ImportMnemonicViewModel::class.java, Provider { ImportMnemonicViewModel(instance(), instance()) })
            put(MainViewModel::class.java, Provider { MainViewModel(instance(), instance(), instance()) })
            put(MessagePraiseViewModel::class.java, Provider { MessagePraiseViewModel(instance()) })
            put(MessageViewModel::class.java, Provider { MessageViewModel(instance()) })
            put(NewFriendViewModel::class.java, Provider { NewFriendViewModel(instance()) })
            put(PayPasswordViewModel::class.java, Provider { PayPasswordViewModel(instance()) })
            put(PromoteDetailViewModel::class.java, Provider { PromoteDetailViewModel(instance()) })
            put(RecommendedGroupViewModel::class.java, Provider { RecommendedGroupViewModel(instance()) })
            put(SearchLocalViewModel::class.java, Provider { SearchLocalViewModel(instance()) })
            put(SearchOnlineViewModel::class.java, Provider { SearchOnlineViewModel(instance(), instance()) })
            put(ServerTipsViewModel::class.java, Provider { ServerTipsViewModel(instance()) })
            put(SettingViewModel::class.java, Provider { SettingViewModel(instance()) })
            put(UserDetailViewModel::class.java, Provider { UserDetailViewModel(instance()) })
            put(PacketRecordViewModel::class.java, Provider { PacketRecordViewModel(instance()) })
            put(SendPacketViewModel::class.java, Provider { SendPacketViewModel(instance(), instance()) })
            put(PacketInfoViewModel::class.java, Provider { PacketInfoViewModel(instance()) })
            put(PraiseRankingViewModel::class.java, Provider { PraiseRankingViewModel(instance(), instance()) })
        }
    }
}