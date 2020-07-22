package com.fzm.chat33.main.activity

import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.fragment.app.Fragment
import androidx.viewpager.widget.ViewPager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.alibaba.android.arouter.facade.annotation.Autowired
import com.alibaba.android.arouter.facade.annotation.Route
import com.alibaba.android.arouter.launcher.ARouter
import com.fuzamei.common.recycleviewbase.CommonAdapter
import com.fuzamei.common.recycleviewbase.ViewHolder
import com.fuzamei.common.utils.BarUtils
import com.fuzamei.common.utils.FinanceUtils
import com.fuzamei.common.view.ScrollPagerAdapter
import com.fuzamei.componentservice.app.AppRoute
import com.fuzamei.componentservice.base.DILoadableActivity
import com.fuzamei.componentservice.config.AppConfig
import com.fuzamei.componentservice.ext.findViewModel
import com.fzm.chat33.R
import com.fzm.chat33.core.bean.PromoteBriefInfo
import com.fzm.chat33.main.fragment.ConditionRewardFragment
import com.fzm.chat33.main.fragment.PromoteRewardFragment
import com.fzm.chat33.main.mvvm.PromoteDetailViewModel
import kotlinx.android.synthetic.main.activity_promote_detail.*
import javax.inject.Inject

/**
 * @author zhengjy
 * @since 2019/07/02
 * Description:推广详情界面
 */
@Route(path = AppRoute.PROMOTE_DETAIL)
class PromoteDetailActivity : DILoadableActivity() {

    @JvmField
    @Autowired(name = "info")
    var promoteInfo: PromoteBriefInfo? = null

    @Inject
    lateinit var provider: ViewModelProvider.Factory
    private lateinit var viewModel: PromoteDetailViewModel

    private var rewardAdapter: CommonAdapter<String>? = null
    private var rewardData = mutableListOf<String>()

    private var fragments = mutableListOf<Fragment>()
    private var titles = arrayListOf("", "")
    private var mAdapter: ScrollPagerAdapter? = null

    override fun setStatusBar() {
        BarUtils.setStatusBarColor(this, ContextCompat.getColor(this, R.color.chat_transparent), 0)
        BarUtils.addMarginTopEqualStatusBarHeight(this, rl_title)
        BarUtils.setStatusBarLightMode(this, false)
    }

    override fun getLayoutId(): Int {
        return R.layout.activity_promote_detail
    }

    override fun initView() {
        ARouter.getInstance().inject(this)
        viewModel = findViewModel(provider)
        showHeadView(promoteInfo)
        iv_back.setOnClickListener { finish() }
        rewardAdapter = object : CommonAdapter<String>(this, R.layout.adapter_promote_reward_tag, rewardData) {
            override fun convert(holder: ViewHolder?, t: String?, position: Int) {
                holder?.setText(R.id.tv_reward, t)
            }
        }
        rv_reward.layoutManager = LinearLayoutManager(this, RecyclerView.HORIZONTAL, false)
        rv_reward.adapter = rewardAdapter
        fragments.add(PromoteRewardFragment())
        fragments.add(ConditionRewardFragment())
        mAdapter = ScrollPagerAdapter(supportFragmentManager, titles, fragments)
        vp_reward.adapter = mAdapter
        vp_reward.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrollStateChanged(state: Int) {

            }

            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {

            }

            override fun onPageSelected(position: Int) {
                switchChoose(position)
            }
        })
        switchChoose(0)
        vp_reward.currentItem = 0

        viewModel.promoteDetail.observe(this, Observer {
            if (it != null) {
                showHeadView(it)
            }
        })
        viewModel.getPromoteBriefInfo()
    }

    override fun initData() {

    }

    override fun setEvent() {
        ll_promote_rule.setOnClickListener {
            ARouter.getInstance().build(AppRoute.WEB_BROWSER)
                    .withString("url", AppConfig.APP_PROMOTE_RULE_URL)
                    .withBoolean("showOptions", false)
                    .navigation()
        }
        tv_promote.setOnClickListener {
            switchChoose(0)
            vp_reward.currentItem = 0
        }
        tv_condition.setOnClickListener {
            switchChoose(1)
            vp_reward.currentItem = 1
        }
    }

    private fun switchChoose(index: Int) {
        when (index) {
            0 -> {
                tv_promote.setTextColor(ContextCompat.getColor(this, R.color.chat_color_title))
                tv_promote.setBackgroundResource(R.drawable.shape_common_table)
                tv_condition.setTextColor(ContextCompat.getColor(this, R.color.chat_text_grey_light))
                tv_condition.setBackgroundResource(0)
            }
            1 -> {
                tv_promote.setTextColor(ContextCompat.getColor(this, R.color.chat_text_grey_light))
                tv_promote.setBackgroundResource(0)
                tv_condition.setTextColor(ContextCompat.getColor(this, R.color.chat_color_title))
                tv_condition.setBackgroundResource(R.drawable.shape_common_table)
            }
        }
    }

    fun showHeadView(info: PromoteBriefInfo?) {
        rewardData.clear()
        tv_reward_tips.text = getString(R.string.chat_promote_total_reward, info?.primary?.currency ?: "")
        tv_reward_num.text = FinanceUtils.stripZero(info?.primary?.total)
        if (info?.inviteNum ?: 0 > 0) {
            rewardData.add(getString(R.string.chat_promote_total_promote, info?.inviteNum))
        }
        if (info?.statistics != null && info.statistics.size > 0) {
            for (item in info.statistics) {
                rewardData.add(getString(R.string.chat_promote_total_reward_item, FinanceUtils.stripZero(item?.total), item?.currency ?: ""))
            }
        }
        rewardAdapter?.notifyDataSetChanged()
    }
}
