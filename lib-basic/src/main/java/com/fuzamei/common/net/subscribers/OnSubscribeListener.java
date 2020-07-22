package com.fuzamei.common.net.subscribers;

/**
 * @author zhengjy
 * @since 2018/07/27
 * Description:
 */
public interface OnSubscribeListener<T> {

    void onSuccess(T t);

    void onError(Throwable t);
}
