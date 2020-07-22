package com.fzm.chat33.core.response;

/**
 * 创建日期：2018/8/8 on 16:41
 * 描述:
 * 作者:wdl
 */
public class ReceiveRedPacketResponse extends BaseResponse {
    /**
     * 红包总额
     */
    public float amount;
    /**
     * 红包总数
     */
    public int total;
    /**
     * 剩余数量
     */
    public int remain;
    /**
     * 币种
     */
    public int coin;
}
