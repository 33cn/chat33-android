package com.fzm.chat33.core.response;

import com.fzm.chat33.core.db.bean.ChatMessage;

import java.util.List;

/**
 * 创建日期：2018/7/30 on 13:52
 * 描述:
 * 作者:wdl
 */
public class ChatListResponse extends BaseResponse{
    public List<ChatMessage> logs;
    public String nextLog;
}
