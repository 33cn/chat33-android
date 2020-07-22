package com.fzm.chat33.core.bean;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;

/**
 * @author zhengjy
 * @since 2019/07/09
 * Description:条件推广详情
 */
public class ConditionReward implements Serializable {

    /**
     * type : standard_reward
     * currency : BTY
     * amount : 1.000
     * num : 1
     * updated_at : 2019-07-09 14:41:04
     */
    private String type;
    private String currency;
    private String amount;
    private String num;
    @SerializedName(value = "updated_at")
    private String updatedAt;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
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

    public String getNum() {
        return num;
    }

    public void setNum(String num) {
        this.num = num;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }

    public static class Wrapper implements Serializable {
        public List<ConditionReward> list;
        public int count;
    }
}
