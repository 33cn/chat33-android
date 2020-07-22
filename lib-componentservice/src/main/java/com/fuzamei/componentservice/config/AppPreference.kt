package com.fuzamei.componentservice.config

import android.media.AudioManager
import com.fuzamei.common.utils.PreferenceDelegate

/**
 * @author zhengjy
 * @since 2020/01/13
 * Description:应用存储的SharedPreferences
 */
object AppPreference {
    /**
     * 用户uid
     */
    var USER_UID by PreferenceDelegate("user_uid", "")
    /**
     * 用户id
     */
    var USER_ID by PreferenceDelegate("user_id", "")
    /**
     * 聊天服务器session
     */
    var SESSION_KEY by PreferenceDelegate("app_session", "")
    var CURRENT_TARGET_ID by PreferenceDelegate("current_target_id", "")
    /**
     * 账户体系token
     */
    var TOKEN by PreferenceDelegate("token", "")
    @JvmStatic
    var bearerToken = "Bearer $TOKEN"
        private set
    /**
     * 友盟推送token
     */
    var PUSH_DEVICE_TOKEN by PreferenceDelegate("PUSH_DEVICE_TOKEN", "")
    /**
     * 记录上一次刷新时最新的记录id或时间
     */
    var LAST_FRIEND_LIST_REFRESH by PreferenceDelegate("LAST_FRIEND_LIST_REFRESH", 0L)
    /**
     * 登录类型：手机密码，手机验证，邮箱密码，邮箱验证
     */
    var USER_LOGIN_TYPE by PreferenceDelegate("USER_LOGIN_TYPE", 1)
    /**
     * 当前已读的群公告
     */
    @Deprecated(message = "在SharedPrefUtil中使用", level = DeprecationLevel.ERROR)
    var READ_CURRENT_NOTICE by PreferenceDelegate("READ_CURRENT_NOTICE", "0")
    /**
     * 语音播放模式
     */
    var SOUND_PLAY_MODE by PreferenceDelegate("SOUND_PLAY_MODE", AudioManager.MODE_NORMAL)
    /**
     * 设备id，在每次重新安装之后发生改变
     */
    @Deprecated(message = "在SharedPrefUtil中使用", level = DeprecationLevel.ERROR)
    var DEVICE_ID by PreferenceDelegate("DEVICE_ID", "")
    /**
     * 上一个登录的账号
     */
    var LAST_LOGIN_PHONE by PreferenceDelegate("LAST_LOGIN_PHONE", "")
    /**
     * 上一次保持前台的时间
     */
    var LAST_FOREGROUND by PreferenceDelegate("LAST_FOREGROUND", 0L)
    /**
     * 上一次转账收款的币种
     */
    var LAST_COIN by PreferenceDelegate("LAST_COIN", "YCC")
}