package com.fzm.chat33.core.bean.param

import com.fuzamei.componentservice.config.AppConfig
import java.io.Serializable

/**
 * @author zhengjy
 * @since 2019/12/05
 * Description:
 */
data class PraiseRankingParam(
        /**
         * 列表类型，1：点赞  2：打赏
         */
        var type: Int,
        var startTime: Long = 0,
        var endTime: Long = 0,
        var startId: Int?,
        var number: Int = AppConfig.PAGE_SIZE
) : Serializable