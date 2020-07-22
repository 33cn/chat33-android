package com.fzm.chat33.core.source.impl

import com.fuzamei.common.net.Result
import com.fuzamei.componentservice.ext.apiCall
import com.fzm.chat33.core.bean.AdInfoBean
import com.fzm.chat33.core.bean.ModuleState
import com.fzm.chat33.core.bean.RoomSessionKeys
import com.fzm.chat33.core.bean.UnreadNumber
import com.fzm.chat33.core.net.api.GeneralService
import com.fzm.chat33.core.source.GeneralDataSource
import javax.inject.Inject

/**
 * @author zhengjy
 * @since 2019/10/08
 * Description:
 */
class NetGeneralDataSource @Inject constructor(
        private val service: GeneralService
) : GeneralDataSource {
    override suspend fun startStatistics(): Result<Any> {
        return apiCall { service.startStatistics() }
    }

    override suspend fun getSplashAdInfo(): Result<AdInfoBean> {
        return apiCall { service.getSplashAdInfo() }
    }

    override suspend fun getModuleState(): Result<ModuleState.Wrapper> {
        return apiCall { service.getModuleState() }
    }

    override suspend fun getRoomSessionKeys(datetime: Long): Result<RoomSessionKeys> {
        val map = mapOf("datetime" to datetime)
        return apiCall { service.getRoomSessionKeys(map) }
    }

    override suspend fun getUnreadApplyNumber(): Result<UnreadNumber> {
        return apiCall { service.getUnreadApplyNumber() }
    }
}