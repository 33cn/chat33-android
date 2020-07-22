package com.fzm.chat33.main.mvvm

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.fuzamei.common.ext.request
import com.fuzamei.common.ext.result
import com.fuzamei.common.ext.start
import com.fuzamei.common.utils.RoomUtils
import com.fuzamei.componentservice.app.LoadingViewModel
import com.fzm.chat33.core.db.ChatDatabase
import com.fzm.chat33.core.db.bean.FriendBean
import com.fzm.chat33.core.repo.ContactsRepository
import javax.inject.Inject

/**
 * @author zhengjy
 * @since 2019/10/10
 * Description:
 */
class BlackListViewModel @Inject constructor(
        private val repository: ContactsRepository
): LoadingViewModel() {

    private val _blackList by lazy { MutableLiveData<FriendBean.Wrapper>() }
    val blackList: LiveData<FriendBean.Wrapper>
        get() = _blackList

    fun getBlockedUsers() {
        start {
            loading()
        }.request {
            repository.getBlackList()
        }.result({
            _blackList.value = it
            RoomUtils.run(Runnable {
                it.userList?.let { list -> ChatDatabase.getInstance().friendsDao().insert(list) }
            })
        }, {
            _blackList.value = null
        }, {
            dismiss()
        })
    }
}