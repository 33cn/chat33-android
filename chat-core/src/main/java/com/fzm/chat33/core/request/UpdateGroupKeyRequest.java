package com.fzm.chat33.core.request;

import java.io.Serializable;
import java.util.List;

/**
 * @author zhengjy
 * @since 2019/05/29
 * Description:更新加密群聊会话密钥请求
 */
public class UpdateGroupKeyRequest extends BaseRequest {

    public int eventType;
    public String roomId;
    public String fromKey;
    public List<Secret> secret;

    public UpdateGroupKeyRequest(String roomId, String fromKey, List<Secret> secret) {
        this.eventType = 26;
        this.fromKey = fromKey;
        this.roomId = roomId;
        this.secret = secret;
    }

    public static class Secret implements Serializable {
        public String userId;
        public String key;

        public Secret(String userId, String key) {
            this.userId = userId;
            this.key = key;
        }
    }
}
