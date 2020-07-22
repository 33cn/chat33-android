package com.fzm.chat33.main.fragment

import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.view.View
import androidx.core.content.ContextCompat
import com.fuzamei.common.recycleviewbase.CommonAdapter
import com.fuzamei.common.recycleviewbase.MultiItemTypeAdapter
import com.fuzamei.common.recycleviewbase.RecyclerViewDivider
import com.fuzamei.common.recycleviewbase.ViewHolder
import com.fuzamei.common.utils.KeyboardUtils
import com.fuzamei.componentservice.base.DILoadableFragment
import com.fuzamei.componentservice.ext.findViewModel
import com.fzm.chat33.R
import com.fzm.chat33.core.db.bean.SearchHistory
import com.fzm.chat33.main.mvvm.SearchLocalViewModel
import kotlinx.android.synthetic.main.fragment_search_history.*
import javax.inject.Inject

/**
 * @author zhengjy
 * @since 2019/09/17
 * Description:本地搜索历史记录
 */
class SearchHistoryFragment : DILoadableFragment() {
    
    private lateinit var mAdapter: CommonAdapter<SearchHistory>
    private val historyList: MutableList<SearchHistory> = mutableListOf()

    @Inject
    lateinit var provider: ViewModelProvider.Factory
    private lateinit var viewModel: SearchLocalViewModel

    override fun getLayoutId(): Int {
        return R.layout.fragment_search_history
    }

    override fun initView(view: View?, savedInstanceState: Bundle?) {
        viewModel = findViewModel(provider)
        rv_history.layoutManager = LinearLayoutManager(activity)
        rv_history.addItemDecoration(RecyclerViewDivider(activity, LinearLayoutManager.VERTICAL,
                0.5f, ContextCompat.getColor(activity, R.color.chat_divide_light)))
        mAdapter = object : CommonAdapter<SearchHistory>(activity, R.layout.item_local_search_history, historyList) {
            override fun convert(holder: ViewHolder?, t: SearchHistory?, position: Int) {
                holder?.setText(R.id.tv_words, t?.keywords)
                holder?.setOnClickListener(R.id.iv_delete) {
                    viewModel.deleteSearchHistory(t?.keywords)
                }
            }
        }
        mAdapter.setOnItemClickListener(object : MultiItemTypeAdapter.OnItemClickListener {
            override fun onItemClick(view: View?, holder: RecyclerView.ViewHolder?, position: Int) {
                viewModel.searchKeywords(historyList[position].keywords)
                KeyboardUtils.hideKeyboard(view)
            }

            override fun onItemLongClick(view: View?, holder: RecyclerView.ViewHolder?, position: Int): Boolean {
                return true
            }
        })
        rv_history.adapter = mAdapter
    }

    override fun initData() {
        viewModel.searchHistory().observe(this, Observer {
            historyList.clear()
            if (it != null) {
                historyList.addAll(it)
            }
            mAdapter.notifyDataSetChanged()
            if (historyList.size == 0) {
                tv_title.visibility = View.GONE
                tv_clear_history.visibility = View.GONE
            } else {
                tv_title.visibility = View.VISIBLE
                tv_clear_history.visibility = View.VISIBLE
            }
        })
    }

    override fun setEvent() {
        tv_clear_history.setOnClickListener { viewModel.clearSearchHistory() }
    }

}