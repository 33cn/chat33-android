package com.fzm.chat33.main.mvvm

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.fuzamei.common.ext.request
import com.fuzamei.common.ext.result
import com.fuzamei.common.ext.start
import com.fuzamei.componentservice.app.LoadingViewModel
import com.fzm.chat33.core.bean.comparator.PinyinComparator
import com.fzm.chat33.core.bean.param.EditRoomUserParam
import com.fzm.chat33.core.db.ChatDatabase
import com.fzm.chat33.core.db.bean.RoomContact
import com.fzm.chat33.core.db.bean.RoomUserBean
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
class GroupMemberViewModel @Inject constructor(
        private val contactData: ContactsRepository
) : LoadingViewModel() {

    private val RESULT = Any()
    
    private val pinyinComparator by lazy { PinyinComparator() }

    private val _muteResult by lazy { MutableLiveData<Int>() }
    val muteResult: LiveData<Int>
        get() = _muteResult

    private val _muteSingleResult by lazy { MutableLiveData<Int>() }
    val muteSingleResult: LiveData<Int>
        get() = _muteSingleResult

    private val _kickOutResult by lazy { MutableLiveData<Any>() }
    val kickOutResult: LiveData<Any>
        get() = _kickOutResult

    private val _roomUsers by lazy { MutableLiveData<RoomUserBean.Wrapper>() }
    val roomUsers: LiveData<RoomUserBean.Wrapper>
        get() = _roomUsers

    private val _roomContacts by lazy { MutableLiveData<List<RoomContact>>() }
    val roomContacts: LiveData<List<RoomContact>>
        get() = _roomContacts

    private val _searchRoomContacts by lazy { MutableLiveData<List<RoomContact>>() }
    val searchRoomContacts: LiveData<List<RoomContact>>
        get() = _searchRoomContacts

    fun getRoomUsers(initLoad: Boolean, roomId: String) {
        start {
            if (initLoad) {
                loading()
            }
        }.request {
            contactData.getRoomUsers(roomId)
        }.result({
            _roomUsers.value = it
        }, {
            _roomUsers.value = null
        }, {
            dismiss()
        })
    }

    /**
     * 单人禁言，解禁
     *
     * @param roomId    群id
     * @param userId    用户id
     * @param deadline  禁言截至时间，0代表解禁
     */
    fun setMutedSingle(roomId: String, userId: String, deadline: Long, position: Int) {
        start {
            loading()
        }.request {
            contactData.setMutedSingle(roomId, userId, deadline)
        }.result({
            _muteSingleResult.value = position
        }, {
            _muteSingleResult.value = null
        }, {
            dismiss()
        })
    }

    /**
     * 设置禁言模式，以及禁言成员名单
     */
    fun setMutedList(roomId: String, listType: Int, users: List<String>?, deadline: Long) {
        start {
            loading()
        }.request {
            contactData.setMutedList(roomId, listType, users, deadline)
        }.result({
            _muteResult.value = listType
        }, {
            _muteResult.value = null
        }, {
            dismiss()
        })
    }

    /**
     * 踢出群成员
     */
    fun kickOutUsers(param: EditRoomUserParam) {
        start {
            loading()
        }.request {
            contactData.kickOutUsers(param)
        }.result({
            _kickOutResult.value = RESULT
        }, {
            _kickOutResult.value = null
        }, {
            dismiss()
        })
    }

    fun getRoomContactsByLevel(roomId: String, userLevel: Int) {
        launch {
            _roomContacts.value = withContext(Dispatchers.IO) {
                ChatDatabase.getInstance().roomUserDao()
                        .getRoomContactsByLevel(roomId, userLevel).sortedWith(pinyinComparator)
            }
        }
    }

    fun searchRoomContactsByLevel(roomId: String, userLevel: Int, keyword: String) {
        launch {
            _searchRoomContacts.value = withContext(Dispatchers.IO) {
                contactData.searchRoomContactsByLevel(roomId, userLevel, keyword).sortedWith(pinyinComparator)
            }
        }
    }
}