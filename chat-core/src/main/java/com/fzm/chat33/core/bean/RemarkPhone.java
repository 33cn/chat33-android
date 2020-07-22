package com.fzm.chat33.core.bean;

import java.io.Serializable;

/**
 * @author zhengjy
 * @since 2019/01/30
 * Description:好友备注手机
 */
public class RemarkPhone implements Serializable {

    public String remark;
    public String phone;

    public RemarkPhone() {

    }

    public RemarkPhone(String remark, String phone) {
        this.remark = remark;
        this.phone = phone;
    }
}
