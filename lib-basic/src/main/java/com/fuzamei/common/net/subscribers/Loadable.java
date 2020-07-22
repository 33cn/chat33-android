package com.fuzamei.common.net.subscribers;

/**
 * @author zhengjy
 * @since 2018/07/27
 * Description:
 */
public interface Loadable {

    /**
     * 显示加载框
     *
     * @param cancelable    加载框是否可以取消
     */
    void loading(boolean cancelable);

    /**
     * 关闭加载框
     */
    void dismiss();
}
