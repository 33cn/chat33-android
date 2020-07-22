package com.fzm.chat33.main.fragment

import android.annotation.SuppressLint
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.alibaba.android.arouter.launcher.ARouter
import com.fuzamei.common.recycleviewbase.RecyclerViewDivider
import com.fuzamei.common.utils.ShowUtils
import com.fuzamei.componentservice.app.AppRoute
import com.fuzamei.componentservice.base.DILoadableFragment
import com.fuzamei.componentservice.config.AppConfig
import com.fuzamei.componentservice.ext.findViewModel
import com.fzm.chat33.R
import com.fzm.chat33.core.db.bean.RoomContact
import com.fzm.chat33.core.db.bean.RoomInfoBean
import com.fzm.chat33.core.db.bean.RoomUserBean
import com.fzm.chat33.core.global.Chat33Const.LEVEL_OWNER
import com.fzm.chat33.core.global.Chat33Const.LEVEL_USER
import com.fzm.chat33.main.adapter.GroupMemberAdapter
import com.fzm.chat33.main.mvvm.GroupMemberViewModel
import kotlinx.android.synthetic.main.layout_book_friend.*
import java.util.*
import javax.inject.Inject

/**
 * @author zhengjy
 * @since 2019/10/22
 * Description:
 */
class GroupMemberListFragment : DILoadableFragment() {

    @Inject
    lateinit var provider: ViewModelProvider.Factory
    private lateinit var viewModel: GroupMemberViewModel

    private var adapter: GroupMemberAdapter? = null
    private lateinit var manager: LinearLayoutManager
    private val originDataList = ArrayList<RoomContact>()
    private val searchDataList = ArrayList<RoomContact>()

    private var roomInfo: RoomInfoBean? = null
    /**
     * 列表操作的类型
     */
    private var action: String? = null
    /**
     * 默认显示不超过memberLevel的群成员
     */
    private var userLevel: Int = LEVEL_OWNER
    private var selectable = false

    private val roomId: String
        get() = roomInfo?.id ?: ""

    private val canAddFriend
        get() = roomInfo?.canAddFriend == 1
    /**
     * 用户自身的memberLevel
     */
    private val memberLevel
        get() = roomInfo?.memberLevel ?: LEVEL_USER

    private var onCheckChangedListener: GroupMemberAdapter.OnCheckChangedListener? = null
    private var initLoad = true
    private var mSearchKeyword: String = ""

    companion object {
        @JvmStatic
        fun create(roomInfo: RoomInfoBean, userLevel: Int = LEVEL_OWNER, action: String = "",
                   selectable: Boolean = false): GroupMemberListFragment {
            return GroupMemberListFragment().apply {
                arguments = Bundle().apply {
                    putSerializable("roomInfo", roomInfo)
                    putInt("userLevel", userLevel)
                    putString("action", action)
                    putBoolean("selectable", selectable)
                }
            }
        }
    }

    override fun getLayoutId(): Int {
        return R.layout.fragment_group_member_list
    }

    @SuppressLint("CheckResult")
    override fun initView(view: View, savedInstanceState: Bundle?) {
        roomInfo = arguments?.getSerializable("roomInfo") as RoomInfoBean
        userLevel = arguments?.getInt("userLevel") ?: LEVEL_OWNER
        action = arguments?.getString("action")
        selectable = arguments?.getBoolean("selectable") ?: false

        viewModel = findViewModel(provider)
        viewModel.loading.observe(this, Observer { setupLoading(it) })
        viewModel.roomUsers.observe(this, Observer { wrapper ->
            if (wrapper != null) {
                initLoad = false
                swipeLayout.finishRefresh()
                updateListener?.onRoomUsersUpdate(wrapper.userList)
                viewModel.getRoomContactsByLevel(roomId, userLevel)
            } else {
                initLoad = false
                swipeLayout.finishRefresh(false)
            }
        })
        viewModel.roomContacts.observe(this, Observer {
            originDataList.clear()
            originDataList.addAll(it)
            adapter?.setCheckState()
            searchKeyword(mSearchKeyword)
        })
        viewModel.searchRoomContacts.observe(this, Observer {
            showSearchResult(it, true)
        })
    }

    override fun initData() {
        sideBar.setTextView(dialog)
        //设置右侧SideBar触摸监听
        sideBar.setOnTouchingLetterChangedListener { s ->
            //该字母首次出现的位置
            val position = adapter?.getPositionForSection(s[0].toInt(), 1)
            if (position != -1) {
                manager.scrollToPositionWithOffset(position!!, 0)
            }
        }
        recyclerView.addItemDecoration(RecyclerViewDivider(activity, LinearLayoutManager.VERTICAL,
                0.5f, ContextCompat.getColor(activity, R.color.chat_color_line)))
        manager = LinearLayoutManager(activity)
        recyclerView.layoutManager = manager
        adapter = GroupMemberAdapter(activity, roomId, viewModel, searchDataList, selectable, action)
        if (onCheckChangedListener != null) {
            adapter?.setOnCheckChangedListener(onCheckChangedListener)
        }
        recyclerView.adapter = adapter
        //item点击事件
        adapter?.setOnItemClickListener { _, position ->
            val bean = searchDataList[position]
            if (listener != null) {
                listener?.onItemClick(bean)
            } else {
                if (bean.id == AppConfig.MY_ID) {
                    ShowUtils.showToastNormal(activity, getString(R.string.chat_tips_group_info1))
                } else {
                    ARouter.getInstance().build(AppRoute.USER_DETAIL)
                            .withString("userId", bean.id)
                            .withString("roomId", roomId)
                            .withInt("memberLevel", memberLevel)
                            .withBoolean("canAddFriend", canAddFriend)
                            .navigation()
                }
            }
        }
        swipeLayout.setEnableLoadMore(false)
        swipeLayout.setOnRefreshListener { getGroupMemberList() }
        getGroupMemberList()
    }

    override fun setEvent() {

    }

    fun getGroupMemberList() {
        viewModel.getRoomUsers(initLoad, roomId)
    }

    @SuppressLint("CheckResult")
    fun searchKeyword(keyword: String) {
        mSearchKeyword = keyword
        adapter?.setSearchKeyword(keyword)
        if (TextUtils.isEmpty(keyword)) {
            showSearchResult(originDataList, false)
            return
        }
        viewModel.searchRoomContactsByLevel(roomId, userLevel, keyword)
    }

    private fun showSearchResult(matchList: List<RoomContact>, isSearch: Boolean) {
        searchDataList.clear()
        searchDataList.addAll(matchList)
        adapter?.notifyDataSetChanged()
        if (isSearch && searchDataList.size == 0) {
            recyclerView.visibility = View.GONE
            statusLayout.showOther()
        } else if (originDataList.size == 0) {
            recyclerView.visibility = View.GONE
            statusLayout.showEmpty()
        } else {
            recyclerView.visibility = View.VISIBLE
            statusLayout.showContent()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        adapter?.onDestroy()
    }

    private var listener: OnItemClickListener? = null

    interface OnItemClickListener {
        fun onItemClick(bean: RoomContact)
    }

    fun setOnItemClickListener(listener: OnItemClickListener) {
        this.listener = listener
    }

    private var updateListener: OnRoomUsersUpdateListener? = null

    interface OnRoomUsersUpdateListener {
        fun onRoomUsersUpdate(roomUsers: List<RoomUserBean>)
    }

    fun setOnRoomUsersUpdateListener(listener: OnRoomUsersUpdateListener) {
        updateListener = listener
    }

    fun setOnCheckChangedListener(onCheckChangedListener: GroupMemberAdapter.OnCheckChangedListener) {
        this.onCheckChangedListener = onCheckChangedListener
    }

    fun checkAll(checkAll: Boolean) {
        for (roomUserBean in originDataList) {
            onCheckChangedListener?.onCheckChanged(null, checkAll, roomUserBean)
        }
        adapter?.checkAll(checkAll)
    }
}