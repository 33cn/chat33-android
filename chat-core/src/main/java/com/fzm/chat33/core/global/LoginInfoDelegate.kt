package com.fzm.chat33.core.global

import android.os.Looper
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.fuzamei.common.bus.LiveBus
import com.fuzamei.common.net.Result
import com.fuzamei.common.net.rxjava.ApiException
import com.fuzamei.common.utils.RoomUtils
import com.fuzamei.componentservice.app.BusEvent
import com.fuzamei.componentservice.config.AppConfig
import com.fuzamei.componentservice.config.AppPreference
import com.fuzamei.componentservice.ext.getDistinct
import com.fzm.chat33.core.db.ChatDatabase
import com.fzm.chat33.core.logic.MessageDispatcher
import com.fzm.chat33.core.net.socket.ChatSocket
import com.fzm.chat33.core.source.UserDataSource
import com.fzm.chat33.core.utils.UserInfoPreference
import com.fzm.chat33.core.utils.UserInfoPreference.SET_PAY_PASSWORD
import com.fzm.chat33.core.utils.UserInfoPreference.USER_MNEMONIC_WORDS
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.atomic.AtomicInteger
import javax.inject.Inject
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

/**
 * @author zhengjy
 * @since 2019/10/15
 * Description:本地登录信息
 */
interface LoginInfoDelegate {

    /**
     * 当前登录用户信息
     */
    val currentUser: LiveData<UserInfo>

    /**
     * 登录失败事件
     */
    val loginFail: LiveData<ApiException>

    /**
     * 登录操作
     */
    fun performLogin()

    /**
     * 注销操作
     */
    fun performLogout()

    /**
     * 更新用户信息
     */
    fun updateInfo()

    /**
     * 手动更新用户信息
     */
    fun updateInfo(block: UserInfo.() -> Unit)

    /**
     * 获取用户id
     */
    fun getUserId(): String?

    /**
     * 是否已经登录
     */
    fun isLogin(): Boolean
}

class LoginInfoDelegateImpl @Inject constructor(
        private val dataSource: UserDataSource,
        private val socket: ChatSocket
) : UserDataSource by dataSource, LoginInfoDelegate, CoroutineScope {

    override val coroutineContext: CoroutineContext
        get() = EmptyCoroutineContext

    private val userPreference: UserInfoPreference
        get() = UserInfoPreference.getInstance()

    private val _currentUser by lazy {
        object : MutableLiveData<UserInfo>() {
            override fun postValue(value: UserInfo?) {
                if (Looper.getMainLooper() == Looper.myLooper()) {
                    setValue(value)
                } else {
                    super.postValue(value)
                }
            }
        }
    }

    private val _distinctUser = _currentUser.getDistinct()

    override val currentUser: LiveData<UserInfo>
        get() = _distinctUser

    private val _loginFail by lazy { MutableLiveData<ApiException>() }
    override val loginFail: LiveData<ApiException>
        get() = _loginFail

    private val loginFailCount: AtomicInteger by lazy { AtomicInteger(0) }

    private val loginFailData: MutableLiveData<AtomicInteger> by lazy { MutableLiveData<AtomicInteger>() }

    init {
        loginFailData.observeForever {
            if (it.get() == 2) {
                // 两次登录（数据库、网络请求）都失败，则通知登录失败
                _loginFail.postValue(ApiException(0))
            }
        }
    }

    private suspend fun loadUserInfoFromNet(): Result<UserInfo>? {
        val type = AppPreference.USER_LOGIN_TYPE
        val result = login(type)
        return if (!result.isSucceed()) {
            if (result.error().errorCode == -1004           // token过期
                    || result.error().errorCode == -2030) { // 帐号被封禁
                _loginFail.postValue(result.error())
                result
            } else {
                loginFailCount.incrementAndGet()
                loginFailData.postValue(loginFailCount)
                null
            }
        } else {
            result
        }
    }

    private fun loadUserInfoFromDb(): Result<UserInfo> {
        val id = AppPreference.USER_ID
        val info = ChatDatabase.getInstance(id).userInfoDao().getUserInfoByUid(id)
        return if (info != null) {
            info.privateKey = UserInfoPreference.getInstance(id).getStringPref(USER_MNEMONIC_WORDS, "")
            Result.Success(info)
        } else {
            loginFailCount.incrementAndGet()
            loginFailData.postValue(loginFailCount)
            Result.Error(ApiException(0))
        }
    }

    override fun updateInfo() {
        if (isLogin()) {
            launch(Dispatchers.Main) {
                withContext(Dispatchers.IO) {
                    getUserInfo(getUserId() ?: "")
                }.dataOrNull()?.let {
                    UserInfo.getInstance().setUserInfo1(it)
                    RoomUtils.run(Runnable {
                        ChatDatabase.getInstance().userInfoDao().update(it)
                    })
                    _currentUser.postValue(it)
                }
            }
        }
    }

    override fun updateInfo(block: UserInfo.() -> Unit) {
        if (isLogin()) {
            val info = _currentUser.value?.apply(block)
            UserInfo.getInstance().setUserInfo1(info)
            RoomUtils.run(Runnable {
                ChatDatabase.getInstance().userInfoDao().update(info)
            })
            _currentUser.postValue(info)
        }
    }

    override fun getUserId(): String? {
        return _currentUser.value?.id
    }

    override fun isLogin(): Boolean {
        return _currentUser.value?.isLogin ?: false
    }

    override fun performLogin() {
        launch(Dispatchers.IO) {
            // 先从本地数据库读取用户信息
            loadUserInfoFromDb().dataOrNull()?.let {
                if (!isLogin()) {
                    // 如果服务端先登录成功，则忽略本地登录结果
                    onSetupLogin(it)
                    _currentUser.postValue(it)
                    if (LiveBus.of(BusEvent::class.java).loginEvent().value != true) {
                        LiveBus.of(BusEvent::class.java).loginEvent().setValue(true)
                    }
                }
            }
        }
        launch(Dispatchers.IO) {
            // 再请求服务端接口，更新用户信息
            loadUserInfoFromNet()?.dataOrNull()?.let {
                onSetupLogin(it)
                setupLocalData(it)
                _currentUser.postValue(it)
                if (LiveBus.of(BusEvent::class.java).loginEvent().value != true) {
                    LiveBus.of(BusEvent::class.java).loginEvent().setValue(true)
                }
            }
        }
    }

    override fun performLogout() {
        launch(Dispatchers.IO) {
            dataSource.logout()
        }
        onSetupLogout()
        _currentUser.postValue(UserInfo.EMPTY_USER)
        LiveBus.of(BusEvent::class.java).loginEvent().setValue(false)
    }

    /**
     * 登录时需要保存的一些数据
     */
    private fun onSetupLogin(info: UserInfo) {
        UserInfo.getInstance().setUserInfo0(info)
        AppConfig.MY_ID = info.id
    }

    private fun setupLocalData(info: UserInfo) {
        RoomUtils.run(Runnable {
            ChatDatabase.getInstance().userInfoDao().insert(info)
        })
        userPreference.setBooleanPref(SET_PAY_PASSWORD, info.isSetPayPwd == 1)
        AppPreference.USER_ID = info.id
        AppPreference.USER_UID = info.uid
    }

    /**
     * 注销时需要重置的一些数据
     */
    private fun onSetupLogout() {
        loginFailCount.set(0)
        socket.disconnect()
        AppPreference.USER_ID = ""
        AppPreference.USER_UID = ""
        AppPreference.TOKEN = ""
        AppPreference.SESSION_KEY = ""
        AppConfig.MY_ID = ""
        AppConfig.CHAT_SESSION = ""
        ChatDatabase.reset()
        UserInfoPreference.reset()
        MessageDispatcher.reset()
        UserInfo.reset()
    }
}