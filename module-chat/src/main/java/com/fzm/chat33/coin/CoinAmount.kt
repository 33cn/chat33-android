package com.fzm.chat33.coin

import java.io.Serializable

/**
 * @author zhengjy
 * @since 2019/11/26
 * Description:打赏红包预选金额数目
 */
data class CoinAmount(
        var coinName: String,
        var preAmount: List<PreAmount>
) : Serializable

data class PreAmount(
        var amount: String,
        var selected: Boolean = false
) : Serializable