package com.fzm.chat33.main.activity

import androidx.lifecycle.Observer
import com.alibaba.android.arouter.facade.annotation.Route
import com.fuzamei.common.bus.LiveBus
import com.fuzamei.componentservice.app.AppRoute
import com.fuzamei.componentservice.app.BusEvent
import com.fuzamei.componentservice.base.DILoadableActivity
import com.fzm.chat33.R
import com.fzm.chat33.main.fragment.BlackListFragment
import kotlinx.android.synthetic.main.activity_black_list.*

/**
 * @author zhengjy
 * @since 2019/10/10
 * Description:
 */
@Route(path = AppRoute.BLACK_LIST)
class BlackListActivity : DILoadableActivity() {

    override fun getLayoutId(): Int {
        return R.layout.activity_black_list
    }

    override fun enableSlideBack(): Boolean {
        return true
    }

    override fun initView() {
        ctb_title.setMiddleText(getString(R.string.chat_title_black_list))
        ctb_title.setLeftListener { finish() }
        addFragment(R.id.fl_container, BlackListFragment())
    }

    override fun initData() {

    }

    override fun setEvent() {
        // 从黑名单进入聊天界面后，关闭黑名单界面
        LiveBus.of(BusEvent::class.java).changeTab().observe(this, Observer { finish() })
    }

}