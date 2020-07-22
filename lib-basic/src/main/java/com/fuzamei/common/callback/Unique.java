package com.fuzamei.common.callback;

/**
 * @author zhengjy
 * @since 2018/10/23
 * Description:
 */
public interface Unique {

    /**
     * 唯一标识id
     * @return
     */
    String getId();

    /**
     * 用于判断内容是否相同
     * @return
     */
    int hashCode();
}
