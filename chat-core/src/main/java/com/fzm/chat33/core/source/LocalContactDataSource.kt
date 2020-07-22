package com.fzm.chat33.core.source

import androidx.lifecycle.LiveData
import com.fzm.chat33.core.db.bean.FriendBean
import com.fzm.chat33.core.db.bean.RoomListBean

/**
 *获取本地的好友或群信息
 */
interface LocalContactDataSource {

    /**
     * 本地数据库好友列表更新
     */
    val updateFriend: LiveData<List<FriendBean>>

    /**
     * 本地数据库黑名单列表更新
     */
    val updateBlocked: LiveData<List<FriendBean>>

    /**
     * 本地数据库群列表更新
     */
    val updateRoom: LiveData<List<RoomListBean>>

    /**
     * 获取本地内存中的好友列表
     */
    fun getLocalFriendList(): List<FriendBean>

    /**
     * 获取本地内存中指定的好友信息
     *
     * @param userId    用户id
     */
    fun getLocalFriendById(userId: String?): FriendBean?

    /**
     * 判断是否是本地好友
     *
     * @param userId    用户id
     */
    fun isLocalFriend(userId: String?) : Boolean

    /**
     * 获取本地内存中的群列表
     */
    fun getLocalRoomList(): List<RoomListBean>

    /**
     * 获取本地内存中指定的群信息
     *
     * @param roomId    群id
     */
    fun getLocalRoomById(roomId: String?): RoomListBean?

    /**
     * 判断是否是本地黑名单
     *
     * @param userId    用户id
     */
    fun isLocalBlock(userId: String?) : Boolean
}