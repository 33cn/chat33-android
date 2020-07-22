package com.fzm.chat33.main.fragment

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.view.View
import com.fuzamei.componentservice.base.DILoadableFragment
import com.fuzamei.componentservice.ext.findViewModel
import com.fzm.chat33.R
import com.fzm.chat33.main.mvvm.PromoteDetailViewModel
import com.scwang.smartrefresh.layout.api.RefreshLayout
import com.scwang.smartrefresh.layout.listener.OnRefreshLoadMoreListener
import kotlinx.android.synthetic.main.fragment_promote_list.*
import javax.inject.Inject

/**
 * @author zhengjy
 * @since 2019/07/09
 * Description:
 */
abstract class BaseRewardFragment : DILoadableFragment() {

    protected var mPage = 1
    protected var mAdapter: RecyclerView.Adapter<*>? = null
    @Inject
    lateinit var provider: ViewModelProvider.Factory
    protected lateinit var viewModel: PromoteDetailViewModel

    override fun getLayoutId(): Int {
        return R.layout.fragment_promote_list
    }

    override fun initView(view: View?, savedInstanceState: Bundle?) {
        viewModel = findViewModel(provider)
        mAdapter = getAdapter()
        rv_promote.layoutManager = LinearLayoutManager(activity)
        rv_promote.adapter = mAdapter
    }

    override fun initData() {
        getRewardList()
    }

    override fun setEvent() {
        swipeLayout.setOnRefreshLoadMoreListener(object : OnRefreshLoadMoreListener{
            override fun onRefresh(refreshLayout: RefreshLayout) {
                mPage = 1
                getRewardList()
            }

            override fun onLoadMore(refreshLayout: RefreshLayout) {
                getRewardList()
            }
        })
    }

    abstract fun getRewardList()

    abstract fun getAdapter(): RecyclerView.Adapter<*>

}