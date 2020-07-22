package com.fzm.chat33.core.source

import com.fuzamei.common.net.Result
import com.fzm.chat33.core.bean.AdInfoBean
import com.fzm.chat33.core.bean.ModuleState
import com.fzm.chat33.core.bean.RoomSessionKeys
import com.fzm.chat33.core.bean.UnreadNumber

/**
 * @author zhengjy
 * @since 2019/09/16
 * Description:
 */
interface GeneralDataSource {

    /**
     * 启动次数统计
     *
     */
    suspend fun startStatistics(): Result<Any>

    /**
     * 获取启动页广告信息
     *
     */
    suspend fun getSplashAdInfo(): Result<AdInfoBean>

    /**
     * 获取模块启用状态
     *
     */
    suspend fun getModuleState(): Result<ModuleState.Wrapper>

    /**
     * 获取指定时间之前的所有群会话密钥
     *
     * @param datetime  指定时间
     */
    suspend fun getRoomSessionKeys(datetime: Long): Result<RoomSessionKeys>

    /**
     * 获取所有未处理的好友请求和入群请求
     */
    suspend fun getUnreadApplyNumber(): Result<UnreadNumber>
}