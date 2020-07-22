package com.fzm.chat33.main.activity

import android.text.TextUtils
import android.view.View
import android.view.View.OnClickListener
import android.widget.ImageView
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.alibaba.android.arouter.facade.annotation.Route
import com.alibaba.android.arouter.launcher.ARouter
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.fuzamei.common.recycleviewbase.CommonAdapter
import com.fuzamei.common.recycleviewbase.MultiItemTypeAdapter.OnItemClickListener
import com.fuzamei.common.recycleviewbase.RecyclerViewDivider
import com.fuzamei.common.recycleviewbase.ViewHolder
import com.fuzamei.common.utils.ToolUtils
import com.fuzamei.componentservice.app.AppRoute
import com.fuzamei.componentservice.base.DILoadableActivity
import com.fuzamei.componentservice.config.AppConfig
import com.fuzamei.componentservice.ext.findViewModel
import com.fzm.chat33.R
import com.fzm.chat33.core.bean.UidSearchBean
import com.fzm.chat33.core.global.Chat33Const
import com.fzm.chat33.core.global.UserInfo
import com.fzm.chat33.global.AppConst
import com.fzm.chat33.main.mvvm.SearchOnlineViewModel
import com.fzm.chat33.widget.ChatAvatarView
import com.fzm.chat33.widget.ChatSearchView.OnSearchCancelListener
import com.fzm.chat33.widget.ChatSearchView.OnTextChangeListener
import kotlinx.android.synthetic.main.activity_search_friends.*
import kotlinx.android.synthetic.main.layout_search_other.*
import javax.inject.Inject

/**
 * @author zhengjy
 * @since 2018/10/18
 * Description:搜索朋友和群
 */
@Route(path = AppRoute.SEARCH_ONLINE, extras = AppConst.NEED_LOGIN)
class SearchFriendsActivity : DILoadableActivity(), OnClickListener {

    private lateinit var adapter: CommonAdapter<UidSearchBean>
    private val data: MutableList<UidSearchBean> = ArrayList()

    @Inject
    lateinit var provider: ViewModelProvider.Factory
    private lateinit var viewModel: SearchOnlineViewModel

    private var searchKey = ""

    override fun enableSlideBack(): Boolean {
        return true
    }

    override fun getLayoutId(): Int {
        return R.layout.activity_search_friends
    }

    override fun initView() {
        viewModel = findViewModel(provider)
        ly_other.visibility = if (AppConfig.APP_RECOMMENDED_GROUP) View.VISIBLE else View.GONE
        viewModel.searchContact.observe(this, Observer {
            if (it == null) {
                if (searchKey != sv_search.getText()) {
                    statusLayout.showContent()
                    return@Observer
                }
                statusLayout.showError()
            } else {
                if (searchKey != sv_search.getText()) {
                    statusLayout.showContent()
                    return@Observer
                }
                if (it.roomInfo == null && it.userInfo == null) {
                    statusLayout.showEmpty()
                } else {
                    statusLayout.showContent()
                    data.clear()
                    data.add(it)
                    adapter.notifyDataSetChanged()
                }
            }
        })
    }

    private fun showOther(show: Boolean) {
        ly_other.visibility = if (AppConfig.APP_RECOMMENDED_GROUP && show) View.VISIBLE else View.GONE
        rl_list.visibility = if (show) View.GONE else View.VISIBLE
    }

    override fun initData() {
        val account = viewModel.currentUser.value?.account ?: ""
        if (TextUtils.isEmpty(account)) {
            tv_my_account.text = getString(R.string.search_my_account, "")
        } else {
            tv_my_account.text = getString(R.string.search_my_account, ToolUtils.encryptString(account, account.length - 8, account.length - 4))
        }
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.addItemDecoration(RecyclerViewDivider(this, LinearLayoutManager.VERTICAL,
                0.5f, ContextCompat.getColor(this, R.color.chat_color_line)))
        adapter = object : CommonAdapter<UidSearchBean>(this, R.layout.item_search_list, data) {
            override fun convert(holder: ViewHolder, bean: UidSearchBean, position: Int) {
                val name: String
                val avatar: String
                val isIdentified: Boolean
                val iconSrc: Int
                val defaultSrc: Int
                if (bean.type == 1) {
                    holder.setText(R.id.tv_tag, getString(R.string.chat_tips_search_tag1))
                    name = bean.roomInfo.name
                    avatar = bean.roomInfo.avatar
                    isIdentified = bean.roomInfo.isIdentified
                    iconSrc = R.drawable.ic_group_identified
                    defaultSrc = R.mipmap.default_avatar_room
                } else {
                    holder.setText(R.id.tv_tag, getString(R.string.chat_tips_search_tag2))
                    avatar = bean.userInfo.avatar
                    name = bean.userInfo.name
                    isIdentified = bean.userInfo.isIdentified
                    iconSrc = R.drawable.ic_user_identified
                    defaultSrc = R.mipmap.default_avatar_round
                }
                holder.setText(R.id.tv_name, name)
                (holder.getView<View>(R.id.iv_avatar) as ChatAvatarView).setIconRes(if (isIdentified) iconSrc else -1)
                if (!TextUtils.isEmpty(avatar)) {
                    Glide.with(instance).load(avatar)
                            .apply(RequestOptions().placeholder(defaultSrc))
                            .into((holder.getView<View>(R.id.iv_avatar) as ImageView))
                } else {
                    holder.setImageResource(R.id.iv_avatar, defaultSrc)
                }
            }
        }
        adapter.setOnItemClickListener(object : OnItemClickListener {
            override fun onItemClick(view: View?, holder: RecyclerView.ViewHolder?, position: Int) {
                if (data[position].type == 1) {
                    ARouter.getInstance().build(AppRoute.JOIN_ROOM)
                            .withSerializable("roomInfo", data[position].roomInfo)
                            .withInt("sourceType", Chat33Const.FIND_TYPE_SEARCH)
                            .navigation()
                } else {
                    ARouter.getInstance().build(AppRoute.USER_DETAIL)
                            .withString("userId", data[position].userInfo.id)
                            .withInt("sourceType", Chat33Const.FIND_TYPE_SEARCH)
                            .navigation()
                }
            }
            override fun onItemLongClick(view: View?, holder: RecyclerView.ViewHolder?, position: Int): Boolean {
                return false
            }
        })
        recyclerView.adapter = adapter
        sv_search.setOnSearchCancelListener(object : OnSearchCancelListener {
            override fun onSearchCancel() {
                finish()
            }
        })
        sv_search.setOnTextChangeListener(object : OnTextChangeListener {
            override fun onTextChange(s: String) {
                if (s.isEmpty()) {
                    showOther(true)
                } else {
                    showOther(false)
                    statusLayout.showLoading()
                    searchKey = s
                    viewModel.searchByUid(searchKey)
                }
            }
        })
    }

    override fun setEvent() {
        ll_my_account.setOnClickListener(this)
        ll_recommended_group.setOnClickListener(this)
    }

    override fun onClick(v: View) {
        val i = v.id
        if (i == R.id.ll_my_account) {
            ARouter.getInstance().build(AppRoute.QR_CODE)
                    .withString("id", UserInfo.getInstance().id)
                    .withString("content", UserInfo.getInstance().uid)
                    .withString("avatar", UserInfo.getInstance().avatar)
                    .withString("name", UserInfo.getInstance().username)
                    .withInt("channelType", Chat33Const.CHANNEL_FRIEND)
                    .navigation()
        } else if (i == R.id.ll_recommended_group) {
            ARouter.getInstance().build(AppRoute.RECOMMEND_GROUPS).navigation()
        }
    }
}