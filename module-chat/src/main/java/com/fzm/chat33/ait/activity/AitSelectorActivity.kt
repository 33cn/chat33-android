package com.fzm.chat33.ait.activity

import android.annotation.SuppressLint
import android.content.Intent
import android.view.View
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.alibaba.android.arouter.facade.annotation.Autowired
import com.alibaba.android.arouter.facade.annotation.Route
import com.alibaba.android.arouter.launcher.ARouter
import com.fuzamei.common.executor.AppExecutors
import com.fuzamei.common.recycleviewbase.RecyclerViewDivider
import com.fuzamei.common.utils.KeyboardUtils
import com.fuzamei.componentservice.app.AppRoute
import com.fuzamei.componentservice.base.DILoadableActivity
import com.fuzamei.componentservice.ext.findViewModel
import com.fzm.chat33.R
import com.fzm.chat33.core.Chat33
import com.fzm.chat33.core.db.ChatDatabase
import com.fzm.chat33.core.db.bean.RoomContact
import com.fzm.chat33.core.db.bean.RoomUserBean
import com.fzm.chat33.main.mvvm.AitSelectorViewModel
import com.fzm.chat33.widget.ChatSearchView
import io.reactivex.Observable
import io.reactivex.ObservableOnSubscribe
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_ait_selector.*
import javax.inject.Inject

/**
 * @author zhengjy
 * @since 2019/08/19
 * Description:
 */
@Route(path = AppRoute.AIT_SELECT)
class AitSelectorActivity : DILoadableActivity() {

    @JvmField
    var originDataList: ArrayList<RoomContact>? = ArrayList()

    @JvmField
    @Autowired
    var targetId: String = ""

    @JvmField
    @Autowired
    var memberLevel: Int = 1

    /**
     * id为-1代表@所有人
     */
    val ALL_USERS: RoomUserBean by lazy(LazyThreadSafetyMode.NONE) {
        RoomUserBean().apply {
            id = "-1"
            nickname = Chat33.getContext().getString(R.string.chat_all_people)
        }
    }

    var adapter: AitListAdapter? = null
    val manager: LinearLayoutManager by lazy(LazyThreadSafetyMode.NONE) { LinearLayoutManager(this) }

    @Inject
    lateinit var provider: ViewModelProvider.Factory
    lateinit var viewModel: AitSelectorViewModel

    override fun getLayoutId(): Int {
        return R.layout.activity_ait_selector
    }

    override fun initView() {
        ARouter.getInstance().inject(this)
        viewModel = findViewModel(provider)
        tv_cancel.setOnClickListener {
            setResult(RESULT_CANCELED)
            finish()
        }
        ait_all.visibility = if (memberLevel > 1) View.VISIBLE else View.GONE

        rv_member.addItemDecoration(RecyclerViewDivider(this, LinearLayoutManager.VERTICAL,
                0.5f, ContextCompat.getColor(this, R.color.chat_color_line)))
        rv_member.layoutManager = manager
        adapter = AitListAdapter(this, arrayListOf())
        adapter?.setOnItemClickListener(object : AitListAdapter.OnItemClickListener {
            override fun onItemClick(view: View, position: Int) {
                setResult(RESULT_OK, Intent().apply {
                    val contact = adapter?.mData!![position]
                    putExtra(RESULT_DATA, RoomUserBean().apply {
                        roomId = contact.roomId
                        id = contact.id
                        roomNickname = contact.roomNickname
                        nickname = contact.nickname
                        avatar = contact.avatar
                        memberLevel = contact.memberLevel
                        roomMutedType = contact.roomMutedType
                        mutedType = contact.mutedType
                        deadline = contact.deadline
                        identification = contact.identification
                        identificationInfo = contact.identificationInfo
                        searchKey = contact.searchKey
                    })
                })
                finish()
            }
        })
        rv_member.adapter = adapter

        sideBar.setTextView(dialog)
        //设置右侧SideBar触摸监听
        sideBar.setOnTouchingLetterChangedListener { s ->
            //该字母首次出现的位置
            val position = adapter!!.getPositionForSection(s[0].toInt())
            if (position != -1) {
                manager.scrollToPositionWithOffset(position, 0)
            }
        }
        viewModel.roomUsers.observe(this, Observer {
            originDataList?.clear()
            originDataList?.addAll(it)
            adapter?.resetList(originDataList)
        })
    }

    override fun initData() {
        viewModel.getRoomUsers(targetId)
    }

    override fun setEvent() {
        ait_all.setOnClickListener {
            setResult(RESULT_OK, Intent().apply { putExtra(RESULT_DATA, ALL_USERS) })
            finish()
        }
        iv_search.setOnClickListener {
            ait_search.expand()
            ait_search.postDelayed({ KeyboardUtils.showKeyboard(ait_search.getFocusView()) }, 100)
        }
        ait_search.setOnSearchCancelListener(object : ChatSearchView.OnSearchCancelListener{
            override fun onSearchCancel() {
                KeyboardUtils.hideKeyboard(ait_search.getFocusView())
                ait_search.reduce()
            }
        })
        ait_search.setOnTextChangeListener(object : ChatSearchView.OnTextChangeListener {
            override fun onTextChange(s: String) {
                searchKeyword(s)
            }
        })
    }

    @SuppressLint("CheckResult")
    fun searchKeyword(keyword: String) {
        adapter?.setKeywords(keyword)
        if (keyword.isEmpty()) {
            rv_member.visibility = View.VISIBLE
            ll_empty.visibility = View.GONE
            adapter?.resetList(originDataList)
            return
        }
        Observable
                .create(ObservableOnSubscribe<List<RoomContact>> { emitter ->
                    val matchList = ChatDatabase.getInstance()
                            .roomUserDao().searchRoomContacts(targetId, keyword)
                    emitter.onNext(matchList)
                })
                .subscribeOn(Schedulers.from(AppExecutors.databaseThreadPool()))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { matchList ->
                    adapter?.resetList(matchList)
                    if (matchList.isEmpty()) {
                        rv_member.visibility = View.GONE
                        ll_empty.visibility = View.VISIBLE
                    } else {
                        rv_member.visibility = View.VISIBLE
                        ll_empty.visibility = View.GONE
                    }
                }
    }

    override fun onBackPressed() {
        if (!ait_search.onBackPressed()) {
            super.onBackPressed()
        }
    }

    companion object {

        @JvmStatic
        val REQUEST_CODE = 101

        @JvmStatic
        val RESULT_DATA = "RESULT_DATA"
    }
}
