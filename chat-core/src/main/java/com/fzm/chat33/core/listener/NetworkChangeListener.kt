package com.fzm.chat33.core.listener

/**
 * @author zhengjy
 * @since 2019/03/08
 * Description:
 */
interface NetworkChangeListener {

    fun onMobileAvailable()

    fun onWifiAvailable()

    fun onDisconnected()
}