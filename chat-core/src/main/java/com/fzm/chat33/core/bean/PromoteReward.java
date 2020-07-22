package com.fzm.chat33.core.bean;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;

/**
 * @author zhengjy
 * @since 2019/07/09
 * Description:邀请推广详情
 */
public class PromoteReward implements Serializable {

    /**
     * uid : 153
     * is_real : 1
     * currency : YCC
     * amount : 3.000
     */
    private String uid;
    @SerializedName(value = "is_real")
    private int isReal;
    private String currency;
    private String amount;

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public int getIsReal() {
        return isReal;
    }

    public void setIsReal(int isReal) {
        this.isReal = isReal;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public static class Wrapper implements Serializable {
        public List<PromoteReward> list;
        public int count;
    }
}
