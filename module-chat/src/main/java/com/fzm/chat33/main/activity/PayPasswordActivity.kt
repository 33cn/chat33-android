package com.fzm.chat33.main.activity

import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import android.content.Intent
import com.alibaba.android.arouter.facade.annotation.Autowired
import com.alibaba.android.arouter.facade.annotation.Route
import com.alibaba.android.arouter.launcher.ARouter
import com.fuzamei.componentservice.app.AppRoute
import com.fuzamei.componentservice.base.DILoadableActivity
import com.fuzamei.componentservice.ext.findViewModel
import com.fzm.chat33.R
import com.fzm.chat33.main.fragment.SetPayPasswordFragment
import com.fzm.chat33.main.fragment.UpdatePayPasswordFragment
import com.fzm.chat33.main.mvvm.PayPasswordViewModel
import kotlinx.android.synthetic.main.activity_pay_password.*
import javax.inject.Inject

/**
 * @author zhengjy
 * @since 2019/03/13
 * Description:
 */
@Route(path = AppRoute.PAY_PASSWORD)
class PayPasswordActivity : DILoadableActivity() {

    companion object {
        const val SET_PASSWORD = 1
        const val UPDATE_PASSWORD = 2
        const val UPDATE_PASSWORD_WITH_CODE = 3

        const val REQUEST_CODE_VERIFY = 1
    }

    private var setPayPasswordFragment: SetPayPasswordFragment? = null
    private var updatePayPasswordFragment: UpdatePayPasswordFragment? = null

    @Autowired
    @JvmField
    var mode: Int = SET_PASSWORD

    @Inject
    lateinit var provider: ViewModelProvider.Factory
    private lateinit var viewModel: PayPasswordViewModel

    override fun getLayoutId(): Int {
        return R.layout.activity_pay_password
    }

    override fun initView() {
        ARouter.getInstance().inject(this)
        viewModel = findViewModel(provider)
        viewModel.loading.observe(this, Observer { setupLoading(it) })
        ctb_title.tv_back.setOnClickListener { onBackPressed() }
        ctb_title.tv_title_middle.text = getString(if (mode == SET_PASSWORD) R.string.chat_title_pay_password1 else R.string.chat_title_pay_password2)
        when(mode) {
            SET_PASSWORD->{
                setPayPasswordFragment = SetPayPasswordFragment.create()
                addFragment(R.id.fl_container, setPayPasswordFragment)
            }
            UPDATE_PASSWORD->{
                updatePayPasswordFragment = UpdatePayPasswordFragment.create()
                addFragment(R.id.fl_container, updatePayPasswordFragment)
            }
            UPDATE_PASSWORD_WITH_CODE->{
                setPayPasswordFragment = SetPayPasswordFragment.create(UPDATE_PASSWORD_WITH_CODE)
                addFragment(R.id.fl_container, setPayPasswordFragment)
            }
        }
    }

    override fun initData() {

    }

    override fun setEvent() {

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_VERIFY) {
            if (data == null) {
                return
            }
            val ticket = data.getStringExtra("ticket")
            setPayPasswordFragment?.dialog?.sendCode(null, ticket)
        }
    }
}