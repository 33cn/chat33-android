package com.fzm.chat33.core.source.impl

import com.fuzamei.common.net.Result
import com.fuzamei.componentservice.ext.apiCall
import com.fzm.chat33.core.bean.ConditionReward
import com.fzm.chat33.core.bean.PromoteBriefInfo
import com.fzm.chat33.core.bean.PromoteReward
import com.fzm.chat33.core.net.api.PromoteService
import com.fzm.chat33.core.source.PromoteDataSource
import javax.inject.Inject

/**
 * @author zhengjy
 * @since 2019/09/16
 * Description:
 */
class NetPromoteDataSource @Inject constructor(
        private val dataSource: PromoteService
) : PromoteDataSource {
    override suspend fun getPromoteBriefInfo(): Result<PromoteBriefInfo> {
        return apiCall { dataSource.getPromoteBriefInfo() }
    }

    override suspend fun getPromoteRewardList(page: Int, size: Int): Result<PromoteReward.Wrapper> {
        val map = mapOf("page" to page, "size" to size)
        return apiCall { dataSource.getPromoteRewardList(map) }
    }

    override suspend fun getConditionRewardList(page: Int, size: Int): Result<ConditionReward.Wrapper> {
        val map = mapOf("page" to page, "size" to size)
        return apiCall { dataSource.getConditionRewardList(map) }
    }

}