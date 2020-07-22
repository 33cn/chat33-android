package com.fzm.chat33.core.bean;

import com.fuzamei.componentservice.config.AppPreference;
import com.google.gson.Gson;

import java.io.Serializable;

/**
 * @author zhengjy
 * @since 2019/07/23
 * Description:认证网页需要的参数
 */
public class IdentifyParam implements Serializable {

    public static final int TYPE_PERSON = 1;
    public static final int TYPE_GROUP = 2;

    /**
     * 认证类型
     * 1：个人     2：群聊
     */
    public int type;
    public String token;
    public String roomId;
    public String session;

    public static String create(String roomId) {
        IdentifyParam param = new IdentifyParam();
        param.token = AppPreference.INSTANCE.getTOKEN();
        param.session = AppPreference.INSTANCE.getSESSION_KEY();
        param.type = TYPE_GROUP;
        param.roomId = roomId;
        return new Gson().toJson(param);
    }

    public static String create() {
        IdentifyParam param = new IdentifyParam();
        param.token = AppPreference.INSTANCE.getTOKEN();
        param.session = AppPreference.INSTANCE.getSESSION_KEY();
        param.type = TYPE_PERSON;
        return new Gson().toJson(param);
    }
}
