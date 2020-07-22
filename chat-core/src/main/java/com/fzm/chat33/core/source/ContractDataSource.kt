package com.fzm.chat33.core.source

import com.fuzamei.common.net.Result
import com.fzm.chat33.core.decentration.contract.UserAddress
import com.fzm.chat33.core.decentration.contract.UsersQuery

/**
 * @author zhengjy
 * @since 2020/02/06
 * Description:合约数据源
 */
interface ContractDataSource {

    /**
     * 创建代扣交易
     */
    suspend fun createNoBalanceTx(private: String, txHex: String): Result<String>

    /**
     * 签名交易
     */
    suspend fun signRawTx(private: String, txHex: String): Result<String>

    /**
     * 发送交易
     */
    suspend fun sendTransaction(data: String): Result<String>

    /**
     * 签名发送交易，请求完合约接口之后，进行签名和发送交易
     */
    suspend fun handleTransaction(action: suspend ContractDataSource.() -> Result<String>): Result<String>

    /**
     * 获取好友列表
     *
     * @param query  查询参数
     */
    suspend fun getFriendList(query: UsersQuery): Result<UserAddress.Wrapper>

    /**
     * 获取黑名单列表
     *
     * @param query  查询参数
     */
    suspend fun getBlockList(query: UsersQuery): Result<UserAddress.Wrapper>

    /**
     * 修改好友
     *
     * @param address   好友地址
     * @param type      操作类型，1：添加好友 2：删除好友
     */
    suspend fun modifyFriend(address: List<String?>, type: Int): Result<String>

    /**
     * 修改黑名单
     *
     * @param address   目标地址
     * @param type      操作类型，1：添加黑名单 2：删除黑名单
     */
    suspend fun modifyBlock(address: List<String?>, type: Int): Result<String>
}