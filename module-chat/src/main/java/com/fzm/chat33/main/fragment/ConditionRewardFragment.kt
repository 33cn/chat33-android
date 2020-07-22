package com.fzm.chat33.main.fragment

import androidx.lifecycle.Observer
import androidx.recyclerview.widget.RecyclerView
import com.fuzamei.common.recycleviewbase.CommonAdapter
import com.fuzamei.common.recycleviewbase.ViewHolder
import com.fuzamei.common.utils.FinanceUtils
import com.fuzamei.componentservice.config.AppConfig
import com.fzm.chat33.R
import com.fzm.chat33.core.bean.ConditionReward
import kotlinx.android.synthetic.main.fragment_promote_list.*

/**
 * @author zhengjy
 * @since 2019/07/09
 * Description:达成条件奖励列表
 */
class ConditionRewardFragment : BaseRewardFragment() {

    private var mData = mutableListOf<ConditionReward>()

    override fun getAdapter(): RecyclerView.Adapter<*> {
        return object : CommonAdapter<ConditionReward>(activity, R.layout.adapter_condition_reward, mData) {
            override fun convert(holder: ViewHolder?, t: ConditionReward?, position: Int) {
                holder?.setText(R.id.tv_time, t?.updatedAt)
                if ("standard_reward" == t?.type) {
                    holder?.setText(R.id.tv_desc, getString(R.string.chat_invite_count_str, t.num))
                } else if ("auth_reward" == t?.type) {
                    holder?.setText(R.id.tv_desc, getString(R.string.chat_real_name))
                }
                holder?.setText(R.id.tv_amount, "+${FinanceUtils.stripZero(t?.amount)}")
                holder?.setText(R.id.tv_unit, t?.currency)
            }
        }
    }

    override fun getRewardList() {
        viewModel.conditionReward.observe(this, Observer {
            if (it == null) {
                swipeLayout.finishRefresh(false)
                swipeLayout.finishLoadMore(false)
                return@Observer
            }
            if (mPage == 1) {
                mData.clear()
            }
            mData.addAll(it.list)
            swipeLayout.finishRefresh(true)
            if (it.count < AppConfig.PAGE_SIZE) {
                swipeLayout.finishLoadMoreWithNoMoreData()
            } else {
                swipeLayout.finishLoadMore(true)
            }
            if (mData.size == 0) {
                statusLayout.showEmpty()
            } else {
                statusLayout.showContent()
            }
            mAdapter?.notifyDataSetChanged()
            mPage++
        })
        viewModel.getConditionRewardList(mPage)
    }
}