package com.fzm.chat33.core.bean.param;

import java.io.Serializable;

/**
 * @author zhengjy
 * @since 2018/10/17
 * Description:
 */
public class ChatGroupParam implements Serializable {
    public int groupStatus;
    public String groupName;
    public long startTime;
    public long endTime;

    public ChatGroupParam(int groupStatus, String groupName, long startTime, long endTime) {
        this.groupStatus = groupStatus;
        this.groupName = groupName;
        this.startTime = startTime;
        this.endTime = endTime;
    }
}
