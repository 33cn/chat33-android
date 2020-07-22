package com.fuzamei.componentservice.app

import com.fuzamei.common.bus.LiveBus
import com.fuzamei.common.bus.ChatEventData
import com.fuzamei.componentservice.event.ChangeTabEvent
import com.fuzamei.componentservice.event.NewFriendRequestEvent
import com.fuzamei.componentservice.event.NicknameRefreshEvent

/**
 * @author zhengjy
 * @since 2019/09/30
 * Description:[LiveBus]发送的事件类型
 */
interface BusEvent {

    /**
     * 主页面tab切换事件
     */
    fun changeTab(): ChatEventData<ChangeTabEvent>

    /**
     * 用户头像刷新事件
     *
     * @param <String> 自己头像的链接
     */
    fun imageRefresh(): ChatEventData<String>

    /**
     * 新的好友请求事件
     */
    fun newFriends(): ChatEventData<NewFriendRequestEvent>

    /**
     * 昵称刷新事件
     */
    fun nicknameRefresh(): ChatEventData<NicknameRefreshEvent>

    /**
     * 刷新通讯录列表事件
     * 1:刷新好友列表     2:刷新群列表     3:刷新黑名单列表
     */
    fun contactsRefresh(): ChatEventData<Int>

    /**
     * 登录/注销事件
     */
    fun loginEvent(): ChatEventData<Boolean>

    /**
     * 登录过期事件
     */
    fun loginExpire(): ChatEventData<Any>
}