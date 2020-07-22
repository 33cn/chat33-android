package com.fzm.chat33.core.repo

import com.fzm.chat33.core.source.ChatDataSource
import javax.inject.Inject
import javax.inject.Singleton

/**
 * @author zhengjy
 * @since 2019/10/10
 * Description:
 */
@Singleton
class ChatRepository @Inject constructor(
        private val chatData: ChatDataSource
) : ChatDataSource by chatData