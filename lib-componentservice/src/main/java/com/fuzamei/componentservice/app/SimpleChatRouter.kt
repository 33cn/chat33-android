package com.fuzamei.componentservice.app

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import com.alibaba.android.arouter.launcher.ARouter

/**
 * @author zhengjy
 * @since 2019/12/04
 * Description:
 */
open class SimpleChatRouter : ChatRouter {

    @SuppressLint("WrongConstant")
    override fun performLogin(data: Bundle?) {
        ARouter.getInstance().build(AppRoute.SPLASH)
                .withBundle("data", data)
                .withFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                .navigation()
    }

    @SuppressLint("WrongConstant")
    override fun gotoLoginPage(data: Bundle?) {
        ARouter.getInstance().build(AppRoute.LOGIN)
                .withBundle("data", data)
                .withFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                .navigation()
    }

    @SuppressLint("WrongConstant")
    override fun gotoMainPage() {
        ARouter.getInstance().build(AppRoute.MAIN)
                .withFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                .navigation()
    }
}