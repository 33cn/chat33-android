package com.fzm.chat33.main.activity

import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.alibaba.android.arouter.facade.annotation.Route
import com.fuzamei.common.utils.KeyboardUtils
import com.fuzamei.componentservice.app.AppRoute
import com.fuzamei.componentservice.base.DILoadableActivity
import com.fuzamei.componentservice.ext.findViewModel
import com.fzm.chat33.R
import com.fzm.chat33.main.fragment.SearchHistoryFragment
import com.fzm.chat33.main.fragment.SearchResultFragment
import com.fzm.chat33.main.mvvm.SearchLocalViewModel
import com.fzm.chat33.widget.ChatSearchView
import kotlinx.android.synthetic.main.activity_search_local.*
import javax.inject.Inject

/**
 * @author zhengjy
 * @since 2019/09/17
 * Description:搜索页面
 */
@Route(path = AppRoute.SEARCH_LOCAL)
class SearchLocalActivity : DILoadableActivity() {

    @Inject
    lateinit var provider: ViewModelProvider.Factory
    private lateinit var viewModel: SearchLocalViewModel

    private val searchHistoryFragment by lazy { SearchHistoryFragment() }
    private val searchResultFragment by lazy { SearchResultFragment() }

    override fun enableSlideBack(): Boolean {
        return true
    }

    override fun getLayoutId(): Int {
        return R.layout.activity_search_local
    }

    override fun initView() {
        viewModel = findViewModel(provider)
    }

    override fun initData() {
        addFragment(R.id.fl_container, searchHistoryFragment)
        addFragment(R.id.fl_container, searchResultFragment)
        showFragment(searchHistoryFragment)
        hideFragment(searchResultFragment)
    }

    override fun onBackPressed() {
        sv_search.cancel()
    }

    override fun setEvent() {
        sv_search.postDelayed({ KeyboardUtils.showKeyboard(sv_search.getFocusView()) }, 100)
        sv_search.setOnSearchCancelListener(object : ChatSearchView.OnSearchCancelListener{
            override fun onSearchCancel() {
                finish()
            }
        })
        sv_search.setOnTextChangeListener(object : ChatSearchView.OnTextChangeListener {
            override fun onTextChange(s: String) {
                viewModel.searchKeywords(s)
                if (s.isNotEmpty()) {
                    showFragment(searchResultFragment)
                    hideFragment(searchHistoryFragment)
                }
            }
        })
        viewModel.searchKey.observe(this, Observer {
            if (sv_search.getText() != it) {
                sv_search.setTextWithoutWatcher(it)
                if (it.isNotEmpty()) {
                    showFragment(searchResultFragment)
                    hideFragment(searchHistoryFragment)
                }
            }
        })
        viewModel.searchResult.observe(this, Observer {
            if (it.list == null) {
                showFragment(searchHistoryFragment)
                hideFragment(searchResultFragment)
            }
        })
    }
}