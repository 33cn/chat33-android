package com.fzm.chat33.core.request;

/**
 * @author zhengjy
 * @since 2019/03/18
 * Description:
 */
public class RedPacketRecordRequest extends BaseRequest {
    /**
     * 收发动作
     * 1:发红包 2:收红包
     */
    public int operation;
    /**
     * 币种代号
     */
    public int coinId;
    /**
     * 红包类型
     */
    public int type;
    /**
     * 开始时间
     */
    public long startTime;
    /**
     * 结束时间
     */
    public long endTime;
    /**
     * 页数
     */
    public int pageNum;
    /**
     * 条数
     */
    public int pageSize = 20;
}
