package com.fzm.chat33.core.bean.param;

import java.io.Serializable;

/**
 * @author zhengjy
 * @since 2019/1/4
 * Description:加入群聊申请请求参数
 */
public class JoinGroupParam implements Serializable {

    /**
     * 群id
     */
    private String roomId;
    /**
     * 用户id
     */
    private String id;
    /**
     * 申请理由
     */
    private String applyReason;
    /**
     * 加群体途径，1搜索 2扫码 3通过好友分享
     * 被邀请入群由服务端处理
     */
    private int sourceType;
    /**
     * 分享群的人的id
     */
    private String sourceId;

    public JoinGroupParam(String roomId, String id, String applyReason, int sourceType) {
        this.roomId = roomId;
        this.id = id;
        this.applyReason = applyReason;
        this.sourceType = sourceType;
    }

    public JoinGroupParam(String roomId, String id, String applyReason, int sourceType, String sourceId) {
        this.roomId = roomId;
        this.id = id;
        this.applyReason = applyReason;
        this.sourceType = sourceType;
        this.sourceId = sourceId;
    }
}
