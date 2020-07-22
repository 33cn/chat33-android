package com.fuzamei.common.net.subscribers;

/**
 * @author zhengjy
 * @since 2018/07/27
 * Description:
 */
public abstract class OnSuccessListener<T> implements OnSubscribeListener<T> {

    @Override
    public void onError(Throwable t) {
        t.printStackTrace();
    }
}
