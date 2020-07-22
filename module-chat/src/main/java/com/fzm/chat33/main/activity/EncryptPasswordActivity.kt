package com.fzm.chat33.main.activity

import com.alibaba.android.arouter.facade.annotation.Autowired
import com.alibaba.android.arouter.facade.annotation.Route
import com.alibaba.android.arouter.launcher.ARouter
import com.fuzamei.componentservice.app.AppRoute
import com.fuzamei.componentservice.base.DILoadableActivity

import com.fzm.chat33.R
import com.fzm.chat33.main.fragment.SetEncryptPasswordFragment
import com.fzm.chat33.main.fragment.UpdateEncryptPasswordFragment

/**
 * @author zhengjy
 * @since 2019/05/24
 * Description:修改密聊密码界面
 */
@Route(path = AppRoute.ENCRYPT_PWD)
class EncryptPasswordActivity : DILoadableActivity() {

    @JvmField
    @Autowired
    var setMode: Boolean = false
    @JvmField
    @Autowired
    var forget: Boolean = false

    override fun getLayoutId(): Int {
        return R.layout.activity_encrypt_password
    }

    override fun enableSlideBack(): Boolean {
        return true
    }

    override fun initView() {
        ARouter.getInstance().inject(this)
        if (setMode) {
            addFragment(R.id.fl_container, SetEncryptPasswordFragment.create(forget))
        } else {
            addFragment(R.id.fl_container, UpdateEncryptPasswordFragment())
        }
    }

    override fun initData() {

    }

    override fun setEvent() {

    }
}
