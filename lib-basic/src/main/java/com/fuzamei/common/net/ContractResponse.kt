package com.fuzamei.common.net

import java.io.Serializable

/**
 * @author zhengjy
 * @since 2020/02/05
 * Description:
 */
data class ContractResponse<T>(
        var id: Int,
        var result: T,
        var error: String?
) : Serializable {

    fun isSuccess(): Boolean {
        return error.isNullOrEmpty()
    }
}