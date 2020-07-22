package com.fzm.chat33.main.activity

import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.alibaba.android.arouter.facade.annotation.Autowired
import com.alibaba.android.arouter.facade.annotation.Route
import com.alibaba.android.arouter.launcher.ARouter
import com.fuzamei.common.recycleviewbase.RecyclerViewDivider
import com.fuzamei.componentservice.app.AppRoute
import com.fuzamei.componentservice.base.DILoadableActivity
import com.fuzamei.componentservice.ext.findViewModel
import com.fzm.chat33.R
import com.fzm.chat33.main.adapter.ChatPraiseAdapter
import com.fzm.chat33.main.mvvm.ChatPraiseViewModel
import com.scwang.smartrefresh.layout.api.RefreshLayout
import com.scwang.smartrefresh.layout.listener.OnRefreshLoadMoreListener
import kotlinx.android.synthetic.main.activity_chat_praise.*
import javax.inject.Inject

/**
 * 创建日期：2018/11/19
 * 描述:群聊赞赏列表
 * 作者:yll
 */
@Route(path = AppRoute.CHAT_PRAISE)
class ChatPraiseActivity : DILoadableActivity() {
    @JvmField
    @Autowired
    var channelType: Int = 0
    @JvmField
    @Autowired
    var targetId: String? = null
    @JvmField
    @Autowired
    var targetName: String? = null
    @Inject
    lateinit var provider: ViewModelProvider.Factory
    lateinit var viewModel: ChatPraiseViewModel
    lateinit var adapter: ChatPraiseAdapter

    private var startId: String? = null

    override fun getLayoutId(): Int {
        return R.layout.activity_chat_praise
    }

    override fun initView() {
        ARouter.getInstance().inject(this)
        viewModel = findViewModel(provider)
        ctb_title.setMiddleText(targetName)
        ctb_title.setLeftListener { finish() }
        recycler.layoutManager = LinearLayoutManager(instance)
        recycler.addItemDecoration(RecyclerViewDivider(instance, LinearLayoutManager.VERTICAL,
                0.5f, ContextCompat.getColor(instance, R.color.chat_divide_light)))
        adapter = ChatPraiseAdapter(instance, targetId, false)
        recycler.adapter = adapter
        viewModel.praiseList.observe(this, Observer {
            if (it == null) {
                swipeLayout.finishRefresh(false)
                statusLayout.showError()
            } else {
                if(startId.isNullOrEmpty()) {
                    viewModel.clearPraise(channelType, targetId!!)
                    swipeLayout.finishRefresh(true)
                    adapter.resetList(it.records)
                    if(it.records.isNullOrEmpty()) {
                        statusLayout.showEmpty()
                    } else {
                        statusLayout.showContent()
                    }
                } else if ("-1" == it.nextLog) {
                    swipeLayout.finishLoadMoreWithNoMoreData()
                    adapter.addList(it.records)
                    statusLayout.showContent()
                } else {
                    swipeLayout.finishLoadMore()
                    adapter.addList(it.records)
                    statusLayout.showContent()
                }
                startId = it.nextLog
            }

        })
        viewModel.getChatPraises(channelType, targetId!!, startId)
    }

    override fun initData() {
    }

    override fun enableSlideBack(): Boolean {
        return true
    }

    override fun setEvent() {
        swipeLayout.setOnRefreshLoadMoreListener(object : OnRefreshLoadMoreListener {
            override fun onLoadMore(refreshLayout: RefreshLayout) {
                viewModel.getChatPraises(channelType, targetId!!, startId)
            }

            override fun onRefresh(refreshLayout: RefreshLayout) {
                startId = null
                viewModel.getChatPraises(channelType, targetId!!, startId)
            }

        })
    }
}