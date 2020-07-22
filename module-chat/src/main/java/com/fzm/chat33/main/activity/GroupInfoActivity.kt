package com.fzm.chat33.main.activity

import android.content.Intent
import androidx.lifecycle.Observer

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders

import com.alibaba.android.arouter.facade.annotation.Autowired
import com.alibaba.android.arouter.facade.annotation.Route
import com.alibaba.android.arouter.launcher.ARouter
import com.fuzamei.componentservice.app.AppRoute
import com.fuzamei.componentservice.base.DILoadableActivity
import com.fzm.chat33.core.db.bean.RoomUserBean
import com.fuzamei.common.utils.ShowUtils
import com.fuzamei.componentservice.app.Loading
import com.fzm.chat33.R
import com.fzm.chat33.global.AppConst
import com.fzm.chat33.main.fragment.GroupInfoFragment
import com.fzm.chat33.main.mvvm.GroupViewModel

import javax.inject.Inject

/**
 * 创建日期：2018/10/10 on 11:26
 * 描述:
 * 作者:wdl
 */
@Route(path = AppRoute.GROUP_INFO, extras = AppConst.NEED_LOGIN)
class GroupInfoActivity : DILoadableActivity() {

    @JvmField
    @Autowired
    var roomId: String? = null

    @Inject
    lateinit var provider: ViewModelProvider.Factory
    private lateinit var viewModel: GroupViewModel

    override fun enableSlideBack(): Boolean {
        return true
    }

    override fun getLayoutId(): Int {
        return R.layout.activity_group_info
    }

    override fun initView() {
        viewModel = ViewModelProviders.of(this, provider).get(GroupViewModel::class.java)
        ARouter.getInstance().inject(this)
        viewModel.loading.observe(this, Observer<Loading> { this.setupLoading(it) })
        viewModel.memberLevel.observe(this, Observer { level ->
            if (level != null) {
                if (level == 3) {
                    ShowUtils.showToastNormal(instance, getString(R.string.chat_tips_group_giveaway_success))
                    viewModel.getRoomInfo(roomId!!)
                }
            }
        })
    }

    override fun initData() {
        addFragment(R.id.fl_container, GroupInfoFragment.create(roomId!!))
    }

    override fun setEvent() {

    }

    override fun onResume() {
        super.onResume()
        viewModel.getRoomInfo(roomId!!)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK) {
            if (requestCode == CODE_CHANGE_OWNER) {
                if (data!!.getSerializableExtra("result") == null) {
                    return
                }
                val bean = data.getSerializableExtra("result") as RoomUserBean
                viewModel.setRoomUserLevel(roomId!!, bean.id, 3)
            }
        }
    }

    companion object {
        val CODE_CHANGE_OWNER = 2
    }
}
