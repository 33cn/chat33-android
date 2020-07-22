package com.fzm.chat33.core.repo

import android.text.TextUtils
import androidx.lifecycle.LiveData
import com.fuzamei.common.base.mvvm.SingleLiveData
import com.fuzamei.common.net.Result
import com.fuzamei.common.utils.RoomUtils
import com.fuzamei.componentservice.config.AppConfig
import com.fuzamei.componentservice.config.AppPreference
import com.fzm.chat33.core.db.ChatDatabase
import com.fzm.chat33.core.db.bean.*
import com.fzm.chat33.core.decentration.contract.UserAddress
import com.fzm.chat33.core.decentration.contract.UsersQuery
import com.fzm.chat33.core.global.Chat33Const
import com.fzm.chat33.core.global.LoginInfoDelegate
import com.fzm.chat33.core.manager.CipherManager
import com.fzm.chat33.core.source.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.lang.Exception
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.coroutineContext

/**
 * @author zhengjy
 * @since 2019/10/10
 * Description:
 */
@Singleton
class ContactsRepository @Inject constructor(
        private val friendData: FriendDataSource,
        private val userData: UserDataSource,
        private val groupData: GroupDataSource,
        private val localData: LocalContactDataSource,
        private val searchData: SearchDataSource,
        private val contractData: ContractDataSource,
        loginInfoDelegate: LoginInfoDelegate
) : FriendDataSource by friendData,
        GroupDataSource by groupData,
        LocalContactDataSource by localData,
        SearchDataSource by searchData,
        LoginInfoDelegate by loginInfoDelegate {

    override suspend fun getRoomList(type: Int): Result<RoomListBean.Wrapper> {
        val result = groupData.getRoomList(type)
        if(result.isSucceed()) {
            if (result.data().roomList != null) {
                val roomList = ArrayList<RoomListBean>()
                for (bean in result.data().roomList) {
                    if (!TextUtils.isEmpty(bean.id)) {
                        roomList.add(bean)
                        ChatDatabase.getInstance().recentMessageDao().changeDisableDeadline(bean.id, bean.disableDeadline)
                    }
                }
                ChatDatabase.getInstance().roomsDao().insert(roomList)
            }
        }
        return result
    }

    suspend fun updateRoomList(): Result<RoomListBean.Wrapper> {
        return getRoomList(3)
    }

    /**
     * 添加黑名单
     *
     * @param address 对方地址
     */
    suspend fun blockUserV2(address: String?): Result<String> {
        return contractData.handleTransaction { modifyBlock(listOf(address), 1) }
    }

    /**
     * 删除黑名单
     *
     * @param address 对方地址
     */
    suspend fun unblockUserV2(address: String?): Result<String> {
        return contractData.handleTransaction { modifyBlock(listOf(address), 2) }
    }

    /**
     * 添加好友
     *
     * @param address 对方地址
     */
    suspend fun addFriendV2(address: String?): Result<String> {
        return contractData.handleTransaction { modifyFriend(listOf(address), 1) }
    }

    /**
     * 删除好友
     *
     * @param address 对方地址
     */
    suspend fun deleteFriendV2(address: String?): Result<String> {
        return contractData.handleTransaction { modifyFriend(listOf(address), 2) }
    }

    /**
     * 递归从合约接口获取所有好友（黑名单）关系
     *
     * @param address   用户自己的地址
     * @param index     查询开始地址
     * @param block     是否是查询黑名单
     */
    private suspend fun getRelativeUsers(address: String?, index: String?, block: Boolean = false): List<UserAddress> {
        val count = AppConfig.PAGE_SIZE * 2
        val result = if (block) {
            contractData.getFriendList(UsersQuery.blockQuery(address ?: "", index ?: ""))
        } else {
            contractData.getBlockList(UsersQuery.friendsQuery(address ?: "", index ?: ""))
        }
        return if (result.isSucceed()) {
            if (result.data().friends.isNullOrEmpty()) {
                emptyList()
            } else {
                val list = arrayListOf<UserAddress>()
                list.addAll(result.data().friends)
                if (result.data().friends.size == count) {
                    list.addAll(getRelativeUsers(address, result.data().friends.last().friendAddress, block))
                }
                return list
            }
        } else {
            emptyList()
        }
    }

    override suspend fun getBlackList(): Result<FriendBean.Wrapper> {
        val blockAddress = getRelativeUsers(CipherManager.getAddress(), "", true).map {
            it.friendAddress
        }
        return if (blockAddress.isEmpty()) {
            Result.Success(FriendBean.Wrapper().apply { userList = listOf() })
        } else {
            // 用地址查询黑名单的信息
            getUserListFromBlockChain(blockAddress, true)
        }
    }

    override suspend fun getFriendList(type: Int, date: Date?, number: Int): Result<FriendBean.Wrapper> {
        return if (currentUser.value?.isChain == true) {
            // 首次通讯录上链后，从合约查询所有好友地址
            val friendAddress = getRelativeUsers(CipherManager.getAddress(), "").map {
                it.friendAddress
            }
            if (friendAddress.isEmpty()) {
                Result.Success(FriendBean.Wrapper().apply { userList = listOf() })
            } else {
                // 用好友地址查询好友的信息
                getUserListFromBlockChain(friendAddress)
            }
        } else {
            // 第一次登陆从服务器拉取原先的好友关系
            getFriendListFromServer(type, date, number)
        }
    }

    /**
     * 从中心化服务器上获取好友列表
     */
    private suspend fun getFriendListFromServer(type: Int, date: Date?, number: Int): Result<FriendBean.Wrapper> {
        val result = friendData.getFriendList(type, date, number)
        if(result.isSucceed()) {
            val friends = if (date != null) {
                val friends = ArrayList<FriendBean>()
                for (i in result.data().userList.indices) {
                    val friend = result.data().userList[i]
                    val id = friend.id
                    // 剔除已经删除的好友
                    if (friend.isDelete == 2) {
                        ChatDatabase.getInstance().friendsDao().delete(id)
                    } else {
                        friends.add(friend)
                    }
                }
                friends
            } else {
                result.data().userList
            }
            CoroutineScope(coroutineContext + Dispatchers.IO).launch {
                if (friends.isEmpty()) {
                    val upload = userData.backupChain()
                    updateInfo {
                        isChain = upload.isSucceed()
                    }
                } else {
                    val backup = contractData.handleTransaction {
                        modifyFriend(friends.map { it.address }, 1)
                    }
                    if (backup.isSucceed()) {
                        val upload = userData.backupChain()
                        updateInfo {
                            isChain = upload.isSucceed()
                        }
                    }
                }
            }
            ChatDatabase.getInstance().friendsDao().insert(friends)
        }
        return result
    }

    /**
     * 从区块链合约上获取好友（黑名单）列表
     *
     * @param address   要查询的地址列表
     * @param block     是否是黑名单地址列表
     */
    private suspend fun getUserListFromBlockChain(address: List<String>, block: Boolean = false): Result<FriendBean.Wrapper> {
        val result = friendData.getUsersByAddress(address)
        if (result.isSucceed()) {
            result.data().userList.forEach {
                if (block || isLocalBlock(it.id)) {
                    it.setIsBlocked(1)
                } else {
                    it.setIsBlocked(0)
                }
            }
            if (block) {
                // 查询的是黑名单则清除数据库中的黑名单数据，防止多端数据不一致
                ChatDatabase.getInstance().friendsDao().deleteBlocked()
            } else {
                // 查询的是好友则清除数据库中的好友数据，防止多端数据不一致
                ChatDatabase.getInstance().friendsDao().deleteFriends()
            }
            ChatDatabase.getInstance().friendsDao().insert(result.data().userList)
        }
        return result
    }

    suspend fun updateFriendsList() {
        getBlackList()
        getFriendList(3, null, -1)
        AppPreference.LAST_FRIEND_LIST_REFRESH = System.currentTimeMillis()
    }

    override suspend fun getUserInfo(id: String): Result<FriendBean> {
        val result = friendData.getUserInfo(id)
        if (result.isSucceed()) {
            val bean = result.data()
            bean.isFriend = if (isLocalFriend(bean.id)) {
                ChatDatabase.getInstance().friendsDao().insert(bean)
                1
            } else {
                0
            }
        }
        return result
    }

    suspend fun getRoomInfoForCache(roomId: String): Result<RoomInfoBean> {
        val result = groupData.getRoomInfo(roomId)
        result.dataOrNull()?.let {
            RoomUtils.run(Runnable {
                if (!it.id.isNullOrEmpty()) {
                    ChatDatabase.getInstance().infoCacheDao().insert(InfoCacheBean(it))
                }
            })
        }
        return result
    }

    fun getRoomInfoSync(roomId: String): LiveData<Result<RoomInfoBean>> {
        val result = SingleLiveData<Result<RoomInfoBean>>()
        GlobalScope.launch(Dispatchers.Main) {
            launch(Dispatchers.IO) {
                result.postValue(groupData.getRoomInfo(roomId))
            }
        }
        return result
    }

    override suspend fun getRoomInfo(roomId: String): Result<RoomInfoBean> {
        val result = groupData.getRoomInfo(roomId)
        result.dataOrNull()?.let {
            if (!it.id.isNullOrEmpty()) {
                // 将群列表信息存入本地数据库
                val roomListBean = RoomListBean(it)
                ChatDatabase.getInstance().roomsDao().insert(roomListBean)
                ChatDatabase.getInstance().recentMessageDao().changeDisableDeadline(roomListBean.id, roomListBean.disableDeadline)
            }
        }
        return result
    }

    suspend fun getRoomInfoByEnterGroup(roomId: String): Result<RoomInfoBean> {
        val result = groupData.getRoomInfo(roomId)
        result.dataOrNull()?.let {
            if (!it.id.isNullOrEmpty()) {
                // 将群列表信息存入本地数据库
                val roomListBean = RoomListBean(it)
                ChatDatabase.getInstance().roomsDao().insert(roomListBean)
                ChatDatabase.getInstance().recentMessageDao().changeDisableDeadline(roomListBean.id, roomListBean.disableDeadline)
                ChatDatabase.getInstance().recentMessageDao().markDelete(false, Chat33Const.CHANNEL_ROOM, it.id)
            }
        }
        return result
    }

    override suspend fun getRoomUsers(roomId: String): Result<RoomUserBean.Wrapper> {
        val result = groupData.getRoomUsers(roomId)
        if (result.isSucceed()) {
            try {
                val wrapper = result.data()
                for (bean in wrapper.userList) {
                    bean.roomId = roomId
                }
                ChatDatabase.getInstance().roomUserDao().deleteRoomUsers(roomId)
                ChatDatabase.getInstance().roomUserDao().insert(wrapper.userList)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        return result
    }

    fun searchContacts(keywords: String): List<Contact> {
        val matchList = ArrayList<Contact>()
        matchList.addAll(searchData.searchFriendsWithBlocked(keywords))
        matchList.addAll(searchData.searchGroups(keywords))
        return matchList
    }
}