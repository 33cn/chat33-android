package com.fzm.chat33.core.request;

/**
 * @author zhengjy
 * @since 2018/12/27
 * Description:客户端开始同步消息请求
 */
public class SyncMessageRequest extends BaseRequest {

    private int eventType;
    private long time;

    public SyncMessageRequest(long time) {
        this.eventType = 42;
        this.time = time;
    }
}
