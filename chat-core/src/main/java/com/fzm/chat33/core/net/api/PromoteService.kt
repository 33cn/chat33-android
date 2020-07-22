package com.fzm.chat33.core.net.api

import com.fuzamei.common.net.rxjava.HttpResult
import com.fzm.chat33.core.bean.ConditionReward
import com.fzm.chat33.core.bean.PromoteBriefInfo
import com.fzm.chat33.core.bean.PromoteReward
import retrofit2.http.Body
import retrofit2.http.POST

/**
 * @author zhengjy
 * @since 2019/09/16
 * Description:
 */
interface PromoteService {
    /**
     * 获取用户推广概况信息
     *
     */
    @POST("chat/user/invite-statistics")
    suspend fun getPromoteBriefInfo(): HttpResult<PromoteBriefInfo>

    /**
     * 获取用户邀请推广奖励记录
     *
     */
    @JvmSuppressWildcards
    @POST("chat/user/single-invite-info")
    suspend fun getPromoteRewardList(@Body map: Map<String, Any>): HttpResult<PromoteReward.Wrapper>

    /**
     * 获取用户条件推广奖励记录
     *
     */
    @JvmSuppressWildcards
    @POST("chat/user/accumulate-invite-info")
    suspend fun getConditionRewardList(@Body map: Map<String, Any>): HttpResult<ConditionReward.Wrapper>
}