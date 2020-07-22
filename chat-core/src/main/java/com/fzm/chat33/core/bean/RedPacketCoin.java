package com.fzm.chat33.core.bean;

import java.io.Serializable;
import java.util.List;

/**
 * @author zhengjy
 * @since 2019/03/15
 * Description:红包功能的币种信息
 */
public class RedPacketCoin implements Serializable {

    /**
     * 币种编号
     */
    public int coinId;
    /**
     * 币种英文名
     */
    public String coinName;
    /**
     * 币种中文名
     */
    public String coinNickname;
    /**
     * 代币全名
     */
    public String coinFullName;
    /**
     * 精度
     */
    public int decimalPlaces;
    /**
     * 币种图标
     */
    public String iconUrl;
    /**
     * 单笔最大额度
     */
    public double singleMax;
    /**
     * 单笔最小额度
     */
    public double singleMin;
    /**
     * 每天最大额度
     */
    public double dailyMax;
    /**
     * 币种币种拥有资产
     */
    public double amount;
    /**
     * 发红包所需手续费
     */
    public double fee;

    public static class Wrapper {
        public List<RedPacketCoin> balances;
    }
}
