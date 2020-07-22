package com.fzm.chat33.main.activity

import android.text.TextUtils
import android.view.View
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.alibaba.android.arouter.facade.annotation.Autowired
import com.alibaba.android.arouter.facade.annotation.Route
import com.alibaba.android.arouter.launcher.ARouter
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.fuzamei.common.utils.DateUtils
import com.fuzamei.common.utils.ShowUtils
import com.fuzamei.componentservice.app.AppRoute
import com.fuzamei.componentservice.base.DILoadableActivity
import com.fuzamei.componentservice.config.AppConfig
import com.fuzamei.componentservice.ext.findViewModel
import com.fzm.chat33.R
import com.fzm.chat33.core.bean.param.JoinGroupParam
import com.fzm.chat33.core.db.bean.RoomInfoBean
import com.fzm.chat33.core.global.Chat33Const
import com.fzm.chat33.core.global.UserInfo
import com.fzm.chat33.core.manager.CipherManager.Companion.hasDHKeyPair
import com.fzm.chat33.core.manager.GroupKeyManager.Companion.notifyGroupEncryptKey
import com.fzm.chat33.global.AppConst
import com.fzm.chat33.main.mvvm.GroupViewModel
import com.fzm.chat33.widget.SwitchView
import com.fzm.chat33.widget.SwitchView.OnStateChangedListener
import com.fuzamei.componentservice.widget.dialog.EasyDialog
import kotlinx.android.synthetic.main.activity_join_room.*
import javax.inject.Inject

/**
 * @author zhengjy
 * @since 2018/10/25
 * Description:群聊简单资料界面
 */
@Route(path = AppRoute.JOIN_ROOM)
class JoinRoomActivity : DILoadableActivity() {

    @JvmField
    @Autowired
    var roomInfo: RoomInfoBean? = null
    @JvmField
    @Autowired
    var markId: String? = null
    @JvmField
    @Autowired
    var sourceType = 0
    @JvmField
    @Autowired
    var sourceId: String? = null
    @JvmField
    @Autowired
    var ignoreDisable = false

    @Inject
    lateinit var provider: ViewModelProvider.Factory
    private lateinit var viewModel: GroupViewModel

    lateinit var groupInfo: RoomInfoBean
    private var isInRoom = false

    override fun enableSlideBack(): Boolean {
        return true
    }

    override fun getLayoutId(): Int {
        return R.layout.activity_join_room
    }

    override fun initView() {
        ARouter.getInstance().inject(this)
        viewModel = findViewModel(provider)
        viewModel.searchRoom.observe(this, Observer {
            if (it.roomInfo != null) {
                groupInfo = it.roomInfo
                setupView(groupInfo)
            } else {
                ShowUtils.showToast(instance, getString(R.string.chat_tips_group_info20))
                finish()
            }
        })
        viewModel.joinRoom.observe(this, Observer {
            notifyGroupEncryptKey(groupInfo.id)
            ShowUtils.showToastNormal(instance, getString(R.string.chat_tips_verify5))
            finish()
        })
        viewModel.stickyTop.observe(this, Observer { bool ->
            if (bool == null) {
                return@Observer
            }
            sb_stick_top.toggleSwitch(bool)
        })
        viewModel.noDisturb.observe(this, Observer { bool ->
            if (bool == null) {
                return@Observer
            }
            sb_dnd.toggleSwitch(bool)
        })
    }

    override fun initData() {
        if (sourceType == 0) {
            sourceType = Chat33Const.FIND_TYPE_SEARCH
        }
        titleBar.setMiddleText(getString(R.string.chat_title_group_info2))
        titleBar.setRightVisible(false)
        titleBar.setLeftListener { finish() }
        if (roomInfo != null) {
            groupInfo = roomInfo!!
            setupView(groupInfo)
        } else {
            viewModel.searchByUid(markId ?: "")
        }
    }

    private fun setupView(bean: RoomInfoBean) {
        if (!TextUtils.isEmpty(bean.avatar)) {
            Glide.with(instance).load(bean.avatar)
                    .apply(RequestOptions().placeholder(R.mipmap.default_avatar_room)).into(iv_avatar)
        } else {
            iv_avatar.setImageResource(R.mipmap.default_avatar_room)
        }
        tv_name.text = bean.name
        tv_room_id.text = getString(R.string.room_id, bean.markId)
        tv_room_num.text = getString(R.string.room_member_num, bean.memberNumber)
        if (bean.isIdentified) {
            tv_identification.text = getString(R.string.chat_tips_identification_tip2, bean.getIdentificationInfo())
            tv_identification.visibility = View.VISIBLE
        } else {
            tv_identification.visibility = View.GONE
        }
        isInRoom = bean.noDisturbing != 0
        tv_confirm.visibility = View.VISIBLE
        if (isInRoom) {
            sb_dnd.isOpened = bean.noDisturbing == 1
            tv_confirm.isEnabled = true
            tv_confirm.setText(R.string.chat_action_enter_group)
        } else {
            if (!ignoreDisable && bean.joinPermission == 3) {
                tv_confirm.isEnabled = false
                tv_confirm.setText(R.string.chat_tips_cant_join_group)
            } else {
                tv_confirm.isEnabled = true
                tv_confirm.setText(R.string.chat_action_join_group)
            }
        }
    }

    override fun setEvent() {
        tv_confirm.setOnClickListener {
            if (isInRoom) {
                if (groupInfo.disableDeadline == 0L) {
                    ARouter.getInstance()
                            .build(AppRoute.CHAT)
                            .withBoolean("isGroupChat", true)
                            .withInt("channelType", Chat33Const.CHANNEL_ROOM)
                            .withString("targetName", groupInfo.name)
                            .withString("targetId", groupInfo.id)
                            .navigation()
                } else { // 群被封禁
                    val tips: String
                    tips = if (AppConst.TIME_FOREVER == groupInfo.disableDeadline) {
                        getString(R.string.room_disable_forever_tips)
                    } else {
                        val deadline = DateUtils.timeToString(groupInfo.disableDeadline, getString(R.string.chat_date_pattern))
                        getString(R.string.room_disable_tips, deadline)
                    }
                    EasyDialog.Builder()
                            .setHeaderTitle(getString(R.string.chat_tips_tips))
                            .setBottomRightText(getString(R.string.chat_action_confirm))
                            .setContent(tips)
                            .setBottomRightClickListener {
                                it.dismiss()
                            }.setCancelable(false).create(instance).show()
                }
            } else {
                if (AppConfig.APP_ENCRYPT && groupInfo.encrypt == 1 && !hasDHKeyPair()) {
                    // 通常不会走到这一步，所有用户都有密钥对
                    ShowUtils.showToastNormal(instance, R.string.chat_set_chat_password)
                } else {
                    if (groupInfo.joinPermission == 1) {
                        ARouter.getInstance().build(AppRoute.FRIEND_VERIFY)
                                .withString("roomId", groupInfo.id)
                                .withString("id", UserInfo.getInstance().id)
                                .withInt("channelType", Chat33Const.CHANNEL_ROOM)
                                .withInt("sourceType", sourceType)
                                .withString("sourceId", sourceId)
                                .navigation()
                    } else {
                        viewModel.joinRoomApply(JoinGroupParam(groupInfo.id,
                                UserInfo.getInstance().id, "", sourceType, sourceId))
                    }
                }
            }
        }
        sb_dnd.setOnStateChangedListener(object : OnStateChangedListener {
            override fun toggleToOn(view: SwitchView) {
                view.toggleSwitch(true)
                viewModel.setDND(groupInfo.id, 1)
            }

            override fun toggleToOff(view: SwitchView) {
                view.toggleSwitch(false)
                viewModel.setDND(groupInfo.id, 1)
            }
        })
        sb_stick_top.setOnStateChangedListener(object : OnStateChangedListener {
            override fun toggleToOn(view: SwitchView) {
                view.toggleSwitch(true)
                viewModel.stickyOnTop(groupInfo.id, 1)
            }

            override fun toggleToOff(view: SwitchView) {
                view.toggleSwitch(false)
                viewModel.stickyOnTop(groupInfo.id, 2)
            }
        })
    }
}