package com.fzm.chat33.core.bean;

import java.io.Serializable;

/**
 * @author zhengjy
 * @since 2018/11/13
 * Description:找币邀请分享信息
 */
public class InviteBean implements Serializable {

    // 邀请码
    public String code;
    // 邀请url
    public String inviteUrl;
    // 邀请注册地址
    public String minviteUrl;
    // 推广人数
    public int inviteNum;
    // 推广获取的总奖励
    public String totalAward;
}
