package com.fzm.chat33.main.activity

import android.text.TextUtils
import android.view.View
import android.widget.CompoundButton
import android.widget.ImageView
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.alibaba.android.arouter.facade.annotation.Autowired
import com.alibaba.android.arouter.facade.annotation.Route
import com.alibaba.android.arouter.launcher.ARouter
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.fuzamei.common.recycleviewbase.CommonAdapter
import com.fuzamei.common.recycleviewbase.MultiItemTypeAdapter
import com.fuzamei.common.recycleviewbase.ViewHolder
import com.fuzamei.common.utils.KeyboardUtils
import com.fuzamei.common.utils.ShowUtils
import com.fuzamei.componentservice.app.AppRoute
import com.fuzamei.componentservice.base.DILoadableActivity
import com.fuzamei.componentservice.config.AppConfig
import com.fuzamei.componentservice.ext.findViewModel
import com.fzm.chat33.R
import com.fzm.chat33.core.bean.param.CreateGroupParam
import com.fzm.chat33.core.bean.param.EditRoomUserParam
import com.fzm.chat33.core.db.bean.FriendBean
import com.fzm.chat33.global.AppConst
import com.fzm.chat33.main.fragment.BookFriendFragment
import com.fzm.chat33.main.mvvm.BookFriendViewModel
import com.fzm.chat33.widget.ChatAvatarView
import com.fzm.chat33.widget.ChatSearchView
import com.fuzamei.componentservice.widget.dialog.EasyDialog
import kotlinx.android.synthetic.main.activity_create_group.*
import javax.inject.Inject

@Route(path = AppRoute.CREATE_GROUP, extras = AppConst.NEED_LOGIN)
class CreateGroupActivity : DILoadableActivity(), View.OnClickListener {

    @Inject
    lateinit var provider: ViewModelProvider.Factory
    private lateinit var viewModel: BookFriendViewModel
    @JvmField
    @Autowired
    var roomId: String? = null
    @JvmField
    @Autowired
    var users: ArrayList<String>? = null
    @JvmField
    @Autowired
    var preCheckedUsers: ArrayList<String>? = null
    private lateinit var bookFriendFragment: BookFriendFragment
    private lateinit var adapter: CommonAdapter<FriendBean>
    private val data = ArrayList<FriendBean>()
    private val memberList = ArrayList<String>()
    private val cacheMap = HashMap<FriendBean, View>()

    private val checkChangeListener = ContactSelectActivity.OnCheckChangedListener { view, checked, contact ->
        val bean = contact as FriendBean
        if (users!!.contains(bean.id)) {
            return@OnCheckChangedListener
        }
        if (checked) {
            memberList.add(bean.id)
            cacheMap[bean] = view
            if (!data.contains(bean)) {
                data.add(bean)
                adapter.notifyItemInserted(data.size - 1)
            }
            tv_create.setText(if (roomId.isNullOrEmpty()) R.string.chat_action_group_create else R.string.chat_action_group_add_member)
        } else {
            memberList.remove(bean.id)
            cacheMap.remove(bean)
            val index = data.indexOf(bean)
            data.remove(bean)
            if (index != -1) {
                adapter.notifyItemRangeRemoved(index, 1)
            }
            if (memberList.size == 0) {
                tv_create.setText(R.string.chat_action_group_skip)
            }
        }
        rv_select.scrollToPosition(data.size - 1)
        tv_member_count.text = data.size.toString()
    }

    override fun getLayoutId(): Int {
        return R.layout.activity_create_group
    }

    override fun initView() {
        ARouter.getInstance().inject(this)
        viewModel = findViewModel(provider)
        tv_title.setText(if (roomId.isNullOrEmpty()) R.string.chat_title_create_group1 else R.string.chat_title_create_group2)
        tv_create.setText(if (roomId.isNullOrEmpty()) R.string.chat_action_group_skip else R.string.chat_action_group_add_member)
        if (preCheckedUsers != null) {
            tv_member_count.text = preCheckedUsers!!.size.toString()
        }
        rv_select.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        adapter = object : CommonAdapter<FriendBean>(this, R.layout.adapter_create_group_select, data) {
            override fun convert(holder: ViewHolder, friendBean: FriendBean?, position: Int) {
                if (friendBean == null) {
                    holder.setImageResource(R.id.head, R.mipmap.default_avatar_round)
                } else {
                    if (TextUtils.isEmpty(friendBean.avatar)) {
                        holder.setImageResource(R.id.head, R.mipmap.default_avatar_round)
                    } else {
                        Glide.with(mContext).load(friendBean.avatar)
                                .apply(RequestOptions().placeholder(R.mipmap.default_avatar_round))
                                .into(holder.getView(R.id.head) as ImageView)
                    }
                    (holder.getView(R.id.head) as ChatAvatarView)
                            .setIconRes(if (friendBean.isIdentified) R.drawable.ic_user_identified else -1)
                }
            }
        }
        rv_select.adapter = adapter

        viewModel.loading.observe(this, Observer { setupLoading(it) })
        viewModel.createResult.observe(this, Observer {
            ShowUtils.showToastNormal(instance, getString(R.string.chat_tips_create_group2))
            finish()
        })
        viewModel.inviteResult.observe(this, Observer {
            ShowUtils.showToastNormal(instance, getString(R.string.chat_tips_create_group5))
            finish()
        })
        viewModel.roomUsers.observe(this, Observer {
            users = ArrayList()
            for (bean in it.userList) {
                users?.add(bean.id)
            }
            setupViews()
        })
    }

    override fun initData() {
        if (!users.isNullOrEmpty()) {
            setupViews()
        } else if (!roomId.isNullOrEmpty()) {
            viewModel.getRoomUsers(roomId!!)
        } else {
            users = ArrayList()
            setupViews()
        }
    }

    override fun setEvent() {
        iv_return.setOnClickListener(this)
        tv_create.setOnClickListener(this)
        iv_search.setOnClickListener(this)
        adapter.setOnItemClickListener(object : MultiItemTypeAdapter.OnItemClickListener {
            override fun onItemClick(view: View, holder: RecyclerView.ViewHolder, position: Int) {
                val button = cacheMap[data[position]] as CompoundButton?
                if (button != null && button.isChecked) {
                    button.isChecked = false
                } else {
                    // 一些滑出及界面的CheckBox状态可能为unchecked，所以需要手动删除
                    cacheMap.remove(data[position])
                    bookFriendFragment.removeCheck(data[position].id)
                    memberList.remove(data[position].id)
                    if (memberList.size == 0) {
                        tv_create.setText(R.string.chat_action_group_skip)
                    }
                    data.removeAt(position)
                    adapter.notifyItemRangeRemoved(position, 1)
                    rv_select.scrollToPosition(data.size - 1)
                    tv_member_count.text = data.size.toString()
                }
            }

            override fun onItemLongClick(view: View, holder: RecyclerView.ViewHolder, position: Int): Boolean {
                return false
            }
        })
        chat_search.setOnSearchCancelListener(object : ChatSearchView.OnSearchCancelListener {
            override fun onSearchCancel() {
                KeyboardUtils.hideKeyboard(chat_search.getFocusView())
                chat_search.reduce()
            }
        })
        chat_search.setOnTextChangeListener(object : ChatSearchView.OnTextChangeListener {
            override fun onTextChange(s: String) {
                bookFriendFragment.searchKeyword(s)
            }
        })
    }

    private fun setupViews() {
        if (!preCheckedUsers.isNullOrEmpty()) {
            memberList.addAll(preCheckedUsers!!)
            for (id in preCheckedUsers!!) {
                val friendBean = viewModel.getLocalFriendById(id)
                if (friendBean != null)
                    data.add(friendBean)
            }
            adapter.notifyDataSetChanged()
            tv_create.setText(R.string.chat_action_group_create)
        }
        bookFriendFragment = BookFriendFragment()
        bookFriendFragment.selectable = true
        bookFriendFragment.preCheckedUsers = preCheckedUsers
        bookFriendFragment.users = users
        bookFriendFragment.checkChangedListener = checkChangeListener
        addFragment(R.id.book_layout, bookFriendFragment)
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.iv_return -> finish()
            R.id.tv_create -> createOrInviteGroup()
            R.id.iv_search -> {
                chat_search.expand()
                chat_search.postDelayed({ KeyboardUtils.showKeyboard(chat_search.getFocusView()) }, 100)
            }
        }
    }

    private fun createOrInviteGroup() {
        var cantJoin = 0
        val room = viewModel.getLocalRoomById(roomId)
        // 开启加密的App，如果是新建群则为加密群，旧的群是否加密要看其本身的设置
        if (AppConfig.APP_ENCRYPT && (room == null || room.encrypt == 1)) {
            for (friendBean in cacheMap.keys) {
                if (TextUtils.isEmpty(friendBean.publicKey)) {
                    cantJoin++
                }
            }
        }
        if (roomId.isNullOrEmpty()) {
            if (cantJoin == 0) {
                createGroup()
            } else {
                EasyDialog.Builder()
                        .setHeaderTitle(getString(R.string.chat_tips_tips))
                        .setContent(getString(R.string.chat_tips_create_group1, cantJoin))
                        .setBottomLeftText(getString(R.string.chat_action_cancel))
                        .setBottomRightText(getString(R.string.chat_action_continue_group_create))
                        .setBottomRightClickListener { dialog ->
                            dialog.dismiss()
                            createGroup()
                        }.create(instance).show()
            }
        } else {
            if (memberList.size == 0) {
                ShowUtils.showToastNormal(instance, getString(R.string.chat_tips_create_group3))
                return
            }
            if (cantJoin == 0) {
                viewModel.inviteUsers(EditRoomUserParam(roomId, memberList))
            } else {
                EasyDialog.Builder()
                        .setHeaderTitle(getString(R.string.chat_tips_tips))
                        .setContent(getString(R.string.chat_tips_create_group4, cantJoin))
                        .setBottomLeftText(getString(R.string.chat_action_cancel))
                        .setBottomRightText(getString(R.string.chat_action_continue_group_invite))
                        .setBottomRightClickListener { dialog ->
                            dialog.dismiss()
                            viewModel.inviteUsers(EditRoomUserParam(roomId, memberList))
                        }.create(instance).show()
            }
        }
    }

    private fun createGroup() {
        val encrypt = if (AppConfig.APP_ENCRYPT) 1 else 2
        viewModel.createGroup(CreateGroupParam("", memberList, encrypt))
    }

    override fun onBackPressed() {
        if (!chat_search.onBackPressed()) {
            super.onBackPressed()
        }
    }

    override fun enableSlideBack(): Boolean {
        return true
    }
}