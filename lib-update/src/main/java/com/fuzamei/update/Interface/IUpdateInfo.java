package com.fuzamei.update.Interface;

/**
 * @author zhengjy
 * @since 2018/08/03
 * Description:更新信息类需要实现的接口
 */
public interface IUpdateInfo {

    /**
     * 获取版本号
     */
    int getVersionCode();
    /**
     * 获取版本名
     */
    String getVersionName();

    /**
     * 获取更新描述信息
     */
    String getDescription();

    /**
     * 获取App下载链接
     */
    String getDownloadUrl();

    /**
     * 是否为强制更新
     */
    boolean isForceUpdate();

    /**
     * 获取安装包大小
     */
    long getApkSize();

    /**
     * 安装文件的md5
     */
    String getFileMD5();
}
