package com.fzm.chat33.core.request;

/**
 * @author zhengjy
 * @since 2019/02/18
 * Description:
 */
public class ChatFileHistoryRequest extends BaseRequest {
    public String id;
    public String startId;
    public int number;
    /**
     * 模糊查询字符串
     */
    public String query;
    /**
     * 文件上传者id
     */
    public String owner;
}
