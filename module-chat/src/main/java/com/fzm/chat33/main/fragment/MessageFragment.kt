package com.fzm.chat33.main.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.viewpager.widget.ViewPager
import com.alibaba.android.arouter.launcher.ARouter
import com.fuzamei.common.bus.LiveBus

import com.fuzamei.common.utils.BarUtils
import com.fuzamei.common.utils.run
import com.fuzamei.common.view.ScrollPagerAdapter
import com.fuzamei.componentservice.app.AppRoute
import com.fuzamei.componentservice.app.BusEvent
import com.fuzamei.componentservice.base.DILoadableFragment
import com.fzm.chat33.R
import com.fzm.chat33.core.db.ChatDatabase
import com.fzm.chat33.core.global.Chat33Const
import com.fzm.chat33.core.global.UserInfo
import com.fzm.chat33.core.net.socket.ChatSocket
import com.fzm.chat33.core.net.socket.SocketState.Companion.CONNECTING
import com.fzm.chat33.core.net.socket.SocketState.Companion.DISCONNECTED
import com.fzm.chat33.core.net.socket.SocketState.Companion.ESTABLISHED
import com.fzm.chat33.core.net.socket.SocketState.Companion.INITIAL
import com.fzm.chat33.core.net.socket.SocketStateChangeListener
import com.fzm.chat33.main.popupwindow.HomeAddPopupWindow
import io.reactivex.disposables.Disposable
import io.reactivex.functions.Consumer
import kotlinx.android.synthetic.main.fragment_message.*
import javax.inject.Inject

/**
 * 创建日期：2018/10/8 on 14:58
 *
 * @author zhengjy
 * @since 2019/10/22
 * Description:首页消息页面
 */
class MessageFragment : DILoadableFragment(), View.OnClickListener {

    private var adapter: ScrollPagerAdapter? = null
    private lateinit var groupMessageFragment: GroupMessageFragment
    private lateinit var contactsFragment: ContactsFragment
    private var fragments: MutableList<Fragment> = arrayListOf()
    private val titles = arrayListOf("", "")

    private var homeAddPopupWindow: HomeAddPopupWindow? = null
    private var netStatus: View? = null

    private var mGroup: Disposable? = null
    private var mContact:Disposable? = null

    @JvmField
    @Inject
    var socket: ChatSocket? = null

    override fun getLayoutId(): Int {
        return R.layout.fragment_message
    }

    override fun initView(view: View, savedInstanceState: Bundle?) {
        rootView.setPadding(rootView.paddingLeft, rootView.paddingTop + BarUtils.getStatusBarHeight(activity),
                rootView.paddingRight, rootView.paddingBottom)
        netStatus = rootView.findViewById(R.id.net_status)
        subscribe()
        groupMessageFragment = GroupMessageFragment()
        contactsFragment = ContactsFragment()
        fragments.add(groupMessageFragment)
        fragments.add(contactsFragment)
        titles.add("")
        titles.add("")
        adapter = ScrollPagerAdapter(childFragmentManager, titles, fragments)
        vp_message.adapter = adapter
        vp_message.offscreenPageLimit = 2
        vp_message.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {

            }

            override fun onPageSelected(position: Int) {
                if (position != 0 && !UserInfo.getInstance().isLogin) {
                    vp_message.currentItem = 0
                    return
                }
                switchChoose(position)
            }

            override fun onPageScrollStateChanged(state: Int) {

            }
        })
        switchChoose(0)
        vp_message.currentItem = 0
    }

    override fun initData() {

    }

    override fun setEvent() {
        iv_add.setOnClickListener(this)
        iv_scan.setOnClickListener(this)
        iv_search.setOnClickListener(this)
        tv_group.setOnClickListener(this)
        tv_contacts.setOnClickListener(this)
        LiveBus.of(BusEvent::class.java).changeTab().observe(this, Observer { event ->
            if (event.tab == 0) {
                vp_message.currentItem = event.subTab
                switchChoose(event.subTab)
            }
        })
    }

    private fun subscribe() {
        if (mGroup == null) {
            mGroup = ChatDatabase.getInstance().recentMessageDao().getMsgCountByChannel(Chat33Const.CHANNEL_ROOM).run(Consumer { count ->
                when {
                    count <= 0 -> tv_group_num.visibility = View.GONE
                    count > 99 -> {
                        tv_group_num.text = "..."
                        tv_group_num.visibility = View.VISIBLE
                    }
                    else -> {
                        tv_group_num.text = count.toString()
                        tv_group_num.visibility = View.VISIBLE
                    }
                }
            })
        }
        if (mContact == null) {
            mContact = ChatDatabase.getInstance().recentMessageDao().getMsgCountByChannel(Chat33Const.CHANNEL_FRIEND).run(Consumer { count ->
                when {
                    count <= 0 -> tv_contacts_num.visibility = View.GONE
                    count > 99 -> {
                        tv_contacts_num.text = "..."
                        tv_contacts_num.visibility = View.VISIBLE
                    }
                    else -> {
                        tv_contacts_num.text = count.toString()
                        tv_contacts_num.visibility = View.VISIBLE
                    }
                }
            })
        }

        socket?.addSocketStateChangeListener(object : SocketStateChangeListener {
            override fun onSocketStateChange(state: Int) {
                when (state) {
                    INITIAL -> {
                    }
                    CONNECTING, DISCONNECTED -> netStatus?.visibility = View.VISIBLE
                    ESTABLISHED -> netStatus?.visibility = View.GONE
                }
            }
        })
    }

    private fun switchChoose(index: Int) {
        if (mGroup == null || mContact == null) {
            subscribe()
        }
        when (index) {
            0 -> {
                tv_group.setTextColor(ContextCompat.getColor(activity, R.color.chat_color_title))
                tv_group.textSize = 20f
                tv_group.setBackgroundResource(R.drawable.shape_common_table)
                tv_contacts.setTextColor(ContextCompat.getColor(activity, R.color.chat_text_grey_light))
                tv_contacts.textSize = 16f
                tv_contacts.setBackgroundResource(0)
            }
            1 -> {
                tv_contacts.setTextColor(ContextCompat.getColor(activity, R.color.chat_color_title))
                tv_contacts.textSize = 20f
                tv_contacts.setBackgroundResource(R.drawable.shape_common_table)
                tv_group.setTextColor(ContextCompat.getColor(activity, R.color.chat_text_grey_light))
                tv_group.textSize = 16f
                tv_group.setBackgroundResource(0)
            }
        }
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.iv_scan -> ARouter.getInstance().build(AppRoute.QR_SCAN).navigation()
            R.id.iv_search -> ARouter.getInstance().build(AppRoute.SEARCH_LOCAL).navigation()
            R.id.iv_add -> showPop(rv_title)
            R.id.tv_room -> {
            }
            R.id.tv_group -> vp_message.currentItem = 0
            R.id.tv_contacts -> vp_message.currentItem = 1
        }
    }

    private fun showPop(view: View) {
        if (homeAddPopupWindow == null) {
            homeAddPopupWindow = HomeAddPopupWindow(activity, LayoutInflater.from(activity).inflate(R.layout.popupwindow_home_add, null))
            homeAddPopupWindow?.softInputMode = WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE
            homeAddPopupWindow?.show(view)
        } else {
            homeAddPopupWindow?.show(view)
        }
    }
}
