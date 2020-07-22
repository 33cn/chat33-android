package com.fzm.chat33.global;

import android.annotation.SuppressLint;
import android.content.Context;
import android.text.TextUtils;

import com.alibaba.android.arouter.facade.Postcard;
import com.alibaba.android.arouter.facade.annotation.Interceptor;
import com.alibaba.android.arouter.facade.callback.InterceptorCallback;
import com.alibaba.android.arouter.facade.template.IInterceptor;
import com.fuzamei.common.utils.ActivityUtils;
import com.fuzamei.componentservice.config.AppPreference;
import com.fzm.chat33.core.Chat33;
import com.fzm.chat33.core.global.UserInfo;

/**
 * @author zhengjy
 * @since 2018/11/01
 * Description:
 */
@Interceptor(priority = 10)
public class LoginInterceptor implements IInterceptor {

    @SuppressLint("WrongConstant")
    @Override
    public void process(Postcard postcard, InterceptorCallback callback) {
        if (postcard.getExtra() == AppConst.NEED_LOGIN) {
            String token = AppPreference.INSTANCE.getTOKEN();
            if (!TextUtils.isEmpty(token)) {
                if (UserInfo.getInstance().isLogin()) {
                    callback.onContinue(postcard);
                } else {
                    Chat33.getRouter().performLogin(postcard.getExtras().getBundle("data"));
                    callback.onInterrupt(null);
                }
            } else {
                Chat33.getRouter().gotoLoginPage(postcard.getExtras().getBundle("data"));
                ActivityUtils.exitToLogin();
                callback.onInterrupt(null);
            }
        } else {
            callback.onContinue(postcard);
        }
    }

    @Override
    public void init(Context context) {

    }
}
