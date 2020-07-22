package com.fuzamei.common.net.rxjava;

import com.fuzamei.common.FzmFramework;
import com.fuzamei.commonlib.R;

/**
 * Created by ljn on 2018/3/30.
 * update
 * Explain 根据请求返回数据结构，传入errorCode
 */

public class ApiException extends RuntimeException {
    private int errorCode;

    /**
     * 动态指定message
     *
     * @param errorCode 错误码，-1表示忽略错误显示
     */
    public ApiException(int errorCode) {
        this(errorCode, getApiExceptionMessage(errorCode));
    }

    public ApiException(String message) {
        super(message);
    }

    public ApiException(Throwable cause) {
        super(cause);
    }

    /**
     * 静态指定message
     *
     * @param errorCode
     * @param errorMessage
     */
    public ApiException(int errorCode, String errorMessage) {
        super(errorMessage);
        this.errorCode = errorCode;
    }

    /**
     * 由于服务器传递过来的错误信息直接给用户看的话，用户未必能够理解
     * 需要根据错误码对错误信息进行一个转换，在显示给用户
     *
     * @param code
     * @return
     */
    private static String getApiExceptionMessage(int code) {
        String message;
        switch (code) {
            case -1:// -1为忽略错误
                message = null;
                break;
            case 0:
                message = FzmFramework.getString(R.string.basic_error_unknown);
                break;
            case 1:
                message = FzmFramework.getString(R.string.basic_error_certificate);
                break;
            case 2:
                message = FzmFramework.getString(R.string.basic_error_service_domain);
                break;
            case 3:
                message = FzmFramework.getString(R.string.basic_error_service);
                break;
            case 4:
                message = FzmFramework.getString(R.string.basic_error_network);
                break;
            case 5:
                message = FzmFramework.getString(R.string.basic_error_response_parse);
                break;
            case 6:
                message = FzmFramework.getString(R.string.basic_error_request);
                break;
            case 7:
                message = FzmFramework.getString(R.string.basic_error_connect);
                break;
            case 8:
                message = FzmFramework.getString(R.string.basic_error_data_structure);
                break;
            case -1004:
                message = FzmFramework.getString(R.string.basic_error_token_expire);
                break;
            default:
                message = FzmFramework.getString(R.string.basic_error_unknown1);
        }
        return message;
    }

    public int getErrorCode() {
        return errorCode;
    }
}