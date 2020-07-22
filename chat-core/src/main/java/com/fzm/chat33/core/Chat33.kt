package com.fzm.chat33.core

import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.app.ActivityManager
import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.os.Build
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import com.alibaba.android.arouter.launcher.ARouter
import com.baidu.crabsdk.CrabSDK
import com.fuzamei.common.FzmFramework
import com.fuzamei.common.bus.LiveBus
import com.fuzamei.common.net.rxjava.ApiException
import com.fuzamei.common.utils.*
import com.fuzamei.common.utils.RoomUtils.Companion.subscribe
import com.fuzamei.componentservice.app.BusEvent
import com.fuzamei.componentservice.app.ChatRouter
import com.fuzamei.componentservice.app.SimpleChatRouter
import com.fuzamei.componentservice.config.AppConfig
import com.fuzamei.componentservice.config.AppPreference
import com.fuzamei.componentservice.di.BaseInjectors
import com.fuzamei.componentservice.event.NewFriendRequestEvent
import com.fuzamei.componentservice.helper.WeChatHelper
import com.fzm.chat33.core.db.ChatDatabase
import com.fzm.chat33.core.db.bean.FriendBean
import com.fzm.chat33.core.db.bean.InfoCacheBean
import com.fzm.chat33.core.db.bean.RoomListBean
import com.fzm.chat33.core.db.bean.RoomUserBean
import com.fzm.chat33.core.di.chatModule
import com.fzm.chat33.core.di.coreRepoModule
import com.fzm.chat33.core.di.dataSourceModule
import com.fzm.chat33.core.global.EventReceiver
import com.fzm.chat33.core.global.LoginInfoDelegate
import com.fzm.chat33.core.global.UserInfo
import com.fzm.chat33.core.listener.NetworkChangeListener
import com.fzm.chat33.core.logic.MessageDispatcher.Companion.addEventReceiver
import com.fzm.chat33.core.logic.MessageDispatcher.Companion.removeEventReceiver
import com.fzm.chat33.core.receiver.NetworkChangeReceiver
import com.fzm.chat33.core.service.MessageService
import com.fzm.chat33.core.source.impl.DatabaseLocalContactDataSource.Companion.get
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.functions.Consumer
import org.kodein.di.Kodein
import org.kodein.di.conf.KodeinGlobalAware
import org.kodein.di.conf.global
import org.kodein.di.generic.instance
import java.security.Security
import java.util.*

/**
 * @author zhengjy
 * @since 2019/12/04
 * Description:
 */
@SuppressLint("StaticFieldLeak")
object Chat33: KodeinGlobalAware {

    private var context: Context? = null

    private var router: ChatRouter? = null

    private val loginDelegate: LoginInfoDelegate by instance()

    private val msgListeners by lazy { arrayListOf<OnMsgCountChangeListener>() }

    private var dispose: CompositeDisposable? = null

    // 从App.java中移过来
    var ignoreUpdate = false
    val newFriendRequest = HashMap<String, NewFriendRequestEvent>()
    var snapModeList: List<String> = ArrayList()

    /**
     * 初始化聊天核心模块的方法
     *
     * 应用中的配置[AppConfig]可以通过覆盖配置文件的方式来修改
     *
     * @param context   Application context
     * @param router    自定义路由跳转
     */
    @JvmStatic
    @JvmOverloads
    fun init(context: Application, debug: Boolean = false, router: ChatRouter = SimpleChatRouter()) {
        this.context = context
        this.router = router
        FzmFramework.init(context)
        Kodein.global.apply {
            BaseInjectors
            // 聊天依赖模块
            addImport(chatModule())
            addImport(dataSourceModule())
            addImport(coreRepoModule())
        }
        if (debug) { // 这两行必须写在init之前，否则这些配置在init过程中将无效
            ARouter.openLog() // 打印日志
            ARouter.openDebug() // 开启调试模式(如果在InstantRun模式下运行，必须开启调试模式！线上版本需要关闭,否则有安全风险)
            ARouter.printStackTrace() // 打印日志的时候打印线程堆栈
        }
        ARouter.init(context)
        ActivityUtils.registerActivityLifecycleCallbacks(context)
        WeChatHelper.INS.init(context)
        // 适配Android8.0通知栏
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            var channelId = "chatMessage"
            var channelName = context.getString(R.string.core_message_title)
            var importance = NotificationManager.IMPORTANCE_MAX
            createNotificationChannel(channelId, channelName, importance)
            channelId = "notification"
            channelName = context.getString(R.string.core_notification_title)
            importance = NotificationManager.IMPORTANCE_LOW
            createNotificationChannel(channelId, channelName, importance)
        }
    }

    /**
     * 登录方法，需要提供外部token
     *
     * @param token     账户系统token
     * @param callback  登录结果回调
     */
    @JvmStatic
    fun login(token: String, callback: OnLoginCallback?) {
        AppPreference.TOKEN = token
        loginDelegate.loginFail.observeForever(object : Observer<ApiException> {
            override fun onChanged(t: ApiException?) {
                callback?.onFail(t)
                loginDelegate.loginFail.removeObserver(this)
            }
        })
        LiveBus.of(BusEvent::class.java).loginEvent().observeForever(object : Observer<Boolean> {
            override fun onChanged(login: Boolean?) {
                if (login == true) {
                    checkService()
                    callback?.onSuccess()
                    afterLogin()
                    LiveBus.of(BusEvent::class.java).loginEvent().removeObserver(this)
                }
            }
        })
        loginDelegate.performLogin()
    }

    /**
     * 退出登录，清除用户信息
     */
    @JvmStatic
    fun logout() {
        msgListeners.clear()
        dispose?.dispose()
        dispose = null
        loginDelegate.performLogout()
    }

    /**
     * 获取当前登录用户的信息
     */
    @JvmStatic
    fun getCurrentUser(): LiveData<UserInfo> {
        return loginDelegate.currentUser
    }

    private fun afterLogin() {
        dispose = CompositeDisposable()
        dispose?.add(subscribe(ChatDatabase.getInstance().recentMessageDao().allMsgCount, Consumer { integer ->
            msgListeners.forEach {
                it.onMsgCountChange(integer)
            }
        }))
    }

    /**
     * 创建通知渠道
     *
     * @param channelId     渠道标识id
     * @param channelName   渠道名
     * @param importance    通知优先级
     */
    @TargetApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel(channelId: String, channelName: String, importance: Int) {
        val channel = NotificationChannel(channelId, channelName, importance)
        val notificationManager = context?.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager?
        notificationManager?.createNotificationChannel(channel)
    }

    /**
     * 设置网络状态变化监听
     */
    @JvmStatic
    fun setNetworkChangeListener(listener: NetworkChangeListener?) {
        val filter = IntentFilter()
        filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION)
        val receiver = NetworkChangeReceiver(listener)
        getContext().registerReceiver(receiver, filter)
    }

    @JvmStatic
    fun getContext(): Context {
        if (context == null) {
            throw NullPointerException("please call init() first")
        }
        return context!!
    }

    @JvmStatic
    fun getRouter(): ChatRouter {
        if (context == null) {
            throw NullPointerException("please call init() first")
        }
        return router!!
    }

    /**
     * 注册事件监听
     *
     * @param eventReceiver
     */
    @JvmStatic
    fun registerEventReceiver(eventReceiver: EventReceiver?) {
        addEventReceiver(eventReceiver!!)
    }

    /**
     * 解除注册事件监听
     *
     * @param eventReceiver
     */
    @JvmStatic
    fun unregisterEventReceiver(eventReceiver: EventReceiver?) {
        removeEventReceiver(eventReceiver!!)
    }


    /**
     * 从本地数据库加载群成员信息到缓存
     */
    @JvmStatic
    fun loadRoomUsers() {
        dispose?.add(subscribe(ChatDatabase.getInstance().roomUserDao().allRoomUsers, Consumer { roomUserBeans ->
            for (bean in roomUserBeans) {
                getLocalCache().roomUserMap.put(bean.roomId + "-" + bean.id, bean)
            }
        }))
    }

    @JvmStatic
    fun loadInfoFromCache(channelType: Int, id: String?): InfoCacheBean? {
        return localCache.infoCacheMap["$channelType-$id"]
    }

    @JvmStatic
    fun loadRoomUserFromCache(roomId: String?, userId: String?): RoomUserBean? {
        return localCache.roomUserMap["$roomId-$userId"]
    }

    @JvmStatic
    fun loadFriendFromCache(id: String?): FriendBean? {
        return get().getLocalFriendById(id)
    }

    @JvmStatic
    fun loadRoomFromCache(id: String?): RoomListBean? {
        return get().getLocalRoomById(id)
    }

    @JvmStatic
    fun loadRoomListFromCache(): List<RoomListBean?>? {
        return get().getLocalRoomList()
    }

    @JvmStatic
    fun getLocalCache(): LocalCache {
        return localCache
    }

    private val localCache = LocalCache()

    /**
     * 从本地数据库加载一些临时用户的缓存信息
     */
    @JvmStatic
    fun loadInfoCache() {
        dispose?.add(subscribe(ChatDatabase.getInstance().infoCacheDao().allInfoCache, Consumer { infoCacheBeans ->
            for (bean in infoCacheBeans) {
                getLocalCache().infoCacheMap.put(bean.channelType.toString() + "-" + bean.id, bean)
            }
        }))
    }

    @JvmStatic
    fun checkService() {
        if (context == null) {
            throw NullPointerException("please call init() first")
        }
        if (UserInfo.getInstance().isLogin && !isServiceWorked() && !ActivityUtils.isBackground()) {
            try {
                LogUtils.i("重启消息Service")
                context?.startService(Intent(context, MessageService::class.java))
            } catch (e: Exception) {
                CrabSDK.uploadException(e)
            }
        }
    }

    /**
     * 检查消息接收服务是否在运行
     */
    @JvmStatic
    fun isServiceWorked(): Boolean {
        try {
            val manager = context?.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager?
            val runningService = manager?.getRunningServices(Integer.MAX_VALUE) ?: return false
            for (i in runningService.indices) {
                if (runningService[i].service.className == "com.fzm.chat33.core.service.MessageService") {
                    if (runningService[i].process.startsWith(context?.packageName ?: "")) {
                        return true
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return false
    }


    /**
     * 检查守护服务是否在运行
     */
    @JvmStatic
    fun isDaemonServiceWorked(): Boolean {
        try {
            val manager = context?.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager?
            val runningService = manager?.getRunningServices(Integer.MAX_VALUE) ?: return false
            for (i in runningService.indices) {
                if (runningService[i].service.className == "com.fzm.chat33.core.service.DaemonService") {
                    if (runningService[i].process.startsWith(context?.packageName ?: "")) {
                        return true
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return false
    }

    class LocalCache {
        var infoCacheMap = LruCache<String, InfoCacheBean>(500)
        var roomUserMap = LruCache<String, RoomUserBean>(500)
        @JvmField
        var localPathMap = HashMap<String, String>()
        @JvmField
        var snapModeList: List<String> = ArrayList()
    }

    /**
     * 添加未读消息数监听
     */
    fun addOnMsgCountChangeListener(listener: OnMsgCountChangeListener) {
        if (!msgListeners.contains(listener)) {
            msgListeners.add(listener)
        }
    }

    /**
     * 移除未读消息数监听
     */
    fun removeOnMsgCountChangeListener(listener: OnMsgCountChangeListener) {
        msgListeners.remove(listener)
    }

    interface OnLoginCallback {
        /**
         * 登录成功
         */
        fun onSuccess()

        /**
         * 登录失败
         */
        fun onFail(t: Throwable?)
    }

    interface OnMsgCountChangeListener {
        /**
         * 总消息数目变化
         *
         * @param count
         */
        fun onMsgCountChange(count: Int)
    }

    interface OnReceiveApplyListener {
        /**
         * 启动时有新的好友或者群请求
         *
         * @param number
         */
        fun onReceiveApply(number: Int)
    }
}