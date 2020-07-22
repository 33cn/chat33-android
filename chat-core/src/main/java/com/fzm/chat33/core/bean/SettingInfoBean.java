package com.fzm.chat33.core.bean;

import java.io.Serializable;

/**
 * @author zhengjy
 * @since 2019/01/08
 * Description:用户设置信息
 */
public class SettingInfoBean implements Serializable {

    /**
     * 加好友是否需要验证，1需要 2不需要
     */
    public int needConfirm;
    /**
     * 加好友是否需要回答问题，1需要 2不需要
     */
    public int needAnswer;
    /**
     * 验证问题
     */
    public String question;
    /**
     * 验证答案
     */
    public String answer;
    /**
     * 别人邀请入群需要我确认
     */
    public int needConfirmInvite;
}
