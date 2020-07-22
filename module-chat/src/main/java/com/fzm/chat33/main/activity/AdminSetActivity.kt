package com.fzm.chat33.main.activity

import android.content.Intent
import android.text.TextUtils
import android.view.View
import android.widget.ImageView
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer

import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager

import com.alibaba.android.arouter.facade.annotation.Autowired
import com.alibaba.android.arouter.facade.annotation.Route
import com.alibaba.android.arouter.launcher.ARouter
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.fuzamei.common.recycleviewbase.CommonAdapter
import com.fuzamei.common.recycleviewbase.RecyclerViewDivider
import com.fuzamei.common.recycleviewbase.ViewHolder
import com.fuzamei.common.utils.ShowUtils
import com.fuzamei.componentservice.app.AppRoute
import com.fuzamei.componentservice.app.Loading
import com.fzm.chat33.core.db.bean.RoomUserBean
import com.fzm.chat33.core.db.bean.RoomInfoBean
import com.fuzamei.componentservice.base.DILoadableActivity
import com.fuzamei.componentservice.ext.findViewModel
import com.fzm.chat33.R
import com.fzm.chat33.core.global.Chat33Const.LEVEL_ADMIN
import com.fzm.chat33.core.global.Chat33Const.LEVEL_OWNER
import com.fzm.chat33.main.mvvm.GroupViewModel
import com.fzm.chat33.widget.ChatAvatarView
import kotlinx.android.synthetic.main.activity_admin_set.*
import java.util.ArrayList

import javax.inject.Inject

/**
 * 创建日期：2018/10/30
 * 描述:群主管理员设置界面
 * 作者:zhengjy
 */
@Route(path = AppRoute.ADMIN_SET)
class AdminSetActivity : DILoadableActivity() {

    @JvmField
    @Autowired
    var roomInfo: RoomInfoBean? = null

    @Inject
    lateinit var provider: ViewModelProvider.Factory
    lateinit var viewModel: GroupViewModel

    private var adapter: CommonAdapter<RoomUserBean>? = null
    private val data = ArrayList<RoomUserBean>()

    private var admin: RoomUserBean? = null
    private lateinit var owner: RoomUserBean

    override fun enableSlideBack(): Boolean {
        return true
    }

    override fun getLayoutId(): Int {
        return R.layout.activity_admin_set
    }

    override fun initView() {
        ARouter.getInstance().inject(this)
        viewModel = findViewModel(provider)
        ctb_title.setLeftListener { finish() }
        ctb_title.setMiddleText(getString(R.string.chat_title_admin_set))

        viewModel.loading.observe(this, Observer<Loading> { this.setupLoading(it) })
        viewModel.memberLevel.observe(this, Observer { level ->
            if (level != null) {
                if (level == 1) {
                    data.remove(admin)
                    adapter?.notifyDataSetChanged()
                    tv_admin_num.text = getString(R.string.chat_tips_admin_num, data.size)
                } else if (level == 2) {
                    ShowUtils.showToastNormal(instance, getString(R.string.chat_tips_add_admin))
                    if (admin != null) {
                        addAdmin(admin!!)
                    }
                }
            }
        })
    }

    override fun initData() {
        for (user in roomInfo!!.users) {
            if (user.memberLevel == LEVEL_ADMIN) {
                data.add(user)
            }
            if (user.memberLevel == LEVEL_OWNER) {
                owner = user
            }
        }
        val options = RequestOptions().placeholder(R.mipmap.default_avatar_round)
        if (TextUtils.isEmpty(owner.avatar)) {
            iv_owner_avatar.setImageResource(R.mipmap.default_avatar_round)
        } else {
            Glide.with(instance).load(owner.avatar).apply(options).into(iv_owner_avatar)
        }
        iv_owner_avatar.setIconRes(if (owner.isIdentified) R.drawable.ic_user_identified else -1)
        tv_admin_num.text = instance.getString(R.string.chat_tips_admin_num, data.size)
        tv_owner_name.text = owner.displayName
        rv_member.addItemDecoration(RecyclerViewDivider(instance, LinearLayoutManager.VERTICAL,
                0.5f, ContextCompat.getColor(this, R.color.chat_color_line)))
        rv_member.layoutManager = LinearLayoutManager(instance)
        adapter = object : CommonAdapter<RoomUserBean>(instance, R.layout.adapter_admin_set, data) {
            override fun convert(holder: ViewHolder, bean: RoomUserBean, position: Int) {
                tv_admin_num.text = mContext.getString(R.string.chat_tips_admin_num, data.size)
                if (data.size == 10) {
                    tv_add_admin.visibility = View.GONE
                } else {
                    tv_add_admin.visibility = View.VISIBLE
                }
                holder.setText(R.id.tv_name, bean.displayName)
                holder.setVisible(R.id.tv_identification, false)
                holder.setOnClickListener(R.id.iv_delete_admin) {
                    admin = bean
                    viewModel.setRoomUserLevel(roomInfo!!.id, bean.id, 1)
                }
                if (TextUtils.isEmpty(bean.avatar)) {
                    holder.setImageResource(R.id.iv_avatar, R.mipmap.default_avatar_round)
                } else {
                    Glide.with(mContext).load(bean.avatar).apply(options).into(holder.getView<View>(R.id.iv_avatar) as ImageView)
                }
                (holder.getView(R.id.iv_avatar) as ChatAvatarView).setIconRes(if (bean.isIdentified) R.drawable.ic_user_identified else -1)
            }
        }
        rv_member.adapter = adapter
    }

    override fun setEvent() {
        tv_add_admin.setOnClickListener {
            ARouter.getInstance().build(AppRoute.SELECT_GROUP_MEMBER)
                    .withString("action", "add_admin")
                    .withSerializable("roomInfo", roomInfo)
                    .withInt("memberLevel", 1)
                    .navigation(instance, CODE_ADD_ADMIN) }
    }

    private fun addAdmin(bean: RoomUserBean) {
        data.add(bean)
        adapter?.notifyItemInserted(data.size)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK) {
            if (requestCode == CODE_ADD_ADMIN) {
                if (data?.getSerializableExtra("result") == null) {
                    return
                }
                admin = data.getSerializableExtra("result") as RoomUserBean
                viewModel.setRoomUserLevel(roomInfo!!.id, admin!!.id, 2)
            }
        }
    }

    companion object {
        const val CODE_ADD_ADMIN = 1
    }
}
