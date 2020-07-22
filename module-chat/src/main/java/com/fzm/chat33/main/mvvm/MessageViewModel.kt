package com.fzm.chat33.main.mvvm

import android.annotation.SuppressLint
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.fuzamei.common.base.mvvm.SingleLiveData
import com.fuzamei.common.executor.AppExecutors
import com.fuzamei.common.net.Result
import com.fuzamei.common.net.Event
import com.fuzamei.common.net.rxjava.ApiException
import com.fuzamei.common.utils.RoomUtils
import com.fuzamei.componentservice.app.LoadingViewModel
import com.fzm.chat33.core.bean.comparator.RecentMsgComparator
import com.fuzamei.componentservice.consts.AppError
import com.fzm.chat33.core.db.ChatDatabase
import com.fzm.chat33.core.db.bean.FriendBean
import com.fzm.chat33.core.db.bean.RecentMessageBean
import com.fzm.chat33.core.db.bean.RoomInfoBean
import com.fzm.chat33.core.global.Chat33Const
import com.fzm.chat33.core.repo.ContactsRepository
import io.reactivex.Flowable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject

/**
 * @author zhengjy
 * @since 2019/10/23
 * Description:
 */
class MessageViewModel @Inject constructor(
        private val repository: ContactsRepository
) : LoadingViewModel() {

    private val _groupMessage by lazy { MutableLiveData<List<RecentMessageBean>>() }
    val groupMessage: LiveData<List<RecentMessageBean>>
        get() = _groupMessage

    private val _groupException by lazy { MutableLiveData<Event<Unit>>() }
    val groupException: LiveData<Event<Unit>>
        get() = _groupException

    private val _contactMessage by lazy { MutableLiveData<List<RecentMessageBean>>() }
    val contactMessage: LiveData<List<RecentMessageBean>>
        get() = _contactMessage

    private val _contactException by lazy { MutableLiveData<Event<Unit>>() }
    val contactException: LiveData<Event<Unit>>
        get() = _contactException

    private var groupDisposable: Disposable? = null
    private var friendDisposable: Disposable? = null

    @SuppressLint("CheckResult")
    fun getGroupMessage() {
        groupDisposable = ChatDatabase.getInstance().recentMessageDao().roomRecentMsgList
                .throttleLast(300, TimeUnit.MILLISECONDS)
                .flatMap {
                    Collections.sort(it, RecentMsgComparator())
                    Flowable.just(it)
                }
                .distinctUntilChanged()
                .subscribeOn(Schedulers.from(AppExecutors.databaseThreadPool()))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    _groupMessage.value = it
                }, { _groupException.value = Event(Unit) })
    }

    @SuppressLint("CheckResult")
    fun getContactMessage() {
        friendDisposable = ChatDatabase.getInstance().recentMessageDao().friendRecentMsgList
                .throttleLast(300, TimeUnit.MILLISECONDS)
                .flatMap {
                    Collections.sort(it, RecentMsgComparator())
                    Flowable.just(it)
                }
                .distinctUntilChanged()
                .subscribeOn(Schedulers.from(AppExecutors.databaseThreadPool()))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    _contactMessage.value = it
                }, { _contactException.value = Event(Unit) })
    }

    fun getRoomInfoForCache(roomId: String): LiveData<Result<RoomInfoBean>> {
        val roomInfo = SingleLiveData<Result<RoomInfoBean>>()
        launch {
            roomInfo.value = withContext(Dispatchers.IO) {
                repository.getRoomInfoForCache(roomId)
            }
        }
        return roomInfo
    }

    fun getUserInfo(userId: String): LiveData<Result<FriendBean>> {
        val userInfo = SingleLiveData<Result<FriendBean>>()
        launch {
            userInfo.value = withContext(Dispatchers.IO) {
                repository.getUserInfo(userId)
            }
        }
        return userInfo
    }

    fun stickyOnTop(id: String, channelType: Int, sticky: Int) {
        launch {
            val result = withContext(Dispatchers.IO) {
                when (channelType) {
                    Chat33Const.CHANNEL_ROOM -> repository.roomStickyOnTop(id, sticky)
                    Chat33Const.CHANNEL_FRIEND -> repository.friendStickyOnTop(id, sticky)
                    else -> Result.Error(ApiException(AppError.IGNORE_ERROR))
                }
            }
            if (result.isSucceed()) {
                RoomUtils.run(Runnable {
                    when (channelType) {
                        Chat33Const.CHANNEL_ROOM -> ChatDatabase.getInstance().roomsDao().changeSticky(id, sticky)
                        Chat33Const.CHANNEL_FRIEND -> ChatDatabase.getInstance().friendsDao().changeSticky(id, sticky)
                    }
                    ChatDatabase.getInstance().recentMessageDao().changeSticky(id, sticky)
                })
            }
        }
    }

    fun setNoDisturb(id: String, channelType: Int, dnd: Int) {
        launch {
            val result = withContext(Dispatchers.IO) {
                when (channelType) {
                    Chat33Const.CHANNEL_ROOM -> repository.roomNoDisturb(id, dnd)
                    Chat33Const.CHANNEL_FRIEND -> repository.friendNoDisturb(id, dnd)
                    else -> Result.Error(ApiException(AppError.IGNORE_ERROR))
                }
            }
            if (result.isSucceed()) {
                RoomUtils.run(Runnable {
                    when (channelType) {
                        Chat33Const.CHANNEL_ROOM -> ChatDatabase.getInstance().roomsDao().changeDnd(id, dnd)
                        Chat33Const.CHANNEL_FRIEND -> ChatDatabase.getInstance().friendsDao().changeDnd(id, dnd)
                    }
                    ChatDatabase.getInstance().recentMessageDao().changeDisturb(id, dnd)
                })
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        groupDisposable?.dispose()
        friendDisposable?.dispose()
    }
}