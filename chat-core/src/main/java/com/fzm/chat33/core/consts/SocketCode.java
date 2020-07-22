package com.fzm.chat33.core.consts;

/**
 * @author zhengjy
 * @since 2019/10/10
 * Description:服务端socket消息状态码
 */
public class SocketCode {
    /**
     * 正常状态码
     */
    public static final int SUCCESS = 0;
    /**
     * 消息被好友拒收
     */
    public static final int FRIEND_REJECT = -2032;
    /**
     * 其他端登录
     */
    public static final int SOCKET_OTHER_LOGIN = 4001;
    /**
     * 其他端修改助记词
     */
    public static final int SOCKET_OTHER_UPDATE_WORDS = 4011;
}
