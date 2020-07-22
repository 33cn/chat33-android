package com.fzm.chat33.main.mvvm

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.fuzamei.common.ext.request
import com.fuzamei.common.ext.result
import com.fuzamei.common.ext.start
import com.fuzamei.common.net.Result
import com.fuzamei.common.utils.RoomUtils
import com.fuzamei.componentservice.app.LoadingViewModel
import com.fzm.chat33.core.bean.param.CreateGroupParam
import com.fzm.chat33.core.bean.param.EditRoomUserParam
import com.fzm.chat33.core.db.ChatDatabase
import com.fzm.chat33.core.db.bean.FriendBean
import com.fzm.chat33.core.db.bean.RoomInfoBean
import com.fzm.chat33.core.db.bean.RoomListBean
import com.fzm.chat33.core.db.bean.RoomUserBean
import com.fzm.chat33.core.manager.GroupKeyManager
import com.fzm.chat33.core.response.StateResponse
import com.fzm.chat33.core.repo.ContactsRepository
import kotlinx.coroutines.*
import java.util.*
import javax.inject.Inject

/**
 * @author zhengjy
 * @since 2019/10/10
 * Description:
 */
class BookFriendViewModel @Inject constructor(
        private val repository: ContactsRepository
): LoadingViewModel() {

    private val _getFriendList by lazy { MutableLiveData<Result<FriendBean.Wrapper>>() }
    val getFriendList: LiveData<Result<FriendBean.Wrapper>>
        get() = _getFriendList

    private val _searchFriendList by lazy { MutableLiveData<List<FriendBean>>() }
    val searchFriendList: LiveData<List<FriendBean>>
        get() = _searchFriendList

    val updateFriend: LiveData<List<FriendBean>>
        get() = repository.updateFriend

    val updateBlocked: LiveData<List<FriendBean>>
        get() = repository.updateBlocked

    private val _createResult by lazy { MutableLiveData<RoomInfoBean>() }
    val createResult: LiveData<RoomInfoBean>
        get() = _createResult

    private val _inviteResult by lazy { MutableLiveData<StateResponse>() }
    val inviteResult: LiveData<StateResponse>
        get() = _inviteResult

    private val _roomUsers by lazy { MutableLiveData<RoomUserBean.Wrapper>() }
    val roomUsers: LiveData<RoomUserBean.Wrapper>
        get() = _roomUsers

    fun getFriendList(type: Int, date: Date?, number: Int) {
        launch {
            _getFriendList.value = withContext(Dispatchers.IO) {
                repository.getFriendList(type, date, number)
            }
        }
    }

    fun getLocalFriendList() : List<FriendBean> {
        return repository.getLocalFriendList()
    }

    fun getLocalFriendById(id: String?) : FriendBean? {
        return repository.getLocalFriendById(id)
    }

    fun getLocalRoomById(id: String?) : RoomListBean? {
        return repository.getLocalRoomById(id)
    }

    fun searchFriend(keywords: String) {
        launch {
            _searchFriendList.value = withContext(Dispatchers.IO) {
                repository.searchFriends(keywords)
            }
        }
    }

    /*-------------------------------建群邀请相关-------------------------------*/
    /**
     * 创建群聊
     */
    fun createGroup(param: CreateGroupParam) {
        start {
            loading()
        }.request {
            repository.createRoom(param)
        }.result(onSuccess = {
            RoomUtils.run(Runnable {
                if (!it.id.isNullOrEmpty()) {
                    val listBean = RoomListBean(it)
                    ChatDatabase.getInstance().roomsDao().insert(listBean)
                }
            })
            GroupKeyManager.notifyGroupEncryptKey(it.id)
            launch(Dispatchers.IO) {
                repository.updateRoomList()
            }
            _createResult.value = it
        }, onComplete = {
            dismiss()
        })
    }

    /**
     * 邀请好友入群
     */
    fun inviteUsers(param: EditRoomUserParam) {
        start {
            loading()
        }.request {
            repository.inviteUsers(param)
        }.result(onSuccess = {
            if (it.state == 1) {
                GroupKeyManager.notifyGroupEncryptKey(param.roomId)
            }
            _inviteResult.value = it
        }, onComplete = {
            dismiss()
        })
    }

    fun getRoomUsers(roomId: String) {
        start {
            loading()
        }.request {
            repository.getRoomUsers(roomId)
        }.result(onSuccess = {
            _roomUsers.value = it
        }, onComplete = {
            dismiss()
        })
    }

}