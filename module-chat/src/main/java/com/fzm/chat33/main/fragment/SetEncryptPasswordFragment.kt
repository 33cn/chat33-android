package com.fzm.chat33.main.fragment

import android.app.Activity.RESULT_OK
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
import kotlinx.android.synthetic.main.fragment_set_encrypt_password.*
import javax.inject.Inject

/**
 * @author zhengjy
 * @since 2019/10/24
 * Description:
 */
class SetEncryptPasswordFragment : DILoadableFragment() {

    @Inject
    lateinit var provider: ViewModelProvider.Factory
    private lateinit var viewModel: EncryptPasswordViewModel

    private var forgetPassword = false

    companion object {
        @JvmStatic
        fun create(forget: Boolean): SetEncryptPasswordFragment {
            return SetEncryptPasswordFragment().apply {
                arguments = Bundle().apply {
                    putBoolean("forget", forget)
                }
            }
        }
    }

    override fun getLayoutId(): Int {
        return R.layout.fragment_set_encrypt_password
    }

    override fun initView(view: View, savedInstanceState: Bundle?) {
        viewModel = findViewModel(provider)
        forgetPassword = arguments?.getBoolean("forget") ?: false
        ctb_title.setMiddleText(getString(R.string.chat_title_set_encrypt_password))
        ctb_title.setRightVisible(false)

        viewModel.loading.observe(this, Observer { setupLoading(it) })
        viewModel.firstError.observe(this, Observer {
            if (it == 0) {
                tv_first_error.text = ""
            } else {
                tv_first_error.text = getString(it)
            }
        })
        viewModel.secondError.observe(this, Observer {
            if (it == 0) {
                tv_second_error.text = ""
            } else {
                tv_second_error.text = getString(it)
            }
        })
        viewModel.mnemonicResult.observe(this, Observer {
            dismiss()
            ShowUtils.showToastNormal(activity, getString(R.string.chat_tips_update_encrypt_pwd7))
            activity.setResult(RESULT_OK)
            finish()
        })
    }

    override fun initData() {
        et_first_pwd.post {
            KeyboardUtils.showKeyboard(et_first_pwd)
        }
    }

    override fun setEvent() {
        ctb_title.setLeftListener { finish() }
        tv_submit.setOnClickListener {
            val first = et_first_pwd.text.toString().trim()
            val second = et_second_pwd.text.toString().trim()
            viewModel.setMnemonicWord(first, second, forgetPassword)
        }
        et_first_pwd.setOnFocusChangeListener { _, hasFocus ->
            val password = et_first_pwd.text.toString().trim()
            if (!hasFocus && password.isNotEmpty()) {
                viewModel.checkFirst(password)
            }
        }
        et_first_pwd.setOnFocusChangeListener { _, _ ->
            viewModel.resetSecondError()
        }
        et_second_pwd.addTextChangedListener(object : SimpleTextWatcher() {
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                viewModel.resetSecondError()
            }
        })
    }
}