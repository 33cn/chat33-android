package com.fzm.chat33.core.decentration.contract

import java.io.Serializable

/**
 * @author zhengjy
 * @since 2020/02/05
 * Description:合约查询接口参数固定结构
 */
data class ContractRequest(
        var jsonrpc: String?,
        var id: Int,
        var method: String?,
        var params: List<Any?>
) : Serializable {

    companion object {
        /**
         * 创建一个合约查询请求
         *
         * @param param 合约参数
         */
        @JvmStatic
        fun createQuery(param: Any?): ContractRequest {
            return ContractRequest("2.0", 1, "Chain33.Query", listOf(param))
        }

        /**
         * 创建一个合约请求
         *
         * @param method    合约方法
         * @param param     合约参数
         */
        @JvmStatic
        fun create(method: String?, param: Any?): ContractRequest {
            return ContractRequest("2.0", 1, method, listOf(param))
        }
    }
}