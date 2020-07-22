package com.fzm.chat33.core.bean.param;

import java.io.Serializable;

/**
 * @author zhengjy
 * @since 2018/10/17
 * Description:添加好友申请请求参数
 */
public class AddFriendParam implements Serializable {

    /**
     * 对方id
     */
    private String id;
    /**
     * 对方地址
     */
    public String address;
    /**
     * 备注
     */
    private String remark;
    /**
     * 申请理由
     */
    private String reason;
    /**
     * 验证问题
     */
    private String answer;
    /**
     * 加好友途径，1搜索 2扫码 3通过好友分享 4通过群
     */
    private int sourceType;
    /**
     * 如果是通过群加的 就填群id 通过好友分享 填好友id
     */
    private String sourceId;

    public AddFriendParam(String id, String reason, int sourceType, String sourceId) {
        this(id, null, reason, null, sourceType, sourceId);
    }

    public AddFriendParam(String id, String reason, String answer, int sourceType, String sourceId) {
        this(id, null, reason, answer, sourceType, sourceId);
    }

    public AddFriendParam(String id, String remark, String reason, String answer, int sourceType, String sourceId) {
        this.id = id;
        this.remark = remark;
        this.reason = reason;
        this.answer = answer;
        this.sourceType = sourceType;
        this.sourceId = sourceId;
    }
}
