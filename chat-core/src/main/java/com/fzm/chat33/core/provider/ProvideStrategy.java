package com.fzm.chat33.core.provider;

/**
 * @author zhengjy
 * @since 2018/12/21
 * Description:不同对象获取头像和昵称的方法
 */
public interface ProvideStrategy<T> {

    int FROM_MEMO = 1001;

    int FROM_DATABASE = 1002;

    int FROM_NETWORK_OR_TEMP_CACHE = 1003;

    T get();

    void load(OnFindInfoListener<T> listener);

    void loadFromMemory(OnFindInfoListener<T> listener);

    void loadFromDatabase(OnFindInfoListener<T> listener);

    void loadFromNetwork(OnFindInfoListener<T> listener);
}
