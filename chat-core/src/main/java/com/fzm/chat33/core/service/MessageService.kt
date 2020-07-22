package com.fzm.chat33.core.service

import android.app.Service
import android.content.Intent
import android.os.IBinder
import androidx.lifecycle.Observer
import com.baidu.crabsdk.CrabSDK
import com.fuzamei.common.bus.LiveBus
import com.fuzamei.common.net.subscribers.OnSuccessListener
import com.fuzamei.common.net.subscribers.RxSubscriber
import com.fuzamei.common.utils.*
import com.fuzamei.componentservice.app.BusEvent
import com.fuzamei.componentservice.config.AppPreference
import com.fzm.chat33.core.logic.MessageDispatcher
import com.fzm.chat33.core.net.RequestManager
import org.kodein.di.conf.KodeinGlobalAware
import org.kodein.di.generic.instance
import java.util.*

/**
 * @author zhengjy
 * @since 2018/10/29
 * Description:全局消息接收服务
 */
class MessageService : Service(), KodeinGlobalAware {

    private var timer: Timer? = null
    private var task: TimerTask? = null
    private val dispatcher: MessageDispatcher? by instance()
    private val observer: Observer<Boolean> = Observer { login ->
        if (login != true) {
            stopSelf()
        }
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        dispatcher?.start()
        initDaemon()
        LiveBus.of(BusEvent::class.java).loginEvent().observeForever(observer)
        return START_STICKY
    }

    private fun initDaemon() {
        timer = Timer()
        task = object : TimerTask() {
            override fun run() {
                LogUtils.d("MessageService Run: " + ToolUtils.formatLogTime(System.currentTimeMillis()))
                SecurityCheckUtil.getSingleInstance().tryShutdownXposed()
                if (!ActivityUtils.isBackground()) {
                    val time = AppPreference.LAST_FOREGROUND
                    if (System.currentTimeMillis() - time > 30 * 1000L) {
                        // 离开前台超过30s，再次进入则算一次启动
                        RequestManager.INS.startStatistics(RxSubscriber<Any>(object : OnSuccessListener<Any?>() {
                            override fun onSuccess(t: Any?) {}
                        }))
                    }
                    AppPreference.LAST_FOREGROUND = System.currentTimeMillis()
                }
            }
        }
        try {
            timer?.schedule(task, 0, 10000)
        } catch (e: Exception) {
            e.printStackTrace()
            CrabSDK.uploadException(e)
        }
    }

    override fun onDestroy() {
        LogUtils.d("MessageService destroy: " + ToolUtils.formatLogTime(System.currentTimeMillis()))
        LiveBus.of(BusEvent::class.java).loginEvent().removeObserver(observer)
        try {
            timer?.cancel()
            task?.cancel()
        } catch (e: Exception) {
            e.printStackTrace()
            CrabSDK.uploadException(e)
        }
        super.onDestroy()
    }
}