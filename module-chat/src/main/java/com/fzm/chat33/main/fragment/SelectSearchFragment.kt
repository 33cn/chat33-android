package com.fzm.chat33.main.fragment

import android.os.Bundle
import android.text.TextUtils
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
import com.fzm.chat33.core.bean.comparator.PinyinComparator
import com.fzm.chat33.core.db.bean.Contact
import com.fzm.chat33.core.global.Chat33Const
import com.fzm.chat33.main.activity.ContactSelectActivity
import com.fzm.chat33.main.activity.ContactSelectActivity.FORWARD_SELECT
import com.fzm.chat33.main.mvvm.ContactSelectViewModel
import com.fzm.chat33.widget.ChatAvatarView
import com.fzm.chat33.widget.HighlightTextView
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.fragment_select_search.*
import java.util.*
import javax.inject.Inject
import kotlin.collections.ArrayList
import kotlin.collections.HashMap
import kotlin.collections.set

class SelectSearchFragment : DILoadableFragment() {

    @Inject
    lateinit var provider: ViewModelProvider.Factory
    private lateinit var viewModel: ContactSelectViewModel
    private lateinit var pinyinComparator: PinyinComparator
    private lateinit var manager: LinearLayoutManager
    private lateinit var adapter: CommonAdapter<Contact>
    private val originDataList = ArrayList<Contact>()
    private val searchDataList = ArrayList<Contact>()
    private val checkState = HashMap<ChatTarget, Boolean>()
    private var mSearchKeyword: String? = null
    private var friendDisposable: Disposable? = null
    private var roomDisposable: Disposable? = null
    var onCheckChangeListener: ContactSelectActivity.OnCheckChangedListener? = null
    var selectType: Int = FORWARD_SELECT
    var selectable: Boolean = false

    override fun getLayoutId(): Int {
        return R.layout.fragment_select_search
    }

    override fun initView(view: View?, savedInstanceState: Bundle?) {
        viewModel = findViewModel(provider)
        pinyinComparator = PinyinComparator()
        manager = LinearLayoutManager(getActivity())
        sideBar.setTextView(dialog)
        swipeLayout.setEnableRefresh(false)
        swipeLayout.setEnableLoadMore(false)
        adapter = object : CommonAdapter<Contact>(getActivity(), R.layout.adapter_group_list, searchDataList) {
            override fun convert(holder: ViewHolder, data: Contact, position: Int) {
                if (position > 0) {
                    val last = searchDataList[position - 1].firstLetter.toUpperCase()[0]
                    val current = data.firstLetter.toUpperCase()[0]
                    if (last == current) {
                        holder.setVisible(R.id.tag, false)
                    } else {
                        holder.setVisible(R.id.tag, true)
                        holder.setText(R.id.tag, data.firstLetter)
                    }
                } else {
                    holder.setVisible(R.id.tag, true)
                    holder.setText(R.id.tag, data.firstLetter)
                }
                holder.setVisible(R.id.cb_select, selectable)
                // 防止复用时出现错乱
                holder.setTag(R.id.cb_select, position)
                holder.setChecked(R.id.cb_select, java.lang.Boolean.TRUE == checkState[ChatTarget(data.channelType(), data.id)])
                if (data.channelType() == Chat33Const.CHANNEL_ROOM) {
                    Glide.with(mContext).load(data.avatar)
                            .apply(RequestOptions().placeholder(R.mipmap.default_avatar_room))
                            .into(holder.getView<View>(R.id.iv_group_avatar) as ImageView)
                    (holder.getView<View>(R.id.iv_group_avatar) as ChatAvatarView).setIconRes(
                            if (!TextUtils.isEmpty(data.identificationInfo)) R.drawable.ic_group_identified else -1)
                } else if (data.channelType() == Chat33Const.CHANNEL_FRIEND) {
                    Glide.with(mContext).load(data.avatar)
                            .apply(RequestOptions().placeholder(R.mipmap.default_avatar_round))
                            .into(holder.getView<View>(R.id.iv_group_avatar) as ImageView)
                    (holder.getView<View>(R.id.iv_group_avatar) as ChatAvatarView).setIconRes(
                            if (!TextUtils.isEmpty(data.identificationInfo)) R.drawable.ic_user_identified else -1)
                }
                holder.setVisible(R.id.iv_disturb, data.isNoDisturb)
                //holder.setText(R.id.tv_group_title, roomListBean.getDisplayName());
                (holder.getView<View>(R.id.tv_group_title) as HighlightTextView).highlightSearchText(data.displayName, mSearchKeyword)

                val checkBox = holder.getView<CheckBox>(R.id.cb_select)
                checkBox.setOnCheckedChangeListener { buttonView, isChecked ->
                    if (buttonView.tag == position) {
                        val contact = searchDataList[position]
                        checkState[ChatTarget(contact.channelType(), contact.id)] = isChecked
                        if (onCheckChangeListener != null) {
                            onCheckChangeListener!!.onCheckChanged(buttonView, isChecked, contact)
                        }
                    }
                }
            }
        }

        rv_search.addItemDecoration(RecyclerViewDivider(getActivity(), LinearLayoutManager.VERTICAL,
                0.5f, ContextCompat.getColor(activity, R.color.chat_color_line)))
        rv_search.layoutManager = manager
        rv_search.adapter = adapter

        viewModel.updateFriend.observe(this, Observer {
            val list: MutableList<Contact> = ArrayList(it)
            viewModel.updateBlocked.value?.let { block -> list.addAll(block) }
            viewModel.updateRoom.value?.let { room -> list.addAll(room) }
            updateSearchList(list)
        })
        viewModel.updateBlocked.observe(this, Observer {
            val list: MutableList<Contact> = ArrayList(it)
            viewModel.updateFriend.value?.let { friend -> list.addAll(friend) }
            viewModel.updateRoom.value?.let { room -> list.addAll(room) }
            updateSearchList(list)
        })
        viewModel.updateRoom.observe(this, Observer {
            val list: MutableList<Contact> = ArrayList(it)
            viewModel.updateFriend.value?.let { friend -> list.addAll(friend) }
            viewModel.updateBlocked.value?.let { block -> list.addAll(block) }
            updateSearchList(list)
        })

        viewModel.searchContactList.observe(this, Observer {
            showSearchResult(it)
        })
    }

    private fun updateSearchList(list: List<Contact>) {
        originDataList.clear()
        originDataList.addAll(list)
        searchKeyword(mSearchKeyword)
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

    fun removeCheck(id: String, channelType: Int) {
        checkState.remove(ChatTarget(channelType, id))
        for (i in searchDataList.indices) {
            val contact = searchDataList[i]
            if (channelType == contact.channelType() && contact.id == id) {
                if (!rv_search.isComputingLayout) {
                    adapter.notifyItemChanged(i)
                }
                break
            }
        }
    }

    fun addCheck(id: String, channelType: Int) {
        checkState[ChatTarget(channelType, id)] = true
        for (i in searchDataList.indices) {
            val contact = searchDataList[i]
            if (channelType == contact.channelType() && contact.id == id) {
                if (!rv_search.isComputingLayout) {
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
        for (i in searchDataList.indices) {
            val sortStr = searchDataList[i].firstLetter
            val firstChar = sortStr.toUpperCase()[0]
            if (firstChar.toInt() == section) {
                return i
            }
        }
        return -1
    }

    fun searchKeyword(keyword: String?) {
        mSearchKeyword = keyword
        if (keyword.isNullOrEmpty()) {
            val matchList = ArrayList<Contact>()
            matchList.addAll(originDataList)
            showSearchResult(matchList)
            return
        }
        viewModel.searchContact(keyword)
    }

    private fun showSearchResult(matchList: List<Contact>) {
        searchDataList.clear()
        searchDataList.addAll(matchList)
        if (searchDataList.size != 0) {
            // 根据a-z进行排序源数据
            Collections.sort(searchDataList, pinyinComparator)
        }
        adapter.notifyDataSetChanged()
        if (searchDataList.size == 0) {
            rv_search.visibility = View.GONE
            search_empty.visibility = View.VISIBLE
        } else {
            rv_search.visibility = View.VISIBLE
            search_empty.visibility = View.GONE
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (roomDisposable != null && !roomDisposable!!.isDisposed) {
            roomDisposable!!.dispose()
        }
        if (friendDisposable != null && !friendDisposable!!.isDisposed) {
            friendDisposable!!.dispose()
        }
    }
}