package com.fzm.chat33.core.request;

/**
 * @author zhengjy
 * @since 019/08/02
 * Description:客户端确认本地消息
 */
public class CheckMessageRequest extends BaseRequest {

    private int eventType;
    // 本次ack的第一条消息记录时间
    private long begin;
    // 本次ack的最后一条消息记录时间
    private long end;
    private int total;

    public CheckMessageRequest(long begin, long end, int total) {
        this.eventType = 45;
        this.begin = begin;
        this.end = end;
        this.total = total;
    }
}
