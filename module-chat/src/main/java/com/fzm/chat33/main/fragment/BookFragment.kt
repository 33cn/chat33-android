package com.fzm.chat33.main.fragment

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager.widget.ViewPager
import com.alibaba.android.arouter.launcher.ARouter
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.fuzamei.common.bus.LiveBus
import com.fuzamei.common.utils.BarUtils
import com.fuzamei.common.view.ScrollPagerAdapter
import com.fuzamei.componentservice.app.AppRoute
import com.fuzamei.componentservice.app.BusEvent
import com.fuzamei.componentservice.base.DILoadableFragment
import com.fuzamei.componentservice.ext.findViewModel
import com.fzm.chat33.R
import com.fzm.chat33.main.activity.MainActivity
import com.fzm.chat33.main.mvvm.MainViewModel
import kotlinx.android.synthetic.main.fragment_book.*
import java.util.*
import javax.inject.Inject

open class BookFragment : DILoadableFragment(), View.OnClickListener {

    companion object {
        const val CHANGE_TAB_TO_MAIN = 1
    }
    @Inject
    lateinit var provider: ViewModelProvider.Factory
    private lateinit var viewModel: MainViewModel
    private lateinit var friendFragment: BookFriendFragment
    private lateinit var groupFragment: BookGroupFragment
    private val fragments = ArrayList<Fragment>()
    private val titles = listOf("", "")

    // 新的好友请求数量
    private var newRequestNum = 0

    override fun getLayoutId(): Int {
        return R.layout.fragment_book
    }

    override fun initView(view: View?, savedInstanceState: Bundle?) {
        viewModel = findViewModel(provider)
        rootView.setPadding(rootView.paddingLeft, rootView.paddingTop + BarUtils.getStatusBarHeight(activity),
                rootView.paddingRight, rootView.paddingBottom)

        initDatas()

        LiveBus.of(BusEvent::class.java).newFriends().observe(this, Observer { event ->
            if (event.clear) {
                newRequestNum = 0
                iv_new_apply.visibility = View.GONE
                tv_new_apply_count.visibility = View.GONE
            } else {
                newRequestNum++
                if (!TextUtils.isEmpty(event.avatar)) {
                    Glide.with(this).load(event.avatar)
                            .apply(RequestOptions().placeholder(R.mipmap.default_avatar_round))
                            .into(iv_new_apply)
                } else {
                    iv_new_apply.setImageResource(R.mipmap.default_avatar_round)
                }
                if (newRequestNum > 99) {
                    tv_new_apply_count.text = "99+"
                } else {
                    tv_new_apply_count.text = newRequestNum.toString()
                }
                iv_new_apply.visibility = View.VISIBLE
                tv_new_apply_count.visibility = View.VISIBLE
            }
        })
        LiveBus.of(BusEvent::class.java).changeTab().observe(this, Observer { event ->
            if (event.tab == 1) {
                vp_book.currentItem = event.subTab
            }
        })

        LiveBus.of(BusEvent::class.java).loginEvent().observe(this, Observer { login ->
            if (login) {
                initDatas()
            }
        })
        viewModel.unreadNumber.observe(this, Observer {
            if(it == null) return@Observer
            newRequestNum = it.number
            if (newRequestNum == 0) {
                tv_new_apply_count.visibility = View.GONE
                iv_new_apply.visibility = View.GONE
            } else {
                if (newRequestNum > 99) {
                    tv_new_apply_count.text = "99+"
                } else {
                    tv_new_apply_count.text = newRequestNum.toString()
                }
                iv_new_apply.setImageResource(R.mipmap.default_avatar_round)
                tv_new_apply_count.visibility = View.VISIBLE
                iv_new_apply.visibility = View.VISIBLE
            }
        })
    }

    private fun initDatas() {
        fragments.clear()
        friendFragment = BookFriendFragment()
        groupFragment = BookGroupFragment()
        fragments.add(friendFragment)
        fragments.add(groupFragment)
        vp_book.adapter = ScrollPagerAdapter(childFragmentManager, titles, fragments)
        vp_book.offscreenPageLimit = 2
        switchChoose(0)
        vp_book.currentItem = 0
        viewModel.getUnreadApplyNumber()
    }

    override fun initData() {
    }

    override fun setEvent() {
        iv_scan.setOnClickListener(this)
        iv_search.setOnClickListener(this)
        iv_add.setOnClickListener(this)
        ly_create_group.setOnClickListener(this)
        ly_new_friend.setOnClickListener(this)
        ly_black_list.setOnClickListener(this)
        tv_often.setOnClickListener(this)
        tv_group.setOnClickListener(this)
        tv_friend.setOnClickListener(this)
        vp_book.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {

            }

            override fun onPageSelected(position: Int) {
                switchChoose(position)
            }

            override fun onPageScrollStateChanged(state: Int) {
                if (state == ViewPager.SCROLL_STATE_IDLE) {
                    friendFragment.toggleSideBar(true)
                } else {
                    friendFragment.toggleSideBar(false)
                }
            }
        })
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.ly_new_friend -> ARouter.getInstance().build(AppRoute.NEW_FRIENDS).navigation()
            R.id.tv_often -> {
                //switchChoose(0);
            }
            R.id.tv_friend -> vp_book.currentItem = 0
            R.id.tv_group -> vp_book.currentItem = 1
            R.id.iv_search -> ARouter.getInstance().build(AppRoute.SEARCH_LOCAL).navigation()
            R.id.iv_scan -> ARouter.getInstance().build(AppRoute.QR_SCAN).navigation()
            R.id.iv_add -> ARouter.getInstance().build(AppRoute.SEARCH_ONLINE).navigation()
            R.id.ly_create_group -> ARouter.getInstance().build(AppRoute.CREATE_GROUP).navigation()
            R.id.ly_black_list -> ARouter.getInstance().build(AppRoute.BLACK_LIST).navigation()
        }
    }

    private fun switchChoose(index: Int) {
        when (index) {
            0 -> {
                tv_friend.setTextColor(ContextCompat.getColor(activity, R.color.chat_color_title))
                tv_friend.setBackgroundResource(R.drawable.shape_common_table)
                tv_group.setTextColor(ContextCompat.getColor(activity, R.color.chat_text_grey_light))
                tv_group.setBackgroundResource(0)
            }
            1 -> {
                tv_friend.setTextColor(ContextCompat.getColor(activity, R.color.chat_text_grey_light))
                tv_friend.setBackgroundResource(0)
                tv_group.setTextColor(ContextCompat.getColor(activity, R.color.chat_color_title))
                tv_group.setBackgroundResource(R.drawable.shape_common_table)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == CHANGE_TAB_TO_MAIN) {
                if (getActivity() != null) {
                    (getActivity() as MainActivity).setTabSelection(0)
                }
            }
        }
    }
}