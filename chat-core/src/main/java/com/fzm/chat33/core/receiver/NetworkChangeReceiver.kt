package com.fzm.chat33.core.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkInfo
import com.fuzamei.common.utils.LogUtils
import com.fzm.chat33.core.listener.NetworkChangeListener

/**
 * @author zhengjy
 * @since 2019/03/07
 * Description:网络状态变化接收器
 */
internal class NetworkChangeReceiver(val listener: NetworkChangeListener?) : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        // 监听网络连接，包括wifi和移动数据的打开和关闭,以及连接上可用的连接都会接到监听
        if (ConnectivityManager.CONNECTIVITY_ACTION == intent?.action) {
            val info = (context?.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager).activeNetworkInfo
            getNetworkInfo(info)
        }
    }

    private fun getNetworkInfo(info: NetworkInfo?) {
        if (info?.state == NetworkInfo.State.CONNECTED && info.isAvailable) {
            when (info.type) {
                ConnectivityManager.TYPE_WIFI -> {
                    LogUtils.d("NET_STATE", "wifi connected")
                    listener?.onWifiAvailable()
                }
                else -> {
                    LogUtils.d("NET_STATE", "other connected")
                    listener?.onMobileAvailable()
                }
            }
        } else {
            LogUtils.d("NET_STATE", "disconnected")
            listener?.onDisconnected()
        }
    }
}