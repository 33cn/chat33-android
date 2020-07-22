package com.fzm.chat33.core.bean;

import java.io.Serializable;

/**
 * @author zhengjy
 * @since 2018/12/29
 * Description:
 */
public class NotificationBean implements Serializable {

    public String id;
    public String title;
    public String content;
    public int channelType;
    public int count;

    public NotificationBean(String id, String title, String content, int channelType, int count) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.channelType = channelType;
        this.count = count;
    }
}
