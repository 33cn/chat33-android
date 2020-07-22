package com.fzm.chat33.core.bean;

import com.fzm.chat33.core.response.MsgSocketResponse;

import java.io.Serializable;

/**
 * @author zhengjy
 * @since 2019/08/02
 * Description:同步序列信号
 */
public class SyncSignal implements Serializable {

    public int eventType;
    public MsgSocketResponse msg;
    public boolean complete;

    public SyncSignal(int eventType, MsgSocketResponse msg, boolean complete) {
        this.eventType = eventType;
        this.msg = msg;
        this.complete = complete;
    }
}
