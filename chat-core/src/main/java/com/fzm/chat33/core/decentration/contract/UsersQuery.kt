package com.fzm.chat33.core.decentration.contract

import com.fuzamei.componentservice.config.AppConfig
import com.fzm.chat33.core.manager.CipherManager
import java.io.Serializable

/**
 * @author zhengjy
 * @since 2020/02/05
 * Description:合约查询好友参数
 */
class UsersQuery private constructor(
        funcName: String,
        mainAddress: String,
        count: Int,
        index: String
) : ContractQuery() {

    companion object {

        fun friendsQuery(mainAddress: String, index: String): UsersQuery {
            return UsersQuery("GetFriends", mainAddress, AppConfig.PAGE_SIZE * 2, index)
        }

        fun blockQuery(mainAddress: String, index: String): UsersQuery {
            return UsersQuery("GetBlockList", mainAddress, AppConfig.PAGE_SIZE * 2, index)
        }
    }

    init {
        execer = "chat"
        this.funcName = funcName
        val time = System.currentTimeMillis()
        val signature = mapOf(
                "mainAddress" to mainAddress,
                "count" to count,
                "index" to index,
                "time" to time
        ).sign(CipherManager.getPrivateKey())
        payload = Params(mainAddress, count, index, time, Signature.create(signature))
    }

    data class Params(
            var mainAddress: String,
            var count: Int,
            var index: String,
            var time: Long,
            var sign: Signature
    ) : Serializable

    data class Signature(
            var publicKey: String,
            var signature: String
    ) : Serializable {

        companion object {
            fun create(signature: String): Signature {
                return Signature(CipherManager.getPublicKey(), signature)
            }
        }
    }
}