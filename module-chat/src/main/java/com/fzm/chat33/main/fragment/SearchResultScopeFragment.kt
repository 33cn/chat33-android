package com.fzm.chat33.main.fragment

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.alibaba.android.arouter.launcher.ARouter
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.fuzamei.common.ext.parseTime
import com.fuzamei.common.recycleviewbase.MultiItemTypeAdapter
import com.fuzamei.common.recycleviewbase.RecyclerViewDivider
import com.fuzamei.common.recycleviewbase.ViewHolder
import com.fuzamei.common.utils.KeyboardUtils
import com.fuzamei.componentservice.app.AppRoute
import com.fuzamei.componentservice.base.DILoadableFragment
import com.fuzamei.componentservice.ext.findViewModel
import com.fzm.chat33.R
import com.fzm.chat33.core.bean.SearchResult
import com.fzm.chat33.core.bean.SearchScope
import com.fzm.chat33.core.bean.ChatTarget
import com.fzm.chat33.core.global.Chat33Const
import com.fzm.chat33.main.adapter.HeaderAdapter
import com.fzm.chat33.main.mvvm.SearchLocalViewModel
import com.fzm.chat33.widget.HighlightTextView
import kotlinx.android.synthetic.main.fragment_search_result_scope.*
import java.io.Serializable
import javax.inject.Inject
import kotlin.properties.Delegates

/**
 * @author zhengjy
 * @since 2019/09/17
 * Description:分类搜索结果
 */
class SearchResultScopeFragment : DILoadableFragment() {

    @Inject
    lateinit var provider: ViewModelProvider.Factory
    private lateinit var viewModel: SearchLocalViewModel

    private val result = mutableListOf<SearchResult>()
    private lateinit var mAdapter: HeaderAdapter<SearchResult>

    private var scope by Delegates.notNull<Int>()
    private var initResult: List<SearchResult>? = null

    override fun getLayoutId(): Int {
        return R.layout.fragment_search_result_scope
    }

    override fun initView(view: View?, savedInstanceState: Bundle?) {
        scope = arguments!!.getInt("scope")
        initResult = arguments!!.getSerializable("initResult") as ArrayList<SearchResult>
        viewModel = findViewModel(provider)
        viewModel.searchScopeResult.observe(this, Observer {
            if (it == null) {
                updateSearchResult(null)
                return@Observer
            }
            if (it.keywords == viewModel.searchKey.value) {
                updateSearchResult(it.list)
            }
        })
    }

    override fun initData() {

    }

    override fun setEvent() {
        rv_result_scoped.layoutManager = LinearLayoutManager(activity)
        rv_result_scoped.addItemDecoration(RecyclerViewDivider(activity, LinearLayoutManager.VERTICAL,
                0.5f, ContextCompat.getColor(activity, R.color.chat_divide_light)))
        mAdapter = object : HeaderAdapter<SearchResult>(activity, R.layout.item_local_search_scope_header,
                R.layout.item_local_search_result_scope, result) {
            @SuppressLint("SimpleDateFormat")
            override fun convert(holder: ViewHolder?, result: SearchResult?, position: Int) {
                when (scope) {
                    SearchScope.FRIEND -> {
                        Glide.with(activity).load(result?.avatar)
                                .apply(RequestOptions().placeholder(R.mipmap.default_avatar_round))
                                .into(holder?.getView(R.id.avatar)!!)
                        holder?.setVisible(R.id.desc, !result?.subTitle.isNullOrEmpty())
                        if (!result?.subTitle.isNullOrEmpty()) {
                            holder?.getView<HighlightTextView>(R.id.desc)?.highlightSearchText(
                                    getString(R.string.chat_tips_user_nickname, result?.subTitle), result?.keywords)
                        }
                        holder?.getView<HighlightTextView>(R.id.title)?.highlightSearchText(
                                result?.title, result?.keywords)
                    }
                    SearchScope.GROUP -> {
                        Glide.with(activity).load(result?.avatar)
                                .apply(RequestOptions().placeholder(R.mipmap.default_avatar_room))
                                .into(holder?.getView(R.id.avatar)!!)
                        holder?.setVisible(R.id.desc, false)
                        holder?.getView<HighlightTextView>(R.id.title)?.highlightSearchText(
                                result?.title, result?.keywords)
                    }
                    SearchScope.CHATLOG -> {
                        if (result!!.chatLogs!![0].channelType == Chat33Const.CHANNEL_ROOM) {
                            Glide.with(activity).load(result.avatar)
                                    .apply(RequestOptions().placeholder(R.mipmap.default_avatar_room))
                                    .into(holder?.getView(R.id.avatar)!!)
                        } else {
                            Glide.with(activity).load(result.avatar)
                                    .apply(RequestOptions().placeholder(R.mipmap.default_avatar_round))
                                    .into(holder?.getView(R.id.avatar)!!)
                        }
                        holder?.getView<HighlightTextView>(R.id.title)?.highlightSearchText(
                                result.title, result.keywords)
                        holder?.setVisible(R.id.desc, true)
                        val message = result.chatLogs!![0]
                        if (result.chatLogs!!.size == 1) {
                            holder?.setText(R.id.desc, message.msg.matchOffsets)
                            holder?.setVisible(R.id.time, true)
                            holder?.setText(R.id.time, result.chatLogs!![0].sendTime.parseTime())
                        } else {
                            holder?.setText(R.id.desc, getString(R.string.chat_tips_search_log_count, result.chatLogs!!.size))
                            holder?.setVisible(R.id.time, false)
                        }
                    }
                }
            }

            override fun convertHeader(holder: ViewHolder?, t: SearchResult?, position: Int) {
                when (scope) {
                    SearchScope.FRIEND -> holder?.setText(R.id.tv_scope, getString(R.string.chat_tips_search_type1))
                    SearchScope.GROUP -> holder?.setText(R.id.tv_scope, getString(R.string.chat_tips_search_type2))
                    SearchScope.CHATLOG -> holder?.setText(R.id.tv_scope, getString(R.string.chat_tips_search_type3))
                }
            }
        }
        mAdapter.setOnItemClickListener(object : MultiItemTypeAdapter.OnItemClickListener {
            override fun onItemClick(view: View?, holder: RecyclerView.ViewHolder?, position: Int) {
                when (scope) {
                    SearchScope.FRIEND -> {
                        ARouter.getInstance().build(AppRoute.USER_DETAIL)
                                .withString("userId", result[position].targetId)
                                .navigation()
                    }
                    SearchScope.GROUP -> {
                        ARouter.getInstance().build(AppRoute.CHAT)
                                .withInt("channelType", Chat33Const.CHANNEL_ROOM)
                                .withString("targetName", result[position].title)
                                .withString("targetId", result[position].targetId)
                                .navigation()
                    }
                    SearchScope.CHATLOG -> {
                        val message = result[position].chatLogs!![0]
                        if (result[position].chatLogs!!.size == 1) {
                            KeyboardUtils.hideKeyboard(view)
                            ARouter.getInstance().build(AppRoute.CHAT)
                                    .withInt("channelType", message.channelType)
                                    .withString("targetName", result[position].title)
                                    .withString("targetId", result[position].targetId)
                                    .withString("fromLogId", message.logId)
                                    .navigation()
                        } else {
                            ARouter.getInstance().build(AppRoute.SEARCH_LOCAL_SCOPE)
                                    .withInt("scope", SearchScope.CHATLOG)
                                    .withSerializable("chatTarget", ChatTarget(message.channelType,
                                            result[position].targetId!!))
                                    .withString("keywords", viewModel.searchKey.value)
                                    .withSerializable("chatLogs", result[position].chatLogs as Serializable)
                                    .navigation()
                        }
                    }
                }
            }

            override fun onItemLongClick(view: View?, holder: RecyclerView.ViewHolder?, position: Int): Boolean {
                return true
            }
        })
        rv_result_scoped.adapter = mAdapter
        updateSearchResult(initResult)
    }

    private fun updateSearchResult(list: List<SearchResult>?) {
        mAdapter.clear()
        when {
            list == null -> {
                // 表示未输入关键词
                rv_result_scoped.visibility = View.VISIBLE
                ll_empty.visibility = View.GONE
            }
            list.isEmpty() -> {
                rv_result_scoped.visibility = View.GONE
                ll_empty.visibility = View.VISIBLE
            }
            else -> {
                rv_result_scoped.visibility = View.VISIBLE
                ll_empty.visibility = View.GONE
                mAdapter.addAll(list)
            }
        }
        mAdapter.notifyDataSetChanged()
    }

    companion object {
        @JvmStatic
        fun create(scope: Int, list: ArrayList<SearchResult>?): SearchResultScopeFragment {
            val fragment = SearchResultScopeFragment()
            val bundle = Bundle().apply {
                putInt("scope", scope)
                putSerializable("initResult", list)
            }
            fragment.arguments = bundle
            return fragment
        }
    }
}