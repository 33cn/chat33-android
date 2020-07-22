package com.fzm.chat33.core.bean;

import java.io.Serializable;

/**
 * @author zhengjy
 * @since 2019/06/14
 * Description:
 */
public class AdInfoBean implements Serializable {

    // 广告id
    public String id;
    // 广告名称
    public String name;
    // 广告播放时长
    public int duration;
    // 广告文件下载地址
    public String url;
    // 广告跳转链接
    public String link;

}
