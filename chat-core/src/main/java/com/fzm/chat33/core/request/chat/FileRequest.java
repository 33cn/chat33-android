package com.fzm.chat33.core.request.chat;

import com.google.gson.annotations.SerializedName;

/**
 * @author zhengjy
 * @since 2019/02/11
 * Description:文件消息请求
 */
public class FileRequest extends BaseChatRequest {
    /**
     * 文件远程地址
     */
    public String fileUrl;
    /**
     * 文件名
     */
    @SerializedName("name")
    public String fileName;
    /**
     * 文件大小
     */
    @SerializedName("size")
    public long fileSize;
    /**
     * 文件md5
     */
    public String md5;

    public FileRequest() {

    }

    public FileRequest(PreForwardRequest request) {
        super(request);
    }
}
