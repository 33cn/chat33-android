package com.fzm.chat33.core.bean;

import java.io.Serializable;

/**
 * @author zhengjy
 * @since 2019/01/31
 * Description:好友备注图片
 */
public class RemarkImage implements Serializable {

    public String showUrl;
    public String ossUrl;

    public RemarkImage() {

    }

    public RemarkImage(String showUrl, String ossUrl) {
        this.showUrl = showUrl;
        this.ossUrl = ossUrl;
    }
}
