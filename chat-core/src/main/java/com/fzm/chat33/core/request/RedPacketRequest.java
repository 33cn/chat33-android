package com.fzm.chat33.core.request;

import com.fzm.chat33.core.request.chat.BaseChatRequest;
import com.fzm.chat33.core.request.chat.PreForwardRequest;

/**
 * @author zhengjy
 * @since 2018/11/02
 * Description:发送红包socket消息格式
 */
public class RedPacketRequest extends BaseChatRequest {

    public int coin;
    public String coinName;
    public String packetId;
    public String packetUrl;
    public int packetType;
    public int packetMode;
    public String remark;

    public RedPacketRequest() {

    }

    public RedPacketRequest(PreForwardRequest request) {
        super(request);
    }
}
