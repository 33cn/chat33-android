package com.fzm.chat33.core.source

import com.fuzamei.common.net.Result
import com.fzm.chat33.core.bean.ConditionReward
import com.fzm.chat33.core.bean.PromoteBriefInfo
import com.fzm.chat33.core.bean.PromoteReward

/**
 * @author zhengjy
 * @since 2019/09/16
 * Description:
 */
interface PromoteDataSource {
    /**
     * 获取用户推广概况信息
     *
     */
    suspend fun getPromoteBriefInfo(): Result<PromoteBriefInfo>

    /**
     * 获取用户邀请推广奖励记录
     *
     */
    suspend fun getPromoteRewardList(page: Int, size: Int): Result<PromoteReward.Wrapper>

    /**
     * 获取用户条件推广奖励记录
     *
     */
    suspend fun getConditionRewardList(page: Int, size: Int): Result<ConditionReward.Wrapper>
}