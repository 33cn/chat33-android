package com.fzm.chat33.main.fragment

import androidx.lifecycle.Observer
import androidx.recyclerview.widget.RecyclerView
import com.fuzamei.common.recycleviewbase.CommonAdapter
import com.fuzamei.common.recycleviewbase.ViewHolder
import com.fuzamei.common.utils.FinanceUtils
import com.fuzamei.componentservice.config.AppConfig
import com.fzm.chat33.R
import com.fzm.chat33.core.bean.PromoteReward
import kotlinx.android.synthetic.main.fragment_promote_list.*
import java.math.BigDecimal

/**
 * @author zhengjy
 * @since 2019/07/09
 * Description:推广下级奖励列表
 */
class PromoteRewardFragment : BaseRewardFragment() {

    private var mData = mutableListOf<PromoteReward>()

    override fun getAdapter(): RecyclerView.Adapter<*> {
        return object : CommonAdapter<PromoteReward>(activity, R.layout.adapter_promote_reward, mData) {
            override fun convert(holder: ViewHolder?, t: PromoteReward?, position: Int) {
                holder?.setText(R.id.tv_uid, "UID ${t?.uid}")
                if (t?.isReal == 1) {
                    holder?.setImageResource(R.id.iv_verify, R.drawable.ic_user_verified)
                    holder?.setTextColorRes(R.id.tv_verify, R.color.chat_text_grey_dark)
                    holder?.setText(R.id.tv_verify, getString(R.string.chat_set_real_name))
                } else {
                    holder?.setImageResource(R.id.iv_verify, R.drawable.ic_user_not_verified)
                    holder?.setTextColorRes(R.id.tv_verify, R.color.chat_text_grey_light)
                    holder?.setText(R.id.tv_verify, getString(R.string.chat_unset_real_name))
                }
                try {
                    val big = BigDecimal(t?.amount)
                    if (big.toDouble() == 0.0) {
                        holder?.setTextColorRes(R.id.tv_amount, R.color.chat_text_grey_light)
                    } else {
                        holder?.setTextColorRes(R.id.tv_amount, R.color.chat_text_grey_dark)
                    }
                } catch (e: Exception) {
                    holder?.setTextColorRes(R.id.tv_amount, R.color.chat_text_grey_light)
                }
                holder?.setText(R.id.tv_amount, "+${FinanceUtils.stripZero(t?.amount)}")
                holder?.setText(R.id.tv_unit, t?.currency)
            }
        }
    }

    override fun getRewardList() {
        viewModel.promoteReward.observe(this, Observer {
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
        viewModel.getPromoteRewardList(mPage)
    }

}