package com.fuzamei.update.Interface;

/**
 * @author zhengjy
 * @since 2018/08/03
 * Description:
 */
public interface OnCheckUpdateListener {

    /**
     * 检查更新结果回调
     *
     * @param needToUpdate  是否需要更新
     * @param force         是否强制更新
     * @param decision      用户选择
     */
    void onCheckUpdateResult(boolean needToUpdate, boolean force, UserDecision decision);
}
