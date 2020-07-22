package com.fzm.chat33.core.request.chat;

/**
 * 创建日期：2018/8/14 on 16:11
 * 描述:
 * 作者:wdl
 */
public class ImageRequest extends BaseChatRequest {
    public String imageUrl;
    public int width;
    public int height;

    public ImageRequest() {

    }

    public ImageRequest(PreForwardRequest request) {
        super(request);
    }
}
