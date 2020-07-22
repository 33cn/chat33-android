package com.fzm.chat33.core.bean;

import java.io.Serializable;

public class DepositSMS implements Serializable {

    public static final String TOKEN_PREFIX = "Bearer ";
    public static final String AREA = "86";

    public static final String CODETYPE_QUICK = "quick";
    public static final String CODETYPE_RESET_PASSWORD = "reset_password";
    public static final String CODETYPE_RESET_PAY_PASSWORD = "reset_pay_password";
    public static final String CODETYPE_WITHDRAW_COIN = "withdraw_coin";
    public static final String SMS_PARAM4 = "FzmRandom4";
    public static final String SMS_PARAM = "FzmRandom";

    public static final String TYPE_MOBILE = "mobile";
    public static final String TYPE_EMAIL = "email";
    //发送验证码方式(sms,email,voice)
    public static final String SEND_SMS = "sms";
    public static final String SEND_EMAIL = "email";
    public static final String SEND_VOICE = "voice";

    public static final String KEY_CODE = "key_code";


    /**
     * isShow : 0
     * isValidate : 0
     * data : {"yys":"Yunpian","mobile":"15700084615"}
     */

    private int isShow;
    private int isValidate;
    private DataBean data;

    public int getIsShow() {
        return isShow;
    }

    public void setIsShow(int isShow) {
        this.isShow = isShow;
    }

    public int getIsValidate() {
        return isValidate;
    }

    public void setIsValidate(int isValidate) {
        this.isValidate = isValidate;
    }

    public DataBean getData() {
        return data;
    }

    public void setData(DataBean data) {
        this.data = data;
    }

    public static class DataBean implements Serializable {
        /**
         * yys : Yunpian
         * mobile : 15700084615
         */
        private String gid;
        private String yys;
        private String mobile;
        //图形验证码验证
        private String jsUrl;
        private String businessId;

        public String getJsUrl() {
            return jsUrl;
        }

        public void setJsUrl(String jsUrl) {
            this.jsUrl = jsUrl;
        }

        public String getBusinessId() {
            return businessId;
        }

        public void setBusinessId(String businessId) {
            this.businessId = businessId;
        }

        public String getGid() {
            return gid;
        }

        public void setGid(String gid) {
            this.gid = gid;
        }

        public String getYys() {
            return yys;
        }

        public void setYys(String yys) {
            this.yys = yys;
        }

        public String getMobile() {
            return mobile;
        }

        public void setMobile(String mobile) {
            this.mobile = mobile;
        }
    }
}
