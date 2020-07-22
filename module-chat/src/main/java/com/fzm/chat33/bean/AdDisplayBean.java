package com.fzm.chat33.bean;

import java.io.Serializable;

/**
 * @author zhengjy
 * @since 2019/06/13
 * Description:广告展示信息
 */
public class AdDisplayBean implements Serializable {

    /**
     * 点击广告跳转链接
     */
    public String link;
    /**
     * 广告类型：
     * 0：图片  1：视频  （2：gif）
     */
    public int type;
    /**
     * 广告显示时长
     */
    public int duration;
    /**
     * 广告文件本地路径
     */
    public String path;

    public AdDisplayBean(String link, int type, int duration, String path) {
        this.link = link;
        this.type = type;
        this.duration = duration;
        this.path = path;
    }
}
