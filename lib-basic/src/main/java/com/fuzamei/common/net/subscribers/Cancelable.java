package com.fuzamei.common.net.subscribers;

/**
 * @author zhengjy
 * @since 2018/07/27
 * Description:
 */
public interface Cancelable {

    /**
     * 用于取消操作
     */
    void cancel();
}
