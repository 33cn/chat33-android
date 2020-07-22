package com.fzm.chat33.core.request.chat;

/**
 * 创建日期：2018/8/14 on 16:11
 * 描述:
 * 作者:wdl
 */
public class MediaRequest extends BaseChatRequest {
    public String mediaUrl;
    public float time;

    public MediaRequest() {

    }

    public MediaRequest(PreForwardRequest request) {
        super(request);
    }
}
