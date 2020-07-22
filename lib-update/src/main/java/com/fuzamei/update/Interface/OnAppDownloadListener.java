package com.fuzamei.update.Interface;

import java.io.File;

/**
 * @author zhengjy
 * @since 2018/08/02
 * Description:
 */
public interface OnAppDownloadListener {

    /**
     * 开始下载
     */
    void onStart();

    /**
     * 下载进度
     */
    void onProgress(float progress);

    /**
     * 下载成功
     *
     * @param force 强制更新
     * @param file App安装文件
     */
    void onSuccess(boolean force, File file);

    /**
     * 下载失败
     *
     * @param force 强制更新
     * @param e     错误，异常
     */
    void onFail(boolean force, Throwable e);
}
