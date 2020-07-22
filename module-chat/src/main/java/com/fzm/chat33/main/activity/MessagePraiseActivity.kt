package com.fzm.chat33.main.activity

import android.view.View
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.alibaba.android.arouter.facade.annotation.Autowired
import com.alibaba.android.arouter.facade.annotation.Route
import com.alibaba.android.arouter.launcher.ARouter
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.fuzamei.common.recycleviewbase.RecyclerViewDivider
import com.fuzamei.common.utils.ScreenUtils
import com.fuzamei.componentservice.app.AppRoute
import com.fuzamei.componentservice.base.DILoadableActivity
import com.fuzamei.componentservice.ext.findViewModel
import com.fzm.chat33.R
import com.fzm.chat33.core.consts.PraiseState.LIKE
import com.fzm.chat33.core.db.bean.ChatMessage
import com.fzm.chat33.core.db.bean.ChatMessage.Type.*
import com.fzm.chat33.core.db.bean.InfoCacheBean
import com.fzm.chat33.core.global.Chat33Const
import com.fzm.chat33.core.provider.ChatInfoStrategy
import com.fzm.chat33.core.provider.InfoProvider
import com.fzm.chat33.core.provider.OnFindInfoListener
import com.fzm.chat33.main.adapter.ChatPraiseAdapter
import com.fzm.chat33.main.mvvm.ChatViewModel
import com.fzm.chat33.main.mvvm.MessagePraiseViewModel
import com.fzm.chat33.utils.PraiseUtil
import com.fzm.chat33.utils.StringUtils
import com.fzm.chat33.widget.chatpraise.*
import com.scwang.smartrefresh.layout.api.RefreshLayout
import com.scwang.smartrefresh.layout.listener.OnRefreshLoadMoreListener
import kotlinx.android.synthetic.main.activity_message_praise.*
import java.math.BigDecimal
import javax.inject.Inject

/**
 * 创建日期：2018/11/19
 * 描述:群聊赞赏列表
 * 作者:yll
 */
@Route(path = AppRoute.MESSAGE_PRAISE)
class MessagePraiseActivity : DILoadableActivity() {

    @JvmField
    @Autowired
    var channelType: Int = 0
    @JvmField
    @Autowired
    var logId: String? = null
    @JvmField
    @Autowired
    var targetId: String? = null
    @Inject
    lateinit var provider: ViewModelProvider.Factory
    lateinit var viewModel: MessagePraiseViewModel
    lateinit var chatViewModel: ChatViewModel
    lateinit var adapter: ChatPraiseAdapter

    private var mMinItemWith: Int = 0// 设置对话框的最大宽度和最小宽度
    private var mMaxItemWith: Int = 0
    private var startId: String? = null
    private var chatPraiseBase: ChatPraiseBase? = null

    override fun getLayoutId(): Int {
        return R.layout.activity_message_praise
    }

    override fun initView() {
        ARouter.getInstance().inject(this)
        viewModel = findViewModel(provider)
        chatViewModel = findViewModel(provider)
        ctb_title.setMiddleText(getString(R.string.chat_praise_title))
        ctb_title.setLeftListener { finish() }
        /*ctb_title.setRightText(getString(R.string.chat_tips_red_packet1))
        ctb_title.setRightListener {
            ARouter.getInstance().build(AppRoute.RED_PACKET_RECORDS).navigation()
        }*/
        recycler.layoutManager = LinearLayoutManager(instance)
        recycler.addItemDecoration(RecyclerViewDivider(instance, LinearLayoutManager.VERTICAL,
                0.5f, ContextCompat.getColor(instance, R.color.chat_divide_light)))
        adapter = ChatPraiseAdapter(instance, targetId, true)
        recycler.adapter = adapter
        viewModel.loading.observe(this, Observer { setupLoading(it) })
        viewModel.praiseDetail.observe(this, Observer {
            message_praise_count.text = "${it.praiseNumber}"
            val format = BigDecimal(it.reward).setScale(2, BigDecimal.ROUND_DOWN)
            message_praise_price.text = "¥${format}"
            tv_chat_praise.visibility = if (it.state and LIKE == LIKE) View.GONE else View.VISIBLE
            tv_chat_praised.visibility = if (it.state and LIKE == LIKE) View.VISIBLE else View.GONE

            ll_bottom.visibility = if (it.log.isSentType) View.GONE else View.VISIBLE
            if (it.log.channelType == Chat33Const.CHANNEL_FRIEND) {
                tv_user_name.visibility = View.GONE
            } else {
                tv_user_name.visibility = View.VISIBLE
            }
            tv_user_name.setTextColor(ContextCompat.getColor(instance, R.color.chat_text_grey_light))
            tv_user_name.tag = it
            val options = RequestOptions().placeholder(R.mipmap.default_avatar_round)
            InfoProvider.getInstance().strategy(ChatInfoStrategy(it.log)).load(object : OnFindInfoListener<InfoCacheBean> {
                override fun onFindInfo(data: InfoCacheBean, place: Int) {
                    if (instance == null || instance.isFinishing) {
                        return
                    }
                    if (it != tv_user_name.tag) {
                        return
                    }
                    Glide.with(instance).load(StringUtils.aliyunFormat(data.avatar, ScreenUtils.dp2px(instance, 35f), ScreenUtils.dp2px(instance, 35f)))
                            .apply(options)
                            .into(iv_user_head)
                    iv_user_head.setIconRes(if (data.isIdentified) R.drawable.ic_user_identified else -1)
                    tv_user_name.text = data.displayName
                }

                override fun onNotExist() {
                    iv_user_head.setImageResource(R.mipmap.default_avatar_round)
                    tv_user_name.setText(R.string.chat_tips_no_name)
                }
            })
            chat_message_layout.layoutDirection = if (it.log.isSentType) View.LAYOUT_DIRECTION_RTL else View.LAYOUT_DIRECTION_LTR
            chat_message_content.removeAllViews()
            chat_message_content.addView(getChatView(it.log))
        })
        viewModel.praiseList.observe(this, Observer {
            if (it == null) {
                swipeLayout.finishRefresh(false)
            } else {
                if (startId.isNullOrEmpty()) {
                    swipeLayout.finishRefresh(true)
                    adapter.resetList(it.records)
                } else if ("-1" == it.nextLog) {
                    swipeLayout.finishLoadMoreWithNoMoreData()
                    adapter.addList(it.records)
                } else {
                    swipeLayout.finishLoadMore()
                    adapter.addList(it.records)
                }
                startId = it.nextLog
            }
        })
        viewModel.praise.observe(this, Observer {
            PraiseUtil.showLike(instance)
            onRefresh()
        })
        viewModel.cancelPraise.observe(this, Observer {
            PraiseUtil.showCancelLike(instance)
            onRefresh()
        })

        onRefresh()
    }

    override fun initData() {
        mMaxItemWith = (ScreenUtils.getScreenWidth(instance) * 0.55f).toInt()
        mMinItemWith = (ScreenUtils.getScreenWidth(instance) * 0.20f).toInt()
    }

    override fun enableSlideBack(): Boolean {
        return true
    }

    override fun setEvent() {
        swipeLayout.setOnRefreshLoadMoreListener(object : OnRefreshLoadMoreListener {
            override fun onLoadMore(refreshLayout: RefreshLayout) {
                viewModel.praiseDetailList(channelType, startId, logId!!)
            }

            override fun onRefresh(refreshLayout: RefreshLayout) {
                onRefresh()
            }
        })
        tv_chat_praise.setOnClickListener {
            viewModel.praise(channelType, logId!!)
        }
        tv_chat_reward.setOnClickListener {
            ARouter.getInstance().build(AppRoute.REWARD_PACKET)
                    .withInt("channelType", channelType)
                    .withString("logId", logId)
                    .navigation()
        }
    }

    private fun onRefresh() {
        viewModel.praiseDetails(channelType, logId!!)
        startId = null
        viewModel.praiseDetailList(channelType, startId, logId!!)
    }

    private fun getChatView(message: ChatMessage): View? {
        chatPraiseBase = when {
            !message.msg.encryptedMsg.isNullOrEmpty() -> ChatPraiseEncrypted(this)
            message.msgType == TEXT -> ChatPraiseText(this)
            message.msgType == AUDIO -> ChatPraiseAudio(this)
            message.msgType == IMAGE -> ChatPraiseImage(this)
            message.msgType == RED_PACKET -> ChatPraiseTextPacket(this)
            message.msgType == VIDEO -> ChatPraiseVideo(this)
            message.msgType == FORWARD -> ChatPraiseForward(this)
            message.msgType == FILE -> ChatPraiseFile(this)
            message.msgType == TRANSFER -> ChatPraiseTransfer(this)
            message.msgType == RECEIPT -> ChatPraiseReceipt(this)
            message.msgType == INVITATION -> ChatPraiseInvitation(this)
            else -> ChatPraiseUnsupported(this)
        }
        chatPraiseBase?.bindData(message)
        return chatPraiseBase?.contentView
    }


    override fun onPause() {
        super.onPause()
        chatPraiseBase?.onPause()
    }
}