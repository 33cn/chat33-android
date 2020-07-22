package com.fzm.chat33.bean;

import com.fuzamei.update.Interface.IUpdateInfo;

import java.io.Serializable;

/**
 * @author zhengjy
 * @since 2018/11/13
 * Description:服务器返回的最新apk的信息
 */
public class UpdateApkInfo implements IUpdateInfo {

    public int versionCode;
    public String versionName;
    public String description;
    public String url;
    public boolean forceUpdate;
    public long size;
    public String md5;

    @Override
    public int getVersionCode() {
        return versionCode;
    }

    @Override
    public String getVersionName() {
        return versionName;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public String getDownloadUrl() {
        return url;
    }

    @Override
    public boolean isForceUpdate() {
        return forceUpdate;
    }

    @Override
    public long getApkSize() {
        return size;
    }

    @Override
    public String getFileMD5() {
        return md5;
    }
}
