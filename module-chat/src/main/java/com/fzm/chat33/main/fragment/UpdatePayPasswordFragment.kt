package com.fzm.chat33.main.fragment

import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.view.View
import com.fuzamei.componentservice.base.DILoadableFragment
import com.fuzamei.componentservice.ext.findViewModel
import com.fzm.chat33.R
import com.fzm.chat33.main.activity.PayPasswordActivity
import com.fzm.chat33.main.mvvm.PayPasswordViewModel
import com.fzm.chat33.widget.ChatCodeView
import kotlinx.android.synthetic.main.fragment_update_pay_password.*
import javax.inject.Inject

/**
 * @author zhengjy
 * @since 2019/03/13
 * Description:
 */
class UpdatePayPasswordFragment : DILoadableFragment() {

    companion object {
        fun create(): UpdatePayPasswordFragment {
            return UpdatePayPasswordFragment()
        }
    }

    @Inject
    lateinit var provider: ViewModelProvider.Factory
    private lateinit var viewModel: PayPasswordViewModel
    private var mCode: String = ""

    override fun getLayoutId(): Int {
        return R.layout.fragment_update_pay_password
    }

    override fun initView(view: View?, savedInstanceState: Bundle?) {
        viewModel = findViewModel(provider)
        viewModel.checkPassword.observe(this, Observer {
            if (it == null) {
                pay_password.clear()
                open(R.id.fl_container, SetPayPasswordFragment.create(PayPasswordActivity.UPDATE_PASSWORD, mCode))
            }
        })
        forget_password.setOnClickListener {
            pay_password.clear()
            open(R.id.fl_container, SetPayPasswordFragment.create(PayPasswordActivity.UPDATE_PASSWORD_WITH_CODE))
        }
        pay_password.setOnCodeCompleteListener(object : ChatCodeView.OnCodeCompleteListener {
            override fun onCodeComplete(view: View?, code: String) {
                mCode = code
                viewModel.checkPayPassword(mCode)
            }
        })
        pay_password.postDelayed({
            pay_password.performClick()
        }, 200)
    }

    override fun initData() {

    }

    override fun setEvent() {

    }
}