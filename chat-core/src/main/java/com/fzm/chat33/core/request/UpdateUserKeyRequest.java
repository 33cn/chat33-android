package com.fzm.chat33.core.request;

/**
 * @author zhengjy
 * @since 2019/05/30
 * Description:更新自己的加密公钥请求
 */
public class UpdateUserKeyRequest extends BaseRequest {

    public int eventType;
    public String publicKey;

    public UpdateUserKeyRequest(String publicKey) {
        this.eventType = 33;
        this.publicKey = publicKey;
    }
}
