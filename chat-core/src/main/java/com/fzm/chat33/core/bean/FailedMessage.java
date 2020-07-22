package com.fzm.chat33.core.bean;

import java.io.Serializable;

/**
 * @author zhengjy
 * @since 2019/01/09
 * Description:发送失败消息
 */
@Deprecated
public class FailedMessage implements Serializable {

    /**
     * 发送的时间
     */
    public long sendTime;
    /**
     * 完整消息内容
     */
    public String content;

    public FailedMessage(long sendTime, String content) {
        this.sendTime = sendTime;
        this.content = content;
    }

    /**
     * 如果消息是在15s内发送失败的，则连接完成后自动重发
     *
     * @return  是否需要重发
     */
    public boolean shouldResend() {
        return System.currentTimeMillis() - sendTime < 15 * 1000L;
    }
}
