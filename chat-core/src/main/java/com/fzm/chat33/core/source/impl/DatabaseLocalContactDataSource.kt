package com.fzm.chat33.core.source.impl

import android.annotation.SuppressLint
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.fuzamei.common.bus.LiveBus
import com.fuzamei.common.executor.AppExecutors
import com.fuzamei.componentservice.app.BusEvent
import com.fzm.chat33.core.bean.comparator.PinyinComparator
import com.fzm.chat33.core.db.ChatDatabase
import com.fzm.chat33.core.db.bean.FriendBean
import com.fzm.chat33.core.db.bean.RoomListBean
import com.fzm.chat33.core.source.*
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import kotlin.collections.HashMap

class DatabaseLocalContactDataSource
@Inject
@SuppressLint("CheckResult") constructor() : LocalContactDataSource {

    private val _userMap = HashMap<String, FriendBean>()
    private val _roomMap = HashMap<String, RoomListBean>()

    private val _updateFriend by lazy { MutableLiveData<List<FriendBean>>() }
    override val updateFriend: LiveData<List<FriendBean>>
        get() = _updateFriend

    private val _updateBlocked by lazy { MutableLiveData<List<FriendBean>>() }
    override val updateBlocked: LiveData<List<FriendBean>>
        get() = _updateBlocked

    private val _updateRoom by lazy { MutableLiveData<List<RoomListBean>>() }
    override val updateRoom: LiveData<List<RoomListBean>>
        get() = _updateRoom

    private val pinyinComparator = PinyinComparator()

    private var allFriendsAndBlocked: Disposable? = null
    private var allGroups: Disposable? = null

    init {
        LiveBus.of(BusEvent::class.java).loginEvent().observeForever { login ->
            if (login) {
                allFriendsAndBlocked = ChatDatabase.getInstance().friendsDao().allFriendsWithBlocked
                        .debounce(300, TimeUnit.MILLISECONDS)
                        .distinctUntilChanged()
                        .subscribeOn(Schedulers.from(AppExecutors.databaseThreadPool()))
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe {
                            val blockList = arrayListOf<FriendBean>()
                            val userList = arrayListOf<FriendBean>()
                            _userMap.clear()
                            for (bean in it) {
                                _userMap[bean.id] = bean
                                if (bean.isBlocked()) {
                                    blockList.add(bean)
                                } else {
                                    userList.add(bean)
                                }
                            }
                            Collections.sort(userList, pinyinComparator)
                            Collections.sort(blockList, pinyinComparator)
                            _updateFriend.postValue(userList)
                            _updateBlocked.postValue(blockList)
                        }
                allGroups = ChatDatabase.getInstance().roomsDao().allRooms
                        .debounce(300, TimeUnit.MILLISECONDS)
                        .distinctUntilChanged()
                        .subscribeOn(Schedulers.from(AppExecutors.databaseThreadPool()))
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe {
                            _roomMap.clear()
                            for (bean in it) {
                                _roomMap[bean.id] = bean
                            }
                            Collections.sort(it, pinyinComparator)
                            _updateRoom.postValue(it)
                        }
            } else {
                allFriendsAndBlocked?.dispose()
                allGroups?.dispose()
                _userMap.clear()
                _roomMap.clear()
                _updateFriend.value = arrayListOf()
                _updateBlocked.value = arrayListOf()
                _updateRoom.value = arrayListOf()
            }
        }
    }

    override fun getLocalFriendList(): List<FriendBean> {
        return _userMap.asSequence().map { it.value }.filter { !it.isBlocked() }.sortedWith(pinyinComparator).toList()
    }

    override fun getLocalFriendById(userId: String?): FriendBean? {
        return _userMap[userId]
    }

    override fun isLocalFriend(userId: String?): Boolean {
        return _userMap[userId] != null
    }

    override fun getLocalRoomList(): List<RoomListBean> {
        return _roomMap.asSequence().map { it.value }.sortedWith(pinyinComparator).toList()
    }

    override fun getLocalRoomById(roomId: String?): RoomListBean? {
        return _roomMap[roomId]
    }

    override fun isLocalBlock(userId: String?): Boolean {
        return _userMap[userId] != null && _userMap[userId]!!.isBlocked()
    }

    private object LocalContactDataSourceHolder {
        val instance: LocalContactDataSource = DatabaseLocalContactDataSource()
    }

    companion object {
        @JvmStatic
        fun get(): LocalContactDataSource {
            return LocalContactDataSourceHolder.instance
        }
    }
}
