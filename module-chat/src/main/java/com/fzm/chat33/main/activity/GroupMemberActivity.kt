package com.fzm.chat33.main.activity

import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import androidx.lifecycle.ViewModelProvider

import com.alibaba.android.arouter.facade.annotation.Autowired
import com.alibaba.android.arouter.facade.annotation.Route
import com.alibaba.android.arouter.launcher.ARouter
import com.fuzamei.common.utils.KeyboardUtils
import com.fuzamei.componentservice.app.AppRoute
import com.fuzamei.componentservice.base.DILoadableActivity
import com.fuzamei.componentservice.ext.findViewModel
import com.fzm.chat33.core.db.bean.RoomInfoBean
import com.fzm.chat33.R
import com.fzm.chat33.core.db.bean.RoomUserBean
import com.fzm.chat33.main.fragment.GroupMemberListFragment
import com.fzm.chat33.main.mvvm.GroupMemberViewModel
import com.fzm.chat33.main.popupwindow.GroupMemberPopupWindow
import com.fzm.chat33.widget.ChatSearchView
import kotlinx.android.synthetic.main.activity_group_member.*

import javax.inject.Inject

/**
 * 创建日期：2018/10/30
 * 描述:群成员列表界面
 * 作者:zhengjy
 */
@Route(path = AppRoute.GROUP_MEMBER)
class GroupMemberActivity : DILoadableActivity(), View.OnClickListener {

    @JvmField
    @Autowired
    var roomInfo: RoomInfoBean? = null
    private var initLoad = true

    @Inject
    lateinit var provider: ViewModelProvider.Factory
    private lateinit var viewModel: GroupMemberViewModel

    private var fragment: GroupMemberListFragment? = null
    private var popupWindow: GroupMemberPopupWindow? = null

    override fun enableSlideBack(): Boolean {
        return true
    }

    override fun getLayoutId(): Int {
        return R.layout.activity_group_member
    }

    override fun initView() {
        ARouter.getInstance().inject(this)
        viewModel = findViewModel(provider)

        tv_room_name.text = roomInfo?.name
    }

    override fun initData() {
        fragment = GroupMemberListFragment.create(roomInfo!!)
        fragment?.setOnRoomUsersUpdateListener(object : GroupMemberListFragment.OnRoomUsersUpdateListener {
            override fun onRoomUsersUpdate(roomUsers: List<RoomUserBean>) {
                roomInfo?.users = roomUsers
            }
        })
        addFragment(R.id.fl_container, fragment)
    }

    override fun setEvent() {
        iv_add.setOnClickListener(this)
        iv_return.setOnClickListener(this)
        iv_search.setOnClickListener(this)
        chat_search.setOnSearchCancelListener(object : ChatSearchView.OnSearchCancelListener {
            override fun onSearchCancel() {
                KeyboardUtils.hideKeyboard(chat_search.getFocusView())
                chat_search.reduce()
            }
        })
        chat_search.setOnTextChangeListener(object : ChatSearchView.OnTextChangeListener {
            override fun onTextChange(s: String) {
                fragment?.searchKeyword(s)
            }
        })
    }

    override fun onResume() {
        super.onResume()
        if (!initLoad) {
            fragment?.getGroupMemberList()
        }
        initLoad = false
    }

    override fun onBackPressed() {
        if (!chat_search.onBackPressed()) {
            super.onBackPressed()
        }
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.iv_add -> showPop(iv_add, roomInfo!!)
            R.id.iv_return -> finish()
            R.id.iv_search -> {
                chat_search.expand()
                chat_search.postDelayed({ KeyboardUtils.showKeyboard(chat_search.getFocusView()) }, 100)
            }
        }
    }

    private fun showPop(view: View, roomInfo: RoomInfoBean) {
        if (popupWindow == null) {
            popupWindow = GroupMemberPopupWindow(this, LayoutInflater.from(this)
                    .inflate(R.layout.popupwindow_group_member, null))
            popupWindow?.setRoomInfo(roomInfo)
            popupWindow?.softInputMode = WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE
            popupWindow?.show(view)
        } else {
            popupWindow?.setRoomInfo(roomInfo)
            popupWindow?.show(view)
        }
    }
}
