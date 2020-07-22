package com.fzm.chat33.main.fragment

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.alibaba.android.arouter.launcher.ARouter
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.fuzamei.common.bus.LiveBus
import com.fuzamei.common.ext.parseTime
import com.fuzamei.common.utils.KeyboardUtils
import com.fuzamei.componentservice.app.AppRoute
import com.fuzamei.componentservice.app.BusEvent
import com.fuzamei.componentservice.base.DILoadableFragment
import com.fuzamei.componentservice.event.ChangeTabEvent
import com.fuzamei.componentservice.ext.findViewModel
import com.fzm.chat33.R
import com.fzm.chat33.core.bean.SearchResult
import com.fzm.chat33.core.bean.SearchScope
import com.fzm.chat33.core.bean.ChatTarget
import com.fzm.chat33.core.global.Chat33Const
import com.fzm.chat33.main.mvvm.SearchLocalViewModel
import com.fzm.chat33.widget.HighlightTextView
import kotlinx.android.synthetic.main.fragment_search_result.*
import java.io.Serializable
import javax.inject.Inject
import kotlin.math.min

/**
 * @author zhengjy
 * @since 2019/09/17
 * Description:聚合搜索结果
 */
class SearchResultFragment : DILoadableFragment() {

    @Inject
    lateinit var provider: ViewModelProvider.Factory
    lateinit var viewModel: SearchLocalViewModel

    private val friends = mutableListOf<SearchResult>()
    private val groups = mutableListOf<SearchResult>()
    private val chatLogs = mutableListOf<SearchResult>()

    private val friendsView = mutableListOf<View>()
    private val groupsView = mutableListOf<View>()
    private val chatLogsView = mutableListOf<View>()

    companion object {
        /**
         * 每种搜索类型最多显示条数
         */
        const val MAX_RESULT_NUM = 3
    }

    override fun getLayoutId(): Int {
        return R.layout.fragment_search_result
    }

    override fun initView(view: View?, savedInstanceState: Bundle?) {
        viewModel = findViewModel(provider)
        handleSearchResult(viewModel.searchResult.value?.list)
    }

    override fun initData() {

    }

    override fun setEvent() {
        viewModel.searchResult.observe(this, Observer {
            if (it.list == null) {
                handleSearchResult(null)
                return@Observer
            }
            if (it.keywords == viewModel.searchKey.value) {
                handleSearchResult(it.list)
            }
        })
    }

    private fun handleSearchResult(list: List<SearchResult>?) {
        when {
            list == null -> {
                // 关键字为空，则不显示
                ll_result.visibility = View.GONE
                ll_empty.visibility = View.GONE
                return
            }
            list.isEmpty() -> {
                // 搜索结果为空，则显示空页面
                ll_result.visibility = View.GONE
                ll_empty.visibility = View.VISIBLE
                return
            }
            else -> {
                ll_result.visibility = View.VISIBLE
                ll_empty.visibility = View.GONE
            }
        }
        val map = list.groupBy { it.searchScope }
        friends.clear()
        groups.clear()
        chatLogs.clear()
        map[SearchScope.FRIEND]?.take(MAX_RESULT_NUM + 1)?.let {
            friends.addAll(it)
        }
        map[SearchScope.GROUP]?.take(MAX_RESULT_NUM + 1)?.let {
            groups.addAll(it)
        }
        map[SearchScope.CHATLOG]?.take(MAX_RESULT_NUM + 1)?.let {
            chatLogs.addAll(it)
        }
        removeViews()
        showSearchResult()
    }

    private fun showSearchResult() {
        // 好友搜索结果
        if (friends.isEmpty()) {
            ll_result_friends.visibility = View.GONE
        } else {
            ll_result_friends.visibility = View.VISIBLE
            tv_friend_more.visibility = if (friends.size > MAX_RESULT_NUM) {
                View.VISIBLE
            } else {
                View.GONE
            }
            tv_friend_more.setOnClickListener {
                ARouter.getInstance().build(AppRoute.SEARCH_LOCAL_SCOPE)
                        .withInt("scope", SearchScope.FRIEND)
                        .withString("keywords", viewModel.searchKey.value)
                        .withSerializable("result", viewModel.getSearchResultByScope(SearchScope.FRIEND) as Serializable)
                        .navigation()
            }
            for (i in 0 until min(friends.size, MAX_RESULT_NUM)) {
                val view = LayoutInflater.from(activity)
                        .inflate(R.layout.item_local_search_result, null)
                bindView(view, friends[i])
                friendsView.add(view)
                ll_result_friends.addView(view)
            }
        }
        // 群聊搜索结果
        if (groups.isEmpty()) {
            ll_result_groups.visibility = View.GONE
        } else {
            ll_result_groups.visibility = View.VISIBLE
            tv_group_more.visibility = if (groups.size > MAX_RESULT_NUM) {
                View.VISIBLE
            } else {
                View.GONE
            }
            tv_group_more.setOnClickListener {
                ARouter.getInstance().build(AppRoute.SEARCH_LOCAL_SCOPE)
                        .withInt("scope", SearchScope.GROUP)
                        .withString("keywords", viewModel.searchKey.value)
                        .withSerializable("result", viewModel.getSearchResultByScope(SearchScope.GROUP) as Serializable)
                        .navigation()
            }
            for (i in 0 until min(groups.size, MAX_RESULT_NUM)) {
                val view = LayoutInflater.from(activity)
                        .inflate(R.layout.item_local_search_result, null)
                bindView(view, groups[i])
                groupsView.add(view)
                ll_result_groups.addView(view)
            }
        }
        // 聊天记录搜索结果
        if (chatLogs.isEmpty()) {
            ll_result_logs.visibility = View.GONE
        } else {
            ll_result_logs.visibility = View.VISIBLE
            tv_log_more.visibility = if (chatLogs.size > MAX_RESULT_NUM) {
                View.VISIBLE
            } else {
                View.GONE
            }
            tv_log_more.setOnClickListener {
                ARouter.getInstance().build(AppRoute.SEARCH_LOCAL_SCOPE)
                        .withInt("scope", SearchScope.CHATLOG)
                        .withString("keywords", viewModel.searchKey.value)
                        .withSerializable("result", viewModel.getSearchResultByScope(SearchScope.CHATLOG) as Serializable)
                        .navigation()
            }
            for (i in 0 until min(chatLogs.size, MAX_RESULT_NUM)) {
                val view = LayoutInflater.from(activity)
                        .inflate(R.layout.item_local_search_result, null)
                bindView(view, chatLogs[i])
                chatLogsView.add(view)
                ll_result_logs.addView(view)
            }
        }
    }

    private fun removeViews() {
        friendsView.forEach {
            ll_result_friends.removeView(it)
        }
        friendsView.clear()
        groupsView.forEach {
            ll_result_groups.removeView(it)
        }
        groupsView.clear()
        chatLogsView.forEach {
            ll_result_logs.removeView(it)
        }
        chatLogsView.clear()
    }

    @SuppressLint("SimpleDateFormat")
    private fun bindView(view: View, result: SearchResult) {
        val avatar = view.findViewById<ImageView>(R.id.avatar)
        val title = view.findViewById<HighlightTextView>(R.id.title)
        val desc = view.findViewById<HighlightTextView>(R.id.desc)
        val time = view.findViewById<TextView>(R.id.time)
        when (result.searchScope) {
            SearchScope.FRIEND -> {
                Glide.with(this).load(result.avatar)
                        .apply(RequestOptions().placeholder(R.mipmap.default_avatar_round))
                        .into(avatar)
                if (result.subTitle.isNullOrEmpty()) {
                    desc.visibility = View.GONE
                } else {
                    desc.visibility = View.VISIBLE
                    desc.highlightSearchText(getString(R.string.chat_tips_user_nickname, result.subTitle), result.keywords)
                }
                title.highlightSearchText(result.title, result.keywords)
                view.setOnClickListener {
                    ARouter.getInstance().build(AppRoute.CHAT)
                            .withInt("channelType", Chat33Const.CHANNEL_FRIEND)
                            .withString("targetId", result.targetId)
                            .navigation()
                    LiveBus.of(BusEvent::class.java).changeTab().setValue(ChangeTabEvent(0, 1))
                    activity.finish()
                }
            }
            SearchScope.GROUP -> {
                Glide.with(this).load(result.avatar)
                        .apply(RequestOptions().placeholder(R.mipmap.default_avatar_room))
                        .into(avatar)
                desc.visibility = View.GONE
                title.highlightSearchText(result.title, result.keywords)
                view.setOnClickListener {
                    ARouter.getInstance().build(AppRoute.CHAT)
                            .withInt("channelType", Chat33Const.CHANNEL_ROOM)
                            .withString("targetName", result.title)
                            .withString("targetId", result.targetId)
                            .navigation()
                    LiveBus.of(BusEvent::class.java).changeTab().setValue(ChangeTabEvent(0, 0))
                    activity.finish()
                }
            }
            SearchScope.CHATLOG -> {
                if (result.chatLogs!![0].channelType == Chat33Const.CHANNEL_ROOM) {
                    Glide.with(this).load(result.avatar)
                            .apply(RequestOptions().placeholder(R.mipmap.default_avatar_room))
                            .into(avatar)
                } else {
                    Glide.with(this).load(result.avatar)
                            .apply(RequestOptions().placeholder(R.mipmap.default_avatar_round))
                            .into(avatar)
                }
                title.highlightSearchText(result.title, result.keywords)
                desc.visibility = View.VISIBLE
                val message = result.chatLogs!![0]
                if (result.chatLogs!!.size == 1) {
                    desc.text = message.msg.matchOffsets
                    time.visibility = View.VISIBLE
                    time.text = result.chatLogs!![0].sendTime.parseTime()
                    view.setOnClickListener {
                        KeyboardUtils.hideKeyboard(view)
                        ARouter.getInstance().build(AppRoute.CHAT)
                                .withInt("channelType", message.channelType)
                                .withString("targetName", result.title)
                                .withString("targetId", result.targetId)
                                .withString("fromLogId", message.logId)
                                .navigation()
                    }
                } else {
                    desc.text = getString(R.string.chat_tips_search_log_count, result.chatLogs!!.size)
                    time.visibility = View.GONE
                    view.setOnClickListener {
                        ARouter.getInstance().build(AppRoute.SEARCH_LOCAL_SCOPE)
                                .withInt("scope", SearchScope.CHATLOG)
                                .withSerializable("chatTarget", ChatTarget(message.channelType, result.targetId!!))
                                .withString("keywords", viewModel.searchKey.value)
                                .withSerializable("chatLogs", result.chatLogs as Serializable)
                                .navigation()
                    }
                }
            }
        }
    }
}