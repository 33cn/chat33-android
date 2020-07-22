package com.fzm.chat33.main.fragment

import android.os.Bundle
import android.text.Html
import android.text.TextUtils
import android.view.View
import androidx.core.content.ContextCompat

import androidx.core.widget.NestedScrollView
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.alibaba.android.arouter.launcher.ARouter
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions

import com.fuzamei.common.bus.LiveBus
import com.fuzamei.common.utils.*
import com.fuzamei.componentservice.app.BusEvent
import com.fuzamei.componentservice.app.AppRoute
import com.fuzamei.componentservice.base.DILoadableFragment
import com.fuzamei.componentservice.config.AppConfig
import com.fuzamei.componentservice.ext.findViewModel
import com.fuzamei.update.util.ApkFileUtil
import com.fzm.chat33.R
import com.fzm.chat33.core.bean.IdentifyParam
import com.fzm.chat33.core.global.Chat33Const
import com.fzm.chat33.core.utils.UserInfoPreference
import com.fzm.chat33.global.LocalData
import com.fzm.chat33.main.adapter.TabLayoutFragmentAdapter
import com.fzm.chat33.main.mvvm.MainViewModel
import com.fuzamei.componentservice.widget.dialog.EasyDialog
import com.fzm.chat33.main.activity.MainActivity
import kotlinx.android.synthetic.main.fragment_my.*

import javax.inject.Inject

/**
 * 创建日期：2018/10/8 on 14:58
 *
 * @author zhengjy
 * @since 2019/10/21
 * Description:我的页面
 */
class MyFragment : DILoadableFragment(), View.OnClickListener {

    @Inject
    lateinit var provider: ViewModelProvider.Factory
    private lateinit var viewModel: MainViewModel

    private var adapter: TabLayoutFragmentAdapter? = null
    private lateinit var titles: Array<String>
    private var enableVerify = false

    override fun getLayoutId(): Int {
        return R.layout.fragment_my
    }

    override fun initView(view: View?, savedInstanceState: Bundle?) {
        rootView.setPadding(rootView.paddingLeft, rootView.paddingTop + BarUtils.getStatusBarHeight(activity),
                rootView.paddingRight, rootView.paddingBottom)
        viewModel = findViewModel(provider)
        val versionName = ApkFileUtil.getVersionName(activity)
        tv_version.text = if (TextUtils.isEmpty(versionName)) "" else "v$versionName"
        titles = resources.getStringArray(R.array.chat_me_tab)

        LiveBus.of(BusEvent::class.java).nicknameRefresh().observe(this, Observer { event ->
            if (event.id == null) {
                tv_name.text = event.nickname
                tv_top_name.text = event.nickname
            }
        })
        LiveBus.of(BusEvent::class.java).imageRefresh().observe(this, Observer { avatar ->
            Glide.with(activity).load(avatar)
                    .apply(RequestOptions().placeholder(R.mipmap.default_avatar_round))
                    .into(iv_avatar)
        })
        viewModel.setting.observe(this, Observer { setting ->
            UserInfoPreference.getInstance().setIntPref(UserInfoPreference.NEED_CONFIRM, setting.needConfirm)
            UserInfoPreference.getInstance().setIntPref(UserInfoPreference.NEED_ANSWER, setting.needAnswer)
            if (!TextUtils.isEmpty(setting.question) && !TextUtils.isEmpty(setting.answer)) {
                UserInfoPreference.getInstance().setStringPref(UserInfoPreference.VERIFY_QUESTION, setting.question)
                UserInfoPreference.getInstance().setStringPref(UserInfoPreference.VERIFY_ANSWER, setting.answer)
            }
            UserInfoPreference.getInstance().setIntPref(UserInfoPreference.NEED_CONFIRM_INVITE, setting.needConfirmInvite)
        })
        viewModel.module.observe(this, Observer { wrapper ->
            wrapper.modules?.forEach { state ->
                if (state.type == 1) {
                    // 启用实名认证模块
                    enableVerify = state.isEnable
                } else if (state.type == 2) {
                    if (state.isEnable) {
                        my_attendance.visibility = View.VISIBLE
                        my_praise_rank.visibility = View.VISIBLE
                    } else {
                        my_attendance.visibility = View.GONE
                        my_praise_rank.visibility = View.GONE
                    }
                }
            }
        })

        adapter = TabLayoutFragmentAdapter(fragmentManager, listOf(*titles))
        stl_unlogin.setTabData(titles)
        nsv_container.setOnScrollChangeListener(NestedScrollView.OnScrollChangeListener { _, _, scrollY, _, _ ->
            val margin = ScreenUtils.dp2px(activity, 15f)
            val offset = margin + tv_name.height
            val alpha: Float
            alpha = if (scrollY < offset) {
                if (scrollY > margin) {
                    (scrollY - margin) * 1.0f / (offset - margin)
                } else {
                    0.0f
                }
            } else {
                1.0f
            }
            tv_top_name.alpha = alpha
        })
    }

    override fun initData() {
        viewModel.currentUser.observe(this, Observer {
            if (it.isLogin) {
                iv_qrcode.visibility = View.VISIBLE
                rl_login.visibility = View.VISIBLE
                ll_unlogin.visibility = View.GONE
                exit_login.visibility = View.VISIBLE
                if (!it.avatar.isNullOrEmpty()) {
                    Glide.with(activity).load(it.avatar)
                            .apply(RequestOptions().placeholder(R.mipmap.default_avatar_round))
                            .into(iv_avatar)
                } else {
                    iv_avatar.setImageResource(R.mipmap.default_avatar_round)
                }
                tv_name.text = it.username
                tv_top_name.text = it.username
                tv_uid.text = getString(R.string.user_info_uid, it.uid)
                tv_account.text = getString(R.string.user_info_account, ToolUtils.encryptPhoneNumber(it.account))
                if (AppConfig.APP_IDENTIFY) {
                    if (it.isIdentified) {
                        tv_identification.setTextColor(ContextCompat.getColor(activity, R.color.chat_text_grey_light))
                        tv_identification.text = getString(R.string.chat_tips_identification_tip3, it.identificationInfo)
                    } else {
                        tv_identification.text = Html.fromHtml(getString(R.string.chat_tips_identification, AppConfig.APP_ACCENT_COLOR_STR))
                    }
                    tv_identification.setOnClickListener(this)
                    tv_identification.visibility = View.VISIBLE
                } else {
                    tv_identification.visibility = View.GONE
                }
                iv_avatar.setIconRes(
                        if (it.isIdentified) R.drawable.ic_user_identified
                        else -1
                )
                if (it.verified == 1) {
                    tv_verify_tips.setText(R.string.chat_identification)
                    tv_verify_tips.visibility = View.VISIBLE
                } else {
                    tv_verify_tips.visibility = View.GONE
                }
            }
        })
    }

    override fun setEvent() {
        iv_scan.setOnClickListener(this)
        iv_qrcode.setOnClickListener(this)
        iv_avatar.setOnClickListener(this)
        tv_name.setOnClickListener(this)

        my_attendance.setOnClickListener(this)
        my_praise_rank.setOnClickListener(this)
        my_red_packet.setOnClickListener(this)
        my_collect.setOnClickListener(this)
        my_secure_setting.setOnClickListener(this)
        my_setting_center.setOnClickListener(this)
        my_verify.setOnClickListener(this)
        my_update.setOnClickListener(this)
        my_share.setOnClickListener(this)
        exit_login.setOnClickListener(this)
    }

    fun refreshState() {
        viewModel.updateInfo()
    }

    private fun getSettingInfo() {
        if (!viewModel.isLogin()) {
            return
        }
        viewModel.getSettingInfo()
        viewModel.getModuleState()
        viewModel.updateInfo()

    }

    override fun onResume() {
        super.onResume()
        if (isAdded && !isHidden) {
            getSettingInfo()
        }
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        if (!hidden) {
            getSettingInfo()
        }
    }

    override fun onClick(v: View) {
        val id = v.id
        if (id == R.id.iv_avatar) {
            ARouter.getInstance().build(AppRoute.EDIT_AVATAR)
                    .withString("avatar", viewModel.currentUser.value?.avatar)
                    .navigation()
        } else if (id == R.id.tv_name) {
//            ARouter.getInstance().build(AppRoute.EDIT_NAME)
//                    .withString("name", viewModel.currentUser.value?.username)
//                    .navigation()
        } else if (id == R.id.iv_scan) {
            ARouter.getInstance().build(AppRoute.QR_SCAN).navigation()
        } else if (id == R.id.tv_identification) {
            val source = IdentifyParam.create()
            val param = AESUtil.encrypt(source, AESUtil.DEFAULT_KEY)
            ARouter.getInstance().build(AppRoute.WEB_BROWSER)
                    .withString("url", AppConfig.APP_URL + "/cert/#/?para=" + param)
                    .withInt("titleColor", -0xcd4d09)
                    .withInt("textColor", -0x50404)
                    .withBoolean("darkMode", false)
                    .withBoolean("showOptions", false)
                    .navigation()
        } else if (id == R.id.iv_qrcode) {
            ARouter.getInstance().build(AppRoute.QR_CODE)
                    .withString("id", viewModel.currentUser.value?.id)
                    .withString("content", viewModel.currentUser.value?.uid)
                    .withString("avatar", viewModel.currentUser.value?.avatar)
                    .withString("name", viewModel.currentUser.value?.username)
                    .withInt("channelType", Chat33Const.CHANNEL_FRIEND)
                    .navigation()
        } else if (id == R.id.my_attendance) {
            ARouter.getInstance().build(AppRoute.ATTENDANCE).navigation()
        } else if (id == R.id.my_praise_rank) {
            ARouter.getInstance().build(AppRoute.PRAISE_RANK).navigation()
        } else if (id == R.id.my_red_packet) {
            ARouter.getInstance().build(AppRoute.RED_PACKET_RECORDS).navigation()
        } else if (id == R.id.my_collect) {
            ShowUtils.showToastNormal(activity, R.string.chat_function_developing)
        } else if (id == R.id.my_secure_setting) {
            ARouter.getInstance().build(AppRoute.SECURITY_SETTING).navigation()
        } else if (id == R.id.my_setting_center) {
            ARouter.getInstance().build(AppRoute.SETTING).navigation()
        } else if (id == R.id.my_verify) {
            if (enableVerify) {
                ARouter.getInstance().build(AppRoute.CHOOSE_VERIFY)
                        .navigation(activity, REQUEST_VERIFY)
            } else {
                ShowUtils.showToastNormal(activity, R.string.chat_function_not_open)
            }
        } else if (id == R.id.my_update) {
            LocalData.checkUpdate(activity, true) {
                (activity as MainActivity).installApk = it
            }
        } else if (id == R.id.my_share) {
            ARouter.getInstance().build(AppRoute.QR_CODE)
                    .withString("id", viewModel.currentUser.value?.id)
                    .withString("content", viewModel.currentUser.value?.uid)
                    .withString("avatar", viewModel.currentUser.value?.avatar)
                    .withString("name", viewModel.currentUser.value?.username)
                    .withInt("channelType", Chat33Const.CHANNEL_FRIEND)
                    .navigation()
        } else if (id == R.id.exit_login) {
            val dialog = EasyDialog.Builder()
                    .setHeaderTitle(getString(R.string.chat_tips_tips))
                    .setBottomLeftText(getString(R.string.chat_action_cancel))
                    .setBottomRightText(getString(R.string.chat_action_confirm))
                    .setContent(getString(R.string.chat_logout_message))
                    .setBottomLeftClickListener(null)
                    .setBottomRightClickListener { dialog ->
                        dialog.dismiss()
                        viewModel.logout()
                    }.create(activity)
            dialog.show()
        }
    }

    companion object {

        @JvmField
        val REQUEST_VERIFY = 1
    }
}
