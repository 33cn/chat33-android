package com.fzm.chat33.main.activity

import androidx.lifecycle.ViewModelProvider
import com.alibaba.android.arouter.facade.annotation.Autowired
import com.alibaba.android.arouter.facade.annotation.Route
import com.alibaba.android.arouter.launcher.ARouter
import com.fuzamei.common.utils.KeyboardUtils
import com.fuzamei.componentservice.app.AppRoute
import com.fuzamei.componentservice.base.DILoadableActivity
import com.fuzamei.componentservice.ext.findViewModel
import com.fzm.chat33.R
import com.fzm.chat33.core.bean.SearchResult
import com.fzm.chat33.core.bean.SearchScope
import com.fzm.chat33.core.bean.ChatTarget
import com.fzm.chat33.core.db.bean.ChatMessage
import com.fzm.chat33.main.fragment.SearchLogDetailFragment
import com.fzm.chat33.main.fragment.SearchResultScopeFragment
import com.fzm.chat33.main.mvvm.SearchLocalViewModel
import com.fzm.chat33.widget.ChatSearchView
import kotlinx.android.synthetic.main.activity_search_local_scope.*
import javax.inject.Inject

/**
 * @author zhengjy
 * @since 2019/09/17
 * Description:分类搜索结果
 */
@Route(path = AppRoute.SEARCH_LOCAL_SCOPE)
class SearchLocalScopeActivity : DILoadableActivity() {

    @JvmField
    @Autowired
    var scope: Int = SearchScope.FRIEND
    @JvmField
    @Autowired
    var keywords: String = ""
    @JvmField
    @Autowired
    var result: ArrayList<SearchResult>? = null
    @JvmField
    @Autowired
    var chatLogs: ArrayList<ChatMessage>? = null
    @JvmField
    @Autowired
    var chatTarget: ChatTarget? = null
    @JvmField
    @Autowired
    var popKeyboard: Boolean = false

    @Inject
    lateinit var provider: ViewModelProvider.Factory
    private lateinit var viewModel: SearchLocalViewModel

    private val searchResultScopeFragment by lazy {
        SearchResultScopeFragment.create(scope, result)
    }
    private val searchLogDetailFragment by lazy {
        SearchLogDetailFragment.create(chatTarget, chatLogs)
    }

    override fun enableSlideBack(): Boolean {
        return true
    }

    override fun getLayoutId(): Int {
        return R.layout.activity_search_local_scope
    }

    override fun initView() {
        ARouter.getInstance().inject(this)
        viewModel = findViewModel(provider)
        when (scope) {
            SearchScope.FRIEND -> sv_search.setHint(getString(R.string.chat_tips_search_hint1))
            SearchScope.GROUP -> sv_search.setHint(getString(R.string.chat_tips_search_hint2))
            SearchScope.CHATLOG -> sv_search.setHint(getString(R.string.chat_tips_search_hint3))
        }
        viewModel.initSearchKey(keywords)
        sv_search.setText(keywords)
        if (popKeyboard) {
            sv_search.postDelayed({ KeyboardUtils.showKeyboard(sv_search.getFocusView()) }, 100)
        }
    }

    override fun initData() {
        if (chatTarget != null) {
            addFragment(R.id.fl_container, searchLogDetailFragment)
        } else {
            addFragment(R.id.fl_container, searchResultScopeFragment)
        }
    }

    override fun setEvent() {
        iv_back.setOnClickListener { finish() }
        sv_search.setOnSearchCancelListener(object : ChatSearchView.OnSearchCancelListener{
            override fun onSearchCancel() {
                finish()
            }
        })
        sv_search.setOnTextChangeListener(object : ChatSearchView.OnTextChangeListener {
            override fun onTextChange(s: String) {
                if (chatTarget != null) {
                    viewModel.searchChatLogs(s, chatTarget!!)
                } else {
                    viewModel.searchKeywordsScoped(s, scope)
                }
            }
        })
    }
}