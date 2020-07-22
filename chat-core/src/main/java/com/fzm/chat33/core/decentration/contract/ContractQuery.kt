package com.fzm.chat33.core.decentration.contract

import java.io.Serializable

/**
 * @author zhengjy
 * @since 2020/02/05
 * Description:合约查询接口参数
 */
abstract class ContractQuery : Serializable {
    /**
     * 执行器
     */
    var execer: String = ""
    /**
     * 方法名
     */
    var funcName: String = ""

    var payload: Any? = null
}