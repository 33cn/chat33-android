package com.fzm.chat33.main.activity

import android.text.Html
import android.text.InputFilter
import android.text.TextUtils
import android.view.View
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider

import com.alibaba.android.arouter.facade.annotation.Autowired
import com.alibaba.android.arouter.facade.annotation.Route
import com.alibaba.android.arouter.launcher.ARouter
import com.fuzamei.common.utils.ScreenUtils
import com.fuzamei.common.utils.ShowUtils
import com.fuzamei.componentservice.app.AppRoute
import com.fuzamei.componentservice.base.DILoadableActivity
import com.fzm.chat33.R
import com.fzm.chat33.utils.SimpleTextWatcher
import com.fuzamei.componentservice.ext.findViewModel
import com.fzm.chat33.main.mvvm.GroupViewModel
import kotlinx.android.synthetic.main.activity_edit_group.*
import javax.inject.Inject

/**
 * @author zhengjy
 * @since 2018/11/26
 * Description:
 */
@Route(path = AppRoute.EDIT_GROUP_INFO)
class EditGroupInfoActivity : DILoadableActivity() {

    @JvmField
    @Autowired
    var roomId: String = ""
    @JvmField
    @Autowired
    var type: Int = 0// 1：发布群公告  2：修改群内昵称
    @JvmField
    @Autowired
    var content: String = ""
    @JvmField
    @Autowired
    var groupName: String? = null

    @Inject
    lateinit var provider: ViewModelProvider.Factory
    lateinit var viewModel: GroupViewModel

    override fun enableSlideBack(): Boolean {
        return true
    }

    override fun getLayoutId(): Int {
        return R.layout.activity_edit_group
    }

    override fun initView() {
        ARouter.getInstance().inject(this)
        viewModel = findViewModel(provider)
        viewModel.loading.observe(this, Observer { setupLoading(it) })
        viewModel.publishNotice.observe(this, Observer {
            if (it != null) {
                ShowUtils.showToastNormal(instance, getString(R.string.chat_tips_edit_group4))
                finish()
            }
        })
        viewModel.memberName.observe(this, Observer {
            if (it != null) {
                ShowUtils.showToastNormal(instance, getString(R.string.chat_tips_edit_group3))
                finish()
            }
        })
    }

    override fun initData() {
        ctb_title.setMiddleText(if (type == 1) getString(R.string.chat_title_edit_group1) else getString(R.string.chat_title_edit_group2))
        ctb_title.setRightVisible(false)
        ctb_title.setLeftListener { finish() }
        if (type != 1) {
            if (!TextUtils.isEmpty(content)) {
                et_content.setText(content)
                et_content.setSelection(content.length)
                tv_name_count.text = getString(R.string.chat_tips_num_20, content.length)
            } else {
                tv_name_count.text = getString(R.string.chat_tips_num_20, 0)
            }
            et_content.filters = arrayOf(InputFilter.LengthFilter(20))
            et_content.setHint(R.string.chat_tips_edit_group1)
            tv_name_tips.setText(R.string.chat_tips_group_remark)
            tv_submit.setText(R.string.chat_action_confirm)
            val tips = getString(R.string.room_member_name_tips, groupName)
            tv_edit_tips.text = Html.fromHtml(tips)
            tv_edit_tips.visibility = View.VISIBLE
        } else {
            if (!TextUtils.isEmpty(content)) {
                et_content.setText(content)
                et_content.setSelection(content.length)
                tv_name_count.text = getString(R.string.chat_tips_num_50, content.length)
            } else {
                tv_name_count.text = getString(R.string.chat_tips_num_50, 0)
            }
            et_content.height = ScreenUtils.dp2px(this, 100f)
            et_content.filters = arrayOf(InputFilter.LengthFilter(50))
            et_content.setHint(R.string.chat_tips_edit_group2)
            tv_name_tips.setText(R.string.chat_tips_group_pub_notification)
            tv_submit.setText(R.string.chat_action_publish)
            tv_edit_tips.visibility = View.GONE
        }
    }

    override fun setEvent() {
        et_content.addTextChangedListener(object : SimpleTextWatcher() {
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                if (TextUtils.isEmpty(s)) {
                    if (type == 2) {
                        tv_name_count.text = getString(R.string.chat_tips_num_20, 0)
                    } else {
                        tv_name_count.text = getString(R.string.chat_tips_num_50, 0)
                    }
                } else {
                    if (type == 2) {
                        tv_name_count.text = getString(R.string.chat_tips_num_20, s.length)
                    } else {
                        tv_name_count.text = getString(R.string.chat_tips_num_50, s.length)
                    }
                }
            }
        })
        tv_submit.setOnClickListener(View.OnClickListener {
            if (type != 1) {
                // 编辑群内昵称
                val newName = et_content.text.toString().trim()
                if (newName == content) {
                    return@OnClickListener
                }
                viewModel.setMemberNickname(roomId, newName)
            } else {
                // 编辑群公告
                val newNotice = et_content.text.toString().trim()
                if (newNotice == content || TextUtils.isEmpty(newNotice)) {
                    return@OnClickListener
                }
                viewModel.publishNotice(roomId, newNotice)
            }
        })
    }
}
