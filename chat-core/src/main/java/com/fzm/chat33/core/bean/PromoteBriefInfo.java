package com.fzm.chat33.core.bean;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;

/**
 * @author zhengjy
 * @since 2019/07/04
 * Description:推广概况
 */
public class PromoteBriefInfo implements Serializable {
    /**
     * invite_num : 2
     * primary : {"currency":"BTY","total":"1.000"}
     * statistics : [{"currency":"BTY","total":"1.000"},{"currency":"YCC","total":"7.000"}]
     */

    @SerializedName(value = "invite_num")
    private int inviteNum;
    private PrimaryBean primary;
    private List<StatisticsBean> statistics;

    public int getInviteNum() {
        return inviteNum;
    }

    public void setInviteNum(int inviteNum) {
        this.inviteNum = inviteNum;
    }

    public PrimaryBean getPrimary() {
        return primary;
    }

    public void setPrimary(PrimaryBean primary) {
        this.primary = primary;
    }

    public List<StatisticsBean> getStatistics() {
        return statistics;
    }

    public void setStatistics(List<StatisticsBean> statistics) {
        this.statistics = statistics;
    }

    public static class PrimaryBean implements Serializable {
        /**
         * currency : BTY
         * total : 1.000
         */
        private String currency;
        private String total;

        public String getCurrency() {
            return currency;
        }

        public void setCurrency(String currency) {
            this.currency = currency;
        }

        public String getTotal() {
            return total;
        }

        public void setTotal(String total) {
            this.total = total;
        }
    }

    public static class StatisticsBean implements Serializable {
        /**
         * currency : BTY
         * total : 1.000
         */

        private String currency;
        private String total;

        public String getCurrency() {
            return currency;
        }

        public void setCurrency(String currency) {
            this.currency = currency;
        }

        public String getTotal() {
            return total;
        }

        public void setTotal(String total) {
            this.total = total;
        }
    }
}
