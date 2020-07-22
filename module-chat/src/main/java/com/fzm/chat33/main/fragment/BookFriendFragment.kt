package com.fzm.chat33.main.fragment

import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.alibaba.android.arouter.launcher.ARouter
import com.fuzamei.common.bus.LiveBus
import com.fuzamei.common.recycleviewbase.RecyclerViewDivider
import com.fuzamei.componentservice.app.AppRoute
import com.fuzamei.componentservice.app.BusEvent
import com.fuzamei.componentservice.base.DILoadableFragment
import com.fuzamei.componentservice.config.AppPreference
import com.fuzamei.componentservice.ext.findViewModel
import com.fzm.chat33.R
import com.fzm.chat33.core.bean.comparator.PinyinComparator
import com.fzm.chat33.core.db.bean.FriendBean
import com.fzm.chat33.core.global.Chat33Const
import com.fzm.chat33.core.global.UserInfo
import com.fzm.chat33.main.activity.ContactSelectActivity
import com.fzm.chat33.main.adapter.FriendsAdapter
import com.fzm.chat33.main.mvvm.BookFriendViewModel
import kotlinx.android.synthetic.main.layout_book_friend.*
import java.util.*
import javax.inject.Inject

class BookFriendFragment : DILoadableFragment(){

    @Inject
    lateinit var provider: ViewModelProvider.Factory
    private lateinit var viewModel: BookFriendViewModel
    private lateinit var adapter: FriendsAdapter
    private lateinit var manager: LinearLayoutManager
    /**
     * 根据拼音来排列RecyclerView里面的数据类
     */
    private lateinit var pinyinComparator: PinyinComparator
    private val originDataList = ArrayList<FriendBean>()
    private val searchDataList = ArrayList<FriendBean>()
    private var mSearchKeyword: String? = null
    var users: ArrayList<String>? = null
    var preCheckedUsers: ArrayList<String>? = null
    var selectable = false
    var checkChangedListener: ContactSelectActivity.OnCheckChangedListener? = null

    override fun getLayoutId(): Int {
        return R.layout.fragment_book_friend;
    }

    override fun initView(view: View?, savedInstanceState: Bundle?) {
        viewModel = findViewModel(provider)
        pinyinComparator = PinyinComparator()
        sideBar.setTextView(dialog)
        recyclerView.addItemDecoration(RecyclerViewDivider(getActivity(), LinearLayoutManager.VERTICAL,
                0.5f, ContextCompat.getColor(activity, R.color.chat_color_line)))
        manager = LinearLayoutManager(getActivity())
        recyclerView.layoutManager = manager
        adapter = FriendsAdapter(getActivity(), searchDataList, selectable, users)
        adapter.setOnCheckChangedListener(checkChangedListener)
        recyclerView.adapter = adapter
        swipeLayout.setEnableLoadMore(false)
        swipeLayout.setOnRefreshListener { getFriendsList(false) }
        viewModel.getFriendList.observe(this, Observer {
            if (it.isSucceed()) {
                AppPreference.LAST_FRIEND_LIST_REFRESH = System.currentTimeMillis()
                swipeLayout.finishRefresh()
            } else if(originDataList.size > 0) {
                swipeLayout.finishRefresh(false)
            } else {
                statusLayout.showError()
                swipeLayout.finishRefresh(false)
            }
        })
        viewModel.searchFriendList.observe(this, Observer {
            Collections.sort(it, pinyinComparator)
            showSearchResult(it, true)
        })
        LiveBus.of(BusEvent::class.java).contactsRefresh().observe(this, observer)
        statusLayout.showLoading()
        viewModel.updateFriend.observe(this, Observer { updateList(it) })
        getFriendsList(true)
    }

    override fun initData() {
    }

    override fun setEvent() {
        statusLayout.emptyView.findViewById<View>(R.id.tv_invite_friends).setOnClickListener {
            ARouter.getInstance().build(AppRoute.QR_CODE)
                    .withString("id", UserInfo.getInstance().id)
                    .withString("content", UserInfo.getInstance().uid)
                    .withString("avatar", UserInfo.getInstance().avatar)
                    .withString("name", UserInfo.getInstance().username)
                    .withInt("channelType", Chat33Const.CHANNEL_FRIEND)
                    .navigation()
        }
        //设置右侧SideBar触摸监听
        sideBar.setOnTouchingLetterChangedListener { s ->
            //该字母首次出现的位置
            val position = adapter.getPositionForSection(s[0].toInt())
            if (position != -1) {
                manager.scrollToPositionWithOffset(position, 0)
            }
        }
        //item点击事件
        adapter.setOnItemClickListener { view, position ->
            try {
                ARouter.getInstance().build(AppRoute.USER_DETAIL)
                        .withString("userId", searchDataList[position].id)
                        .navigation()
            } catch (e: NullPointerException) {
                searchDataList.removeAt(position)
                adapter.notifyItemRemoved(position)
            }
        }
    }

    private fun updateList(friendList: List<FriendBean>) {
        originDataList.clear()
        originDataList.addAll(friendList)
        searchKeyword(mSearchKeyword)
        if (preCheckedUsers != null && preCheckedUsers!!.size > 0) {
            // 预先选择好的好友
            for (i in originDataList.indices) {
                if (preCheckedUsers!!.contains(originDataList[i].id)) {
                    adapter.check(originDataList[i].id)
                }
            }
        }
    }

    private fun getFriendsList(allData: Boolean) {
        val lastRefresh = if (originDataList.size == 0) {
            0
        } else {
            AppPreference.LAST_FRIEND_LIST_REFRESH
        }
        val date = if (allData) null else if (lastRefresh == 0L) null else Date(lastRefresh)
        viewModel.getFriendList(3, date, -1)
    }

    fun searchKeyword(keyword: String?) {
        mSearchKeyword = keyword
        adapter.setSearchKeyword(keyword)

        if (keyword.isNullOrEmpty()) {
            showSearchResult(originDataList, false)
            return
        }
        viewModel.searchFriend(keyword)
    }

    private fun showSearchResult(matchList: List<FriendBean>, isSearch: Boolean) {
        searchDataList.clear()
        searchDataList.addAll(matchList)
        adapter.notifyDataSetChanged()
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

    fun toggleSideBar(visible: Boolean) {
        if (sideBar != null) {
            sideBar.visibility = if (visible) View.VISIBLE else View.INVISIBLE
        }
    }

    fun removeCheck(id: String) {
        adapter.removeCheck(id)
    }

    private val observer = Observer<Int> {
        if (it == 1) {
            // 如果昵称改变则全部刷新
            getFriendsList(true)
        }
    }
}