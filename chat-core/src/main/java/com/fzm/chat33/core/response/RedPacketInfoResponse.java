package com.fzm.chat33.core.response;

import com.fzm.chat33.core.bean.RedPacketReceiveInfo;

/**
 * @author zhengjy
 * @since 2019/03/15
 * Description:
 */
public class RedPacketInfoResponse extends BaseResponse {

    /**
     * 发红包者id，chat33内部id
     */
    public String senderId;

    /**
     * 发送者uid
     */
    public String uid;

    /**
     * 发送者名称
     */
    public String senderName;

    /**
     * 发送者头像
     */
    public String senderAvatar;

    /**
     * 红包id
     */
    public String packetId;

    /**
     * 红包h5链接
     */
    public String packetUrl;

    /**
     * 1:随机红包 2:固定金额红包
     */
    public int type;

    /**
     * 币种代号
     */
    public int coinId;

    /**
     * 币种名称：YCC，BTC...
     */
    public String coinName;

    /**
     * 红包总金额
     */
    public double amount;

    /**
     * 红包总个数
     */
    public int size;

    /**
     * 红包接收用户列表，默认为空，所有人可抢
     */
    public String toUsers;

    /**
     * 剩余红包数量
     */
    public int remain;

    /**
     * 发出红包状态：1:生效中 2:已领取完 3:过期已退回 4:已完成
     */
    public int status;

    /**
     * 领取时间，unix时间戳
     */
    public long createdAt;

    /**
     * 红包备注
     */
    public String remark;

    /**
     * 当前用户的领取详情
     */
    public RedPacketReceiveInfo revInfo;
}
