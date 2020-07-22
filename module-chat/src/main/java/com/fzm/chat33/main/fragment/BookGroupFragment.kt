package com.fzm.chat33.main.fragment

import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.ImageView
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.alibaba.android.arouter.launcher.ARouter
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.fuzamei.common.bus.LiveBus
import com.fuzamei.common.recycleviewbase.CommonAdapter
import com.fuzamei.common.recycleviewbase.MultiItemTypeAdapter
import com.fuzamei.common.recycleviewbase.RecyclerViewDivider
import com.fuzamei.common.recycleviewbase.ViewHolder
import com.fuzamei.common.utils.DateUtils
import com.fuzamei.componentservice.app.AppRoute
import com.fuzamei.componentservice.app.BusEvent
import com.fuzamei.componentservice.base.DILoadableFragment
import com.fuzamei.componentservice.config.AppConfig
import com.fuzamei.componentservice.event.ChangeTabEvent
import com.fuzamei.componentservice.ext.findViewModel
import com.fzm.chat33.R
import com.fzm.chat33.core.bean.comparator.PinyinComparator
import com.fzm.chat33.core.db.bean.RoomListBean
import com.fzm.chat33.core.global.Chat33Const
import com.fzm.chat33.global.AppConst
import com.fzm.chat33.main.mvvm.BookGroupViewModel
import com.fzm.chat33.widget.ChatAvatarView
import com.fuzamei.componentservice.widget.dialog.EasyDialog
import kotlinx.android.synthetic.main.fragment_book_group.*
import kotlinx.android.synthetic.main.fragment_book_group.dialog
import kotlinx.android.synthetic.main.fragment_book_group.sideBar
import kotlinx.android.synthetic.main.fragment_book_group.statusLayout
import kotlinx.android.synthetic.main.fragment_book_group.swipeLayout
import java.util.*
import javax.inject.Inject

class BookGroupFragment : DILoadableFragment(){

    @Inject
    lateinit var provider: ViewModelProvider.Factory
    private lateinit var viewModel: BookGroupViewModel
    private lateinit var manager: LinearLayoutManager
    private lateinit var adapter: CommonAdapter<RoomListBean>
    private lateinit var pinyinComparator: PinyinComparator
    private val data = ArrayList<RoomListBean>()

    override fun getLayoutId(): Int {
        return R.layout.fragment_book_group
    }

    override fun initView(view: View?, savedInstanceState: Bundle?) {
        viewModel = findViewModel(provider)
        pinyinComparator = PinyinComparator()
        statusLayout.emptyView.findViewById<View>(R.id.ll_recommended_group).visibility = if (AppConfig.APP_RECOMMENDED_GROUP) View.VISIBLE else View.GONE
        manager = LinearLayoutManager(getActivity())
        sideBar.setTextView(dialog)

        swipeLayout.setEnableLoadMore(false)
        swipeLayout.setOnRefreshListener { getRoomList() }
        adapter = object : CommonAdapter<RoomListBean>(getActivity(), R.layout.adapter_group_list, data) {
            override fun convert(holder: ViewHolder, roomListBean: RoomListBean, position: Int) {
                if (position > 0) {
                    val last = data[position - 1].firstLetter.toUpperCase()[0]
                    val current = data[position].firstLetter.toUpperCase()[0]
                    if (last == current) {
                        holder.setVisible(R.id.tag, false)
                    } else {
                        holder.setVisible(R.id.tag, true)
                        holder.setText(R.id.tag, data[position].firstLetter)
                    }
                } else {
                    holder.setVisible(R.id.tag, true)
                    holder.setText(R.id.tag, data[position].firstLetter)
                }
                if (TextUtils.isEmpty(roomListBean.avatar)) {
                    holder.setImageResource(R.id.iv_group_avatar, R.mipmap.default_avatar_room)
                } else {
                    Glide.with(mContext).load(roomListBean.avatar)
                            .apply(RequestOptions().placeholder(R.mipmap.default_avatar_room))
                            .into(holder.getView<View>(R.id.iv_group_avatar) as ImageView)
                }
                (holder.getView<View>(R.id.iv_group_avatar) as ChatAvatarView).setIconRes(if (roomListBean.isIdentified) R.drawable.ic_group_identified else -1)
                holder.setText(R.id.tv_group_title, roomListBean.name)
                holder.setVisible(R.id.iv_disturb, roomListBean.noDisturbing == 1)
            }
        }
        rv_room.addItemDecoration(RecyclerViewDivider(getActivity(), LinearLayoutManager.VERTICAL,
                0.5f, ContextCompat.getColor(activity, R.color.chat_color_line)))
        rv_room.layoutManager = manager
        rv_room.adapter = adapter

        viewModel.getRoomList.observe(this, Observer {
            if (it.isSucceed()) {
                swipeLayout.finishRefresh()
            } else if(data.size > 0) {
                swipeLayout.finishRefresh(false)
            } else {
                statusLayout.showError()
                swipeLayout.finishRefresh(false)
            }
        })
        LiveBus.of(BusEvent::class.java).contactsRefresh().observe(this, observer)
        statusLayout.showLoading()
        viewModel.updateRoom.observe(this, Observer { updateList(it) })
        getRoomList()
    }

    override fun initData() {
    }

    override fun setEvent() {
        statusLayout.emptyView.findViewById<View>(R.id.tv_create_group).setOnClickListener {
            ARouter.getInstance().build(AppRoute.CREATE_GROUP).navigation()
        }
        statusLayout.emptyView.findViewById<View>(R.id.ll_recommended_group).setOnClickListener {
            ARouter.getInstance().build(AppRoute.RECOMMEND_GROUPS).navigation()
        }
        //设置右侧SideBar触摸监听
        sideBar.setOnTouchingLetterChangedListener { s ->
            //该字母首次出现的位置
            val position = getPositionForSection(s[0].toInt())
            if (position != -1) {
                manager.scrollToPositionWithOffset(position, 0)
            }
        }
        adapter.setOnItemClickListener(object : MultiItemTypeAdapter.OnItemClickListener {
            override fun onItemClick(view: View, holder: RecyclerView.ViewHolder, position: Int) {
                try {
                    if (data[position].disableDeadline == 0L) {
                        ARouter.getInstance()
                                .build(AppRoute.CHAT)
                                .withBoolean("isGroupChat", true)
                                .withInt("channelType", Chat33Const.CHANNEL_ROOM)
                                .withString("targetName", data[position].name)
                                .withString("targetId", data[position].id)
                                .navigation()
                        LiveBus.of(BusEvent::class.java).changeTab().setValue(ChangeTabEvent(0, 0))
                    } else {
                        // 群被封禁
                        val tips = if (AppConst.TIME_FOREVER == data[position].disableDeadline) {
                            resources.getString(R.string.room_disable_forever_tips)
                        } else {
                            val deadline = DateUtils.timeToString(data[position].disableDeadline, resources.getString(R.string.chat_date_pattern))
                            resources.getString(R.string.room_disable_tips, deadline)
                        }
                        EasyDialog.Builder()
                                .setHeaderTitle(resources.getString(R.string.chat_tips_tips))
                                .setBottomRightText(resources.getString(R.string.chat_action_confirm))
                                .setContent(tips)
                                .setBottomRightClickListener { dialog -> dialog.dismiss() }.setCancelable(false).create(getActivity()).show()
                    }
                } catch (e: NullPointerException) {
                    data.removeAt(position)
                    adapter.notifyItemRemoved(position)
                }
            }

            override fun onItemLongClick(view: View, holder: RecyclerView.ViewHolder, position: Int): Boolean {
                return false
            }
        })
    }

    private fun updateList(roomList: List<RoomListBean>) {
        data.clear()
        data.addAll(roomList)
        if (data.size == 0) {
            rv_room.visibility = View.GONE
            statusLayout.showEmpty()
        } else {
            rv_room.visibility = View.VISIBLE
            statusLayout.showContent()
            // 根据a-z进行排序源数据
            Collections.sort(data, pinyinComparator)
        }
        adapter.notifyDataSetChanged()
    }

    /**
     * 根据分类的首字母的Char ascii值获取其第一次出现该首字母的位置
     */
    fun getPositionForSection(section: Int): Int {
        for (i in data.indices) {
            val sortStr = data[i].firstLetter
            val firstChar = sortStr.toUpperCase()[0]
            if (firstChar.toInt() == section) {
                return i
            }
        }
        return -1
    }

    private fun getRoomList() {
        viewModel.getRoomList(3)
    }

    private val observer = Observer<Int> {
        if (it == 2) {
            getRoomList()
        }
    }
}