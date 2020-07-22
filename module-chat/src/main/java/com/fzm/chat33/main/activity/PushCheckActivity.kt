package com.fzm.chat33.main.activity

import android.annotation.SuppressLint
import com.fuzamei.componentservice.base.LoadableActivity
import com.fzm.chat33.R
import kotlinx.android.synthetic.main.activity_push_check.*
import androidx.core.app.NotificationManagerCompat
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.provider.Settings.EXTRA_APP_PACKAGE
import android.provider.Settings.EXTRA_CHANNEL_ID
import android.view.View
import com.alibaba.android.arouter.facade.annotation.Route
import com.fuzamei.componentservice.app.AppRoute

/**
 * @author zhengjy
 * @since 2019/06/04
 * Description:检查消息推送通知权限是否打开
 */
@Route(path = AppRoute.PUSH_CHECK)
class PushCheckActivity : LoadableActivity() {

    override fun getLayoutId(): Int {
        return R.layout.activity_push_check
    }

    override fun initView() {
        ctb_title.setMiddleText(getString(R.string.chat_setting_notification))
        ctb_title.setRightVisible(false)
    }

    override fun initData() {
        checkNotifySetting()
    }

    override fun setEvent() {
        ctb_title.setLeftListener { finish() }
        tv_setting.setOnClickListener { gotoNotificationSetting() }
    }

    private fun gotoNotificationSetting() {
        try {
            val intent = Intent().apply {
                when {
                    Build.VERSION.SDK_INT >= Build.VERSION_CODES.O -> {
                        //这种方案适用于 API 26, 即8.0（含8.0）以上可以用
                        action = Settings.ACTION_APP_NOTIFICATION_SETTINGS
                        putExtra(EXTRA_APP_PACKAGE, packageName)
                        putExtra(EXTRA_CHANNEL_ID, applicationInfo.uid)
                    }
                    else -> {
                        //这种方案适用于 API21——25，即 5.0——7.1 之间的版本可以使用
                        putExtra("app_package", packageName)
                        putExtra("app_uid", applicationInfo.uid)
                    }
                }
            }
            startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
            // 部分手机出现异常则跳转到应用设置界面
            val intent = Intent().apply {
                action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                data = Uri.fromParts("package", packageName, null)
            }
            startActivity(intent)
        }
    }

    @SuppressLint("SetTextI18n")
    private fun checkNotifySetting() {
        val manager = NotificationManagerCompat.from(this)
        val isOpened = manager.areNotificationsEnabled()

        if (isOpened) {
            tv_content.text = getString(R.string.chat_permission_notification_opened_message, Build.MODEL, Build.VERSION.SDK_INT, Build.VERSION.RELEASE, packageName)
            tv_setting.visibility = View.GONE
        } else {
            tv_content.text = getString(R.string.chat_error_permission_notification)
            tv_setting.visibility = View.VISIBLE
        }
    }
}