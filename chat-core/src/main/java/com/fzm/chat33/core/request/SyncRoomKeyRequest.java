package com.fzm.chat33.core.request;

/**
 * @author zhengjy
 * @since 2019/08/02
 * Description:客户端开始同步群密钥
 */
public class SyncRoomKeyRequest extends BaseRequest {

    private int eventType;
    private long datetime;

    public SyncRoomKeyRequest(long time) {
        this.eventType = 27;
        this.datetime = time;
    }
}
