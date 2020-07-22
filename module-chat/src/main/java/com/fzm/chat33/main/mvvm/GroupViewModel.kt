package com.fzm.chat33.main.mvvm

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.fuzamei.common.bus.LiveBus
import com.fuzamei.common.ext.request
import com.fuzamei.common.ext.result
import com.fuzamei.common.ext.start
import com.fuzamei.common.utils.RoomUtils
import com.fuzamei.componentservice.app.BusEvent
import com.fuzamei.componentservice.app.LoadingViewModel
import com.fuzamei.componentservice.event.NicknameRefreshEvent
import com.fzm.chat33.core.bean.GroupNotice
import com.fzm.chat33.core.bean.UidSearchBean
import com.fzm.chat33.core.bean.param.JoinGroupParam
import com.fzm.chat33.core.db.ChatDatabase
import com.fzm.chat33.core.db.bean.RoomInfoBean
import com.fzm.chat33.core.global.Chat33Const
import com.fzm.chat33.core.global.LoginInfoDelegate
import com.fzm.chat33.core.utils.UserInfoPreference
import com.fzm.chat33.core.repo.ChatRepository
import com.fzm.chat33.core.repo.ContactsRepository
import javax.inject.Inject

/**
 * @author zhengjy
 * @since 2019/10/11
 * Description:
 */
class GroupViewModel @Inject constructor(
        private val contactData: ContactsRepository,
        private val chatData: ChatRepository,
        private val loginInfoDelegate: LoginInfoDelegate
) : LoadingViewModel(), LoginInfoDelegate by loginInfoDelegate {

    private val RESULT = Any()

    private val roomInfoDao
        get() = ChatDatabase.getInstance().roomInfoDao()

    private val roomsDao
        get() = ChatDatabase.getInstance().roomsDao()

    private val recentMessageDao
        get() = ChatDatabase.getInstance().recentMessageDao()

    private val chatMessageDao
        get() = ChatDatabase.getInstance().chatMessageDao()

    private val _roomInfo by lazy { MutableLiveData<RoomInfoBean>() }
    val roomInfo: LiveData<RoomInfoBean>
        get() = _roomInfo

    private val _stickyTop by lazy { MutableLiveData<Boolean>() }
    val stickyTop: LiveData<Boolean>
        get() = _stickyTop

    private val _noDisturb by lazy { MutableLiveData<Boolean>() }
    val noDisturb: LiveData<Boolean>
        get() = _noDisturb

    private val _permissionResult by lazy { MutableLiveData<Any>() }
    val permissionResult: LiveData<Any>
        get() = _permissionResult

    private val _recordPermission by lazy { MutableLiveData<Boolean>() }
    val recordPermission: LiveData<Boolean>
        get() = _recordPermission

    private val _muteResult by lazy { MutableLiveData<Int>() }
    val muteResult: LiveData<Int>
        get() = _muteResult

    private val _memberLevel by lazy { MutableLiveData<Int>() }
    val memberLevel: LiveData<Int>
        get() = _memberLevel

    private val _deleteResult by lazy { MutableLiveData<Any>() }
    val deleteResult: LiveData<Any>
        get() = _deleteResult

    private val _quitResult by lazy { MutableLiveData<Any>() }
    val quitResult: LiveData<Any>
        get() = _quitResult

    // 群公告列表
    private val _noticeList by lazy { MutableLiveData<GroupNotice.Wrapper>() }
    val noticeList: LiveData<GroupNotice.Wrapper>
        get() = _noticeList

    // 删除群公告
    private val _deleteNotice by lazy { MutableLiveData<Int>() }
    val deleteNotice: LiveData<Int>
        get() = _deleteNotice

    // 发布群公告
    private val _publishNotice by lazy { MutableLiveData<Any>() }
    val publishNotice: LiveData<Any>
        get() = _publishNotice

    private val _memberName by lazy { MutableLiveData<Any>() }
    val memberName: LiveData<Any>
        get() = _memberName

    // 通过uid找到群聊
    private val _searchRoom by lazy { MutableLiveData<UidSearchBean>() }
    val searchRoom: LiveData<UidSearchBean>
        get() = _searchRoom

    // 加群请求
    private val _joinRoom by lazy { MutableLiveData<Any>() }
    val joinRoom: LiveData<Any>
        get() = _joinRoom

    /**
     * 获取群信息
     */
    fun getRoomInfo(roomId: String) {
        start {
            loading()
        }.request {
            contactData.getRoomInfo(roomId)
        }.result({
            _roomInfo.value = it
        }, {
            _roomInfo.value = null
        }, {
            dismiss()
        })
    }

    /**
     * 群聊免打扰
     *
     * @param dnd    1：不提示群消息  2：提示群消息
     */
    fun setDND(roomId: String, dnd: Int) {
        request {
            contactData.roomNoDisturb(roomId, dnd)
        }.result({
            _noDisturb.value = null
            RoomUtils.run(Runnable {
                roomsDao.changeDnd(roomId, dnd)
                recentMessageDao.changeDisturb(roomId, dnd)
            })
        }, {
            _noDisturb.value = dnd != 1
        })
    }

    /**
     * 群聊置顶
     *
     * @param sticky    1：置顶  2：不置顶
     */
    fun stickyOnTop(roomId: String, sticky: Int) {
        request {
            contactData.roomStickyOnTop(roomId, sticky)
        }.result({
            _stickyTop.value = null
            RoomUtils.run(Runnable {
                roomsDao.changeSticky(roomId, sticky)
                recentMessageDao.changeSticky(roomId, sticky)
            })
        }, {
            _stickyTop.value = sticky != 1
        })
    }

    /**
     * 设置群的权限
     *
     * @param canAddFriend      群成员之间是否可添加好友
     * @param joinPermission    是否允许加群
     * @param recordPermission  是否允许新成员查看群历史记录
     */
    fun setPermission(roomId: String, canAddFriend: Int, joinPermission: Int, recordPermission: Int) {
        request {
            contactData.setPermission(roomId, canAddFriend, joinPermission, recordPermission)
        }.result({
            if (canAddFriend == 0 && joinPermission == 0) {
                _recordPermission.value = null
            } else {
                _permissionResult.value = RESULT
            }
        }, {
            if (canAddFriend == 0 && joinPermission == 0) {
                _recordPermission.value = recordPermission != 1
            } else {
                _permissionResult.value = null
            }
        })
    }

    /**
     * 设置群成员等级
     *
     * @param level     1：普通成员  2：管理员  3：群主
     */
    fun setRoomUserLevel(roomId: String, userId: String, level: Int) {
        start {
            loading()
        }.request {
            contactData.setRoomUserLevel(roomId, userId, level)
        }.result({
            _memberLevel.value = level
        }, {
            _memberLevel.value = null
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
     * 解散群聊
     */
    fun deleteRoom(roomId: String) {
        start {
            loading()
        }.request {
            contactData.deleteRoom(roomId)
        }.result({
            _deleteResult.value = RESULT
            RoomUtils.run(Runnable {
                recentMessageDao.deleteMessage(Chat33Const.CHANNEL_ROOM, roomId)
                roomsDao.delete(roomId)
            })
        }, {
            _deleteResult.value = null
        }, {
            dismiss()
        })
    }

    /**
     * 退出群聊
     */
    fun quitRoom(roomId: String) {
        start {
            loading()
        }.request {
            contactData.quitRoom(roomId)
        }.result({
            _quitResult.value = RESULT
            UserInfoPreference.getInstance().setBooleanPref("${UserInfoPreference.NO_MORE_CHAT_LOG}${Chat33Const.CHANNEL_ROOM}-${roomId}", false)
            RoomUtils.run(Runnable {
                chatMessageDao.deleteGroupMessage(roomId)
                recentMessageDao.deleteMessage(Chat33Const.CHANNEL_ROOM, roomId)
                roomsDao.delete(roomId)
            })
        }, {
            _quitResult.value = null
        }, {
            dismiss()
        })
    }

    /**
     * 获取群公告列表
     *
     * @param roomId    群id
     * @param startId   从哪条公告开始拉取，null代表从最新开始拉取
     * @param number    拉取公告条数
     */
    fun getGroupNoticeList(roomId: String, startId: String?, number: Int) {
        start {
            loading()
        }.request {
            contactData.getGroupNoticeList(roomId, startId, number)
        }.result({
            _noticeList.value = it
        }, {
            _noticeList.value = null
        }, {
            dismiss()
        })
    }

    /**
     * 发布群公告
     *
     * @param roomId    群id
     * @param content   群公告内容
     */
    fun publishNotice(roomId: String, content: String) {
        start {
            loading()
        }.request {
            contactData.publishNotice(roomId, content)
        }.result({
            _publishNotice.value = RESULT
        }, {
            _publishNotice.value = null
        }, {
            dismiss()
        })
    }

    /**
     * 撤回消息，在这里用户删除群公告
     *
     * @param logId     要撤回的消息logId
     * @param type      消息类型:1：群消息；2：好友消息
     */
    fun revokeMessage(logId: String, type: Int, position: Int) {
        start {
            loading()
        }.request {
            chatData.revokeMessage(logId, type)
        }.result({
            _deleteNotice.value = position
        }, {
            _deleteNotice.value = null
        }, {
            dismiss()
        })
    }

    /**
     * 设置自己在群内的昵称
     *
     * @param roomId    群id
     * @param nickname  群中昵称
     */
    fun setMemberNickname(roomId: String, nickname: String) {
        start {
            loading()
        }.request {
            contactData.setMemberNickname(roomId, nickname)
        }.result({
            _memberName.value = RESULT
            RoomUtils.run(Runnable {
                ChatDatabase.getInstance().roomUserDao()
                        .updateNickname(roomId, getUserId(), nickname)
                LiveBus.of(BusEvent::class.java).nicknameRefresh()
                        .setValue(NicknameRefreshEvent(getUserId(), nickname))
            })
        }, {
            _memberName.value = null
        }, {
            dismiss()
        })
    }

    /**
     * 通过uid搜索群聊（或者用户）
     *
     * @param markId 群聊或用户的uid
     */
    fun searchByUid(markId: String) {
        request {
            contactData.searchByUid(markId)
        }.result {
            _searchRoom.value = it
        }
    }

    /**
     * 发起入群申请
     *
     * @param param 入群申请参数
     */
    fun joinRoomApply(param: JoinGroupParam) {
        start {
            loading()
        }.request {
            contactData.joinRoomApply(param)
        }.result(onSuccess = {
            _joinRoom.value = it
        }, onComplete = {
            dismiss()
        })
    }
}