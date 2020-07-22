package com.fzm.chat33.core.source.impl

import com.fuzamei.common.net.Result
import com.fuzamei.componentservice.config.AppConfig
import com.fuzamei.componentservice.ext.apiCall3
import com.fzm.chat33.core.decentration.contract.*
import com.fzm.chat33.core.net.api.ContractService
import com.fzm.chat33.core.source.ContractDataSource
import com.fzm.chat33.core.utils.UserInfoPreference
import com.fzm.chat33.core.utils.UserInfoPreference.USER_PRIVATE_KEY
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map

/**
 * @author zhengjy
 * @since 2020/02/06
 * Description:
 */
class NetContractDataSource(
        private val service: ContractService
) : ContractDataSource {
    override suspend fun createNoBalanceTx(private: String, txHex: String): Result<String> {
        val request = ContractRequest.create(
                "Chain33.CreateNoBalanceTransaction",
                TransactionParams.createNoBalanceTx(private, txHex)
        )
        return apiCall3 { service.createNoBalanceTx(request) }
    }

    override suspend fun signRawTx(private: String, txHex: String): Result<String> {
        val request = ContractRequest.create(
                "Chain33.SignRawTx",
                TransactionParams.createSign(private, txHex)
        )
        return apiCall3 { service.signRawTx(request) }
    }

    override suspend fun sendTransaction(data: String): Result<String> {
        val request = ContractRequest.create("Chain33.SendTransaction", SendTxParams(data))
        return apiCall3 { service.sendTransaction(request) }
    }

    override suspend fun handleTransaction(action: suspend ContractDataSource.() -> Result<String>): Result<String> {
        val channel = Channel<Result<String>>(1)
        flow {
            emit(action())
        }.map {
            if (it.isSucceed()) {
                createNoBalanceTx(AppConfig.NO_BALANCE_PRIVATE_KEY, it.data())
            } else {
                Result.Error(it.error())
            }
        }.map {
            if (it.isSucceed()) {
                signRawTx(UserInfoPreference.getInstance().getStringPref(USER_PRIVATE_KEY, ""), it.data())
            } else {
                Result.Error(it.error())
            }
        }.map {
            if (it.isSucceed()) {
                sendTransaction(it.data())
            } else {
                Result.Error(it.error())
            }
        }.collect {
            channel.send(it)
        }
        return channel.receive()
    }

    override suspend fun getFriendList(query: UsersQuery): Result<UserAddress.Wrapper> {
        val request = ContractRequest.createQuery(query)
        return apiCall3 { service.getFriendList(request) }
    }

    override suspend fun getBlockList(query: UsersQuery): Result<UserAddress.Wrapper> {
        val request = ContractRequest.createQuery(query)
        return apiCall3 { service.getBlockList(request) }
    }

    override suspend fun modifyFriend(address: List<String?>, type: Int): Result<String> {
        val friendAddress = address.flatMap {
            listOf(mapOf("friendAddress" to it, "type" to type))
        }
        val request = ContractRequest.create(
                "chat.CreateRawUpdateFriendTx",
                mapOf("friends" to friendAddress)
        )
        return apiCall3 { service.modifyFriend(request) }
    }

    override suspend fun modifyBlock(address: List<String?>, type: Int): Result<String> {
        val friendAddress = address.flatMap {
            listOf(mapOf("targetAddress" to it, "type" to type))
        }
        val request = ContractRequest.create(
                "chat.CreateRawUpdateBlockTx",
                mapOf("list" to friendAddress)
        )
        return apiCall3 { service.modifyBlock(request) }
    }
}