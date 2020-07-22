package com.fzm.chat33.main.mvvm

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.fuzamei.componentservice.app.LoadingViewModel
import com.fzm.chat33.core.bean.SearchResult
import com.fzm.chat33.core.bean.SearchScope
import com.fzm.chat33.core.bean.ChatTarget
import com.fzm.chat33.core.db.ChatDatabase
import com.fzm.chat33.core.db.bean.SearchHistory
import com.fzm.chat33.core.global.LoginInfoDelegate
import com.fzm.chat33.core.repo.SearchRepository
import io.reactivex.subjects.PublishSubject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit
import javax.inject.Inject

/**
 * @author zhengjy
 * @since 2019/09/17
 * Description:
 */
class SearchLocalViewModel @Inject constructor(
        private val repository: SearchRepository
) : LoadingViewModel(), LoginInfoDelegate by repository {

    private fun historyDao() = ChatDatabase.getInstance().searchHistoryDao()

    /**
     * 搜索历史记录列表
     *
     * @return [LiveData]
     */
    fun searchHistory() = historyDao().getSearchHistory()

    /**
     * 当前搜索框内的关键词
     */
    private val _searchKey by lazy { MutableLiveData<String>() }
    val searchKey: LiveData<String>
        get() = _searchKey

    /**
     * 聚合搜索结果
     */
    private val _searchResult by lazy { MutableLiveData<SearchResult.Wrapper>() }
    val searchResult: LiveData<SearchResult.Wrapper>
        get() = _searchResult

    /**
     * 分类搜索结果
     */
    private val _searchScopeResult by lazy { MutableLiveData<SearchResult.Wrapper>() }
    val searchScopeResult: LiveData<SearchResult.Wrapper>
        get() = _searchScopeResult

    /**
     * 聊天记录搜索结果
     */
    private val _searchChatLogs by lazy { MutableLiveData<SearchResult>() }
    val searchChatLogs: LiveData<SearchResult>
        get() = _searchChatLogs

    private val historyEvent: PublishSubject<String> = PublishSubject.create()

    private val disposable = historyEvent.debounce(1500, TimeUnit.MILLISECONDS)
            .filter { it == searchKey.value }
            .subscribe {
                // 1.5秒后如果搜索关键词没有变，则保存搜索记录
                historyDao().insert(SearchHistory(it, System.currentTimeMillis()))
            }

    /**
     * 提供外部手动修改搜索关键字，建议尽量不使用
     */
    fun initSearchKey(keywords: String?) {
        _searchKey.value = keywords
    }

    /**
     * 全局搜索关键字
     *
     * @param keywords      关键字
     */
    fun searchKeywords(keywords: String) = launch {
        _searchKey.value = keywords
        if (keywords.isEmpty()) {
            _searchResult.value = SearchResult.Wrapper(keywords, null)
            return@launch
        }
        _searchResult.value = withContext(Dispatchers.IO) {
            historyEvent.onNext(keywords)
            repository.searchDataScoped(keywords, SearchScope.ALL)
        }
    }

    /**
     * 从全局搜索结果中，过滤出指定范围的内容
     *
     * @param   scope 搜索范围
     * @see     [SearchScope]
     */
    fun getSearchResultByScope(scope: Int): List<SearchResult>? {
        return _searchResult.value?.list?.filter {
            scope == it.searchScope || scope == SearchScope.ALL
        }
    }

    /**
     * 在指定的范围内搜索关键字
     *
     * @param   keywords  关键字
     * @param   scope     搜索范围
     * @see     [SearchScope]
     */
    fun searchKeywordsScoped(keywords: String, scope: Int) = launch {
        _searchKey.value = keywords
        if (keywords.isEmpty()) {
            _searchScopeResult.value = SearchResult.Wrapper(keywords, null)
            return@launch
        }
        _searchScopeResult.value = withContext(Dispatchers.IO) {
            repository.searchDataScoped(keywords, scope)
        }
    }

    /**
     * 搜索指定对象的聊天记录
     *
     * @param keywords  关键字
     * @param target    需要查询聊天记录的对象
     */
    fun searchChatLogs(keywords: String, target: ChatTarget) = launch {
        _searchKey.value = keywords
        if (keywords.isEmpty()) {
            _searchChatLogs.value = null
            return@launch
        }
        _searchChatLogs.value = withContext(Dispatchers.IO) {
            val chatLogs = repository.searchChatLogs(keywords, target)
            SearchResult(SearchScope.CHATLOG, keywords, target.targetId, null, null, null, chatLogs)
        }
    }

    /**
     * 清除搜索历史记录
     */
    fun clearSearchHistory() {
        historyDao().deleteAllHistory()
    }

    /**
     * 清除指定搜索历史记录
     *
     * @param key    搜索记录的关键词
     */
    fun deleteSearchHistory(key: String?) {
        if (!key.isNullOrEmpty()) {
            historyDao().deleteHistory(key)
        }
    }

    override fun onCleared() {
        super.onCleared()
        disposable.dispose()
    }
}