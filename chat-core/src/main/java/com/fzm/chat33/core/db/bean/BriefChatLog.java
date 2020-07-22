package com.fzm.chat33.core.db.bean;

import com.fzm.chat33.core.response.BaseResponse;

/**
 * @author zhengjy
 * @since 2018/12/25
 * Description:
 */
public class BriefChatLog extends BaseResponse {

    public int msgType;
    public String logId;
    public long datetime;
    public ChatFile msg;
    public SenderInfo senderInfo;
    // 聊天消息列表中是否显示时间
    public transient boolean showTime;

}
