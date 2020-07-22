package com.fzm.chat33.core.net.api

import com.fuzamei.common.net.ContractResponse
import com.fzm.chat33.core.decentration.contract.ContractRequest
import com.fzm.chat33.core.decentration.contract.UserAddress
import retrofit2.http.Body
import retrofit2.http.POST

/**
 * @author zhengjy
 * @since 2020/02/06
 * Description:
 */
interface ContractService {

    /**
     * 创建代扣交易
     */
    @POST("/")
    suspend fun createNoBalanceTx(@Body request: ContractRequest): ContractResponse<String>

    /**
     * 签名交易
     */
    @POST("/")
    suspend fun signRawTx(@Body request: ContractRequest): ContractResponse<String>

    /**
     * 发送交易
     */
    @POST("/")
    suspend fun sendTransaction(@Body request: ContractRequest): ContractResponse<String>

    /**
     * 获取好友列表
     *
     * @param request 合约请求参数
     */
    @POST("/")
    suspend fun getFriendList(@Body request: ContractRequest): ContractResponse<UserAddress.Wrapper>

    /**
     * 获取黑名单列表
     *
     * @param request 合约请求参数
     */
    @POST("/")
    suspend fun getBlockList(@Body request: ContractRequest): ContractResponse<UserAddress.Wrapper>

    /**
     * 修改好友
     *
     * @param request 合约请求参数
     */
    @POST("/")
    suspend fun modifyFriend(@Body request: ContractRequest): ContractResponse<String>

    /**
     * 修改黑名单
     *
     * @param request 合约请求参数
     */
    @POST("/")
    suspend fun modifyBlock(@Body request: ContractRequest): ContractResponse<String>
}