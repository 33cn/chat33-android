package com.fuzamei.componentservice.consts;

/**
 * @author zhengjy
 * @since 2019/10/10
 * Description:服务端socket消息状态码
 */
public class AppError {

    /**
     * 忽略错误
     */
    public static final int IGNORE_ERROR = -1;
    /**
     * 解密失败
     */
    public static final int DECRYPT_ERROR = 1000;
    /**
     * 加密失败
     */
    public static final int ENCRYPT_ERROR = 1001;
    /**
     * 创建助记词失败
     */
    public static final int CREATE_WORDS_ERROR = 1002;
}
