package com.fzm.chat33.core.net.api

import com.fuzamei.common.net.rxjava.HttpResult
import com.fzm.chat33.core.bean.AdInfoBean
import com.fzm.chat33.core.bean.ModuleState
import com.fzm.chat33.core.bean.RoomSessionKeys
import com.fzm.chat33.core.bean.UnreadNumber
import retrofit2.http.Body
import retrofit2.http.POST

/**
 * @author zhengjy
 * @since 2019/09/16
 * Description:
 */
interface GeneralService {

    /**
     * 启动次数统计
     *
     */
    @POST("chat/open")
    suspend fun startStatistics(): HttpResult<Any>

    /**
     * 获取启动页广告信息
     *
     */
    @POST("chat/chat33/getAdvertisement")
    suspend fun getSplashAdInfo(): HttpResult<AdInfoBean>

    /**
     * 获取模块启用状态
     *
     */
    @POST("chat/public/module-state")
    suspend fun getModuleState(): HttpResult<ModuleState.Wrapper>

    /**
     * 获取指定时间之前的所有群会话密钥
     *
     * @param map
     */
    @JvmSuppressWildcards
    @POST("chat/chat33/roomSessionKey")
    suspend fun getRoomSessionKeys(@Body map: Map<String, Any>): HttpResult<RoomSessionKeys>

    /**
     * 获取所有未处理的好友请求和入群请求
     */
    @POST("chat/chat33/unreadApplyNumber")
    suspend fun getUnreadApplyNumber(): HttpResult<UnreadNumber>
}