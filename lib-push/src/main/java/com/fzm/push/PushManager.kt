package com.fzm.push

import android.app.Application
import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.net.Uri
import androidx.core.app.NotificationCompat
import android.util.Log
import com.fuzamei.common.bus.LiveBus
import com.fuzamei.common.net.subscribers.OnSuccessListener
import com.fuzamei.common.net.subscribers.RxSubscriber

import com.fuzamei.componentservice.app.RouterHelper
import com.fuzamei.componentservice.config.AppConfig
import com.fzm.chat33.core.net.RequestManager
import com.fuzamei.componentservice.app.BusEvent
import com.fuzamei.componentservice.config.AppPreference
import com.umeng.commonsdk.UMConfigure
import com.umeng.message.IUmengCallback
import com.umeng.message.IUmengRegisterCallback
import com.umeng.message.PushAgent
import com.umeng.message.UmengMessageHandler
import com.umeng.message.entity.UMessage

import org.android.agoo.huawei.HuaWeiRegister
import org.android.agoo.mezu.MeizuRegister
import org.android.agoo.xiaomi.MiPushRegistar

/**
 * @author zhengjy
 * @since 2019/08/12
 * Description:
 */
object PushManager {

    private lateinit var mPushAgent: PushAgent
    private var manager: NotificationManager? = null

    private var lastNotificationTime = 0L

    @JvmStatic
    fun init(context: Context) {
        UMConfigure.init(context, AppConfig.UMENG_APP_KEY, BuildConfig.FLAVOR_product,
                UMConfigure.DEVICE_TYPE_PHONE, AppConfig.UMENG_MESSAGE_SECRET)
        UMConfigure.setLogEnabled(BuildConfig.DEBUG || AppConfig.DEVELOP)
        mPushAgent = PushAgent.getInstance(context)

        manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager?
        mPushAgent.resourcePackageName = "com.fzm.chat33"
        mPushAgent.messageHandler = object : UmengMessageHandler() {
            override fun getNotification(context: Context, uMessage: UMessage): Notification {
                val builder = NotificationCompat.Builder(context, "chatMessage")
                builder.setSmallIcon(R.drawable.ic_notification_chat)
                builder.setContentTitle(uMessage.title)
                builder.setContentText(uMessage.text)
                builder.setAutoCancel(true)

                val targetId = uMessage.extra["targetId"]
                val channelType = Integer.valueOf(uMessage.extra["channelType"]!!)
                val intent = Intent().apply {
                    if (AppConfig.MY_ID.isNullOrEmpty()) {
                        val uri = Uri.parse("${RouterHelper.APP_LINK}?type=chatNotification&channelType=$channelType&targetId=$targetId")
                        component = ComponentName(context.packageName, "com.fzm.chat33.main.activity.MainActivity")
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        putExtra("route", uri)
                    } else {
                        component = ComponentName(context.packageName, "com.fzm.chat33.main.activity.ChatActivity")
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        putExtra("channelType", channelType)
                        putExtra("targetId", targetId)
                    }
                }
                val contentIntent = PendingIntent.getActivity(context, 0, intent,
                        PendingIntent.FLAG_UPDATE_CURRENT)
                // 指定点击跳转页面
                builder.setContentIntent(contentIntent)
                return builder.build()
            }

            override fun dealWithNotificationMessage(context: Context, uMessage: UMessage) {
                Log.d("PushAgent", "notification title:${uMessage.title}  text:${uMessage.text}")
                if (AppConfig.MY_ID.isNullOrEmpty()) {
                    // 没有登录则不显示通知
                    return
                }
                if (System.currentTimeMillis() - lastNotificationTime > 1000L) {
                    // 消息通知间隔大于1s，才发出提示音
                    val uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
                    RingtoneManager.getRingtone(context, uri).play()
                }
                lastNotificationTime = System.currentTimeMillis()
                try {
                    val notification = getNotification(context, uMessage)
                    val targetId = uMessage.extra["targetId"]!!
                    manager?.notify(Integer.valueOf(targetId), notification)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            override fun dealWithCustomMessage(context: Context, uMessage: UMessage) {
                Log.d("PushAgent", "custom:${uMessage.custom}")
            }
        }
        mPushAgent.displayNotificationNumber = 5
        mPushAgent.setNotificaitonOnForeground(true)

        mPushAgent.register(object : IUmengRegisterCallback {
            override fun onSuccess(deviceToken: String) {
                //注册成功会返回deviceToken deviceToken是推送消息的唯一标志
                Log.d("PushAgent", "注册成功：deviceToken：-------->  $deviceToken")
                AppPreference.PUSH_DEVICE_TOKEN = deviceToken
                if (!AppConfig.MY_ID.isNullOrEmpty()) {
                    RequestManager.INS.setDeviceToken(deviceToken, RxSubscriber(object : OnSuccessListener<Any>() {
                        override fun onSuccess(t: Any?) {
                            Log.d("deviceToken", "deviceToken上传成功")
                        }
                    }))
                }
            }

            override fun onFailure(s: String, s1: String) {
                Log.e("PushAgent", "注册失败：-------->  s:$s,s1:$s1")
            }
        })

        if (!AppConfig.DEVELOP) {
            MiPushRegistar.register(context, AppConfig.MI_PUSH_ID, AppConfig.MI_PUSH_KEY)
            HuaWeiRegister.register(context as Application?)
            MeizuRegister.register(context, AppConfig.MEIZU_PUSH_ID, AppConfig.MEIZU_PUSH_KEY)
//            OppoRegister.register(this, "appkey", "appSecret");
//            VivoRegister.register(this);
        }

        LiveBus.of(BusEvent::class.java).loginEvent().observeForever { login ->
            if (login) {
                enablePush()
            } else {
                disablePush()
            }
        }
    }

    private fun enablePush() {
        mPushAgent.enable(object : IUmengCallback {
            override fun onSuccess() {
                Log.d("PushAgent", "启用成功")
            }

            override fun onFailure(s: String, s1: String) {
                Log.e("PushAgent", "启用失败：-------->  s:$s,s1:$s1")
            }
        })
    }

    private fun disablePush() {
        mPushAgent.disable(object : IUmengCallback {
            override fun onSuccess() {
                Log.d("PushAgent", "停用成功")
            }

            override fun onFailure(s: String, s1: String) {
                Log.e("PushAgent", "停用失败：-------->  s:$s,s1:$s1")
            }
        })
    }
}
