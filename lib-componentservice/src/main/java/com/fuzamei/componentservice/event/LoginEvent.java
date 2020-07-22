package com.fuzamei.componentservice.event;

/**
 * 创建日期：2018/9/3 on 15:17
 * 描述:
 * 作者:wdl
 */
public class LoginEvent extends BaseEvent{
    public boolean login;

    public LoginEvent(boolean login) {
        this.login = login;
    }
}
