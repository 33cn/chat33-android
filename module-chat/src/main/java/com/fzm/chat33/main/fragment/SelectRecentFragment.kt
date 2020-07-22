package com.fzm.chat33.main.fragment

import android.os.Bundle
import android.view.View
import android.widget.CheckBox
import android.widget.ImageView
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.fuzamei.common.recycleviewbase.CommonAdapter
import com.fuzamei.common.recycleviewbase.MultiItemTypeAdapter
import com.fuzamei.common.recycleviewbase.RecyclerViewDivider
import com.fuzamei.common.recycleviewbase.ViewHolder
import com.fuzamei.componentservice.base.DILoadableFragment
import com.fuzamei.componentservice.ext.findViewModel
import com.fzm.chat33.R
import com.fzm.chat33.core.bean.ChatTarget
import com.fzm.chat33.core.db.bean.RecentContact
import com.fzm.chat33.core.global.Chat33Const
import com.fzm.chat33.main.activity.ContactSelectActivity
import com.fzm.chat33.main.activity.ContactSelectActivity.ADDRESS_SELECT
import com.fzm.chat33.main.activity.ContactSelectActivity.FORWARD_SELECT
import com.fzm.chat33.main.mvvm.ContactSelectViewModel
import com.fzm.chat33.widget.ChatAvatarView
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.fragment_book_recent.*
import java.util.Collections
import javax.inject.Inject
import kotlin.Comparator
import kotlin.collections.ArrayList
import kotlin.collections.HashMap
import kotlin.collections.set

class SelectRecentFragment : DILoadableFragment(){

    @Inject
    lateinit var provider: ViewModelProvider.Factory
    private lateinit var viewModel: ContactSelectViewModel
    private lateinit var adapter: CommonAdapter<RecentContact>
    private val originDataList = ArrayList<RecentContact>()
    private val checkState = HashMap<ChatTarget, Boolean>()
    var onCheckChangeListener: ContactSelectActivity.OnCheckChangedListener? = null
    var selectable: Boolean = false
    var selectType: Int = FORWARD_SELECT

    override fun getLayoutId(): Int {
        return R.layout.fragment_book_recent
    }

    override fun initView(view: View?, savedInstanceState: Bundle?) {
        viewModel = findViewModel(provider)
        swipeLayout.setEnableRefresh(false)
        swipeLayout.setEnableLoadMore(false)
        adapter = object : CommonAdapter<RecentContact>(getActivity(), R.layout.adapter_group_list, originDataList) {
            override fun convert(holder: ViewHolder, data: RecentContact, position: Int) {
                holder.setVisible(R.id.tag, false)
                holder.setVisible(R.id.cb_select, selectable)
                // 防止复用时出现错乱
                holder.setTag(R.id.cb_select, position)
                holder.setChecked(R.id.cb_select, java.lang.Boolean.TRUE == checkState[ChatTarget(data.channelType(), data.id)])
                if (data.channelType() == Chat33Const.CHANNEL_ROOM) {
                    Glide.with(mContext).load(data.avatar)
                            .apply(RequestOptions().placeholder(R.mipmap.default_avatar_room))
                            .into(holder.getView<View>(R.id.iv_group_avatar) as ImageView)
                    (holder.getView<View>(R.id.iv_group_avatar) as ChatAvatarView).setIconRes(if (data.isIdentified) R.drawable.ic_group_identified else -1)
                } else if (data.channelType() == Chat33Const.CHANNEL_FRIEND) {
                    Glide.with(mContext).load(data.avatar)
                            .apply(RequestOptions().placeholder(R.mipmap.default_avatar_round))
                            .into(holder.getView<View>(R.id.iv_group_avatar) as ImageView)
                    (holder.getView<View>(R.id.iv_group_avatar) as ChatAvatarView).setIconRes(if (data.isIdentified) R.drawable.ic_user_identified else -1)
                }
                holder.setText(R.id.tv_group_title, data.displayName)
                holder.setVisible(R.id.iv_disturb, data.noDisturb == 1)
                val checkBox = holder.getView<CheckBox>(R.id.cb_select)
                checkBox.setOnCheckedChangeListener { buttonView, isChecked ->
                    if (buttonView.tag == position) {
                        val contact = originDataList[position]
                        checkState[ChatTarget(data.channelType(), data.id)] = isChecked
                        if (onCheckChangeListener != null) {
                            onCheckChangeListener!!.onCheckChanged(buttonView, isChecked, contact)
                        }
                    }
                }
            }
        }
        rv_room.addItemDecoration(RecyclerViewDivider(getActivity(), LinearLayoutManager.VERTICAL,
                0.5f, ContextCompat.getColor(activity, R.color.chat_color_line)))
        rv_room.layoutManager = LinearLayoutManager(getActivity())
        rv_room.adapter = adapter

        viewModel.recentList.observe(this, Observer {
            showRecentList(it)
        })

        if (selectType == FORWARD_SELECT) {
            viewModel.getRecentFriendList()
            viewModel.getRecentRoomList()
        } else if (selectType == ADDRESS_SELECT) {
            viewModel.getRecentFriendList()
        }
    }

    override fun initData() {

    }

    override fun setEvent() {
        adapter.setOnItemClickListener(object : MultiItemTypeAdapter.OnItemClickListener {
            override fun onItemClick(view: View, holder: RecyclerView.ViewHolder, position: Int) {
                holder.itemView.findViewById<View>(R.id.cb_select).performClick()
            }

            override fun onItemLongClick(view: View, holder: RecyclerView.ViewHolder, position: Int): Boolean {
                return false
            }
        })
    }

    fun removeCheck(id: String, channelType: Int) {
        for (i in originDataList.indices) {
            val contact = originDataList[i]
            if (channelType == contact.channelType() && contact.id == id) {
                checkState.remove(ChatTarget(channelType, id))
                if (!rv_room.isComputingLayout) {
                    adapter.notifyItemChanged(i)
                }
                break
            }
        }
    }

    fun addCheck(id: String, channelType: Int) {
        for (i in originDataList.indices) {
            val contact = originDataList[i]
            if (channelType == contact.channelType() && contact.id == id) {
                checkState[ChatTarget(channelType, id)] = true
                if (!rv_room.isComputingLayout) {
                    adapter.notifyItemChanged(i)
                }
                break
            }
        }
    }

    private fun showRecentList(dataList: List<RecentContact>) {
        statusLayout.showContent()
        originDataList.clear()
        originDataList.addAll(dataList)
        Collections.sort(originDataList, mMessageComparator)
        adapter.notifyDataSetChanged()
    }

    private val mMessageComparator = Comparator<RecentContact> { o1, o2 ->
        // 根据置顶排序
        when {
            o1.stickyTop < o2.stickyTop -> -1
            o1.stickyTop > o2.stickyTop -> 1
            o1.datetime > o2.datetime -> -1
            o1.datetime < o2.datetime -> 1
            else -> 0
        }
    }
}