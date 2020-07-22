package com.fzm.chat33.main.fragment

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.view.View
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
import com.fuzamei.componentservice.app.AppRoute
import com.fuzamei.componentservice.app.BusEvent
import com.fuzamei.componentservice.base.DILoadableFragment
import com.fuzamei.componentservice.ext.findViewModel
import com.fzm.chat33.R
import com.fzm.chat33.core.bean.comparator.PinyinComparator
import com.fzm.chat33.core.db.bean.FriendBean
import com.fzm.chat33.core.global.Chat33Const.FIND_TYPE_DEFAULT
import com.fzm.chat33.main.mvvm.BlackListViewModel
import kotlinx.android.synthetic.main.fragment_black_list.*
import java.util.*
import javax.inject.Inject

/**
 * @author zhengjy
 * @since 2019/10/10
 * Description:
 */
class BlackListFragment : DILoadableFragment() {

    @Inject
    lateinit var provider: ViewModelProvider.Factory
    private lateinit var viewModel: BlackListViewModel

    private val data = mutableListOf<FriendBean>()
    private lateinit var mAdapter: BlockAdapter
    private lateinit var manager: LinearLayoutManager

    override fun getLayoutId(): Int {
        return R.layout.fragment_black_list
    }

    override fun initView(view: View?, savedInstanceState: Bundle?) {
        viewModel = findViewModel(provider)
        manager = LinearLayoutManager(activity)
        rv_blocked.layoutManager = manager
        rv_blocked.addItemDecoration(RecyclerViewDivider(activity, LinearLayoutManager.VERTICAL,
                0.5f, ContextCompat.getColor(activity, R.color.chat_divide_light)))
        mAdapter = BlockAdapter(activity, R.layout.adapter_name_item, data)
        mAdapter.setOnItemClickListener(object : MultiItemTypeAdapter.OnItemClickListener {
            override fun onItemClick(view: View?, holder: RecyclerView.ViewHolder?, position: Int) {
                ARouter.getInstance().build(AppRoute.USER_DETAIL)
                        .withString("userId", data[position].id)
                        .withInt("sourceType", FIND_TYPE_DEFAULT)
                        .navigation()
            }

            override fun onItemLongClick(view: View?, holder: RecyclerView.ViewHolder?, position: Int): Boolean {
                return true
            }
        })
        rv_blocked.adapter = mAdapter
        sideBar.setTextView(dialog)
        //设置右侧SideBar触摸监听
        sideBar.setOnTouchingLetterChangedListener { s ->
            //该字母首次出现的位置
            val position = mAdapter.getPositionForSection(s[0].toInt())
            if (position != -1) {
                manager.scrollToPositionWithOffset(position, 0)
            }
        }

        viewModel.blackList.observe(this, Observer {
            if (it?.userList != null) {
                swipeLayout.finishRefresh(true)
                data.clear()
                if (it.userList.isEmpty()) {
                    statusLayout.showEmpty()
                } else {
                    data.addAll(it.userList)
                    Collections.sort(data, PinyinComparator())
                    statusLayout.showContent()
                }
                mAdapter.notifyDataSetChanged()
            } else {
                swipeLayout.finishRefresh(false)
                statusLayout.showError()
            }
        })
    }

    override fun initData() {
        viewModel.getBlockedUsers()
    }

    override fun setEvent() {
        swipeLayout.setEnableLoadMore(false)
        swipeLayout.setOnRefreshListener {
            viewModel.getBlockedUsers()
        }
        LiveBus.of(BusEvent::class.java).contactsRefresh().observe(this, Observer {
            if (it == 3) {
                viewModel.getBlockedUsers()
            }
        })
    }

    inner class BlockAdapter(
            context: Context,
            layout: Int,
            data: List<FriendBean>
    ) : CommonAdapter<FriendBean>(context, layout, data) {
        @SuppressLint("DefaultLocale")
        override fun convert(holder: ViewHolder?, blocked: FriendBean?, position: Int) {
            val bean = blocked!!
            if (position > 0) {
                val last = mDatas[position - 1].firstLetter.toUpperCase()[0]
                val current = bean.firstLetter.toUpperCase()[0]
                if (last == current) {
                    holder?.setVisible(R.id.tag, false)
                } else {
                    holder?.setVisible(R.id.tag, true)
                    holder?.setText(R.id.tag, bean.firstLetter)
                }
            } else {
                holder?.setVisible(R.id.tag, true)
                holder?.setText(R.id.tag, bean.firstLetter)
            }

            Glide.with(activity).load(bean.avatar)
                    .apply(RequestOptions().placeholder(R.mipmap.default_avatar_round))
                    .into(holder?.getView(R.id.iv_avatar)!!)
            holder?.setText(R.id.tv_name, bean.displayName)
            if (bean.isIdentified) {
                holder?.setVisible(R.id.tv_identification, true)
                holder?.setText(R.id.tv_identification, bean.identificationInfo)
            } else {
                holder?.setVisible(R.id.tv_identification, false)
            }
        }

        /**
         * 根据分类的首字母的Char ascii值获取其第一次出现该首字母的位置
         */
        @SuppressLint("DefaultLocale")
        fun getPositionForSection(section: Int): Int {
            for (i in 0 until itemCount) {
                val sortStr = mDatas[i].firstLetter
                val firstChar = sortStr.toUpperCase()[0]
                if (firstChar.toInt() == section) {
                    return i
                }
            }
            return -1
        }
    }
}