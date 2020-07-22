package com.fzm.chat33.core.repo

import com.fuzamei.common.net.Result
import com.fzm.chat33.core.global.LoginInfoDelegate
import com.fzm.chat33.core.global.UserInfo
import com.fzm.chat33.core.response.StateResponse
import com.fzm.chat33.core.source.SettingDataSource
import com.fzm.chat33.core.utils.UserInfoPreference
import javax.inject.Inject
import javax.inject.Singleton

/**
 * @author zhengjy
 * @since 2019/09/16
 * Description:
 */
@Singleton
class SettingRepository @Inject constructor(
        private val dataSource: SettingDataSource,
        private val loginInfoDelegate: LoginInfoDelegate
) : SettingDataSource by dataSource, LoginInfoDelegate by loginInfoDelegate {

    override suspend fun isSetPayPassword(): Result<StateResponse> {
        val result = dataSource.isSetPayPassword()
        if(result.isSucceed()) {
            val stateResponse = result.data()
            UserInfoPreference.getInstance().setBooleanPref(UserInfoPreference.SET_PAY_PASSWORD, stateResponse.state == 1)
            UserInfo.getInstance().setIsSetPayPwd(stateResponse.state)
        }
        return result
    }

}