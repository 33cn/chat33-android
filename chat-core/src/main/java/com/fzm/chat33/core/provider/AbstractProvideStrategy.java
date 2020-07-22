package com.fzm.chat33.core.provider;

/**
 * @author zhengjy
 * @since 2018/12/21
 * Description:
 */
public abstract class AbstractProvideStrategy<T> implements ProvideStrategy<T> {

    protected String id;

    @Override
    public T get() {
        return null;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void load(final OnFindInfoListener listener) {
        loadFromMemory(new OnFindInfoListener() {

            @Override
            public void onFindInfo(Object data, int place) {
                listener.onFindInfo(data, FROM_MEMO);
            }

            @Override
            public void onNotExist() {
                loadFromDatabase(new OnFindInfoListener() {
                    @Override
                    public void onFindInfo(Object data, int place) {
                        listener.onFindInfo(data, FROM_DATABASE);
                    }

                    @Override
                    public void onNotExist() {
                        loadFromNetwork(new OnFindInfoListener() {
                            @Override
                            public void onFindInfo(Object data, int place) {
                                listener.onFindInfo(data, FROM_NETWORK_OR_TEMP_CACHE);
                            }

                            @Override
                            public void onNotExist() {
                                listener.onNotExist();
                            }
                        });
                    }
                });
            }
        });
    }
}
