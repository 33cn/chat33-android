package com.fzm.chat33.core.request.chat;

import java.util.List;

/**
 * 创建日期：2018/8/14 on 16:11
 * 描述:
 * 作者:wdl
 */
public class TextRequest extends BaseChatRequest {
    public String content;
    public List<String> aitList;

    public TextRequest() {

    }

    public TextRequest(PreForwardRequest request) {
        super(request);
    }
}
