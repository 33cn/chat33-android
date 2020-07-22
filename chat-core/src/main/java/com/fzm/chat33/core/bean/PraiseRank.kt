package com.fzm.chat33.core.bean

import java.io.Serializable

/**
 * @author zhengjy
 * @since 2019/12/05
 * Description:
 */
data class PraiseRank(
        /**
         * 排名
         */
        var ranking: Int,
        /**
         * 用户信息
         */
        var user: PraiseUser,
        /**
         * 赞赏金额
         */
        var price: Double,
        /**
         * 点赞数量
         */
        var number: Int
) : Serializable {

    data class Wrapper(
            var enterprise: PraiseEnterprise?,
            /**
             * 自己的排名信息
             */
            var mine: PraiseRank?,
            var records: List<PraiseRank>,
            var nextLog: Int,
            var type: Int
    ) : Serializable
}

data class PraiseEnterprise(
        /**
         * 企业名称
         */
        var name: String
) : Serializable

