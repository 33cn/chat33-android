package com.fzm.chat33.core.repo

import com.fzm.chat33.core.source.*
import javax.inject.Inject
import javax.inject.Singleton

/**
 * @author zhengjy
 * @since 2019/09/16
 * Description:
 */
@Singleton
class MainRepository @Inject constructor(
        private val chatData: ChatDataSource,
        private val userData: UserDataSource,
        private val generalData: GeneralDataSource,
        private val settingData: SettingDataSource
) : ChatDataSource by chatData,
        UserDataSource by userData,
        GeneralDataSource by generalData,
        SettingDataSource by settingData