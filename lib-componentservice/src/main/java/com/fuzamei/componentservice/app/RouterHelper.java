package com.fuzamei.componentservice.app;

import com.fuzamei.componentservice.config.AppConfig;

import java.util.HashMap;
import java.util.Map;

/**
 * @author zhengjy
 * @since 2019/04/08
 * Description:
 */
public class RouterHelper {

    public static String APP_SCHEME = AppConfig.APP_SCHEME;
    public static String APP_HOST = AppConfig.APP_HOST;
    public static String APP_LINK = AppConfig.APP_SCHEME + "://" + APP_HOST;

    public static Map<String, String> routeMap = new HashMap<>();

    // type所对应的页面path
    static {
        // 用户详情页面
        routeMap.put("shareUserDetail", AppRoute.USER_DETAIL);
        // 群摘要信息页面
        routeMap.put("shareJoinRoom", AppRoute.JOIN_ROOM);
        // 聊天页面
        routeMap.put("chatNotification", AppRoute.CHAT);
        // 选择联系人页面
        routeMap.put("systemShare", AppRoute.CONTACT_SELECT);
        // 应用内浏览器
        routeMap.put("appWebBrowser", AppRoute.WEB_BROWSER);
        // 我的二维码页面
        routeMap.put("myQRCode", AppRoute.QR_CODE);
        // 聊天扫一扫
        routeMap.put("scanQRCode", AppRoute.QR_SCAN);
    }
}
