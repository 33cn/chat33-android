package com.fzm.chat33.core.request.chat;

/**
 * @author zhengjy
 * @since 2019/04/15
 * Description:
 */
public class TransactionRequest extends BaseChatRequest {

    public String coinName;
    public String amount;
    public String recordId;

    public TransactionRequest() {

    }

    public TransactionRequest(PreForwardRequest request) {
        super(request);
    }
}
