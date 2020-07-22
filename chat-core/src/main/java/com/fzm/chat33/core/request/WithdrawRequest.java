package com.fzm.chat33.core.request;

/**
 * @author zhengjy
 * @since 2019/04/15
 * Description:提币（转账）请求参数
 */
public class WithdrawRequest extends BaseRequest {

    /**
     * 支付对方的收款请求时需要传logId
     */
    public String logId;

    public String currency;
    public String amount;
    public String fee;
    public String opp_address;
    public String rid;
    public String mode;
    public String payword;
    public String code;

    public WithdrawRequest(String currency, String amount, String fee, String opp_address, String rid, String mode, String payword, String code) {
        this.currency = currency;
        this.amount = amount;
        this.fee = fee;
        this.opp_address = opp_address;
        this.rid = rid;
        this.mode = mode;
        this.payword = payword;
        this.code = code;
    }
}
