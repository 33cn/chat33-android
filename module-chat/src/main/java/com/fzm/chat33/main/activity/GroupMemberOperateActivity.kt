package com.fzm.chat33.main.activity

import android.content.Intent
import android.text.Html
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider

import com.alibaba.android.arouter.facade.annotation.Autowired
import com.alibaba.android.arouter.facade.annotation.Route
import com.alibaba.android.arouter.launcher.ARouter
import com.fuzamei.common.utils.KeyboardUtils
import com.fuzamei.common.utils.ShowUtils
import com.fuzamei.componentservice.app.AppRoute
import com.fuzamei.componentservice.base.DILoadableActivity
import com.fuzamei.componentservice.config.AppConfig
import com.fuzamei.componentservice.ext.findViewModel
import com.fzm.chat33.R
import com.fzm.chat33.core.bean.param.EditRoomUserParam
import com.fzm.chat33.core.db.bean.RoomContact
import com.fzm.chat33.core.db.bean.RoomInfoBean
import com.fzm.chat33.core.db.bean.RoomUserBean
import com.fzm.chat33.main.adapter.GroupMemberAdapter
import com.fzm.chat33.main.fragment.GroupMemberListFragment
import com.fzm.chat33.main.mvvm.GroupMemberViewModel
import com.fzm.chat33.widget.ChatSearchView
import com.fuzamei.componentservice.widget.dialog.EasyDialog
import com.fzm.chat33.widget.popup.MutePopupWindow
import kotlinx.android.synthetic.main.activity_group_member_operate.*

import java.util.ArrayList
import javax.inject.Inject

/**
 * @author zhengjy
 * @since 2018/10/30
 * Description:群成员选择页面
 */
@Route(path = AppRoute.SELECT_GROUP_MEMBER)
class GroupMemberOperateActivity : DILoadableActivity(), View.OnClickListener {

    private val CHANGE_OWNER = "change_owner"
    private val ADD_ADMIN = "add_admin"
    private val REMOVE = "remove"
    private val MUTE = "mute"
    private val MUTE_REVERSE = "mute_reverse"

    @JvmField
    @Autowired
    var roomInfo: RoomInfoBean? = null
    @JvmField
    @Autowired
    var selectable = false
    @JvmField
    @Autowired
    var memberLevel: Int = 0
    @JvmField
    @Autowired
    var action = REMOVE

    @Inject
    lateinit var provider: ViewModelProvider.Factory
    lateinit var viewModel: GroupMemberViewModel

    private lateinit var fragment: GroupMemberListFragment
    private var mutePopupWindow: MutePopupWindow? = null
    private val memberList = ArrayList<String>()
    private val nameList = ArrayList<String>()

    override fun enableSlideBack(): Boolean {
        return true
    }

    override fun getLayoutId(): Int {
        return R.layout.activity_group_member_operate
    }

    override fun initView() {
        viewModel = findViewModel(provider)
        ARouter.getInstance().inject(this)
        tv_back.setOnClickListener(this)
        iv_search.setOnClickListener(this)
        chat_search.setOnSearchCancelListener(object : ChatSearchView.OnSearchCancelListener {
            override fun onSearchCancel() {
                KeyboardUtils.hideKeyboard(chat_search.getFocusView())
                chat_search.reduce()
            }
        })
        chat_search.setHint(getString(R.string.chat_search_group_friend_hint))
        chat_search.setOnTextChangeListener(object : ChatSearchView.OnTextChangeListener {
            override fun onTextChange(s: String) {
                fragment.searchKeyword(s)
            }
        })
        viewModel.loading.observe(this, Observer { setupLoading(it) })
        viewModel.kickOutResult.observe(this, Observer {
            if (it != null) {
                ShowUtils.showToastNormal(instance, getString(R.string.chat_tips_member_operate2))
                finish()
            }
        })
        viewModel.muteResult.observe(this, Observer {
            if (it == null) {
                return@Observer
            }
            if (it == 2) {
                ShowUtils.showToastNormal(instance, getString(R.string.chat_tips_member_operate4))
            } else if (it == 3) {
                ShowUtils.showToastNormal(instance, getString(R.string.chat_tips_member_operate6))
            }
            finish()
        })

        when {
            REMOVE == action -> {
                // 移除群成员
                tv_remove.setText(R.string.chat_action_member_operate1)
                tv_title.text = getString(R.string.chat_title_member_operate1)
                rl_bottom.visibility = View.VISIBLE
                tv_remove.setOnClickListener(View.OnClickListener {
                    if (memberList.size == 0) {
                        ShowUtils.showToastNormal(instance, getString(R.string.chat_tips_member_operate1))
                        return@OnClickListener
                    }
                    viewModel.kickOutUsers(EditRoomUserParam(roomInfo!!.id, memberList))
                })
            }
            CHANGE_OWNER == action -> {
                // 转让群主
                tv_title.text = getString(R.string.chat_title_member_operate2)
                rl_bottom.visibility = View.GONE
            }
            ADD_ADMIN == action -> {
                // 添加群管理员
                tv_title.text = getString(R.string.chat_title_member_operate3)
                rl_bottom.visibility = View.GONE
            }
            MUTE == action -> {
                // 禁言群成员
                tv_remove.setText(R.string.chat_action_member_operate2)
                tv_title.text = getString(R.string.chat_title_member_operate4)
                rl_bottom.visibility = View.VISIBLE
                tv_remove.setOnClickListener(View.OnClickListener {
                    if (memberList.size == 0) {
                        ShowUtils.showToastNormal(instance, getString(R.string.chat_tips_member_operate3))
                        return@OnClickListener
                    }
                    mutePopupWindow = MutePopupWindow(instance, LayoutInflater.from(instance)
                            .inflate(R.layout.popup_sustom_service_operation, null))
                    mutePopupWindow?.softInputMode = WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE
                    mutePopupWindow?.setOnTimeSelectListener { time ->
                        viewModel.setMutedList(roomInfo!!.id, 2, memberList, time)
                    }
                    mutePopupWindow?.showAtLocation(tv_remove, Gravity.CENTER, 0, 0)
                })
            }
            MUTE_REVERSE == action -> {
                // 白名单群成员
                tv_remove.text = getString(R.string.chat_action_member_operate3)
                tv_title.text = getString(R.string.chat_title_member_operate5)
                rl_bottom.visibility = View.VISIBLE
                tv_remove.setOnClickListener(View.OnClickListener {
                    if (memberList.size == 0) {
                        ShowUtils.showToastNormal(instance, getString(R.string.chat_tips_member_operate5))
                        return@OnClickListener
                    }
                    viewModel.setMutedList(roomInfo!!.id, 3, memberList, -1)
                })
            }
        }
    }

    override fun initData() {
        fragment = GroupMemberListFragment.create(roomInfo!!, memberLevel, action, selectable)
        fragment.setOnItemClickListener(object : GroupMemberListFragment.OnItemClickListener {
            override fun onItemClick(bean: RoomContact) {
                val roomUser = RoomUserBean().apply {
                    this.roomId = bean.roomId
                    this.id = bean.id
                    this.nickname = bean.nickname
                    this.roomNickname = bean.roomNickname
                    this.avatar = bean.avatar
                    this.memberLevel = bean.memberLevel
                    this.roomMutedType = bean.roomMutedType
                    this.mutedType = bean.mutedType
                    this.deadline = bean.deadline
                    this.identification = bean.identification
                    this.identificationInfo = bean.identificationInfo
                    this.searchKey = bean.searchKey
                }
                if (CHANGE_OWNER == action) {
                    val content = getString(R.string.chat_dialog_giveaway, AppConfig.APP_ACCENT_COLOR_STR, roomUser.displayName)
                    EasyDialog.Builder()
                            .setHeaderTitle(getString(R.string.chat_tips_tips))
                            .setBottomLeftText(getString(R.string.chat_action_cancel))
                            .setBottomRightText(getString(R.string.chat_action_confirm))
                            .setContent(Html.fromHtml(content))
                            .setBottomLeftClickListener(null)
                            .setBottomRightClickListener {
                                val intent = Intent()
                                intent.putExtra("result", roomUser)
                                setResult(RESULT_OK, intent)
                                finish()
                            }.create(instance).show()
                } else {
                    setResult(RESULT_OK, Intent().putExtra("result", roomUser))
                    finish()
                }
            }
        })
        fragment.setOnCheckChangedListener(GroupMemberAdapter.OnCheckChangedListener { _, checked, bean ->
            if (checked) {
                if (!memberList.contains(bean.id)) {
                    memberList.add(bean.id)
                }
                nameList.add(bean.getDisplayName()!!)
            } else {
                memberList.remove(bean.id)
                nameList.remove(bean.getDisplayName())
            }
        })
        addFragment(R.id.fl_container, fragment)
    }

    override fun setEvent() {
        cb_select.setOnCheckedChangeListener { _, isChecked -> fragment.checkAll(isChecked) }
    }

    override fun onClick(v: View) {
        val i = v.id
        if (i == R.id.tv_back) {
            finish()
        } else if (i == R.id.iv_search) {
            chat_search.expand()
            chat_search.postDelayed({ KeyboardUtils.showKeyboard(chat_search.getFocusView()) }, 100)
        }
    }

    override fun onBackPressed() {
        if (!chat_search.onBackPressed()) {
            super.onBackPressed()
        }
    }
}
