package com.fzm.chat33.core.request;

/**
 * @author zhengjy
 * @since 2019/03/14
 * Description:
 */
public class PayPasswordRequest extends BaseRequest {

    /**
     * 校验方式，可选
     * code验证码
     * password:原支付密码
     */
    private String mode;
    /**
     * mode=code时必填。
     * 可选值为sms/voice/email
     */
    private String type;
    /**
     * mode=code时必填。
     * 验证码，所用验证码类型是‘reset_pay_password’
     */
    private String code;
    /**
     * mode=password时必填。原支付密码
     */
    private String oldPayPassword;
    private String payPassword;

    public static PayPasswordRequest useCode(String type, String code, String password) {
        PayPasswordRequest request = new PayPasswordRequest();
        request.mode = "code";
        request.type = type;
        request.code = code;
        request.payPassword = password;
        return request;
    }

    public static PayPasswordRequest usePassword(String oldPassword, String password) {
        PayPasswordRequest request = new PayPasswordRequest();
        request.mode = "password";
        request.oldPayPassword = oldPassword;
        request.payPassword = password;
        return request;
    }

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getOldPayPassword() {
        return oldPayPassword;
    }

    public void setOldPayPassword(String oldPayPassword) {
        this.oldPayPassword = oldPayPassword;
    }

    public String getPayPassword() {
        return payPassword;
    }

    public void setPayPassword(String payPassword) {
        this.payPassword = payPassword;
    }
}
