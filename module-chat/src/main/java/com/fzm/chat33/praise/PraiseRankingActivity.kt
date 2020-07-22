package com.fzm.chat33.praise

import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.text.SpannableString
import android.text.style.RelativeSizeSpan
import android.view.View
import android.view.animation.DecelerateInterpolator
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.alibaba.android.arouter.facade.annotation.Autowired
import com.alibaba.android.arouter.facade.annotation.Route
import com.alibaba.android.arouter.launcher.ARouter
import com.angcyo.dsladapter.*
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.fuzamei.common.ext.format
import com.fuzamei.common.recycleviewbase.RecyclerViewDivider
import com.fuzamei.common.utils.BarUtils
import com.fuzamei.common.utils.DateUtils
import com.fuzamei.common.utils.FinanceUtils
import com.fuzamei.componentservice.app.AppRoute
import com.fuzamei.componentservice.base.DILoadableActivity
import com.fuzamei.componentservice.ext.dp2px
import com.fuzamei.componentservice.ext.findViewModel
import com.fzm.chat33.R
import com.fzm.chat33.core.bean.PraiseRank
import com.fzm.chat33.core.bean.param.PraiseRankingParam
import com.fzm.chat33.core.db.bean.FriendBean
import com.fzm.chat33.core.global.Chat33Const
import com.fzm.chat33.core.provider.InfoProvider
import com.fzm.chat33.core.provider.OnFindInfoListener
import com.fzm.chat33.core.provider.UserInfoStrategy
import com.fzm.chat33.main.mvvm.PraiseRankingViewModel
import com.fzm.chat33.widget.ChatAvatarView
import com.google.android.material.appbar.AppBarLayout
import kotlinx.android.synthetic.main.activity_praise_ranking.*
import java.util.*
import javax.inject.Inject
import kotlin.math.abs

/**
 * @author zhengjy
 * @since 2019/12/05
 * Description:
 */
@Route(path = AppRoute.PRAISE_RANK)
class PraiseRankingActivity : DILoadableActivity() {

    companion object {
        const val LIKE_PAGE = 1
        const val REWARD_PAGE = 2
        const val DATE_FORMAT = "MM.dd"
        const val START_ID = 1
    }

    @Inject
    lateinit var provider: ViewModelProvider.Factory
    lateinit var viewModel: PraiseRankingViewModel

    @JvmField
    @Autowired
    var start: Long = 0
    @JvmField
    @Autowired
    var end: Long = 0

    private var dslAdapter: DslAdapter = DslAdapter()

    private val pageStore = mutableMapOf(LIKE_PAGE to START_ID, REWARD_PAGE to START_ID)
    private val mineStore = mutableMapOf<Int, PraiseRank>()
    private val listStore: Map<Int, MutableList<PraiseRank>> = mapOf(LIKE_PAGE to mutableListOf(), REWARD_PAGE to mutableListOf())
    var current = LIKE_PAGE

    val request = PraiseRankingParam(type = LIKE_PAGE, startId = pageStore[current])

    override fun getLayoutId(): Int {
        return R.layout.activity_praise_ranking
    }

    override fun setStatusBar() {
        BarUtils.setStatusBarColor(this, ContextCompat.getColor(this, R.color.chat_transparent), 0)
        val height = BarUtils.getStatusBarHeight(this)
        val param = bg_title.layoutParams
        param.height += height
        bg_title.layoutParams = param
        BarUtils.addMarginTopEqualStatusBarHeight(this, rl_title)
        BarUtils.setStatusBarLightMode(this, false)
    }

    override fun initView() {
        viewModel = findViewModel(provider)
        ARouter.getInstance().inject(this)
        if (start == 0L || end == 0L) {
            val now = Date()
            start = DateUtils.getWeekMonday(now).time
            end = DateUtils.getWeekSunday(now).time
            tv_history.visibility = View.VISIBLE
        } else {
            tv_history.visibility = View.GONE
        }
        request.apply {
            startTime = start
            endTime = end
        }
        rv_ranking_list.apply {
            addItemDecoration(RecyclerViewDivider(instance, LinearLayoutManager.VERTICAL,
                    0.5f, ContextCompat.getColor(instance, R.color.chat_forward_divider_receive)))
            layoutManager = LinearLayoutManager(instance)
            adapter = dslAdapter
        }

        viewModel.loading.observe(this, Observer { setupLoading(it) })
        viewModel.rankList.observe(this, Observer {
            if (it == null) {
                swipeLayout.finishLoadMore(false)
                return@Observer
            }
            if (it.enterprise?.name?.isNotEmpty() == true) {
                // 企业信息不为空则显示
                tv_company.text = it.enterprise?.name
            }
            it.mine?.apply { mineStore[it.type] = this }
            listStore[it.type]?.addAll(it.records)
            pageStore[it.type] = it.nextLog
            if (it.type == current) {
                showMyRank()
                bindData(it.records)
                if (it.nextLog == -1) {
                    swipeLayout.finishLoadMoreWithNoMoreData()
                } else {
                    swipeLayout.finishLoadMore()
                }
            }
        })
    }

    override fun initData() {
        doRequest(true)
    }

    private fun doRequest(show: Boolean) {
        request.apply {
            type = current
            startId = pageStore[current]
        }
        viewModel.getRankList(request, show)
    }

    override fun setEvent() {
        swipeLayout.setEnableRefresh(false)
        swipeLayout.setEnableLoadMore(true)
        swipeLayout.setOnLoadMoreListener {
            doRequest(false)
        }
        tv_history.setOnClickListener {
            ARouter.getInstance().build(AppRoute.PRAISE_RANK_HISTORY).navigation()
        }
        iv_back.setOnClickListener { finish() }
        change_like.setOnClickListener { changePage(LIKE_PAGE) }
        change_reward.setOnClickListener { changePage(REWARD_PAGE) }
        appbar.addOnOffsetChangedListener(AppBarLayout.OnOffsetChangedListener { appBar, offset ->
            val max = dp2px(150f).toFloat()
            val scrollY = abs(offset)
            if (scrollY > max) {
                if (bg_title.alpha == 1.0f || tv_title.alpha == 1.0f ) {
                    return@OnOffsetChangedListener
                }
                bg_title.alpha = 1.0f
                tv_title.alpha = 1.0f
            } else {
                bg_title.alpha = scrollY / max
                tv_title.alpha = scrollY / max
            }
        })
    }

    private fun changePage(page: Int) {
        current = page
        if (current == LIKE_PAGE) {
            ll_like_list.visibility = View.VISIBLE
            ll_reward_list.visibility = View.GONE
        } else {
            ll_like_list.visibility = View.GONE
            ll_reward_list.visibility = View.VISIBLE
        }
        swipeLayout.setNoMoreData(pageStore[current] == -1)
        dslAdapter.resetItem(listOf())
        if (pageStore[current] == START_ID) {
            doRequest(true)
        } else {
            showMyRank()
            bindData(listStore[current])
        }
    }

    @SuppressLint("SetTextI18n")
    private fun showMyRank() {
        if (hasNoRank(mineStore[current])) {
            my_rank.text = getString(R.string.chat_praise_no_rank)
        } else {
            my_rank.text = mineStore[current]?.ranking?.toString()
        }
        Glide.with(instance).load(viewModel.currentUser.value?.avatar)
                .apply(RequestOptions().placeholder(R.mipmap.default_avatar_round))
                .into(iv_avatar)
        iv_avatar.setIconRes(if (viewModel.currentUser.value?.isIdentified == true) R.drawable.ic_user_identified else -1)
        tv_name.text = viewModel.currentUser.value?.username
        animateTab()
        if (current == LIKE_PAGE) {
            tv_date_like.text = "${start.format(DATE_FORMAT)}—${end.format(DATE_FORMAT)}"
            cl_my_rank.setBackgroundResource(R.mipmap.bg_like_mine)
            tv_num.text = mineStore[current]?.number.toString()
            val drawable = ContextCompat.getDrawable(instance, R.drawable.ic_thumb_up_white)
            drawable?.setBounds(0, 0, dp2px(11f), dp2px(11f))
            tv_num.setCompoundDrawables(null, null, drawable, null)
            tv_num.compoundDrawablePadding = 5
        } else {
            tv_date_reward.text = "${start.format(DATE_FORMAT)}—${end.format(DATE_FORMAT)}"
            cl_my_rank.setBackgroundResource(R.mipmap.bg_reward_mine)
            val sp = SpannableString("¥${FinanceUtils.getPlainNum(mineStore[current]?.price ?: 0.0, 2)}")
            sp.setSpan(RelativeSizeSpan(0.6f), 0, 1, 0)
            tv_num.text = sp
            tv_num.setCompoundDrawables(null, null, null, null)
            tv_num.compoundDrawablePadding = 0
        }
    }

    private fun animateTab() {
        if (current == LIKE_PAGE) {
            ObjectAnimator.ofFloat(tv_num, "translationX", 80f, 0f).apply {
                interpolator = DecelerateInterpolator()
                start()
            }
            ObjectAnimator.ofFloat(tips_like1, "translationY", 80f, 0f).apply {
                interpolator = DecelerateInterpolator()
                start()
            }
            ObjectAnimator.ofFloat(tv_date_like, "translationY", 80f, 0f).apply {
                interpolator = DecelerateInterpolator()
                start()
            }
            tips_like1.text = getString(R.string.chat_praise_rank_like1)
        } else {
            ObjectAnimator.ofFloat(tv_num, "translationX", 80f, 0f).apply {
                interpolator = DecelerateInterpolator()
                start()
            }
            ObjectAnimator.ofFloat(tips_reward2, "translationY", 80f, 0f).apply {
                interpolator = DecelerateInterpolator()
                start()
            }
            ObjectAnimator.ofFloat(tv_date_reward, "translationY", 80f, 0f).apply {
                interpolator = DecelerateInterpolator()
                start()
            }
            tips_reward2.text = getString(R.string.chat_praise_rank_reward2)
        }
    }

    private fun bindData(data: List<PraiseRank>?) {
        data?.forEach { item ->
            when {
                item.ranking <= 3 && !hasNoRank(item) -> dslAdapter.dslItem(R.layout.item_praise_rank1) {
                    onItemBindOverride = { holder, _, _ ->
                        when (item.ranking) {
                            1 -> holder.img(R.id.rank).setImageResource(R.mipmap.ic_ranking_first)
                            2 -> holder.img(R.id.rank).setImageResource(R.mipmap.ic_ranking_second)
                            3 -> holder.img(R.id.rank).setImageResource(R.mipmap.ic_ranking_third)
                        }
                        loadInfo(holder, item)
                    }
                    onItemClick = {
                        if (item.user.id != viewModel.getUserId()) {
                            ARouter.getInstance().build(AppRoute.REWARD_PACKET)
                                    .withString("targetId", item.user.id)
                                    .withInt("channelType", Chat33Const.CHANNEL_FRIEND)
                                    .navigation()
                        }
                    }
                }
                else -> dslAdapter.dslItem(R.layout.item_praise_rank2) {
                    onItemBindOverride = { holder, _, _ ->
                        val rank = if (hasNoRank(item)) {
                            // 点赞或打赏为0则不显示排名
                            getString(R.string.chat_praise_no_rank)
                        } else {
                            item.ranking.toString()
                        }
                        when {
                            rank.length <= 3 -> holder.tv(R.id.rank).textSize = 20f
                            else -> holder.tv(R.id.rank).textSize = 15f
                        }
                        holder.tv(R.id.rank).text = rank
                        loadInfo(holder, item)
                    }
                    onItemClick = {
                        if (item.user.id != viewModel.getUserId()) {
                            ARouter.getInstance().build(AppRoute.REWARD_PACKET)
                                    .withString("targetId", item.user.id)
                                    .withInt("channelType", Chat33Const.CHANNEL_FRIEND)
                                    .navigation()
                        }
                    }
                }
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun loadInfo(holder: DslViewHolder, item: PraiseRank) {
        // 先恢复默认空显示，防止闪烁
        holder.tv(R.id.tv_name).text = ""
        holder.img(R.id.iv_avatar).setImageResource(R.mipmap.default_avatar_round)
        // 设置tag
        holder.img(R.id.iv_avatar).setTag(R.id.iv_avatar, item.user.id)
        InfoProvider.getInstance().strategy(UserInfoStrategy(item.user.id)).load(object : OnFindInfoListener<FriendBean> {
            override fun onFindInfo(data: FriendBean?, place: Int) {
                if (data?.id != holder.img(R.id.iv_avatar).getTag(R.id.iv_avatar)) {
                    return
                }
                val avatar = holder.img(R.id.iv_avatar) as ChatAvatarView
                Glide.with(instance).load(data?.avatar)
                        .apply(RequestOptions().placeholder(R.mipmap.default_avatar_round))
                        .into(avatar)
                avatar.setIconRes(if (data?.isIdentified == true) R.drawable.ic_user_identified else -1)
                holder.tv(R.id.tv_name).text = data?.displayName
            }

            override fun onNotExist() {
                holder.tv(R.id.tv_name).text = item.user.name
                Glide.with(instance).load(item.user.avatar)
                        .apply(RequestOptions().placeholder(R.mipmap.default_avatar_round))
                        .into(holder.img(R.id.iv_avatar))
            }
        })
        if (current == LIKE_PAGE) {
            holder.tv(R.id.tv_num).text = item.number.toString()
            holder.tv(R.id.tv_num).setTextColor(ContextCompat.getColor(this, R.color.chat_color_accent))
            val drawable = ContextCompat.getDrawable(instance, R.drawable.ic_thumb_up_accent)
            drawable?.setBounds(0, 0, dp2px(11f), dp2px(11f))
            holder.tv(R.id.tv_num).setCompoundDrawables(null, null, drawable, null)
            holder.tv(R.id.tv_num).compoundDrawablePadding = 5
        } else {
            val sp = SpannableString("¥${FinanceUtils.getPlainNum(item.price, 2)}")
            sp.setSpan(RelativeSizeSpan(0.6f), 0, 1, 0)
            holder.tv(R.id.tv_num).text = sp
            holder.tv(R.id.tv_num).setTextColor(ContextCompat.getColor(this, R.color.chat_reward_orange))
            holder.tv(R.id.tv_num).setCompoundDrawables(null, null, null, null)
            holder.tv(R.id.tv_num).compoundDrawablePadding = 0
        }
    }

    /**
     * 是否有排名，如果点赞打赏为0，则不显示排名
     */
    private fun hasNoRank(item: PraiseRank?): Boolean {
        return (current == LIKE_PAGE && item?.number == 0) || (current == REWARD_PAGE && item?.price == 0.0)
    }
}