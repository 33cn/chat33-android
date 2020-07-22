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
import com.fzm.chat33.core.bean.comparator.PinyinComparator
import com.fzm.chat33.core.db.bean.RoomListBean
import com.fzm.chat33.core.global.Chat33Const
import com.fzm.chat33.main.activity.ContactSelectActivity
import com.fzm.chat33.main.mvvm.ContactSelectViewModel
import com.fzm.chat33.widget.ChatAvatarView
import kotlinx.android.synthetic.main.fragment_book_group.*
import java.util.*
import javax.inject.Inject
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class SelectGroupFragment : DILoadableFragment(){

    @Inject
    lateinit var provider: ViewModelProvider.Factory
    private lateinit var viewModel: ContactSelectViewModel
    private lateinit var pinyinComparator: PinyinComparator
    private lateinit var manager: LinearLayoutManager
    private lateinit var adapter: CommonAdapter<RoomListBean>
    private val originDataList = ArrayList<RoomListBean>()
    private val checkState = HashMap<String, Boolean>()
    var onCheckChangeListener: ContactSelectActivity.OnCheckChangedListener? = null
    var selectable: Boolean = false

    override fun getLayoutId(): Int {
        return R.layout.fragment_book_group
    }

    override fun initView(view: View?, savedInstanceState: Bundle?) {
        viewModel = findViewModel(provider)
        pinyinComparator = PinyinComparator()
        manager = LinearLayoutManager(getActivity())
        sideBar.setTextView(dialog)
        swipeLayout.setEnableRefresh(false)
        swipeLayout.setEnableLoadMore(false)
        adapter = object : CommonAdapter<RoomListBean>(getActivity(), R.layout.adapter_group_list, originDataList) {
            override fun convert(holder: ViewHolder, roomListBean: RoomListBean, position: Int) {
                if (position > 0) {
                    val last = originDataList[position - 1].firstLetter.toUpperCase()[0]
                    val current = roomListBean.firstLetter.toUpperCase()[0]
                    if (last == current) {
                        holder.setVisible(R.id.tag, false)
                    } else {
                        holder.setVisible(R.id.tag, true)
                        holder.setText(R.id.tag, roomListBean.firstLetter)
                    }
                } else {
                    holder.setVisible(R.id.tag, true)
                    holder.setText(R.id.tag, roomListBean.firstLetter)
                }
                holder.setVisible(R.id.cb_select, selectable)
                // 防止复用时出现错乱
                holder.setTag(R.id.cb_select, position)
                holder.setChecked(R.id.cb_select, java.lang.Boolean.TRUE == checkState[roomListBean.id])
                Glide.with(mContext).load(roomListBean.avatar)
                        .apply(RequestOptions().placeholder(R.mipmap.default_avatar_room))
                        .into(holder.getView<View>(R.id.iv_group_avatar) as ImageView)
                (holder.getView<View>(R.id.iv_group_avatar) as ChatAvatarView).setIconRes(if (roomListBean.isIdentified) R.drawable.ic_group_identified else -1)
                holder.setVisible(R.id.iv_disturb, roomListBean.noDisturbing == 1)
                holder.setText(R.id.tv_group_title, roomListBean.name)
                val checkBox = holder.getView<CheckBox>(R.id.cb_select)
                checkBox.setOnCheckedChangeListener { buttonView, isChecked ->
                    if (buttonView.tag == position) {
                        val data = originDataList[position]
                        //checkState.put(data, isChecked);
                        if (onCheckChangeListener != null) {
                            onCheckChangeListener!!.onCheckChanged(buttonView, isChecked, data)
                        }
                    }
                }
            }
        }
        rv_room.addItemDecoration(RecyclerViewDivider(getActivity(), LinearLayoutManager.VERTICAL,
                0.5f, ContextCompat.getColor(activity, R.color.chat_color_line)))
        rv_room.layoutManager = manager
        rv_room.adapter = adapter

        statusLayout.showLoading()
        viewModel.updateRoom.observe(this, Observer { updateRoomList(it) })
    }

    override fun initData() {
    }

    override fun setEvent() {
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
                holder.itemView.findViewById<View>(R.id.cb_select).performClick()
            }

            override fun onItemLongClick(view: View, holder: RecyclerView.ViewHolder, position: Int): Boolean {
                return false
            }
        })
    }

    fun removeCheck(id: String) {
        for (i in originDataList.indices) {
            val bean = originDataList[i]
            if (bean.id == id) {
                checkState[id] = false
                if (!rv_room.isComputingLayout) {
                    adapter.notifyItemChanged(i)
                }
                break
            }
        }
    }

    fun checkGroup(id: String) {
        for (i in originDataList.indices) {
            val bean = originDataList[i]
            if (bean.id == id && originDataList[i].channelType() == Chat33Const.CHANNEL_ROOM) {
                checkState[id] = true
                if (!rv_room.isComputingLayout) {
                    adapter.notifyItemChanged(i)
                }
                break
            }
        }
    }

    /**
     * 根据分类的首字母的Char ascii值获取其第一次出现该首字母的位置
     */
    fun getPositionForSection(section: Int): Int {
        for (i in originDataList.indices) {
            val sortStr = originDataList[i].firstLetter
            val firstChar = sortStr.toUpperCase()[0]
            if (firstChar.toInt() == section) {
                return i
            }
        }
        return -1
    }

    private fun updateRoomList(roomBeans: List<RoomListBean>) {
        originDataList.clear()
        statusLayout.showContent()
        originDataList.addAll(roomBeans)
        if (originDataList.size != 0) {
            // 根据a-z进行排序源数据
            Collections.sort(originDataList, pinyinComparator)
        }
        adapter.notifyDataSetChanged()
    }
}