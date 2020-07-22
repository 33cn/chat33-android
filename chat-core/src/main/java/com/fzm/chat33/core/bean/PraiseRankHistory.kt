package com.fzm.chat33.core.bean

import java.io.Serializable

/**
 * @author zhengjy
 * @since 2019/12/05
 * Description:
 */
data class PraiseRankHistory(
        /**
         * 点赞排名
         */
        var like: Like,
        /**
         * 打赏排名
         */
        var reward: Reward,
        /**
         * 排行榜统计起始时间
         */
        var startTime: Long,
        /**
         * 排行榜统计结束
         */
        var endTime: Long
) : Serializable {

    data class Like(
            var ranking: Int,
            var number: Int
    ) : Serializable

    data class Reward(
            var ranking: Int,
            var price: Double
    ) : Serializable

    data class Wrapper(
            var records: List<PraiseRankHistory>
    ) : Serializable
}