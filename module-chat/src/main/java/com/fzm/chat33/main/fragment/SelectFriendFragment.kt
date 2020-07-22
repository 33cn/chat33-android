package com.fzm.chat33.main.fragment

import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.fuzamei.common.recycleviewbase.RecyclerViewDivider
import com.fuzamei.componentservice.base.DILoadableFragment
import com.fuzamei.componentservice.ext.findViewModel
import com.fzm.chat33.R
import com.fzm.chat33.core.bean.comparator.PinyinComparator
import com.fzm.chat33.core.db.bean.FriendBean
import com.fzm.chat33.main.activity.ContactSelectActivity
import com.fzm.chat33.main.adapter.FriendsAdapter
import com.fzm.chat33.main.mvvm.ContactSelectViewModel
import kotlinx.android.synthetic.main.layout_book_friend.*
import java.util.Collections
import javax.inject.Inject

class SelectFriendFragment : DILoadableFragment(){

    @Inject
    lateinit var provider: ViewModelProvider.Factory
    private lateinit var viewModel: ContactSelectViewModel
    private lateinit var pinyinComparator: PinyinComparator
    private lateinit var adapter: FriendsAdapter
    private lateinit var manager: LinearLayoutManager
    private val originDataList = ArrayList<FriendBean>()
    var onCheckChangeListener: ContactSelectActivity.OnCheckChangedListener? = null
    var selectable: Boolean = false

    override fun getLayoutId(): Int {
        return R.layout.fragment_book_friend
    }

    override fun initView(view: View?, savedInstanceState: Bundle?) {
        viewModel = findViewModel(provider)
        pinyinComparator = PinyinComparator()
        sideBar.setTextView(dialog)
        swipeLayout.setEnableRefresh(false)
        swipeLayout.setEnableLoadMore(false)
        recyclerView.addItemDecoration(RecyclerViewDivider(getActivity(), LinearLayoutManager.VERTICAL,
                0.5f, ContextCompat.getColor(activity, R.color.chat_color_line)))
        manager = LinearLayoutManager(getActivity())
        recyclerView.layoutManager = manager
        adapter = FriendsAdapter(getActivity(), originDataList, selectable, null)
        adapter.setOnCheckChangedListener(onCheckChangeListener)
        recyclerView.adapter = adapter

        statusLayout.showLoading()
        viewModel.updateFriend.observe(this, Observer { updateFriendsList(it) })
    }

    override fun initData() {
    }

    override fun setEvent() {
        //设置右侧SideBar触摸监听
        sideBar.setOnTouchingLetterChangedListener { s ->
            //该字母首次出现的位置
            val position = adapter.getPositionForSection(s[0].toInt())
            if (position != -1) {
                manager.scrollToPositionWithOffset(position, 0)
            }
        }
    }

    fun removeCheck(id: String) {
        if (!recyclerView.isComputingLayout) {
            adapter.removeCheck(id)
        }
    }

    fun checkFriend(id: String) {
        if (!recyclerView.isComputingLayout) {
            adapter.check(id)
        }
    }

    fun toggleSideBar(visible: Boolean) {
        sideBar.visibility = if (visible) View.VISIBLE else View.INVISIBLE
    }

    private fun updateFriendsList(friendBeans: List<FriendBean> ) {
        originDataList.clear()
        statusLayout.showContent()
        originDataList.addAll(friendBeans)
        if (originDataList.size != 0) {
            // 根据a-z进行排序源数据
            Collections.sort(originDataList, pinyinComparator)
        }
        adapter.notifyDataSetChanged()
    }
}