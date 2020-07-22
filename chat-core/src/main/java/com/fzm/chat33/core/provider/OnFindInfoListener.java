package com.fzm.chat33.core.provider;

/**
 * @author zhengjy
 * @since 2018/12/21
 * Description:
 */
public interface OnFindInfoListener<T> {

    /**
     * 信息获取成功
     *
     * @param data      信息
     * @param place     找到信息的途径
     */
    void onFindInfo(T data, int place);

    /**
     * 信息获取失败，由上层负责显示默认信息
     */
    void onNotExist();
}
