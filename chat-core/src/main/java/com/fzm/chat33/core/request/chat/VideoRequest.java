package com.fzm.chat33.core.request.chat;

/**
 * @author zhengjy
 * @since 2019/02/11
 * Description:视频消息请求
 */
public class VideoRequest extends BaseChatRequest {
    /**
     * 视频远程地址
     */
    public String mediaUrl;
    /**
     * 视频时长
     */
    public float time;

    public int width;

    public int height;

    public VideoRequest() {

    }

    public VideoRequest(PreForwardRequest request) {
        super(request);
    }
}
