package com.fzm.chat33.main.activity

import com.alibaba.android.arouter.facade.annotation.Route
import com.alibaba.android.arouter.launcher.ARouter
import com.fuzamei.common.bus.LiveBus
import com.fuzamei.componentservice.app.AppRoute
import com.fuzamei.componentservice.app.BusEvent
import com.fuzamei.componentservice.base.DILoadableActivity
import com.fzm.chat33.R
import com.fzm.chat33.app.App
import com.fzm.chat33.main.fragment.NewFriendFragment
import com.fuzamei.componentservice.event.NewFriendRequestEvent
import kotlinx.android.synthetic.main.activity_new_friend.*

/**
 * 创建日期：2018/10/10 on 11:26
 *
 * @author zhengjy
 * @since 2019/10/21
 * Description:好友申请页面
 */
@Route(path = AppRoute.NEW_FRIENDS)
class NewFriendActivity : DILoadableActivity() {

    override fun enableSlideBack(): Boolean {
        return true
    }

    override fun getLayoutId(): Int {
        return R.layout.activity_new_friend
    }

    override fun initView() {
        App.getInstance().newFriendRequest.clear()
        LiveBus.of(BusEvent::class.java).newFriends()
                .setValue(NewFriendRequestEvent(null, null, true))
    }

    override fun initData() {
        addFragment(R.id.fl_container, NewFriendFragment())
    }

    override fun setEvent() {
        iv_return.setOnClickListener { finish() }
        iv_add.setOnClickListener {
            ARouter.getInstance().build(AppRoute.SEARCH_ONLINE).navigation()
        }
    }
}
