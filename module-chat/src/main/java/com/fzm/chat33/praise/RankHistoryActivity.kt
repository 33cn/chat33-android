package com.fzm.chat33.praise

import android.text.SpannableString
import android.text.style.RelativeSizeSpan
import android.view.View
import android.widget.TextView
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.alibaba.android.arouter.facade.annotation.Route
import com.alibaba.android.arouter.launcher.ARouter
import com.fuzamei.common.ext.format
import com.fuzamei.common.recycleviewbase.CommonAdapter
import com.fuzamei.common.recycleviewbase.MultiItemTypeAdapter
import com.fuzamei.common.recycleviewbase.ViewHolder
import com.fuzamei.common.utils.FinanceUtils
import com.fuzamei.componentservice.app.AppRoute
import com.fuzamei.componentservice.base.DILoadableActivity
import com.fuzamei.componentservice.ext.findViewModel
import com.fzm.chat33.R
import com.fzm.chat33.core.bean.PraiseRankHistory
import com.fzm.chat33.main.mvvm.PraiseRankingViewModel
import kotlinx.android.synthetic.main.activity_rank_history.*
import javax.inject.Inject

/**
 * @author zhengjy
 * @since 2019/12/06
 * Description:
 */
@Route(path = AppRoute.PRAISE_RANK_HISTORY)
class RankHistoryActivity : DILoadableActivity() {

    companion object {
        private const val PAGE_SIZE = 8
    }

    @Inject
    lateinit var provider: ViewModelProvider.Factory
    lateinit var viewModel: PraiseRankingViewModel

    val historyList = mutableListOf<PraiseRankHistory>()
    lateinit var mAdapter: CommonAdapter<PraiseRankHistory>

    override fun getLayoutId(): Int {
        return R.layout.activity_rank_history
    }

    override fun initView() {
        viewModel = findViewModel(provider)
        mAdapter = object : CommonAdapter<PraiseRankHistory>(this, R.layout.item_rank_history, historyList) {
            override fun convert(holder: ViewHolder?, t: PraiseRankHistory?, position: Int) {
                holder?.setText(R.id.tv_date, "${t?.startTime?.format("yyyy.MM.dd")}—${t?.endTime?.format("yyyy.MM.dd")}")
                if (t?.like?.number == 0) {
                    holder?.setText(R.id.like_rank, getString(R.string.chat_praise_no_rank))
                } else {
                    holder?.setText(R.id.like_rank, t?.like?.ranking.toString())
                }
                holder?.setText(R.id.like_num, t?.like?.number.toString())
                if (t?.reward?.price == 0.0) {
                    holder?.setText(R.id.reward_rank, getString(R.string.chat_praise_no_rank))
                } else {
                    holder?.setText(R.id.reward_rank, t?.reward?.ranking.toString())
                }
                val amount = holder?.getView<TextView>(R.id.reward_amount)
                val sp = SpannableString("¥${FinanceUtils.getPlainNum(t?.reward?.price ?: 0.0, 2)}")
                sp.setSpan(RelativeSizeSpan(0.6f), 0, 1, 0)
                amount?.text = sp
            }
        }.apply {
            setOnItemClickListener(object : MultiItemTypeAdapter.OnItemClickListener {
                override fun onItemLongClick(view: View?, holder: RecyclerView.ViewHolder?, position: Int): Boolean {
                    return true
                }

                override fun onItemClick(view: View?, holder: RecyclerView.ViewHolder?, position: Int) {
                    ARouter.getInstance().build(AppRoute.PRAISE_RANK)
                            .withLong("start", historyList[position].startTime)
                            .withLong("end", historyList[position].endTime)
                            .navigation()
                }
            })
        }
        rv_history.apply {
            layoutManager = LinearLayoutManager(instance)
            adapter = mAdapter
        }
        viewModel.loading.observe(this, Observer { setupLoading(it) })
        viewModel.rankHistory.observe(this, Observer {
            historyList.addAll(it.records)
            mAdapter.notifyDataSetChanged()
            if (it.records.size < PAGE_SIZE) {
                swipeLayout.finishLoadMoreWithNoMoreData()
            } else {
                swipeLayout.finishLoadMore()
            }
        })
    }

    override fun initData() {
        viewModel.getRankHistory(PAGE_SIZE, true)
    }
    
    override fun setEvent() {
        ctb_title.setLeftListener { finish() }
        ctb_title.setMiddleText(getString(R.string.chat_title_rank_history))
        swipeLayout.setEnableRefresh(false)
        swipeLayout.setEnableLoadMore(true)
        swipeLayout.setOnLoadMoreListener {
            viewModel.getRankHistory(PAGE_SIZE, false)
        }
    }
}