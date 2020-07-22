package com.fzm.chat33.main.fragment

import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.text.Html
import android.view.View
import com.fuzamei.common.utils.KeyboardUtils
import com.fuzamei.common.utils.ShowUtils
import com.fuzamei.common.utils.ToolUtils
import com.fuzamei.componentservice.base.DILoadableFragment
import com.fuzamei.componentservice.config.AppConfig
import com.fuzamei.componentservice.ext.findViewModel
import com.fzm.chat33.R
import com.fzm.chat33.core.request.PayPasswordRequest
import com.fzm.chat33.main.activity.PayPasswordActivity
import com.fzm.chat33.main.mvvm.PayPasswordViewModel
import com.fzm.chat33.utils.CodeTimer
import com.fzm.chat33.utils.TipsDialogUtil
import com.fzm.chat33.widget.ChatCodeView
import com.fzm.chat33.widget.VerifyCodeDialog
import kotlinx.android.synthetic.main.fragment_set_pay_password.*
import javax.inject.Inject

/**
 * @author zhengjy
 * @since 2019/03/13
 * Description:
 */
class SetPayPasswordFragment : DILoadableFragment() {

    companion object {
        const val REQUEST_CODE_VERIFY = 1

        fun create(mode: Int = PayPasswordActivity.SET_PASSWORD, oldPassword: String = ""): SetPayPasswordFragment {
            val fragment = SetPayPasswordFragment()
            val bundle = Bundle()
            bundle.putInt("mode", mode)
            bundle.putString("oldPassword", oldPassword)
            fragment.arguments = bundle
            return fragment
        }
    }

    @Inject
    lateinit var provider: ViewModelProvider.Factory
    private lateinit var viewModel: PayPasswordViewModel
    var dialog: VerifyCodeDialog? = null
    var timer: CodeTimer? = null
    var mode: Int? = null
    var mCode: String = ""
    var oldPassword: String = ""

    var mPassword: String = ""
    var mType: String = ""

    override fun getLayoutId(): Int {
        mode = arguments?.get("mode") as Int
        oldPassword = arguments?.get("oldPassword") as String
        return R.layout.fragment_set_pay_password
    }

    override fun initView(view: View?, savedInstanceState: Bundle?) {
        viewModel = findViewModel(provider)
        tv_tips.text = Html.fromHtml(getString(R.string.chat_tips_pay_password1,
                AppConfig.APP_ACCENT_COLOR_STR,
                ToolUtils.encryptPhoneNumber(viewModel.currentUser.value?.account),
                getString(if (mode == PayPasswordActivity.SET_PASSWORD) R.string.chat_title_pay_password1 else R.string.chat_title_pay_password2)))
        if (mode == PayPasswordActivity.UPDATE_PASSWORD) {
            pay_password.postDelayed({
                pay_password.performClick()
            }, 200)
        } else {
            showVerifyDialog()
        }
    }

    override fun initData() {

    }

    override fun setEvent() {
        viewModel.setPayPassword.observe(this, Observer {
            if (it == null) {
                val tips = getString(
                        if (mode == PayPasswordActivity.SET_PASSWORD)
                            R.string.chat_tips_pay_password2
                        else
                            R.string.chat_tips_pay_password3
                )
                TipsDialogUtil.showSuccessTips(activity, tips, TipsDialogUtil.LENGTH_LONG) { activity.finish() }
            } else {
                if (mode != PayPasswordActivity.UPDATE_PASSWORD && it.errorCode < 0) {
                    // 验证码错误
                    showVerifyDialog()
                }
            }
        })
        pay_password.setOnCodeCompleteListener(object : ChatCodeView.OnCodeCompleteListener {
            override fun onCodeComplete(view: View?, code: String) {
                mPassword = code
            }
        })
        confirm_password.setOnClickListener {
            if (pay_password.isCompleteText()) {
                setPayPassword()
            } else {
                ShowUtils.showToastNormal(activity, R.string.chat_error_input_password_incomplete)
            }
        }
    }

    private fun showVerifyDialog() {
        dialog = VerifyCodeDialog.Builder(activity, this)
                .setPhone(viewModel.currentUser.value?.account)
                .setOnDialogClickListener(object : VerifyCodeDialog.OnDialogClickListener() {
                    override fun onClose(view: View?) {
                        finish()
                    }
                })
                .setOnCodeCompleteListener { v, type, code ->
                    KeyboardUtils.hideKeyboard(v)
                    mType = type ?: ""
                    mCode = code ?: ""
                }
                .show()
    }

    private fun setPayPassword() {
        val request = if (mode == PayPasswordActivity.UPDATE_PASSWORD) {
            PayPasswordRequest.usePassword(oldPassword, mPassword)
        } else {
            PayPasswordRequest.useCode(mType, mCode, mPassword)
        }
        viewModel.setPayPassword(request)
    }
}