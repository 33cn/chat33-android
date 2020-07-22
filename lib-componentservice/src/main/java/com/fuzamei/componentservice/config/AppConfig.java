package com.fuzamei.componentservice.config;

import com.fuzamei.componentservice.BuildConfig;

/**
 * Created by xiufeng on 2018/4/12.
 */
public class AppConfig {

    /**
     * 数据库
     */
    public static final String ROOT_CACHE = "mxn";
    public static final String IMAGE_CACHE = "imageCache";
    public static final String AUDIO_CACHE = "audioCache";

    public final static String TOKEN_PREFIX = "Bearer ";

    public final static int PAGE_SIZE = 20;
    // 其他
    public final static int DEFAULT_TIMEOUT = 20;
    public final static int DEFAULT_TIMEOUT_LONG = 60;
    // 最长语音录制时间
    public final static int DEFAULT_MAX_RECORD_TIME = 60;

    public static String MY_ID = "";
    public static boolean SERVER_LOGIN = false;
    public static String CHAT_SESSION = "";

    /**
     * 开发测试服
     */
    public final static String DLD = "http://172.16.103.31:8089/";
    public final static String DLD_SOCKET = "ws://172.16.103.31:8089/chat/ws/";

    /*------------------------------------------App正式、测试环境配置-----------------------------------------------*/

    public final static boolean DEVELOP = BuildConfig.DEVELOP;

    public final static String WALLET_URL_NAME = "wallet";
    public final static String WALLET_BASE_URL = AppConfigProperty.getInstance().config.getProperty("WALLET_BASE_URL");

    public final static String DEPOSIT_URL_NAME = "deposit";
    public final static String DEPOSIT_BASE_URL = AppConfigProperty.getInstance().config.getProperty("DEPOSIT_BASE_URL");

    public final static String BASE_URL_NAME = "chat";
    public final static String CHAT_BASE_URL = AppConfigProperty.getInstance().config.getProperty("CHAT_BASE_URL");
    public final static String SOCKET_URL = AppConfigProperty.getInstance().config.getProperty("SOCKET_URL");

    /**
     * 合约接口地址
     */
    public final static String CONTRACT_BASE_URL = AppConfigProperty.getInstance().config.getProperty("CONTRACT_BASE_URL");

    /**
     * 友盟推送
     */
    public final static String UMENG_APP_KEY = AppConfigProperty.getInstance().config.getProperty("UMENG_APP_KEY");
    public final static String UMENG_MESSAGE_SECRET = AppConfigProperty.getInstance().config.getProperty("UMENG_MESSAGE_SECRET");
    /**
     * 代扣地址的私钥
     */
    public final static String NO_BALANCE_PRIVATE_KEY = AppConfigProperty.getInstance().config.getProperty("NO_BALANCE_PRIVATE_KEY");

    /**
     * 百度crab appKey
     */
    public final static String BAIDU_CRAB_KEY = AppConfigProperty.getInstance().config.getProperty("BAIDU_CRAB_KEY");
    /**
     * App主域名
     * 非final,提供给SDK外部修改
     */
    public final static String APP_URL = AppConfigProperty.getInstance().config.getProperty("APP_URL");
    /**
     * App分享页面地址
     */
    public final static String APP_SHARE_URL = APP_URL + "/share.html";
    /**
     * App推广奖励规则页面地址
     */
    public final static String APP_PROMOTE_RULE_URL = APP_URL + "/rule";
    /**
     * App用户协议页面地址
     */
    public final static String APP_AGREEMENT_URL = APP_URL + "/agreement";

    /*---------------------------------------------App基本配置--------------------------------------------------*/
    /**
     * 阿里云oss配置
     */
    public final static String ALIYUN_OSS_END_POINT = AppBaseConfigProperty.getInstance().config.getProperty("ALIYUN_OSS_END_POINT");
    public final static String ALIYUN_OSS_ACCESS_KEY = AppBaseConfigProperty.getInstance().config.getProperty("ALIYUN_OSS_ACCESS_KEY");
    public final static String ALIYUN_OSS_SECRET_KEY = AppBaseConfigProperty.getInstance().config.getProperty("ALIYUN_OSS_SECRET_KEY");
    public final static String ALIYUN_OSS_BUCKET = AppBaseConfigProperty.getInstance().config.getProperty("ALIYUN_OSS_BUCKET");
    /**
     * 微信开放平台ID
     */
    public final static String WX_APP_ID = AppBaseConfigProperty.getInstance().config.getProperty("WX_APP_ID");
    /**
     * 小米系统推送通道
     */
    public final static String MI_PUSH_ID = AppBaseConfigProperty.getInstance().config.getProperty("MI_PUSH_ID");
    public final static String MI_PUSH_KEY = AppBaseConfigProperty.getInstance().config.getProperty("MI_PUSH_KEY");
    /**
     * 魅族系统推送通道
     */
    public final static String MEIZU_PUSH_ID = AppBaseConfigProperty.getInstance().config.getProperty("MEIZU_PUSH_ID");
    public final static String MEIZU_PUSH_KEY = AppBaseConfigProperty.getInstance().config.getProperty("MEIZU_PUSH_KEY");
    /**
     * app主色调
     */
    public final static String APP_ACCENT_COLOR_STR = AppBaseConfigProperty.getInstance().config.getProperty("APP_ACCENT_COLOR_STR");
    public final static int APP_ACCENT_COLOR_INT = Integer.parseInt(AppBaseConfigProperty.getInstance().config.getProperty("APP_ACCENT_COLOR_INT"));
    /**
     * 是否显示启动页广告
     */
    public final static boolean APP_SHOW_AD = Boolean.parseBoolean(AppBaseConfigProperty.getInstance().config.getProperty("APP_SHOW_AD"));
    /**
     * 是否启用加密
     */
    public final static boolean APP_ENCRYPT = Boolean.parseBoolean(AppBaseConfigProperty.getInstance().config.getProperty("APP_ENCRYPT"));
    /**
     * 是否启用文件加密
     */
    public final static boolean FILE_ENCRYPT = Boolean.parseBoolean(AppBaseConfigProperty.getInstance().config.getProperty("FILE_ENCRYPT"));
    /**
     * 是否启用认证
     */
    public final static boolean APP_IDENTIFY = Boolean.parseBoolean(AppBaseConfigProperty.getInstance().config.getProperty("APP_IDENTIFY"));
    /**
     * 是否启用推荐群
     */
    public final static boolean APP_RECOMMENDED_GROUP = Boolean.parseBoolean(AppBaseConfigProperty.getInstance().config.getProperty("APP_RECOMMENDED_GROUP"));
    /**
     * 是否开启消息奖励
     */
    public final static boolean APP_MESSAGE_REWARD = Boolean.parseBoolean(AppBaseConfigProperty.getInstance().config.getProperty("APP_MESSAGE_REWARD"));
    /**
     * app名称
     */
    public final static String APP_NAME = AppBaseConfigProperty.getInstance().config.getProperty("APP_NAME");
    public final static String APP_NAME_EN = AppBaseConfigProperty.getInstance().config.getProperty("APP_NAME_EN");
    public final static String APP_ID = AppBaseConfigProperty.getInstance().config.getProperty("APP_ID");
    /**
     * app跳转链接
     */
    public final static String APP_SCHEME = AppBaseConfigProperty.getInstance().config.getProperty("APP_SCHEME");
    public final static String APP_HOST = AppBaseConfigProperty.getInstance().config.getProperty("APP_HOST");

    public final static String ENC_PREFIX = FILE_ENCRYPT ? "$ENC$" : "";

    public final static String ENC_INFIX = "+3581F5OXkN@SqquE!foUh";
}
