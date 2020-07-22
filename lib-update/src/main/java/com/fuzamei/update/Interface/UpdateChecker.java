package com.fuzamei.update.Interface;

import androidx.annotation.WorkerThread;

import com.alibaba.android.arouter.facade.template.IProvider;

/**
 * @author zhengjy
 * @since 2018/08/07
 * Description:更新检查器
 */
public interface UpdateChecker extends IProvider {

    /**
     * 检查应用更新，要以同步方式请求网络
     *
     * @return  更新信息
     */
    @WorkerThread
    IUpdateInfo checkUpdate() throws Exception;
}
