package com.fuzamei.componentservice.app

import android.os.Bundle

/**
 * @author zhengjy
 * @since 2019/12/04
 * Description:
 */
interface ChatRouter {

    /**
     * 执行登录操作，通常是将用户信息加载到内存中
     *
     * @param data 参数
     */
    fun performLogin(data: Bundle?)

    /**
     * 跳转登录页面
     *
     * @param data 跳转参数
     */
    fun gotoLoginPage(data: Bundle?)

    /**
     * 跳转主界面
     */
    fun gotoMainPage()
}