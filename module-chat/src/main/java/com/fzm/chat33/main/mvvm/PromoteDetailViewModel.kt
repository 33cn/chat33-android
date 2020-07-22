package com.fzm.chat33.main.mvvm

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.fuzamei.common.base.mvvm.LifecycleViewModel
import com.fuzamei.common.ext.request
import com.fuzamei.common.ext.result
import com.fuzamei.componentservice.config.AppConfig
import com.fzm.chat33.core.bean.ConditionReward
import com.fzm.chat33.core.bean.PromoteBriefInfo
import com.fzm.chat33.core.bean.PromoteReward
import com.fzm.chat33.core.repo.PromoteRepository
import javax.inject.Inject

/**
 * @author zhengjy
 * @since 2019/08/09
 * Description:
 */
class PromoteDetailViewModel @Inject constructor(
        private val repository: PromoteRepository
) : LifecycleViewModel() {

    private val _promoteDetail by lazy { MutableLiveData<PromoteBriefInfo>() }
    val promoteDetail: LiveData<PromoteBriefInfo>
        get() = _promoteDetail

    private val _promoteReward by lazy { MutableLiveData<PromoteReward.Wrapper>() }
    val promoteReward: LiveData<PromoteReward.Wrapper>
        get() = _promoteReward

    private val _conditionReward by lazy { MutableLiveData<ConditionReward.Wrapper>() }
    val conditionReward: LiveData<ConditionReward.Wrapper>
        get() = _conditionReward

    fun getPromoteBriefInfo() {
        request {
            repository.getPromoteBriefInfo()
        }.result({
            _promoteDetail.value = it
        }, {
            _promoteDetail.value = null
        })
    }

    fun getPromoteRewardList(page: Int) {
        request {
            repository.getPromoteRewardList(page, AppConfig.PAGE_SIZE)
        }.result({
            _promoteReward.value = it
        }, {
            _promoteReward.value = null
        })
    }

    fun getConditionRewardList(page: Int) {
        request {
            repository.getConditionRewardList(page, AppConfig.PAGE_SIZE)
        }.result({
            _conditionReward.value = it
        }, {
            _conditionReward.value = null
        })
    }
}