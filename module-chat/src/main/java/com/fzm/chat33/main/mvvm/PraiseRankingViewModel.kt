package com.fzm.chat33.main.mvvm

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.fuzamei.common.ext.request
import com.fuzamei.common.ext.result
import com.fuzamei.common.ext.start
import com.fuzamei.componentservice.app.LoadingViewModel
import com.fzm.chat33.core.bean.PraiseRank
import com.fzm.chat33.core.bean.PraiseRankHistory
import com.fzm.chat33.core.bean.param.PraiseRankingParam
import com.fzm.chat33.core.global.LoginInfoDelegate
import com.fzm.chat33.core.repo.ChatRepository
import javax.inject.Inject

/**
 * @author zhengjy
 * @since 2019/12/05
 * Description:
 */
class PraiseRankingViewModel @Inject constructor(
        private val repository: ChatRepository,
        loginDelegate: LoginInfoDelegate
) : LoadingViewModel(), LoginInfoDelegate by loginDelegate {

    private val _rankList by lazy { MutableLiveData<PraiseRank.Wrapper>() }
    val rankList: LiveData<PraiseRank.Wrapper>
        get() = _rankList

    private val _rankHistory by lazy { MutableLiveData<PraiseRankHistory.Wrapper>() }
    val rankHistory: LiveData<PraiseRankHistory.Wrapper>
        get() = _rankHistory

    private var historyPage = 1

    fun getRankList(param: PraiseRankingParam, show: Boolean) {
        if (param.startId == -1) {
            return
        }
        val requestType = param.type
        start {
            if (show) {
                loading()
            }
        }.request {
            repository.praiseRanking(param)
        }.result(onSuccess = {
            _rankList.value = it.apply { type = requestType }
        }, onError = {
            _rankList.value = null
        }, onComplete = {
            dismiss()
        })
    }

    fun getRankHistory(size: Int, show: Boolean) {
        start {
            if (show) {
                loading()
            }
        }.request {
            repository.praiseRankingHistory(historyPage, size)
        }.result(onSuccess = {
            historyPage++
            _rankHistory.value = it
        }, onComplete = {
            dismiss()
        })
    }
}
