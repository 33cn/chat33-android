package com.fzm.chat33.main.fragment

import android.content.Intent
import android.os.Bundle
import android.text.Html
import android.text.TextUtils
import android.view.Gravity
import android.view.View
import android.widget.ImageView
import androidx.core.app.ActivityOptionsCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.alibaba.android.arouter.launcher.ARouter
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.fuzamei.common.bus.LiveBus
import com.fuzamei.common.recycleviewbase.CommonAdapter
import com.fuzamei.common.recycleviewbase.MultiItemTypeAdapter
import com.fuzamei.common.recycleviewbase.ViewHolder
import com.fuzamei.common.utils.AESUtil
import com.fuzamei.common.utils.ShowUtils
import com.fuzamei.common.widget.BottomPopupWindow
import com.fuzamei.componentservice.app.AppRoute
import com.fuzamei.componentservice.app.BusEvent
import com.fuzamei.componentservice.base.DILoadableFragment
import com.fuzamei.componentservice.config.AppConfig
import com.fuzamei.componentservice.ext.findViewModel
import com.fzm.chat33.R
import com.fzm.chat33.core.bean.SearchScope
import com.fzm.chat33.core.bean.ChatTarget
import com.fzm.chat33.core.bean.IdentifyParam
import com.fzm.chat33.core.db.bean.RoomInfoBean
import com.fzm.chat33.core.db.bean.RoomUserBean
import com.fzm.chat33.core.global.Chat33Const
import com.fzm.chat33.core.global.Chat33Const.LEVEL_OWNER
import com.fzm.chat33.main.activity.GroupInfoActivity
import com.fzm.chat33.main.activity.LargePhotoActivity
import com.fzm.chat33.main.mvvm.GroupViewModel
import com.fzm.chat33.widget.ChatAvatarView
import com.fzm.chat33.widget.SwitchView
import com.fuzamei.componentservice.widget.dialog.EasyDialog
import com.fzm.chat33.core.Chat33
import kotlinx.android.synthetic.main.fragment_group_info.*
import javax.inject.Inject

/**
 * @author zhengjy
 * @since 2019/10/22
 * Description:
 */
class GroupInfoFragment : DILoadableFragment(), View.OnClickListener {

    @Inject
    lateinit var provider: ViewModelProvider.Factory
    private lateinit var viewModel: GroupViewModel

    private var data = ArrayList<RoomUserBean>()
    private var owner = RoomUserBean()
    private lateinit var adapter: CommonAdapter<RoomUserBean>

    private lateinit var roomId: String
    private var roomInfo: RoomInfoBean? = null

    private var joinLimitPopup: BottomPopupWindow? = null
    private var addFriendLimitPopup: BottomPopupWindow? = null
    private var muteListPopup: BottomPopupWindow? = null
    private lateinit var option1: Array<String>
    private lateinit var tips1: Array<String>
    private lateinit var option2: Array<String>
    private lateinit var option3: Array<String>
    private lateinit var tips3: Array<String>

    companion object {
        @JvmStatic
        fun create(roomId: String): GroupInfoFragment {
            return GroupInfoFragment().apply {
                arguments = Bundle().apply {
                    putString("roomId", roomId)
                }
            }
        }
    }

    override fun getLayoutId(): Int {
        return R.layout.fragment_group_info
    }

    override fun initView(view: View, savedInstanceState: Bundle?) {
        roomId = arguments?.getString("roomId") ?: ""
        viewModel = findViewModel(provider)

        viewModel.roomInfo.observe(this, Observer { roomInfoBean ->
            if (roomInfoBean == null) {
                return@Observer
            }
            roomInfo = roomInfoBean
            data.clear()
            if (roomInfoBean.joinPermission == 3) {
                if (roomInfoBean.memberLevel > 1) {
                    data.add(RoomUserBean(1))
                }
            } else {
                data.add(RoomUserBean(1))
            }
            if (roomInfoBean.memberLevel > 1) {
                data.add(RoomUserBean(2))
            }
            if (roomInfoBean.users != null) {
                val size = data.size
                if (roomInfoBean.users.size + size <= 10) {
                    data.addAll(0, roomInfoBean.users)
                } else {
                    data.addAll(0, roomInfoBean.users.subList(0, 10 - size))
                }
                for (user in roomInfoBean.users) {
                    if (user.memberLevel == LEVEL_OWNER) {
                        owner = user
                    }
                }
            }
            adapter.notifyDataSetChanged()
            setupViews()
        })
        viewModel.stickyTop.observe(this, Observer { bool ->
            if (bool == null) {
                return@Observer
            }
            sv_sticky_top.toggleSwitch(bool)
        })
        viewModel.noDisturb.observe(this, Observer { bool ->
            if (bool == null) {
                return@Observer
            }
            sv_dnd.toggleSwitch(bool)
        })
        viewModel.recordPermission.observe(this, Observer { bool ->
            if (bool == null) {
                return@Observer
            }
            sv_history.toggleSwitch(bool)
        })
        viewModel.permissionResult.observe(this, Observer { result ->
            if (result != null) {
                ShowUtils.showToastNormal(activity, getString(R.string.chat_tips_group_info18))
                viewModel.getRoomInfo(roomId)
            }
        })
        viewModel.muteResult.observe(this, Observer { result ->
            if (result != null) {
                ShowUtils.showToastNormal(activity, getString(R.string.chat_tips_group_info15))
                if (result == 1) {
                    tv_mute_settings.setText(R.string.chat_tips_group_info11)
                } else if (result == 4) {
                    tv_mute_settings.setText(R.string.chat_tips_group_info14)
                }
            }
        })
        viewModel.deleteResult.observe(this, Observer { result ->
            if (result != null) {
                ShowUtils.showToastNormal(activity, getString(R.string.chat_tips_group_info16))
                Chat33.getRouter().gotoMainPage()
                LiveBus.of(BusEvent::class.java).contactsRefresh().setValue(2)
                finish()
            }
        })
        viewModel.quitResult.observe(this, Observer { result ->
            if (result != null) {
                ShowUtils.showToastNormal(activity, getString(R.string.chat_tips_group_info17))
                Chat33.getRouter().gotoMainPage()
                LiveBus.of(BusEvent::class.java).contactsRefresh().setValue(2)
                finish()
            }
        })
    }

    override fun initData() {
        option1 = resources.getStringArray(R.array.chat_choose_join_limit)
        tips1 = resources.getStringArray(R.array.chat_choose_join_limit_tips)
        option2 = resources.getStringArray(R.array.chat_choose_add_friend_limit)
        option3 = resources.getStringArray(R.array.chat_choose_group_mute)
        tips3 = resources.getStringArray(R.array.chat_choose_add_friend_tips)
    }

    override fun setEvent() {
        ly_see_member.setOnClickListener(this)
        iv_return.setOnClickListener(this)
        tv_exit_dissolve.setOnClickListener(this)
        ll_manager.setOnClickListener(this)
        ll_room_owner.setOnClickListener(this)
        ll_invite.setOnClickListener(this)
        ll_friend.setOnClickListener(this)
        iv_qr_code.setOnClickListener(this)
        ll_notification.setOnClickListener(this)
        ll_group_nickname.setOnClickListener(this)
        ll_mute.setOnClickListener(this)
        ll_chat_history.setOnClickListener(this)
        ll_chat_file.setOnClickListener(this)
        tv_identification.setOnClickListener(this)

        sv_dnd.setOnStateChangedListener(object : SwitchView.OnStateChangedListener {
            override fun toggleToOn(view: SwitchView) {
                view.toggleSwitch(true)
                viewModel.setDND(roomId, 1)
            }

            override fun toggleToOff(view: SwitchView) {
                view.toggleSwitch(false)
                viewModel.setDND(roomId, 2)
            }
        })
        sv_sticky_top.setOnStateChangedListener(object : SwitchView.OnStateChangedListener {
            override fun toggleToOn(view: SwitchView) {
                view.toggleSwitch(true)
                viewModel.stickyOnTop(roomId, 1)
            }

            override fun toggleToOff(view: SwitchView) {
                view.toggleSwitch(false)
                viewModel.stickyOnTop(roomId, 2)
            }
        })
        sv_room_helper.setOnStateChangedListener(object : SwitchView.OnStateChangedListener {
            override fun toggleToOn(view: SwitchView) {
                view.toggleSwitch(true)
                //setRoomHelper(view, 1)
            }

            override fun toggleToOff(view: SwitchView) {
                view.toggleSwitch(false)
                //setRoomHelper(view, 2)
            }
        })
        sv_history.setOnStateChangedListener(object : SwitchView.OnStateChangedListener {
            override fun toggleToOn(view: SwitchView) {
                view.toggleSwitch(true)
                viewModel.setPermission(roomId, 0, 0, 1)
            }

            override fun toggleToOff(view: SwitchView) {
                view.toggleSwitch(false)
                viewModel.setPermission(roomId, 0, 0, 2)
            }
        })
        rv_member.layoutManager = GridLayoutManager(activity, 5)
        adapter = object : CommonAdapter<RoomUserBean>(activity, R.layout.adapter_group_info_member, data) {
            override fun convert(holder: ViewHolder, bean: RoomUserBean, position: Int) {
                when (bean.operation) {
                    1 -> {
                        holder.setImageResource(R.id.operate, R.mipmap.icon_group_info_add)
                        holder.setVisible(R.id.ly_head, false)
                        holder.setVisible(R.id.ly_operate, true)
                        holder.setText(R.id.operate_type, mContext.getString(R.string.chat_action_invite))
                    }
                    2 -> {
                        holder.setImageResource(R.id.operate, R.mipmap.icon_group_info_minus)
                        holder.setVisible(R.id.ly_head, false)
                        holder.setVisible(R.id.ly_operate, true)
                        holder.setText(R.id.operate_type, mContext.getString(R.string.chat_action_remove))
                    }
                    else -> {
                        holder.setVisible(R.id.ly_head, true)
                        holder.setVisible(R.id.ly_operate, false)
                        if (!TextUtils.isEmpty(bean.avatar)) {
                            Glide.with(mContext).load(bean.avatar)
                                    .apply(RequestOptions().placeholder(R.mipmap.default_avatar_round))
                                    .into(holder.getView(R.id.head) as ImageView)
                        } else {
                            holder.setImageResource(R.id.head, R.mipmap.default_avatar_round)
                        }
                        (holder.getView(R.id.head) as ChatAvatarView)
                                .setIconRes(if (bean.isIdentified) R.drawable.ic_user_identified else -1)
                        holder.setText(R.id.name, bean.displayName)
                    }
                }
            }
        }
        adapter.setOnItemClickListener(object : MultiItemTypeAdapter.OnItemClickListener {
            override fun onItemClick(view: View, holder: RecyclerView.ViewHolder, position: Int) {
                when (data[position].operation) {
                    1 -> ARouter.getInstance().build(AppRoute.CREATE_GROUP)
                            .withString("roomId", roomId)
                            .navigation()
                    2 -> ARouter.getInstance().build(AppRoute.SELECT_GROUP_MEMBER)
                            .withSerializable("roomInfo", roomInfo)
                            .withBoolean("selectable", true)
                            .withString("action", "remove")
                            .withInt("memberLevel", 1)
                            .navigation()
                    else -> {
                        if (data[position].id == viewModel.getUserId()) {
                            ShowUtils.showToastNormal(activity, getString(R.string.chat_tips_group_info1))
                            return
                        }
                        ARouter.getInstance().build(AppRoute.USER_DETAIL)
                                .withString("userId", data[position].id)
                                .withString("roomId", roomId)
                                .withInt("memberLevel", roomInfo?.memberLevel ?: 1)
                                .withBoolean("canAddFriend", roomInfo?.canAddFriend == 1)
                                .navigation()
                    }
                }
            }

            override fun onItemLongClick(view: View, holder: RecyclerView.ViewHolder, position: Int): Boolean {
                return false
            }
        })
        rv_member.adapter = adapter
    }

    private fun setupViews() {
        if (!TextUtils.isEmpty(roomInfo?.avatar)) {
            Glide.with(activity).load(roomInfo?.avatar)
                    .apply(RequestOptions().placeholder(R.mipmap.default_avatar_room))
                    .into(iv_group_head)
        }
        iv_group_head.setIconRes(if (roomInfo?.isIdentified == true) R.drawable.ic_group_identified else -1)
        tv_name.text = roomInfo?.name
        tv_room_markid.text = getString(R.string.room_id, roomInfo?.markId)
        tv_member_num.text = getString(R.string.room_member_num_total, roomInfo?.memberNumber)
        sv_dnd.isOpened = roomInfo?.noDisturbing == 1
        sv_sticky_top.isOpened = roomInfo?.onTop == 1
        tv_notify_count.text = getString(R.string.chat_tips_group_info2, roomInfo?.systemMsg?.number
                ?: 0)
        if (roomInfo?.systemMsg?.number != 0) {
            tv_notification_content.text = roomInfo!!.systemMsg.list[0].content
            ll_notification_content.visibility = View.VISIBLE
        } else {
            ll_notification_content.visibility = View.GONE
        }
        tv_group_nickname.text = roomInfo?.roomNickname.orEmpty()
        if (AppConfig.APP_IDENTIFY) {
            if (roomInfo?.isIdentified == true) {
                if (roomInfo?.memberLevel ?: 1 > 1) {
                    tv_identification.text = resources.getString(R.string.chat_tips_identification_tip3, roomInfo?.getIdentificationInfo())
                } else {
                    tv_identification.text = getString(R.string.chat_tips_identification_tip2, roomInfo?.getIdentificationInfo())
                }
            } else {
                if (roomInfo?.memberLevel ?: 1 > 1) {
                    tv_identification.text = Html.fromHtml(resources.getString(R.string.chat_tips_identification, AppConfig.APP_ACCENT_COLOR_STR))
                }
            }
            tv_identification.visibility = View.VISIBLE
        } else {
            tv_identification.visibility = View.GONE
        }

        if (roomInfo?.memberLevel ?: 1 > 1) {
            val drawable = ContextCompat.getDrawable(activity, R.mipmap.icon_my_edit)
            drawable?.setBounds(0, 0, drawable.minimumWidth, drawable.minimumHeight)
            tv_name.setCompoundDrawables(null, null, drawable, null)
            tv_name.compoundDrawablePadding = 20
            iv_group_head.setOnClickListener(this)
            tv_name.setOnClickListener(this)
            ly_group_set.visibility = View.VISIBLE
            tv_set_admin.text = getString(R.string.chat_tips_group_info3, roomInfo?.managerNumber)
            tv_change_owner.text = owner.nickname
            when (roomInfo?.joinPermission) {
                1 -> tv_join_limit.setText(R.string.chat_tips_group_info4)
                2 -> tv_join_limit.setText(R.string.chat_tips_group_info5)
                else -> tv_join_limit.setText(R.string.chat_tips_group_info6)
            }
            when (roomInfo?.canAddFriend) {
                1 -> tv_friend_limit.setText(R.string.chat_tips_group_info7)
                else -> tv_friend_limit.setText(R.string.chat_tips_group_info8)
            }
            when (roomInfo?.memberLevel) {
                2 -> tv_exit_dissolve.setText(R.string.chat_tips_group_info9)
                else -> tv_exit_dissolve.setText(R.string.chat_tips_group_info10)
            }
            when (roomInfo?.roomMutedType) {
                1 -> tv_mute_settings.setText(R.string.chat_tips_group_info11)
                2 -> tv_mute_settings.text = getString(R.string.chat_tips_group_info12, roomInfo?.mutedNumber)
                3 -> tv_mute_settings.text = getString(R.string.chat_tips_group_info13, roomInfo?.mutedNumber)
                4 -> tv_mute_settings.setText(R.string.chat_tips_group_info14)
            }
            sv_history.isOpened = roomInfo?.recordPermission == 1
        } else {
            tv_name.setCompoundDrawables(null, null, null, null)
            iv_group_head.setOnClickListener(this)
            tv_name.setOnClickListener(null)
            ly_group_set.visibility = View.GONE
            tv_exit_dissolve.setText(R.string.chat_tips_group_info9)
        }
    }

    override fun onClick(v: View?) {
        if (roomInfo == null && v?.id != R.id.iv_return) {
            return
        }
        val i = v?.id
        if (i == R.id.iv_qr_code) {
            ARouter.getInstance().build(AppRoute.QR_CODE)
                    .withString("id", roomInfo?.id)
                    .withString("content", roomInfo?.markId)
                    .withString("avatar", roomInfo?.avatar)
                    .withString("name", roomInfo?.name)
                    .navigation()
        } else if (i == R.id.iv_group_head) {
            if (roomInfo?.memberLevel ?: 1 > 1) {
                ARouter.getInstance().build(AppRoute.EDIT_AVATAR)
                        .withString("avatar", roomInfo?.avatar)
                        .withString("id", roomInfo?.id)
                        .withInt("channelType", Chat33Const.CHANNEL_ROOM)
                        .navigation()
            } else {
                val intent = Intent(activity, LargePhotoActivity::class.java)
                intent.putExtra(LargePhotoActivity.IMAGE_URL, roomInfo?.avatar)
                intent.putExtra(LargePhotoActivity.CHANNEL_TYPE, Chat33Const.CHANNEL_ROOM)
                startActivity(intent, ActivityOptionsCompat
                        .makeSceneTransitionAnimation(activity,
                                v, "shareImage").toBundle())
            }
        } else if (i == R.id.tv_name) {
            ARouter.getInstance().build(AppRoute.EDIT_NAME)
                    .withString("id", roomInfo?.id)
                    .withString("name", roomInfo?.name)
                    .withInt("channelType", Chat33Const.CHANNEL_ROOM)
                    .navigation()
        } else if (i == R.id.ly_see_member) {
            ARouter.getInstance().build(AppRoute.GROUP_MEMBER)
                    .withSerializable("roomInfo", roomInfo)
                    .navigation()
        } else if (i == R.id.tv_identification) {
            if (roomInfo?.memberLevel ?: 1 > 1) {
                val source = IdentifyParam.create(roomInfo?.id)
                val param = AESUtil.encrypt(source, AESUtil.DEFAULT_KEY)
                ARouter.getInstance().build(AppRoute.WEB_BROWSER)
                        .withString("url", AppConfig.APP_URL + "/cert/#/?para=" + param)
                        .withInt("titleColor", -0xcd4d09)
                        .withInt("textColor", -0x50404)
                        .withBoolean("darkMode", false)
                        .withBoolean("showOptions", false)
                        .navigation()
            }
        } else if (i == R.id.iv_return) {
            finish()
        } else if (i == R.id.ll_notification) {
            ARouter.getInstance().build(AppRoute.GROUP_NOTICE)
                    .withString("roomId", roomId)
                    .withInt("memberLevel", roomInfo?.memberLevel ?: 1)
                    .navigation()
        } else if (i == R.id.ll_group_nickname) {
            ARouter.getInstance().build(AppRoute.EDIT_GROUP_INFO)
                    .withString("roomId", roomId)
                    .withString("groupName", roomInfo?.name)
                    .withInt("type", 2)
                    .withString("content", roomInfo?.roomNickname)
                    .navigation()
        } else if (i == R.id.ll_chat_history) {
            ARouter.getInstance()
                    .build(AppRoute.SEARCH_LOCAL_SCOPE)
                    .withInt("scope", SearchScope.CHATLOG)
                    .withSerializable("chatTarget", ChatTarget(Chat33Const.CHANNEL_ROOM, roomId))
                    .withBoolean("popKeyboard", true)
                    .navigation()
        } else if (i == R.id.ll_chat_file) {
            ARouter.getInstance().build(AppRoute.CHAT_FILE)
                    .withString("targetId", roomId)
                    .withInt("channelType", Chat33Const.CHANNEL_ROOM)
                    .navigation()
        } else if (i == R.id.ll_manager) {
            ARouter.getInstance().build(AppRoute.ADMIN_SET)
                    .withSerializable("roomInfo", roomInfo)
                    .navigation()
        } else if (i == R.id.ll_room_owner) {
            if (roomInfo?.memberLevel ?: 1 == 3) {
                ARouter.getInstance().build(AppRoute.SELECT_GROUP_MEMBER)
                        .withSerializable("roomInfo", roomInfo)
                        .withBoolean("selectable", false)
                        .withInt("memberLevel", 2)
                        .withString("action", "change_owner")
                        .navigation(activity, GroupInfoActivity.CODE_CHANGE_OWNER)
            }
        } else if (i == R.id.ll_invite) {
            if (joinLimitPopup == null) {
                joinLimitPopup = BottomPopupWindow(activity, listOf(*option1), listOf(*tips1),
                        BottomPopupWindow.OnItemClickListener { _, popupWindow, position ->
                            popupWindow.dismiss()
                            if (position != 3) {
                                viewModel.setPermission(roomId, 0, position + 1, 0)
                            }
                        })
            }
            joinLimitPopup?.showAtLocation(ll_invite, Gravity.BOTTOM, 0, 0)
        } else if (i == R.id.ll_friend) {
            if (addFriendLimitPopup == null) {
                addFriendLimitPopup = BottomPopupWindow(activity, listOf(*option2),
                        BottomPopupWindow.OnItemClickListener { _, popupWindow, position ->
                            popupWindow.dismiss()
                            if (position != 2) {
                                viewModel.setPermission(roomId, position + 1, 0, 0)
                            }
                        })
            }
            addFriendLimitPopup?.showAtLocation(ll_friend, Gravity.BOTTOM, 0, 0)
        } else if (i == R.id.ll_mute) {
            if (muteListPopup == null) {
                muteListPopup = BottomPopupWindow(activity, listOf(*option3), listOf(*tips3),
                        BottomPopupWindow.OnItemClickListener { _, popupWindow, position ->
                            popupWindow.dismiss()
                            when (position) {
                                0 -> viewModel.setMutedList(roomId, 1, null, -1)
                                1 -> viewModel.setMutedList(roomId, 4, null, -1)
                                2 -> ARouter.getInstance().build(AppRoute.SELECT_GROUP_MEMBER)
                                        .withSerializable("roomInfo", roomInfo)
                                        .withBoolean("selectable", true)
                                        .withInt("memberLevel", 1)
                                        .withString("action", "mute_reverse")
                                        .navigation()
                                3 -> ARouter.getInstance().build(AppRoute.SELECT_GROUP_MEMBER)
                                        .withSerializable("roomInfo", roomInfo)
                                        .withBoolean("selectable", true)
                                        .withInt("memberLevel", 1)
                                        .withString("action", "mute")
                                        .navigation()
                            }
                        })
            }
            muteListPopup?.showAtLocation(ll_mute, Gravity.BOTTOM, 0, 0)
        } else if (i == R.id.tv_exit_dissolve) {
            if (roomInfo?.memberLevel ?: 1 == 3) {
                val content = getString(R.string.chat_dialog_dismiss_group, AppConfig.APP_ACCENT_COLOR_STR, roomInfo?.name)
                val dialog = EasyDialog.Builder()
                        .setHeaderTitle(getString(R.string.chat_tips_tips))
                        .setBottomLeftText(getString(R.string.chat_action_cancel))
                        .setBottomRightText(getString(R.string.chat_action_confirm))
                        .setContent(Html.fromHtml(content))
                        .setBottomLeftClickListener(null)
                        .setBottomRightClickListener { dialog ->
                            dialog.dismiss()
                            viewModel.deleteRoom(roomId)
                        }.create(activity)
                dialog.show()
            } else {
                val content = getString(R.string.chat_dialog_exit_group, AppConfig.APP_ACCENT_COLOR_STR, roomInfo?.name)
                val dialog = EasyDialog.Builder()
                        .setHeaderTitle(getString(R.string.chat_tips_tips))
                        .setBottomLeftText(getString(R.string.chat_action_cancel))
                        .setBottomRightText(getString(R.string.chat_action_confirm))
                        .setContent(Html.fromHtml(content))
                        .setBottomLeftClickListener(null)
                        .setBottomRightClickListener { dialog ->
                            dialog.dismiss()
                            viewModel.quitRoom(roomId)
                        }.create(activity)
                dialog.show()
            }
        }
    }
}