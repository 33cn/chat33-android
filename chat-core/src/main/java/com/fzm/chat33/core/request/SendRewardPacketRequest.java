package com.fzm.chat33.core.request;

/**
 * @author zhengjy
 * @since 2019/11/21
 * Description:
 */
public class SendRewardPacketRequest extends BaseRequest {

    /**
     * 打赏消息的来源
     */
    public int channelType;

    /**
     * 打赏消息id
     */
    public String logId;

    /**
     * 打赏用户id
     */
    public String userId;

    /**
     * 币种
     */
    public String currency;

    /**
     * 金额
     */
    public double amount;

    /**
     * 支付密码
     */
    public String password;
}
