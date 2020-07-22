package com.fzm.chat33.core.bean

import java.io.Serializable

/**
 * 创建日期：2019/11/19
 * 描述:赞赏详情
 * 作者:yll
 */
data class PraiseBean(
        var channelType: Int,
        var recordId: String,
        var logId: String,
        var createTime: Long,
        var user: PraiseUser,
        var type: Int,
        var coinName: String,
        var amount: Double
) : Serializable {

    data class Wrapper(
            var records: List<PraiseBean>,
            var nextLog: String
    ) : Serializable
}

