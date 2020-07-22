package com.fuzamei.componentservice.event;

/**
 * 创建日期：2018/8/31 on 16:13
 * 描述:用户昵称刷新事件
 * 作者:wdl
 */
public class NicknameRefreshEvent extends BaseEvent {
    public String id;
    public String nickname;
    public boolean updateSelf = true;

    public NicknameRefreshEvent(String nickname) {
        this.nickname = nickname;
    }

    public NicknameRefreshEvent(String id, String nickname) {
        this.id = id;
        this.nickname = nickname;
    }

    public NicknameRefreshEvent(String id, String nickname, boolean updateSelf) {
        this.id = id;
        this.nickname = nickname;
        this.updateSelf = updateSelf;
    }
}
