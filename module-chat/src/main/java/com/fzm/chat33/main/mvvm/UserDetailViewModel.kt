package com.fzm.chat33.main.mvvm

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.fuzamei.common.bus.LiveBus
import com.fuzamei.common.ext.request
import com.fuzamei.common.ext.result
import com.fuzamei.common.ext.start
import com.fuzamei.common.net.Result
import com.fuzamei.common.utils.RoomUtils
import com.fuzamei.componentservice.app.BusEvent
import com.fuzamei.componentservice.app.LoadingViewModel
import com.fzm.chat33.core.bean.UidSearchBean
import com.fzm.chat33.core.db.ChatDatabase
import com.fzm.chat33.core.db.bean.FriendBean
import com.fzm.chat33.core.db.bean.RoomUserBean
import com.fzm.chat33.core.global.Chat33Const
import com.fzm.chat33.core.repo.ContactsRepository
import com.fzm.chat33.core.response.BoolResponse
import com.fzm.chat33.core.response.StateResponse
import com.fzm.chat33.core.utils.UserInfoPreference
import javax.inject.Inject

class UserDetailViewModel @Inject constructor(
        private val repository: ContactsRepository
) : LoadingViewModel()  {

    private val _blockUser by lazy { MutableLiveData<Any>() }
    val blockUser: LiveData<Any>
        get() = _blockUser

    private val _unBlockUser by lazy { MutableLiveData<Any>() }
    val unBlockUser: LiveData<Any>
        get() = _unBlockUser

    private val _userInfo by lazy { MutableLiveData<FriendBean>() }
    val userInfo: LiveData<FriendBean>
        get() = _userInfo

    private val _uidSearchBean by lazy { MutableLiveData<UidSearchBean>() }
    val uidSearchBean: LiveData<UidSearchBean>
        get() = _uidSearchBean

    private val _roomUserBean by lazy { MutableLiveData<RoomUserBean>() }
    val roomUserBean: LiveData<RoomUserBean>
        get() = _roomUserBean

    private val _stickyOnTop by lazy { MutableLiveData<Int>() }
    val stickyOnTop: LiveData<Int>
        get() = _stickyOnTop

    private val _setDND by lazy { MutableLiveData<Int>() }
    val setDND: LiveData<Int>
        get() = _setDND

    private val _deleteFriend by lazy { MutableLiveData<Any>() }
    val deleteFriend: LiveData<Any>
        get() = _deleteFriend

    private val _checkAnswer by lazy { MutableLiveData<CheckAnswerResponse>() }
    val checkAnswer: LiveData<CheckAnswerResponse>
        get() = _checkAnswer

    private val _addFriendNeedAnswer by lazy { MutableLiveData<StateResponse>() }
    val addFriendNeedAnswer: LiveData<StateResponse>
        get() = _addFriendNeedAnswer

    private val _addFriend by lazy { MutableLiveData<StateResponse>() }
    val addFriend: LiveData<StateResponse>
        get() = _addFriend

    private val _setMutedSingle by lazy { MutableLiveData<Long>() }
    val setMutedSingle: LiveData<Long>
        get() = _setMutedSingle

    fun isBlock(userId : String): Boolean {
        return repository.isLocalBlock(userId)
    }

    fun blockUser(userId: String, address: String?) {
        start {
            loading()
        }.request {
            repository.blockUserV2(address)
        }.result(onSuccess = {
            _blockUser.value = it
            RoomUtils.run(Runnable {
                ChatDatabase.getInstance().friendsDao().updateBlocked(userId, 1)
                ChatDatabase.getInstance().recentMessageDao().deleteMessage(Chat33Const.CHANNEL_FRIEND, userId)
                LiveBus.of(BusEvent::class.java).contactsRefresh().setValue(1)
            })
        }, onComplete = {
            dismiss()
        })
    }

    fun unblockUser(userId: String, address: String?) {
        start {
            loading()
        }.request {
            repository.unblockUserV2(address)
        }.result(onSuccess = {
            _unBlockUser.value = it
            RoomUtils.run(Runnable {
                ChatDatabase.getInstance().friendsDao().updateBlocked(userId, 0)
                LiveBus.of(BusEvent::class.java).contactsRefresh().setValue(1)
                LiveBus.of(BusEvent::class.java).contactsRefresh().setValue(3)
            })
        }, onComplete = {
            dismiss()
        })
    }

    fun getUserInfo(userId: String) {
        request {
            repository.getUserInfo(userId)
        }.result {
            _userInfo.value = it
        }
    }

    fun searchByUid(userId: String) {
        request {
            repository.searchByUid(userId)
        }.result {
            _uidSearchBean.value = it
        }
    }

    fun getRoomUserInfo(roomId: String, userId: String) {
        request {
            repository.getRoomUserInfo(roomId, userId)
        }.result {
            _roomUserBean.value = it
            RoomUtils.run(Runnable {
                it.roomId = roomId
                it.id = userId
                ChatDatabase.getInstance().roomUserDao().insert(it)
            })
        }
    }

    fun friendStickyOnTop(userId: String, sticky: Int) {
        request {
            repository.friendStickyOnTop(userId, sticky)
        }.result (onSuccess = {
            RoomUtils.run(Runnable {
                ChatDatabase.getInstance().friendsDao().changeSticky(userId, sticky)
                ChatDatabase.getInstance().recentMessageDao().changeSticky(userId, sticky)
            })
        }, onError = {
            _stickyOnTop.value = sticky
        })
    }

    fun friendNoDisturb(userId: String, dnd: Int) {
        request {
            repository.friendNoDisturb(userId, dnd)
        }.result (onSuccess = {
            RoomUtils.run(Runnable {
                ChatDatabase.getInstance().friendsDao().changeDnd(userId, dnd)
                ChatDatabase.getInstance().recentMessageDao().changeDisturb(userId, dnd)
            })
        }, onError = {
            _setDND.value = dnd
        })
    }

    fun deleteFriend(userId: String, address: String) {
        request {
            repository.deleteFriendV2(address)
        }.result {
            _deleteFriend.value = it
            UserInfoPreference.getInstance().setBooleanPref("${UserInfoPreference.NO_MORE_CHAT_LOG}${Chat33Const.CHANNEL_FRIEND}-${userId}", false)
            RoomUtils.run(Runnable {
                ChatDatabase.getInstance().chatMessageDao().deletePrivateMessage(userId)
                ChatDatabase.getInstance().recentMessageDao().deleteMessage(Chat33Const.CHANNEL_FRIEND, userId)
                ChatDatabase.getInstance().friendsDao().delete(userId)
            })
            // 2020年3月6日 10:53:13，不刷新好友列表，因为有可能拉到的还是原来的列表
//            LiveBus.of(BusEvent::class.java).contactsRefresh().setValue(1)
        }
    }

    fun checkAnswer(userId: String, content: String) {
        start {
            loading()
        }.request {
            repository.checkAnswer(userId, content)
        }.result(onSuccess = {
            _checkAnswer.value = CheckAnswerResponse(it, content)
        }, onComplete = {
            dismiss()
        })
    }

    @Deprecated("")
    fun addFriend(userId: String, reason: String, content: String, sourceType: Int, sourceId: String?, address: String?) {
        start {
            loading()
        }.request {
            val result = repository.addFriendV2(address)
            if (result.isSucceed()) {
                Result.Success(StateResponse().apply { state = 3 })
            } else {
                Result.Error(result.error())
            }
//            repository.addFriend(AddFriendParam(userId, reason, content, sourceType, sourceId))
        }.result(onSuccess = {
            _addFriendNeedAnswer.value = it
        }, onComplete = {
            dismiss()
        })
    }

    fun addFriend(userId: String, reason: String, sourceType: Int, sourceId: String?, address: String?) {
        start {
            loading()
        }.request {
            val result = repository.addFriendV2(address)
            if (result.isSucceed()) {
                Result.Success(StateResponse().apply { state = 3 })
            } else {
                Result.Error(result.error())
            }
//            repository.addFriend(AddFriendParam(userId, reason, sourceType, sourceId))
        }.result(onSuccess = {
            _addFriend.value = it
            // 2020年3月6日 10:55:33，不刷新好友列表，因为有可能拉到的还是原来的列表
//            LiveBus.of(BusEvent::class.java).contactsRefresh().setValue(1)
        }, onComplete = {
            dismiss()
        })
    }

    fun setMutedSingle(roomId: String, userId: String, time: Long) {
        start {
            loading()
        }.request {
            repository.setMutedSingle(roomId, userId, time)
        }.result(onSuccess = {
            _setMutedSingle.value = time
        }, onComplete = {
            dismiss()
        })
    }
}

data class CheckAnswerResponse (
        var response: BoolResponse,
        var content: String
)