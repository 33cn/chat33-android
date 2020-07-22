package com.fzm.chat33.main.fragment

import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.ImageView
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.alibaba.android.arouter.launcher.ARouter
import com.baidu.crabsdk.CrabSDK
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.fuzamei.common.bus.LiveBus
import com.fuzamei.common.callback.GlideTarget
import com.fuzamei.common.net.EventObserver

import com.fuzamei.common.recycleviewbase.CommonAdapter
import com.fuzamei.common.recycleviewbase.MultiItemTypeAdapter
import com.fuzamei.common.recycleviewbase.RecyclerViewDivider
import com.fuzamei.common.recycleviewbase.ViewHolder
import com.fuzamei.common.utils.RoomUtils
import com.fuzamei.common.utils.ShowUtils
import com.fuzamei.common.utils.ToolUtils
import com.fuzamei.componentservice.app.AppRoute
import com.fuzamei.componentservice.app.BusEvent
import com.fuzamei.componentservice.base.DILoadableFragment
import com.fuzamei.componentservice.event.ChangeTabEvent
import com.fuzamei.componentservice.ext.findViewModel
import com.fuzamei.componentservice.ext.flatMapLines
import com.fzm.chat33.R
import com.fzm.chat33.core.db.ChatDatabase
import com.fzm.chat33.core.db.bean.ChatMessage
import com.fzm.chat33.core.db.bean.FriendBean
import com.fzm.chat33.core.db.bean.RecentMessageBean
import com.fzm.chat33.core.global.Chat33Const
import com.fzm.chat33.core.global.UserInfo
import com.fzm.chat33.main.mvvm.MessageViewModel
import com.fzm.chat33.main.popupwindow.SessionOperatePopupWindow
import com.fzm.chat33.widget.ChatAvatarView
import kotlinx.android.synthetic.main.fragment_contacts.*
import javax.inject.Inject

/**
 * 创建日期：2018/10/8 on 14:58
 *
 * @author zhengjy
 * @since 2018/10/29
 * Description:最近个人消息列表
 */
class ContactsFragment : DILoadableFragment() {

    @Inject
    lateinit var provider: ViewModelProvider.Factory
    lateinit var viewModel: MessageViewModel

    private lateinit var adapter: CommonAdapter<RecentMessageBean>
    private val recentMessageList = arrayListOf<RecentMessageBean>()
    private var popupWindow: SessionOperatePopupWindow? = null

    private var touchX: Int = 0
    private var touchY: Int = 0
    private var isClick = false

    override fun getLayoutId(): Int {
        return R.layout.fragment_contacts
    }

    override fun initView(view: View, savedInstanceState: Bundle?) {
        viewModel = findViewModel(provider)
        rv_contacts.layoutManager = LinearLayoutManager(activity)
        rv_contacts.addItemDecoration(RecyclerViewDivider(activity, LinearLayoutManager.VERTICAL,
                0.5f, ContextCompat.getColor(activity, R.color.chat_color_line)))
        adapter = object : CommonAdapter<RecentMessageBean>(activity, R.layout.adapter_contacts, recentMessageList) {
            override fun convert(holder: ViewHolder, message: RecentMessageBean, position: Int) {
                if (TextUtils.isEmpty(message.getDisplayName())) {
                    holder.getView<View>(R.id.image).setTag(R.id.image, message.id)
                    holder.getView<View>(R.id.title).setTag(R.id.title, message.id)
                    val target = GlideTarget(holder.getView(R.id.image), R.id.image, message.id)
                    viewModel.getUserInfo(message.id).observe(this@ContactsFragment, Observer {
                        if (it.isSucceed()) {
                            modifyFriendData(it.data())
                            Glide.with(mContext).load(it.data().avatar)
                                    .apply(RequestOptions().placeholder(R.mipmap.default_avatar_round))
                                    .into(target)
                            (holder.getView(R.id.image) as ChatAvatarView)
                                    .setIconRes(if (it.data().isIdentified) R.drawable.ic_user_identified else -1)
                            if (message.id == holder.getView<View>(R.id.title).getTag(R.id.title)) {
                                holder.setText(R.id.title, it.data().displayName)
                            }
                            message.isDeleted = true
                        } else {
                            if (message.id == holder.getView<View>(R.id.title).getTag(R.id.title)) {
                                holder.setText(R.id.title, mContext.getString(R.string.chat_tips_no_name))
                            }
                            message.isDeleted = true
                        }
                    })
                } else {
                    Glide.with(mContext).load(message.avatar)
                            .apply(RequestOptions().placeholder(R.mipmap.default_avatar_round))
                            .into(holder.getView<View>(R.id.image) as ImageView)
                    holder.setText(R.id.title, message.getDisplayName())
                    (holder.getView(R.id.image) as ChatAvatarView)
                            .setIconRes(if (message.isIdentified()) R.drawable.ic_user_identified else -1)
                }
                val rewardNum = message.recent_like + message.recent_reward
                if (rewardNum > 0) {
                    holder.setVisible(R.id.tv_reward, true)
                    holder.setText(R.id.tv_reward, getString(R.string.chat_unread_reward, rewardNum))
                    if (message.recent_reward > 0) {
                        holder.setTextColorRes(R.id.tv_reward, R.color.chat_reward_orange)
                    } else {
                        holder.setTextColorRes(R.id.tv_reward, R.color.chat_color_accent)
                    }
                } else {
                    holder.setVisible(R.id.tv_reward, false)
                }

                if (!message.isDeleted) {
                    if (message.stickyTop == 1) {
                        holder.setBackgroundRes(R.id.ll_container, R.drawable.basic_selector_bg_dark)
                    } else {
                        holder.setBackgroundRes(R.id.ll_container, R.drawable.basic_selector_bg)
                    }
                    holder.setVisible(R.id.iv_disturb, message.noDisturb == 1)
                } else {
                    holder.setBackgroundRes(R.id.ll_container, R.drawable.basic_selector_bg)
                    holder.setVisible(R.id.iv_disturb, false)
                }
                //final RecentMessage.LastLogBean log = message.getLastLog();
                holder.setText(R.id.time, ToolUtils.timeFormat(message.datetime))
                var content: String? = ""
                when (message.msgType) {
                    ChatMessage.Type.SYSTEM -> content = mContext.getString(R.string.core_msg_type1) + message.content.orEmpty()
                    ChatMessage.Type.TEXT -> content = if (message.content == null) mContext.getString(R.string.core_msg_type11) else message.content
                    ChatMessage.Type.AUDIO -> content = mContext.getString(R.string.core_msg_type2)
                    ChatMessage.Type.IMAGE -> content = mContext.getString(R.string.core_msg_type3)
                    ChatMessage.Type.RED_PACKET -> {
                        content = mContext.getString(R.string.core_msg_type4) + message.redBagRemark.orEmpty()
                    }
                    ChatMessage.Type.VIDEO -> content = mContext.getString(R.string.core_msg_type5)
                    ChatMessage.Type.NOTIFICATION -> content = mContext.getString(R.string.core_msg_type6) + message.content.orEmpty()
                    ChatMessage.Type.FORWARD -> content = mContext.getString(R.string.core_msg_type7)
                    ChatMessage.Type.FILE -> content = mContext.getString(R.string.core_msg_type12) + message.fileName.orEmpty()
                    ChatMessage.Type.TRANSFER -> content = if (message.isSentType()) {
                        mContext.getString(R.string.core_msg_type8) + mContext.getString(R.string.core_msg_type_transfer_out)
                    } else {
                        mContext.getString(R.string.core_msg_type8) + mContext.getString(R.string.core_msg_type_transfer_in)
                    }
                    ChatMessage.Type.RECEIPT -> content = if (message.isSentType()) {
                        mContext.getString(R.string.core_msg_type9) + mContext.getString(R.string.core_msg_type_receipt_out)
                    } else {
                        mContext.getString(R.string.core_msg_type9) + mContext.getString(R.string.core_msg_type_receipt_in)
                    }
                    ChatMessage.Type.INVITATION -> content = if (message.inviterId == UserInfo.getInstance().id) {
                        mContext.getString(R.string.core_msg_type14)
                    } else {
                        mContext.getString(R.string.core_msg_type13)
                    }
                }
                if (message.isSnap == 1) {
                    content = mContext.getString(R.string.core_msg_type10)
                }
                holder.setText(R.id.desc, content.flatMapLines())
                if (message.number == 0) {
                    holder.setVisible(R.id.num, false)
                } else {
                    val num = if (message.number > 99) "..." else message.number.toString()
                    holder.setText(R.id.num, num)
                    if (message.noDisturb == 1) {
                        holder.setBackgroundRes(R.id.num, R.drawable.shape_grey_dot)
                    } else {
                        holder.setBackgroundRes(R.id.num, R.drawable.shape_red_dot)
                    }
                    holder.setVisible(R.id.num, true)
                }
            }
        }
        adapter.setOnItemTouchListener { _, event ->
            touchX = event.x.toInt()
            touchY = event.y.toInt()
            when (event.action) {
                MotionEvent.ACTION_DOWN -> isClick = true
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> isClick = false
            }
        }
        adapter.setOnItemClickListener(object : MultiItemTypeAdapter.OnItemClickListener {
            override fun onItemClick(view: View, holder: RecyclerView.ViewHolder, position: Int) {
                try {
                    val recentMessage = recentMessageList[position]
                    ARouter.getInstance()
                            .build(AppRoute.CHAT)
                            .withBoolean("isGroupChat", false)
                            .withInt("channelType", Chat33Const.CHANNEL_FRIEND)
                            .withString("targetName", recentMessage.getDisplayName())
                            .withString("targetId", recentMessage.id)
                            .withBoolean("isDeleted", recentMessage.isDeleted)
                            .navigation()
                } catch (e: NullPointerException) {
                    ShowUtils.showToastNormal(activity, getString(R.string.chat_tips_msg_not_exist))
                    RoomUtils.run(Runnable {
                        ChatDatabase.getInstance().recentMessageDao()
                                .deleteMessage(Chat33Const.CHANNEL_FRIEND, recentMessageList[position].id)
                    })
                } catch (e: Exception) {
                    CrabSDK.uploadException(e)
                }
            }

            override fun onItemLongClick(view: View, holder: RecyclerView.ViewHolder, position: Int): Boolean {
                try {
                    val recentMessage = recentMessageList[position]
                    showPop(view, recentMessage)
                } catch (e: NullPointerException) {
                    RoomUtils.run(Runnable {
                        ChatDatabase.getInstance().recentMessageDao()
                                .deleteMessage(Chat33Const.CHANNEL_FRIEND, recentMessageList[position].id)
                    })
                } catch (e: Exception) {
                    CrabSDK.uploadException(e)
                }
                return true
            }
        })
        rv_contacts.adapter = adapter
        getContactsList()
    }

    override fun initData() {

    }

    override fun setEvent() {
        val startChat = statusLayout.emptyView.findViewById<View>(R.id.tv_start_chat)
        startChat.setOnClickListener {
            LiveBus.of(BusEvent::class.java).changeTab().setValue(ChangeTabEvent(2, 0))
        }
        viewModel.contactMessage.observe(this, Observer { recentMessages ->
            recentMessageList.clear()
            recentMessageList.addAll(recentMessages)
            if (!isClick) {
                adapter.notifyDataSetChanged()
                if (recentMessages.isEmpty()) {
                    rv_contacts.visibility = View.GONE
                    statusLayout.showEmpty()
                } else {
                    rv_contacts.visibility = View.VISIBLE
                    statusLayout.showContent()
                }
            }
        })
        viewModel.contactException.observe(this, EventObserver {
            getContactsList()
        })
    }

    private fun getContactsList() {
        viewModel.getContactMessage()
    }

    private fun showPop(view: View, message: RecentMessageBean) {
        if (popupWindow == null) {
            popupWindow = SessionOperatePopupWindow(activity, viewModel,
                    LayoutInflater.from(activity).inflate(R.layout.popupwindow_session_operate, null), Chat33Const.CHANNEL_FRIEND)
            popupWindow?.softInputMode = WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE
        }
        view.setBackgroundColor(ContextCompat.getColor(activity, R.color.basic_color_bg_pressed))
        popupWindow?.setOnDismissListener {
            if (!message.isDeleted) {
                if (message.stickyTop == 1) {
                    view.setBackgroundResource(R.drawable.basic_selector_bg_dark)
                } else {
                    view.setBackgroundResource(R.drawable.basic_selector_bg)
                }
            } else {
                view.setBackgroundResource(R.drawable.basic_selector_bg)
            }
        }
        popupWindow?.setId(message.id)
        popupWindow?.setName(message.getDisplayName())
        popupWindow?.setSticky(message.onTop)
        popupWindow?.setDnd(message.noDisturbing)
        popupWindow?.setIsDeleted(message.isDeleted)
        popupWindow?.show(view, touchX, touchY)
    }

    private fun modifyFriendData(bean: FriendBean) {
        for (datum in recentMessageList) {
            if (bean.id.equals(datum.id, ignoreCase = true)) {
                datum.avatar = bean.avatar
                datum.name = bean.name
                datum.remark = bean.remark
                datum.identification = bean.identification
                break
            }
        }
    }
}
