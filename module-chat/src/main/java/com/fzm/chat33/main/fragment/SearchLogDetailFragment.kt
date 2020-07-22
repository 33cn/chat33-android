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
import com.fzm.chat33.core.bean.ChatTarget
import com.fzm.chat33.core.db.bean.ChatMessage
import com.fzm.chat33.core.db.bean.FriendBean
import com.fzm.chat33.core.db.bean.InfoCacheBean
import com.fzm.chat33.core.db.bean.RoomListBean
import com.fzm.chat33.core.global.Chat33Const
import com.fzm.chat33.core.provider.*
import com.fzm.chat33.main.adapter.HeaderAdapter
import com.fzm.chat33.main.mvvm.SearchLocalViewModel
import com.fzm.chat33.widget.ChatAvatarView
import com.fzm.chat33.widget.HighlightTextView
import kotlinx.android.synthetic.main.fragment_search_log_detail.*
import javax.inject.Inject
import kotlin.properties.Delegates

/**
 * @author zhengjy
 * @since 2019/09/17
 * Description:特定用户聊天记录搜索结果
 */
class SearchLogDetailFragment : DILoadableFragment() {

    var channelType by Delegates.notNull<Int>()
    lateinit var targetId: String

    private val chatLogs = mutableListOf<ChatMessage>()
    private lateinit var mAdapter: HeaderAdapter<ChatMessage>

    private var initResult: List<ChatMessage>? = null

    @Inject
    lateinit var provider: ViewModelProvider.Factory
    private lateinit var viewModel: SearchLocalViewModel

    override fun getLayoutId(): Int {
        return R.layout.fragment_search_log_detail
    }

    override fun initView(view: View?, savedInstanceState: Bundle?) {
        initResult = arguments!!.getSerializable("chatLogs") as ArrayList<ChatMessage>?
        val target: ChatTarget = arguments!!.getSerializable("target") as ChatTarget
        channelType = target.channelType
        targetId = target.targetId
        viewModel = findViewModel(provider)
        viewModel.searchChatLogs.observe(this, Observer {
            if (it == null) {
                updateSearchResult(null)
                return@Observer
            }
            if (it.keywords == viewModel.searchKey.value) {
                updateSearchResult(it.chatLogs)
            }
        })
    }

    override fun initData() {

    }

    override fun setEvent() {
        rv_result_logs.layoutManager = LinearLayoutManager(activity)
        rv_result_logs.addItemDecoration(RecyclerViewDivider(activity, LinearLayoutManager.VERTICAL,
                0.5f, ContextCompat.getColor(activity, R.color.chat_divide_light)))
        mAdapter = object : HeaderAdapter<ChatMessage>(activity, R.layout.item_local_search_scope_header,
                R.layout.item_local_search_result_scope, chatLogs) {
            @SuppressLint("SimpleDateFormat")
            override fun convert(holder: ViewHolder?, message: ChatMessage?, position: Int) {
                holder?.setText(R.id.desc, message?.msg?.matchOffsets)
                holder?.setVisible(R.id.time, true)
                holder?.setText(R.id.time, message?.sendTime?.parseTime())
                if (message?.isSentType == true) {
                    Glide.with(activity).load(viewModel.currentUser.value?.avatar)
                            .apply(RequestOptions().placeholder(R.mipmap.default_avatar_round))
                            .into(holder?.getView(R.id.avatar)!!)
                    holder?.getView<HighlightTextView>(R.id.title)?.highlightSearchText(
                            viewModel.currentUser.value?.username, viewModel.searchKey.value)
                } else {
                    holder?.getView<View>(R.id.title)?.setTag(R.id.title, message?.logId)
                    if (channelType == Chat33Const.CHANNEL_ROOM) {
                        InfoProvider.getInstance().strategy(ChatInfoStrategy(message)).load(object : OnFindInfoListener<InfoCacheBean> {
                            override fun onFindInfo(data: InfoCacheBean?, place: Int) {
                                val title = holder!!.getView<View>(R.id.title)
                                if (message!!.logId != title.getTag(R.id.title)) {
                                    return
                                }
                                Glide.with(activity).load(data?.avatar)
                                        .apply(RequestOptions().placeholder(R.mipmap.default_avatar_round))
                                        .into(holder.getView(R.id.avatar)!!)
                                (holder.getView<View>(R.id.avatar) as ChatAvatarView).setIconRes(
                                        if (data?.isIdentified == true) R.drawable.ic_user_identified else -1)
                                holder.getView<HighlightTextView>(R.id.title)?.highlightSearchText(
                                        data?.displayName, viewModel.searchKey.value)
                            }

                            override fun onNotExist() {
                                notFindInfo(message, holder)
                            }
                        })
                    } else {
                        InfoProvider.getInstance().strategy(UserInfoStrategy(targetId)).load(object : OnFindInfoListener<FriendBean> {
                            override fun onFindInfo(data: FriendBean?, place: Int) {
                                findInfo(data, message, holder)
                            }

                            override fun onNotExist() {
                                notFindInfo(message, holder)
                            }
                        })
                    }
                }
            }

            private fun findInfo(data: FriendBean?, message: ChatMessage?, holder: ViewHolder?) {
                val title = holder!!.getView<View>(R.id.title)
                if (message!!.logId != title.getTag(R.id.title)) {
                    return
                }
                Glide.with(activity).load(data?.avatar)
                        .apply(RequestOptions().placeholder(R.mipmap.default_avatar_round))
                        .into(holder.getView(R.id.avatar)!!)
                (holder.getView<View>(R.id.avatar) as ChatAvatarView).setIconRes(
                        if (data?.isIdentified == true) R.drawable.ic_user_identified else -1)
                holder.getView<HighlightTextView>(R.id.title)?.highlightSearchText(
                        data?.displayName, viewModel.searchKey.value)
            }

            private fun notFindInfo(message: ChatMessage?, holder: ViewHolder?) {
                val title = holder!!.getView<View>(R.id.title)
                if (message!!.logId != title.getTag(R.id.title)) {
                    return
                }
                holder.setImageResource(R.id.avatar, R.mipmap.default_avatar_round)
                holder.setText(R.id.title, getString(R.string.chat_tips_no_name))
            }

            override fun convertHeader(holder: ViewHolder?, t: ChatMessage?, position: Int) {
                if (channelType == Chat33Const.CHANNEL_ROOM) {
                    InfoProvider.getInstance().strategy(RoomInfoStrategy(targetId)).load(object :
                            OnFindInfoListener<RoomListBean> {
                        override fun onFindInfo(data: RoomListBean?, place: Int) {
                            holder?.setText(R.id.tv_scope, getString(R.string.chat_tips_search_log_target, data?.displayName))
                        }

                        override fun onNotExist() {
                            holder?.setText(R.id.tv_scope, getString(R.string.chat_tips_search_log_target, getString(R.string.chat_tips_no_name)))
                        }
                    })
                } else {
                    InfoProvider.getInstance().strategy(UserInfoStrategy(targetId)).load(object :
                            OnFindInfoListener<FriendBean> {
                        override fun onFindInfo(data: FriendBean?, place: Int) {
                            holder?.setText(R.id.tv_scope, getString(R.string.chat_tips_search_log_target, data?.displayName))
                        }

                        override fun onNotExist() {
                            holder?.setText(R.id.tv_scope, getString(R.string.chat_tips_search_log_target, getString(R.string.chat_tips_no_name)))
                        }
                    })
                }
            }
        }
        mAdapter.setOnItemClickListener(object : MultiItemTypeAdapter.OnItemClickListener{
            override fun onItemClick(view: View?, holder: RecyclerView.ViewHolder?, position: Int) {
                KeyboardUtils.hideKeyboard(view)
                ARouter.getInstance().build(AppRoute.CHAT)
                        .withInt("channelType", channelType)
                        .withString("targetId", targetId)
                        .withString("fromLogId", chatLogs[position].logId)
                        .navigation()
            }

            override fun onItemLongClick(view: View?, holder: RecyclerView.ViewHolder?, position: Int): Boolean {
                return true
            }

        })
        rv_result_logs.adapter = mAdapter
        updateSearchResult(initResult)
    }

    private fun updateSearchResult(list: List<ChatMessage>?) {
        mAdapter.clear()
        when {
            list == null -> {
                // 表示未输入关键词
                rv_result_logs.visibility = View.VISIBLE
                ll_empty.visibility = View.GONE
            }
            list.isEmpty() -> {
                rv_result_logs.visibility = View.GONE
                ll_empty.visibility = View.VISIBLE
            }
            else -> {
                rv_result_logs.visibility = View.VISIBLE
                ll_empty.visibility = View.GONE
                mAdapter.addAll(list)
            }
        }
        mAdapter.notifyDataSetChanged()
    }

    companion object {
        @JvmStatic
        fun create(target: ChatTarget?, chatLogs: ArrayList<ChatMessage>?): SearchLogDetailFragment {
            val fragment = SearchLogDetailFragment()
            val bundle = Bundle().apply {
                putSerializable("target", target)
                putSerializable("chatLogs", chatLogs)
            }
            fragment.arguments = bundle
            return fragment
        }
    }
}