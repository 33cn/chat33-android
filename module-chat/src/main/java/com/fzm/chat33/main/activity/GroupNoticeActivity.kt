package com.fzm.chat33.main.activity

import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import android.view.View
import androidx.lifecycle.Observer

import com.alibaba.android.arouter.facade.annotation.Autowired
import com.alibaba.android.arouter.facade.annotation.Route
import com.alibaba.android.arouter.launcher.ARouter
import com.fuzamei.common.recycleviewbase.helper.ItemTouchListener
import com.fuzamei.common.utils.ShowUtils
import com.fuzamei.componentservice.app.AppRoute
import com.fuzamei.componentservice.base.DILoadableActivity
import com.fuzamei.componentservice.ext.findViewModel
import com.fuzamei.componentservice.widget.dialog.EasyDialog
import com.fzm.chat33.R
import com.fzm.chat33.core.bean.GroupNotice
import com.fzm.chat33.global.AppConst
import com.fzm.chat33.main.adapter.GroupNoticeListAdapter
import com.fzm.chat33.main.mvvm.GroupViewModel
import com.scwang.smartrefresh.layout.api.RefreshLayout
import com.scwang.smartrefresh.layout.listener.OnRefreshLoadMoreListener
import kotlinx.android.synthetic.main.activity_group_notice.*

import java.util.ArrayList

import javax.inject.Inject

/**
 * @author zhengjy
 * @since 2018/11/28
 * Description:群公告列表页面
 */
@Route(path = AppRoute.GROUP_NOTICE)
class GroupNoticeActivity : DILoadableActivity(), View.OnClickListener {

    @JvmField
    @Autowired
    var roomId: String? = null
    @JvmField
    @Autowired
    var memberLevel: Int = 0
    private var nextLog: String? = null

    @Inject
    lateinit var provider: ViewModelProvider.Factory
    lateinit var viewModel: GroupViewModel

    private var adapter: GroupNoticeListAdapter? = null
    private val groupNotices = ArrayList<GroupNotice>()

    override fun enableSlideBack(): Boolean {
        return true
    }

    override fun getLayoutId(): Int {
        return R.layout.activity_group_notice
    }

    override fun initView() {
        ARouter.getInstance().inject(this)
        viewModel = findViewModel(provider)
        iv_add!!.visibility = if (memberLevel == 1) View.GONE else View.VISIBLE

        viewModel.noticeList.observe(this, Observer { wrapper ->
            if (wrapper != null) {
                if (nextLog == null) {
                    groupNotices.clear()
                }
                nextLog = wrapper.nextLog
                swipeLayout!!.finishRefresh()
                if ("-1" == wrapper.nextLog) {
                    swipeLayout!!.finishLoadMoreWithNoMoreData()
                } else {
                    swipeLayout!!.finishLoadMore()
                }
                if (wrapper.list != null && wrapper.list.size > 0) {
                    groupNotices.addAll(wrapper.list)
                }
                adapter!!.notifyDataSetChanged()
            } else {
                swipeLayout!!.finishRefresh()
                swipeLayout!!.finishLoadMore()
            }
        })
        viewModel.deleteNotice.observe(this, Observer { position ->
            if (position != null && position < groupNotices.size) {
                groupNotices.removeAt(position)
                adapter!!.notifyItemRemoved(position)
                ShowUtils.showToastNormal(instance, getString(R.string.chat_tips_group_info19))
            }
        })
        viewModel.loading.observe(this, Observer { setupLoading(it) })
    }

    override fun initData() {
        swipeLayout!!.setOnRefreshLoadMoreListener(object : OnRefreshLoadMoreListener {
            override fun onRefresh(refreshLayout: RefreshLayout) {
                nextLog = null
                getGroupNoticeList()
            }

            override fun onLoadMore(refreshLayout: RefreshLayout) {
                getGroupNoticeList()
            }
        })
        rv_notice!!.layoutManager = LinearLayoutManager(this)
        adapter = GroupNoticeListAdapter(this, R.layout.item_group_notice, groupNotices)
        adapter!!.setCanSwipe(memberLevel > 1)
        adapter!!.setItemTouchListener(object : ItemTouchListener {
            override fun onItemClick(view: View, position: Int) {

            }

            override fun onRightMenuClick(view: View, position: Int) {
                val dialog = EasyDialog.Builder()
                        .setHeaderTitle(getString(R.string.chat_tips_tips))
                        .setContent(getString(R.string.chat_dialog_delete_group_notice))
                        .setBottomLeftText(getString(R.string.chat_action_cancel))
                        .setBottomRightText(getString(R.string.chat_action_confirm))
                        .setBottomRightClickListener { dialog ->
                            dialog.dismiss()
                            viewModel.revokeMessage(groupNotices[position].logId, 1, position)
                        }.create(instance)
                dialog.show()
            }
        })
        rv_notice!!.adapter = adapter
    }

    private fun getGroupNoticeList() {
        viewModel.getGroupNoticeList(roomId!!, nextLog, AppConst.PAGE_SIZE)
    }

    override fun setEvent() {
        iv_back!!.setOnClickListener(this)
        iv_add!!.setOnClickListener(this)
    }

    override fun onResume() {
        super.onResume()
        nextLog = null
        getGroupNoticeList()
    }

    override fun onClick(v: View) {
        val i = v.id
        if (i == R.id.iv_back) {
            finish()
        } else if (i == R.id.iv_add) {
            ARouter.getInstance().build(AppRoute.EDIT_GROUP_INFO)
                    .withString("roomId", roomId)
                    .withInt("type", 1)
                    .navigation()
        }
    }
}
