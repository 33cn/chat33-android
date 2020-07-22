package com.fzm.chat33.main.activity

import android.view.View
import android.widget.CheckBox
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.alibaba.android.arouter.facade.annotation.Route
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.fuzamei.common.recycleviewbase.CommonAdapter
import com.fuzamei.common.recycleviewbase.MultiItemTypeAdapter
import com.fuzamei.common.recycleviewbase.ViewHolder
import com.fuzamei.common.utils.ShowUtils
import com.fuzamei.componentservice.app.AppRoute
import com.fuzamei.componentservice.base.DILoadableActivity
import com.fuzamei.componentservice.ext.findViewModel
import com.fzm.chat33.R
import com.fzm.chat33.core.bean.RecommendGroup
import com.fzm.chat33.core.manager.GroupKeyManager
import com.fzm.chat33.main.mvvm.RecommendedGroupViewModel
import kotlinx.android.synthetic.main.activity_recommended_group.*
import javax.inject.Inject

/**
 * @author zhengjy
 * @since 2019/07/01
 * Description:
 */
@Route(path = AppRoute.RECOMMEND_GROUPS)
class RecommendedGroupActivity : DILoadableActivity() {

    /**
     * 推荐群批次，从1开始
     */
    private var times = 1
    private var mGroups = mutableListOf<RecommendGroup>()
    private var mAdapter: CommonAdapter<RecommendGroup>? = null

    @Inject
    lateinit var provider: ViewModelProvider.Factory
    lateinit var viewModel: RecommendedGroupViewModel

    override fun getLayoutId(): Int {
        return R.layout.activity_recommended_group
    }

    override fun initView() {
        viewModel = findViewModel(provider)
        ctb_title.tv_title_middle.text = getString(R.string.chat_guess_like)
        ctb_title.setLeftVisible(false)
        ctb_title.setRightText(getString(R.string.chat_action_group_skip))
        ctb_title.setRightListener { finish() }
        viewModel.loading.observe(this, Observer { setupLoading(it) })
        viewModel.recommendGroups.observe(this, Observer {
            times = it?.nextTimes ?: times
            mGroups.clear()
            mGroups.addAll(it?.roomList ?: arrayListOf())
            for (group in mGroups) {
                // 默认全部选中
                group.selected = true
            }
            if (mGroups.size == 0) {
                ShowUtils.showToastNormal(instance, R.string.chat_group_no_suggest)
            }
            mAdapter?.notifyDataSetChanged()
        })

        viewModel.batchJoinRoomApply.observe(this, Observer {
            for (group in mGroups) {
                if (group.encrypt == 1 && it?.list?.contains(group.id) == true) {
                    // 加密群通知更新群密钥
                    GroupKeyManager.notifyGroupEncryptKey(group.id)
                }
            }
            ShowUtils.showToastNormal(instance, R.string.chat_send_apply_success)
            finish()
        })
    }

    override fun initData() {
        mAdapter = object : CommonAdapter<RecommendGroup>(this, R.layout.adapter_recommended_group, mGroups) {
            override fun convert(holder: ViewHolder?, t: RecommendGroup?, position: Int) {
                holder!!.setText(R.id.tv_name, t?.name)
                Glide.with(instance).load(t?.avatar)
                        .apply(RequestOptions().placeholder(R.mipmap.default_avatar_room))
                        .into(holder.getView(R.id.iv_avatar))
                holder.setTag(R.id.cb_select, position)
                val checkBox: CheckBox? = holder.getView(R.id.cb_select)
                checkBox?.setOnCheckedChangeListener { view, isChecked ->
                    if (view.tag == position) {
                        t?.selected = isChecked
                    }
                }
            }
        }
        mAdapter?.setOnItemClickListener(object : MultiItemTypeAdapter.OnItemClickListener {
            override fun onItemClick(view: View, holder: RecyclerView.ViewHolder, position: Int) {
                holder.itemView.findViewById<View>(R.id.cb_select).performClick()
            }

            override fun onItemLongClick(view: View, holder: RecyclerView.ViewHolder, position: Int): Boolean {
                return false
            }
        })
        rv_recommended.layoutManager = LinearLayoutManager(this)
        rv_recommended.adapter = mAdapter
        getRecommendedGroups()
    }

    private fun getRecommendedGroups() {
        viewModel.recommendGroups(times)
    }

    override fun setEvent() {
        tv_join.setOnClickListener { joinGroups() }
        tv_change.setOnClickListener { getRecommendedGroups() }
    }

    private fun joinGroups() {
        if (mGroups.size == 0) {
            return
        }
        val groupIds = mutableListOf<String>()
        for (group in mGroups) {
            if (group.selected) {
                groupIds.add(group.id)
            }
        }
        viewModel.batchJoinRoomApply(groupIds)
    }
}