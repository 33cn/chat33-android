package com.fzm.chat33.main.mvvm

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.fuzamei.common.ext.request
import com.fuzamei.common.ext.result
import com.fuzamei.componentservice.app.LoadingViewModel
import com.fzm.chat33.core.bean.UidSearchBean
import com.fzm.chat33.core.global.LoginInfoDelegate
import com.fzm.chat33.core.repo.ContactsRepository

import javax.inject.Inject

/**
 * @author zhengjy
 * @since 2019/11/01
 * Description:
 */
class SearchOnlineViewModel @Inject constructor(
        private val repository: ContactsRepository,
        loginInfoDelegate: LoginInfoDelegate
) : LoadingViewModel(), LoginInfoDelegate by loginInfoDelegate {

    private val _searchContact by lazy { MutableLiveData<UidSearchBean>() }
    val searchContact: LiveData<UidSearchBean>
        get() = _searchContact

    /**
     * 通过uid搜索群聊或者用户
     *
     * @param markId 群聊或用户的uid
     */
    fun searchByUid(markId: String) {
        request {
            repository.searchByUid(markId)
        }.result({
            _searchContact.value = it
        }, {
            _searchContact.value = null
        })
    }
}