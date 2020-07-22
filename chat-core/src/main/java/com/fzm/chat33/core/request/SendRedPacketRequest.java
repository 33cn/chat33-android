package com.fzm.chat33.core.request;

import java.io.Serializable;

/**
 * 创建日期：2018/8/7 on 16:31
 * 描述:
 * 作者:wdl
 */
public class SendRedPacketRequest extends BaseRequest {

    public int cType;// 0：私聊红包 1：群聊红包

    public String toId;// 接收者id或群id

    public String toUsers;// 如果不为空，则只有接收者可领取，且接受者人数须等于红包个数

    public int coin;// 币种id  BTY:3

    public String coinName;// 币种名

    public int type;// 1 拼手气红包 2 普通红包

    public double amount;// 金额

    public int size = 1;// 红包个数

    public String remark;// 红包备注

    public Ext ext;// 平台拓展参数

    public static class Ext implements Serializable {
        public String pay_password;

        public Ext(String pay_password) {
            this.pay_password = pay_password;
        }
    }
}
