package com.fzm.chat33.main.fragment

import android.os.Bundle
import android.view.View
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.fuzamei.common.utils.KeyboardUtils
import com.fuzamei.common.utils.ShowUtils
import com.fuzamei.componentservice.base.DILoadableFragment
import com.fuzamei.componentservice.ext.findViewModel
import com.fzm.chat33.R
import com.fzm.chat33.main.mvvm.EncryptPasswordViewModel
import com.fzm.chat33.utils.SimpleTextWatcher
import kotlinx.android.synthetic.main.fragment_update_encrypt_password.*
import javax.inject.Inject

/**
 * @author zhengjy
 * @since 2019/10/24
 * Description:
 */
class UpdateEncryptPasswordFragment : DILoadableFragment() {

    @Inject
    lateinit var provider: ViewModelProvider.Factory
    private lateinit var viewModel: EncryptPasswordViewModel
    
    override fun getLayoutId(): Int {
        return R.layout.fragment_update_encrypt_password
    }

    override fun initView(view: View, savedInstanceState: Bundle?) {
        viewModel = findViewModel(provider)
        ctb_title.setMiddleText(getString(R.string.chat_title_encrypt_password))
        ctb_title.setRightVisible(false)

        viewModel.loading.observe(this, Observer { setupLoading(it) })
        viewModel.oldError.observe(this, Observer {
            if (it == 0) {
                tv_password_error.text = ""
            } else {
                tv_password_error.text = getString(it)
            }
        })
        viewModel.newError.observe(this, Observer {
            if (it == 0) {
                tv_new_password_error.text = ""
            } else {
                tv_new_password_error.text = getString(it)
            }
        })
        viewModel.newSecondError.observe(this, Observer {
            if (it == 0) {
                tv_new_password_error_again.text = ""
            } else {
                tv_new_password_error_again.text = getString(it)
            }
        })
        viewModel.changeResult.observe(this, Observer {
            dismiss()
            ShowUtils.showToastNormal(activity, getString(R.string.chat_tips_update_encrypt_pwd1))
            finish()
        })
    }

    override fun initData() {
        et_old_pwd.post {
            KeyboardUtils.showKeyboard(et_old_pwd)
        }
    }

    override fun setEvent() {
        ctb_title.setLeftListener { finish() }
        tv_submit.setOnClickListener {
            val old = et_old_pwd.text.toString().trim()
            val new = et_new_pwd.text.toString().trim()
            val newAgain = et_new_pwd_again.text.toString().trim()
            viewModel.changePassword(old, new, newAgain)
        }
        et_old_pwd.setOnFocusChangeListener { _, hasFocus ->
            val old = et_old_pwd.text.toString().trim()
            if (!hasFocus && old.isNotEmpty()) {
                viewModel.checkOldPassword(old)
            }
        }
        et_new_pwd.setOnFocusChangeListener { _, hasFocus ->
            val new = et_new_pwd.text.toString().trim()
            if (!hasFocus && new.isNotEmpty()) {
                viewModel.checkNew(new)
            }
            viewModel.resetSecondError()
        }
        et_new_pwd_again.addTextChangedListener(object : SimpleTextWatcher() {
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                viewModel.resetSecondError()
            }
        })
    }
}