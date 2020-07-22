package com.fzm.chat33.main.fragment

import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.ImageView
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager

import com.alibaba.android.arouter.launcher.ARouter
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.fuzamei.common.bus.LiveBus
import com.fuzamei.common.recycleviewbase.CommonAdapter
import com.fuzamei.common.recycleviewbase.ViewHolder
import com.fuzamei.common.utils.ActivityUtils
import com.fuzamei.common.utils.RoomUtils
import com.fuzamei.common.utils.ShowUtils
import com.fuzamei.common.utils.ToolUtils
import com.fuzamei.componentservice.app.AppRoute
import com.fuzamei.componentservice.app.BusEvent
import com.fuzamei.componentservice.base.DILoadableFragment
import com.fuzamei.componentservice.config.AppConfig
import com.fuzamei.componentservice.event.ChangeTabEvent
import com.fuzamei.componentservice.ext.findViewModel
import com.fzm.chat33.R
import com.fzm.chat33.core.Chat33
import com.fzm.chat33.core.bean.ApplyInfoBean
import com.fzm.chat33.core.bean.RelationshipBean
import com.fzm.chat33.core.db.ChatDatabase
import com.fzm.chat33.core.global.UserInfo
import com.fzm.chat33.core.manager.GroupKeyManager
import com.fzm.chat33.global.AppConst
import com.fzm.chat33.main.activity.NewFriendActivity
import com.scwang.smartrefresh.layout.api.RefreshLayout
import com.scwang.smartrefresh.layout.listener.OnRefreshLoadMoreListener

import java.util.ArrayList

import com.fzm.chat33.core.global.Chat33Const.CHANNEL_FRIEND
import com.fzm.chat33.core.global.Chat33Const.CHANNEL_ROOM
import com.fzm.chat33.main.mvvm.NewFriendViewModel
import kotlinx.android.synthetic.main.fragment_new_friend.*
import javax.inject.Inject

/**
 * @author zhengjy
 * @since 2019/10/21
 * Description:好友申请页面
 */
class NewFriendFragment : DILoadableFragment() {

    @Inject
    lateinit var provider: ViewModelProvider.Factory
    lateinit var viewModel: NewFriendViewModel

    private val data = ArrayList<ApplyInfoBean>()
    private var adapter: CommonAdapter<ApplyInfoBean>? = null
    private var nextId = "-1"
    private var initLoad = true

    override fun getLayoutId(): Int {
        return R.layout.fragment_new_friend
    }

    override fun initView(view: View?, savedInstanceState: Bundle?) {
        viewModel = findViewModel(provider)
        viewModel.loading.observe(this, Observer { setupLoading(it) })
        viewModel.applyList.observe(this, Observer {
            initLoad = false
            if (it.isSucceed()) {
                nextId = it.data().nextId
                if (it.data().clearData) {
                    data.clear()
                }
                swipeLayout.finishRefresh()
                if ("-1" == it.data().nextId/*data.size() == wrapper.totalNumber*/) {
                    swipeLayout.finishLoadMoreWithNoMoreData()
                } else {
                    swipeLayout.finishLoadMore()
                }
                if (it.data().applyList != null && it.data().applyList.size > 0) {
                    data.addAll(it.data().applyList)
                }
                v_line.visibility = if (data.size > 0) View.VISIBLE else View.GONE
                adapter?.notifyDataSetChanged()
            } else {
                v_line.visibility = if (data.size > 0) View.VISIBLE else View.GONE
                swipeLayout.finishRefresh(false)
                swipeLayout.finishLoadMore(false)
            }
        })
        viewModel.friendRequest.observe(this, Observer { triple ->
            if (triple.first == 1) {
                LiveBus.of(BusEvent::class.java).contactsRefresh().setValue(1)
                RoomUtils.run(Runnable {
                    ChatDatabase.getInstance().recentMessageDao().markDelete(false, CHANNEL_FRIEND, triple.third)
                })
                ShowUtils.showToastNormal(activity, activity.getString(R.string.chat_tips_new_friend1))
                data[triple.second].status = 3
            } else {
                ShowUtils.showToastNormal(activity, activity.getString(R.string.chat_tips_new_friend2))
                data[triple.second].status = 2
            }
            adapter?.notifyItemChanged(triple.second)
        })
        viewModel.groupRequest.observe(this, Observer { triple ->
            if (triple.first == 1) {
                val room = Chat33.loadRoomFromCache(triple.third)
                // 如果是加密群聊则更新会话密钥
                if (room?.encrypt == 1) {
                    GroupKeyManager.notifyGroupEncryptKey(triple.third)
                }
                LiveBus.of(BusEvent::class.java).contactsRefresh().setValue(2)
                ShowUtils.showToastNormal(activity, activity.getString(R.string.chat_tips_new_friend3))
                data[triple.second].status = 3
                adapter?.notifyItemChanged(triple.second)
            } else {
                ShowUtils.showToastNormal(activity, activity.getString(R.string.chat_tips_new_friend4))
                data[triple.second].status = 2
                adapter?.notifyItemChanged(triple.second)
            }
        })
    }

    override fun initData() {
        swipeLayout.setOnRefreshLoadMoreListener(object : OnRefreshLoadMoreListener {
            override fun onLoadMore(refreshLayout: RefreshLayout) {
                viewModel.getFriendsApplyList(nextId, AppConst.PAGE_SIZE, initLoad)
            }

            override fun onRefresh(refreshLayout: RefreshLayout) {
                viewModel.getFriendsApplyList(null, AppConst.PAGE_SIZE, initLoad)
            }
        })
        adapter = object : CommonAdapter<ApplyInfoBean>(activity, R.layout.adapter_new_friend, data) {
            override fun convert(holder: ViewHolder, applyInfoBean: ApplyInfoBean, position: Int) {
                val senderAvatar = applyInfoBean.senderInfo.avatar
                val receiverAvatar = applyInfoBean.receiveInfo.avatar
                val reason = applyInfoBean.applyReason
                if (position == 0) {
                    holder.setText(R.id.tv_date, ToolUtils.formatMonth(applyInfoBean.datetime))
                    holder.setVisible(R.id.tv_date, true)
                } else {
                    val preMonth = ToolUtils.formatMonth(data[position - 1].datetime)
                    val currentMonth = ToolUtils.formatMonth(applyInfoBean.datetime)
                    if (currentMonth == preMonth) {
                        holder.setVisible(R.id.tv_date, false)
                    } else {
                        holder.setText(R.id.tv_date, currentMonth)
                        holder.setVisible(R.id.tv_date, true)
                    }
                }
                if (applyInfoBean.senderInfo.id == AppConfig.MY_ID) {
                    holder.setText(R.id.tv_name, applyInfoBean.receiveInfo.name)
                    if (!TextUtils.isEmpty(receiverAvatar)) {
                        Glide.with(mContext).load(receiverAvatar)
                                .apply(RequestOptions().placeholder(R.mipmap.default_avatar_round))
                                .into(holder.getView<View>(R.id.iv_avatar) as ImageView)
                    } else {
                        holder.setImageResource(R.id.iv_avatar, R.mipmap.default_avatar_round)
                    }
                } else {
                    holder.setText(R.id.tv_name, applyInfoBean.senderInfo.name)
                    if (!TextUtils.isEmpty(senderAvatar)) {
                        Glide.with(mContext).load(senderAvatar)
                                .apply(RequestOptions().placeholder(R.mipmap.default_avatar_room))
                                .into(holder.getView<View>(R.id.iv_avatar) as ImageView)
                    } else {
                        holder.setImageResource(R.id.iv_avatar, R.mipmap.default_avatar_room)
                    }
                }
                holder.setText(R.id.tv_desc, applyInfoBean.source)
                if (!TextUtils.isEmpty(reason)) {
                    holder.setText(R.id.tv_verify_info, reason)
                    holder.setVisible(R.id.tv_verify_info, true)
                } else {
                    holder.setVisible(R.id.tv_verify_info, false)
                }
                val status = applyInfoBean.status
                if (status == 1) {
                    if (applyInfoBean.senderInfo.id == AppConfig.MY_ID) {
                        holder.setText(R.id.tv_status, mContext.getString(R.string.chat_tips_new_friend_status1))
                        holder.setVisible(R.id.tv_status, true)
                        holder.setVisible(R.id.iv_disagree, false)
                        holder.setVisible(R.id.iv_agree, false)
                    } else {
                        holder.setVisible(R.id.tv_status, false)
                        holder.setVisible(R.id.iv_disagree, true)
                        holder.setVisible(R.id.iv_agree, true)
                    }
                } else if (status == 2) {
                    holder.setText(R.id.tv_status, mContext.getString(R.string.chat_tips_new_friend_status2))
                    holder.setVisible(R.id.tv_status, true)
                    holder.setVisible(R.id.iv_disagree, false)
                    holder.setVisible(R.id.iv_agree, false)
                } else {
                    holder.setText(R.id.tv_status, mContext.getString(R.string.chat_tips_new_friend_status3))
                    holder.setVisible(R.id.tv_status, true)
                    holder.setVisible(R.id.iv_disagree, false)
                    holder.setVisible(R.id.iv_agree, false)
                }
                if (status != 1) {
                    holder.setOnClickListener(R.id.fl_container) {
                        val infoBean: ApplyInfoBean.InfoBean
                        val channelType: Int
                        when {
                            applyInfoBean.receiveInfo.id == UserInfo.getInstance().id -> {
                                channelType = 3
                                infoBean = applyInfoBean.senderInfo
                            }
                            applyInfoBean.senderInfo.id == UserInfo.getInstance().id -> {
                                channelType = if (applyInfoBean.type == 1) 2 else 3
                                infoBean = applyInfoBean.receiveInfo
                            }
                            else -> {
                                channelType = 3
                                infoBean = applyInfoBean.senderInfo
                            }
                        }
                        viewModel.hasRelationship(channelType, infoBean.id)
                                .observe(this@NewFriendFragment, Observer {
                                    navigateToChat(infoBean, channelType, it)
                                })
                    }
                }
                holder.setOnClickListener(R.id.iv_agree) {
                    if (applyInfoBean.type == 1) {
                        viewModel.dealJoinRoomApply(applyInfoBean.receiveInfo.id,
                                applyInfoBean.senderInfo.id, 1, position)
                    } else {
                        viewModel.dealFriendRequest(applyInfoBean.senderInfo.id, 1, position)
                    }
                }
                holder.setOnClickListener(R.id.iv_disagree) {
                    if (applyInfoBean.type == 1) {
                        viewModel.dealJoinRoomApply(applyInfoBean.receiveInfo.id,
                                applyInfoBean.senderInfo.id, 2, position)
                    } else {
                        viewModel.dealFriendRequest(applyInfoBean.senderInfo.id, 2, position)
                    }
                }
            }
        }
        rv_friend.layoutManager = LinearLayoutManager(activity)
        rv_friend.adapter = adapter
        viewModel.getFriendsApplyList(null, AppConst.PAGE_SIZE, initLoad)
    }

    override fun setEvent() {

    }

    private fun navigateToChat(infoBean: ApplyInfoBean.InfoBean, channelType: Int, relationship: RelationshipBean) {
        if (channelType == CHANNEL_ROOM) {
            if (relationship.isInRoom) {
                ARouter.getInstance()
                        .build(AppRoute.CHAT)
                        .withBoolean("isGroupChat", true)
                        .withInt("channelType", CHANNEL_ROOM)
                        .withString("targetName", infoBean.name)
                        .withString("targetId", infoBean.id)
                        .navigation()
                ActivityUtils.finish(NewFriendActivity::class.java.name)
                LiveBus.of(BusEvent::class.java).changeTab().setValue(ChangeTabEvent(0, 0))
            } else {
                ARouter.getInstance().build(AppRoute.JOIN_ROOM)
                        .withString("markId", infoBean.markId)
                        .navigation()
            }
        } else if (channelType == CHANNEL_FRIEND) {
            if (relationship.isFriend) {
                ARouter.getInstance()
                        .build("/app/chatActivity")
                        .withBoolean("isGroupChat", false)
                        .withInt("channelType", CHANNEL_FRIEND)
                        .withString("targetName", infoBean.name)
                        .withString("targetId", infoBean.id)
                        .navigation()
                ActivityUtils.finish(NewFriendActivity::class.java.name)
                LiveBus.of(BusEvent::class.java).changeTab().setValue(ChangeTabEvent(0, 1))
            } else {
                ARouter.getInstance().build(AppRoute.USER_DETAIL)
                        .withString("userId", infoBean.id)
                        .navigation()
            }
        }
    }
}
