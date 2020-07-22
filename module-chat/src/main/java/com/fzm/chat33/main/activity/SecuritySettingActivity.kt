package com.fzm.chat33.main.activity

import android.view.View
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.alibaba.android.arouter.facade.annotation.Route
import com.alibaba.android.arouter.launcher.ARouter
import com.fuzamei.componentservice.app.AppRoute
import com.fuzamei.componentservice.base.DILoadableActivity
import com.fuzamei.componentservice.ext.findViewModel
import com.fzm.chat33.R
import com.fzm.chat33.core.utils.UserInfoPreference
import com.fzm.chat33.main.mvvm.SettingViewModel
import kotlinx.android.synthetic.main.activity_security_setting.*
import javax.inject.Inject

/**
 * @author zhengjy
 * @since 2019/05/24
 * Description:用户安全设置界面
 */
@Route(path = AppRoute.SECURITY_SETTING)
class SecuritySettingActivity : DILoadableActivity(), View.OnClickListener {

    private var isSetPayPwd = false

    @Inject
    lateinit var provider: ViewModelProvider.Factory
    lateinit var viewModel: SettingViewModel

    override fun getLayoutId(): Int {
        return R.layout.activity_security_setting
    }

    override fun enableSlideBack(): Boolean {
        return true
    }

    override fun initView() {
        viewModel = findViewModel(provider)
        ctb_title.setMiddleText(getString(R.string.chat_title_security_setting))
        ctb_title.setRightVisible(false)
        tv_encrypted_password_tips.text = getString(R.string.chat_tips_action1)
        viewModel.loading.observe(this, Observer { setupLoading(it) })
        viewModel.isSetPayPassword.observe(this, Observer {
            val mode: Int = if (it.state == 0) {
                PayPasswordActivity.SET_PASSWORD
            } else {
                PayPasswordActivity.UPDATE_PASSWORD
            }
            ARouter.getInstance().build(AppRoute.PAY_PASSWORD).withInt("mode", mode).navigation()
        })
    }

    override fun initData() {
        refreshState()
    }

    override fun onResume() {
        super.onResume()
        refreshState()
    }

    private fun refreshState() {
        isSetPayPwd = UserInfoPreference.getInstance().getBooleanPref(UserInfoPreference.SET_PAY_PASSWORD, false)
        tv_pay_password_tips.text = getString(if (isSetPayPwd) R.string.chat_tips_action1 else R.string.chat_tips_action2)

    }

    override fun setEvent() {
        ctb_title.setLeftListener { finish() }
        ll_pay_password.setOnClickListener(this)
        ll_encrypted_password?.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.ll_pay_password -> {
                if (isSetPayPwd) {
                    ARouter.getInstance().build(AppRoute.PAY_PASSWORD).withInt("mode", PayPasswordActivity.UPDATE_PASSWORD).navigation()
                } else {
                    viewModel.isSetPayPassword()
                }
            }
            R.id.ll_encrypted_password -> {
                ARouter.getInstance().build(AppRoute.ENCRYPT_PWD).withBoolean("setMode", false).navigation()
//                if (isSetEncryptPwd) {
//                    ARouter.getInstance().build(AppRoute.ENCRYPT_PWD).withBoolean("setMode", false).navigation()
//                } else {
//                    val setPassword = SetEncryptPasswordDialog(this, false)
//                    setPassword.setOnDismissListener {
//                        refreshState()
//                    }
//                    setPassword.show()
//                }
            }
        }
    }
}