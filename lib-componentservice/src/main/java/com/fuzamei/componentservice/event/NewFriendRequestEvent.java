package com.fuzamei.componentservice.event;

/**
 * @author zhengjy
 * @since 2018/11/02
 * Description:新的好友请求事件
 */
public class NewFriendRequestEvent extends BaseEvent {

    public String id;
    public String avatar;
    // 清空新的请求
    public boolean clear;

    public NewFriendRequestEvent(String id, String avatar, boolean clear) {
        this.id = id;
        this.avatar = avatar;
        this.clear = clear;
    }

    public NewFriendRequestEvent(String avatar, boolean clear) {
        this.id = id;
        this.avatar = avatar;
        this.clear = clear;
    }
}
