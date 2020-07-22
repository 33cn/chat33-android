package com.fzm.chat33.main.activity

import androidx.core.content.ContextCompat

import androidx.lifecycle.ViewModelProvider

import com.alibaba.android.arouter.facade.annotation.Autowired
import com.alibaba.android.arouter.facade.annotation.Route
import com.alibaba.android.arouter.launcher.ARouter
import com.fuzamei.common.utils.ActivityUtils
import com.fuzamei.common.utils.BarUtils
import com.fuzamei.componentservice.app.AppRoute
import com.fuzamei.componentservice.base.DILoadableActivity
import com.fzm.chat33.main.mvvm.ServerTipsViewModel
import com.fuzamei.componentservice.widget.dialog.EasyDialog
import com.fuzamei.componentservice.ext.findViewModel
import com.fzm.chat33.R
import com.fzm.chat33.core.Chat33

import javax.inject.Inject

/**
 * @author zhengjy
 * @since 2018/11/13
 * Description:用于显示服务端返回信息的对话框的activity
 */
@Route(path = AppRoute.SERVER_TIPS)
class ServerTipsActivity : DILoadableActivity() {

    @JvmField
    @Autowired
    var tips: String? = null

    @Inject
    lateinit var provider: ViewModelProvider.Factory
    private lateinit var viewModel: ServerTipsViewModel

    private var dialog: EasyDialog? = null

    override fun setStatusBar() {
        BarUtils.setStatusBarColor(this, ContextCompat.getColor(this, R.color.chat_transparent), 0)
    }

    override fun onBackPressed() {
        finish()
    }

    override fun getLayoutId(): Int {
        return R.layout.activity_multiple_login
    }

    override fun initView() {
        ARouter.getInstance().inject(this)
        viewModel = findViewModel(provider)
        viewModel.performLogout()
        dialog = EasyDialog.Builder()
                .setHeaderTitle(getString(R.string.chat_tips_tips))
                .setBottomRightText(getString(R.string.chat_action_confirm))
                .setContent(if (tips.isNullOrEmpty()) getString(R.string.chat_tips_unknown_error) else tips)
                .setBottomRightClickListener { openLoginPage() }.setCancelable(false).create(this)
        dialog!!.show()
    }

    private fun openLoginPage() {
        Chat33.getRouter().gotoLoginPage(null)
        ActivityUtils.exitToLogin()
    }

    override fun initData() {

    }

    override fun setEvent() {

    }
}
