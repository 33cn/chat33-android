package com.fzm.chat33.main.mvvm

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.fuzamei.common.ext.request
import com.fuzamei.common.ext.result
import com.fuzamei.common.ext.start
import com.fuzamei.common.utils.SharedPrefUtil
import com.fuzamei.componentservice.app.LoadingViewModel
import com.fuzamei.componentservice.config.AppPreference
import com.fzm.chat33.core.Chat33
import com.fzm.chat33.core.bean.ModuleState
import com.fzm.chat33.core.bean.SettingInfoBean
import com.fzm.chat33.core.bean.UnreadNumber
import com.fzm.chat33.core.global.LoginInfoDelegate
import com.fzm.chat33.core.repo.ContactsRepository
import com.fzm.chat33.core.repo.MainRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * @author zhengjy
 * @since 2019/10/08
 * Description:
 */
class MainViewModel @Inject constructor(
        private val repository: MainRepository,
        private val contactsRepository: ContactsRepository,
        private val loginDelegate: LoginInfoDelegate
) : LoadingViewModel(), LoginInfoDelegate by loginDelegate {

    private val _unreadNum by lazy { MutableLiveData<UnreadNumber>() }
    val unreadNumber: LiveData<UnreadNumber>
        get() = _unreadNum

    private val _editSelfName by lazy { MutableLiveData<String>() }
    val editSelfName: LiveData<String>
        get() = _editSelfName

    /**-------------------------------我的模块-------------------------------**/
    private val _setting by lazy { MutableLiveData<SettingInfoBean>() }
    val setting: LiveData<SettingInfoBean>
        get() = _setting

    private val _module by lazy { MutableLiveData<ModuleState.Wrapper>() }
    val module: LiveData<ModuleState.Wrapper>
        get() = _module

    fun login() {
        performLogin()
    }

    fun logout() {
        performLogout()
    }

    fun uploadDeviceToken() {
        launch(Dispatchers.IO) {
            val deviceToken = AppPreference.PUSH_DEVICE_TOKEN
            if (deviceToken.isNotEmpty()) {
                repository.uploadDeviceToken(deviceToken)
            }
        }
    }

    fun getUnreadApplyNumber() {
        request {
            repository.getUnreadApplyNumber()
        }.result(onSuccess = {
            _unreadNum.value = it
        }, onError = {
            _unreadNum.value = null
        })
    }

    fun editName(channelType: Int, id: String, name: String) {
        start {
            loading()
        }.request {
            repository.editName(channelType, id, name)
        }.result({
            _editSelfName.value = name
        }, {
            _editSelfName.value = null
        }, {
            dismiss()
        })
    }

    fun getSettingInfo() {
        request {
            repository.getSettingInfo()
        }.result {
            _setting.value = it
        }
    }

    fun getModuleState() {
        request {
            repository.getModuleState()
        }.result {
            _module.value = it
        }
    }

    fun updateFriendsList() {
        launch {
            contactsRepository.updateFriendsList()
        }
    }

    fun updateRoomList() {
        launch {
            contactsRepository.updateRoomList()
        }
    }

    fun loadRoomUsers() {
        Chat33.loadRoomUsers()
    }

    fun loadInfoCache() {
        Chat33.loadInfoCache()
    }
}