package com.fuzamei.componentservice.app;

import android.os.Build;
import android.text.TextUtils;

import com.fuzamei.common.bus.LiveBus;
import com.fuzamei.common.utils.ToolUtils;
import com.fuzamei.componentservice.config.AppConfig;
import com.fuzamei.componentservice.config.AppPreference;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;

import okhttp3.Headers;
import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

/**
 * @author zhengjy
 * @since 2018/11/15
 * Description:
 */
public class Chat33Interceptor implements Interceptor {

    private String uuid;

    @Override
    public Response intercept(Chain chain) throws IOException {
        String token = AppPreference.INSTANCE.getTOKEN();
        Request original = chain.request();
        Request.Builder requestBuilder = original.newBuilder();
        String cookie_session = AppPreference.INSTANCE.getSESSION_KEY();
        if (TextUtils.isEmpty(cookie_session)) {
            requestBuilder.removeHeader("cookie");
        } else {
            requestBuilder.addHeader("cookie", cookie_session);
        }
        requestBuilder.addHeader("FZM-APP-ID", AppConfig.APP_ID);
        if (TextUtils.isEmpty(uuid)) {
            uuid = ToolUtils.getUUID();
        }
        requestBuilder.addHeader("FZM-UUID", uuid);
        if (!TextUtils.isEmpty(token)) {
            requestBuilder.addHeader("FZM-AUTH-TOKEN", token);
            requestBuilder.addHeader("Authorization", AppConfig.TOKEN_PREFIX + token);
        }
        requestBuilder.addHeader("Fzm-Request-Source", "chat");
        requestBuilder.addHeader("FZM-VERSION", ToolUtils.getVersionName());
        requestBuilder.addHeader("FZM-DEVICE", "Android");
        requestBuilder.addHeader("FZM-DEVICE-NAME", Build.MODEL);
        requestBuilder
                .addHeader("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8")
                .method(original.method(), original.body());
        Request request = requestBuilder.build();
        Response response = chain.proceed(request);
        //请求之后
        String body = response.body().string();
        // 登录完成后，保存cookie
        Headers headers = response.headers();
        List<String> cookies = headers.values("Set-Cookie");
        if (cookies != null && cookies.size() > 0) {
            String session = cookies.get(0);
            if (session.startsWith("session-login")) {
                String result = session.substring(0, session.indexOf(";"));
                if (!TextUtils.isEmpty(result)) {
                    AppPreference.INSTANCE.setSESSION_KEY(result);
                    AppConfig.CHAT_SESSION = result;
                }
            }
        }
        try {
            JSONObject json = new JSONObject(body);
            int code = json.getInt("result");
            if (code == -1004) {
                // chat33 session登录过期
                LiveBus.of(BusEvent.class).loginExpire().postValue(null);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        MediaType mediaType = response.body().contentType();
        return response.newBuilder()
                .body(ResponseBody.create(mediaType, body))
                .build();
    }
}
