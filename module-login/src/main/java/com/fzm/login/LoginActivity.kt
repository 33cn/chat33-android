package com.fzm.login

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.CountDownTimer
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.view.KeyEvent
import android.view.View
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.alibaba.android.arouter.facade.annotation.Autowired
import com.alibaba.android.arouter.facade.annotation.Route
import com.alibaba.android.arouter.launcher.ARouter
import com.fuzamei.common.utils.BarUtils
import com.fuzamei.common.utils.KeyboardUtils
import com.fuzamei.common.utils.ShowUtils
import com.fuzamei.componentservice.app.AppRoute
import com.fuzamei.componentservice.app.RouterHelper
import com.fuzamei.componentservice.base.DILoadableActivity
import com.fuzamei.componentservice.config.AppConfig
import com.fuzamei.componentservice.config.AppPreference
import com.fuzamei.componentservice.ext.findViewModel
import com.fzm.chat33.login.R
import kotlinx.android.synthetic.main.chat_activity_login.*
import org.kodein.di.generic.instance

/**
 * @author zhengjy
 * @since 2019/03/08
 * Description:
 */
@Route(path = AppRoute.LOGIN)
class LoginActivity : DILoadableActivity() {

    val REQUESTCODE_VERIFY = 111

    private var mPhone: String = ""
        get() {
            return field.replace(" ".toRegex(), "")
        }
    private var msgCountDownTimer: MsgCountDownTimer? = null

    private val provider: ViewModelProvider.Factory by instance(tag = "login")
    private lateinit var viewModel: LoginViewModel

    /**
     * 用于控制是否在登录之前就能看打开应用内浏览器
     */
    @Autowired
    @JvmField
    var showAD: Boolean = false
    @Autowired
    @JvmField
    var data: Bundle? = null

    override fun setStatusBar() {
        BarUtils.setStatusBarColor(this, ContextCompat.getColor(this, R.color.basic_transparent), 0)
        BarUtils.addMarginTopEqualStatusBarHeight(this, chat_iv_logo)
        BarUtils.setStatusBarLightMode(this, false)
    }

    override fun getLayoutId(): Int {
        return R.layout.chat_activity_login
    }

    override fun initView() {
        ARouter.getInstance().inject(this)
        viewModel = findViewModel(provider)
    }

    override fun initData() {
        val phone = AppPreference.LAST_LOGIN_PHONE
        if (phone.isNotEmpty()) {
            et_phone.setText(phone)
            et_phone.setSelection(phone.length)
            mPhone = phone
        }
    }

    override fun setEvent() {
        viewModel.loading.observe(this, Observer { setupLoading(it) })
        viewModel.loginResult.observe(this, Observer {
            if (it != null) {
                dismiss()
                ShowUtils.showToastNormal(instance, if (it.type == 0) R.string.login_tips_register_success else R.string.login_tips_login_success)
                AppPreference.LAST_LOGIN_PHONE = et_phone.text.toString().trim()
                AppPreference.TOKEN = it.token
                ARouter.getInstance().build(AppRoute.MAIN).withBundle("data", data).navigation()
                finish()
            }
        })
        viewModel.codeResult.observe(this, Observer {
            if (it != null) {
                AppPreference.LAST_LOGIN_PHONE = et_phone.text.toString().trim()
                ShowUtils.showToastNormal(instance, R.string.login_tips_code_sent)
                msgCountDownTimer = MsgCountDownTimer()
                msgCountDownTimer?.codeView = tv_get_code
                msgCountDownTimer?.start()
                et_code.postDelayed({
                    KeyboardUtils.showKeyboard(et_code)
                }, 1000)
            }
        })
        et_phone.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                if (count == 1) {
                    val length = s.toString().length
                    if (length == 3 || length == 8) {
                        et_phone.setText("$s ")
                        et_phone.setSelection(et_phone.text.toString().length)
                    }
                }
                if (start == 3 || start == 8) {
                    if (before == 1 && count == 0) {
                        et_phone.setText(s.subSequence(0, s.length - 1))
                        et_phone.setSelection(et_phone.text.toString().length)
                    }
                }
                mPhone = s.toString()
            }

            override fun afterTextChanged(s: Editable) {
            }
        })
        tv_protocol.setOnClickListener {
            ARouter.getInstance().build(AppRoute.WEB_BROWSER)
                    .withString("title", getString(R.string.login_tips_privacy_title))
                    .withString("url", AppConfig.APP_AGREEMENT_URL)
                    .withBoolean("showOptions", false)
                    .navigation()
        }
        tv_get_code.setOnClickListener {
            if (checkPhone()) {
                viewModel.sendCode(mPhone)
            }
        }
        btn_login.setOnClickListener {
            if (checkPhone() && checkCode()) {
                if (ll_user_protocol.visibility == View.VISIBLE && !cb_select.isChecked) {
                    ShowUtils.showToastNormal(instance, R.string.login_tip_agree_protocol)
                    return@setOnClickListener
                }
                KeyboardUtils.hideKeyboard(btn_login)
                val code = et_code.text.toString().trim()
                viewModel.login(mPhone, code)
            }
        }
        val route: Uri? = data?.getParcelable("route")
        val type = route?.getQueryParameter("type")
        if (showAD && type == "appWebBrowser") {
            val path = RouterHelper.routeMap[type]
            if (!TextUtils.isEmpty(path)) {
                val uri = Uri.parse(RouterHelper.APP_LINK + path + "?" + route.query)
                ARouter.getInstance().build(uri).navigation()
            }
            data?.putParcelable("route", null)
        }
    }

    private fun checkPhone(): Boolean {
        if (mPhone.isEmpty()) {
            ShowUtils.showToastNormal(this, R.string.login_tips_input_phone)
            return false
        }
        if (mPhone.length != 11) {
            ShowUtils.showToastNormal(this, R.string.login_tips_correct_phone)
            return false
        }
        return true
    }

    private fun checkCode(): Boolean {
        if (et_code.text.toString().trim().isEmpty()) {
            ShowUtils.showToastNormal(this, R.string.login_tips_input_code)
            return false
        }
        if (et_code.text.toString().trim().length < 6) {
            ShowUtils.showToastNormal(this, R.string.login_tips_correct_code)
            return false
        }
        return true
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            val intent = Intent()
            intent.action = Intent.ACTION_MAIN
            intent.addCategory(Intent.CATEGORY_HOME)
            startActivity(intent)
            return true
        }
        return super.onKeyDown(keyCode, event)
    }

    override fun onDestroy() {
        super.onDestroy()
        msgCountDownTimer?.cancel()
    }

    inner class MsgCountDownTimer(
            millisInFuture: Long = 60_000L,
            countDownInterval: Long = 1_000L
    ) : CountDownTimer(millisInFuture, countDownInterval) {
        var codeView: TextView? = null

        override fun onTick(millisUntilFinished: Long) {
            codeView?.isClickable = false
            codeView?.text = getString(R.string.login_tips_code_count, millisUntilFinished / 1000)
        }

        override fun onFinish() {
            codeView?.setText(R.string.login_action_send_code)
            codeView?.isClickable = true
        }
    }
}