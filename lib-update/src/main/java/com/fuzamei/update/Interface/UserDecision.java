package com.fuzamei.update.Interface;

/**
 * @author zhengjy
 * @since 2018/08/03
 * Description:代表用户决定的类
 */
public interface UserDecision {

    /**
     * 用户决定是否升级
     *
     * @param update true表示同意升级
     */
    void update(boolean update);

    /**
     * 获取更新信息，作为决定依据
     *
     * @return 更新的内容
     */
    IUpdateInfo getUpdateInfo();
}
