package com.fzm.chat33.main.mvvm

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.fuzamei.common.ext.request
import com.fuzamei.common.ext.result
import com.fuzamei.componentservice.app.LoadingViewModel
import com.fzm.chat33.core.db.ChatDatabase
import com.fzm.chat33.core.db.bean.RoomContact
import com.fzm.chat33.core.repo.ContactsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * @author zhengjy
 * @since 2019/10/22
 * Description:群成员列表操作
 */
class AitSelectorViewModel @Inject constructor(
        private val contactData: ContactsRepository
) : LoadingViewModel() {

    private val _roomUsers by lazy { MutableLiveData<List<RoomContact>>() }
    val roomUsers: LiveData<List<RoomContact>>
        get() = _roomUsers

    fun getRoomUsers(roomId: String) {
        request {
            contactData.getRoomUsers(roomId)
        }.result{
            launch {
                _roomUsers.value = withContext(Dispatchers.IO) {
                    ChatDatabase.getInstance().roomUserDao().getRoomContacts(roomId)
                }
            }
        }
    }
}