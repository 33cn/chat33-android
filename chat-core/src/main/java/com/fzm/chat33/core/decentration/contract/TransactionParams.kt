package com.fzm.chat33.core.decentration.contract

import java.io.Serializable

/**
 * @author zhengjy
 * @since 2020/02/07
 * Description:
 */
data class TransactionParams(
        var addr: String?,
        var privkey: String?,
        var txHex: String,
        var expire: String?,
        var index: Int,
        var token: String?,
        var fee: Long,
        var newToAddr: String?
) : Serializable {

    companion object {
        @JvmStatic
        fun createSign(private: String, txHex: String): TransactionParams {
            return TransactionParams(null, private, txHex, "1h", 2, null, 0L, null)
        }

        @JvmStatic
        fun createNoBalanceTx(private: String, txHex: String): TransactionParams {
            return TransactionParams(null, private, txHex, "1h", 0, null, 0L, null)
        }
    }
}