package com.fzm.chat33.core.repo

import com.fzm.chat33.core.source.PromoteDataSource
import javax.inject.Inject

/**
 * @author zhengjy
 * @since 2019/09/16
 * Description:
 */
class PromoteRepository @Inject constructor(
        private val dataSource: PromoteDataSource
) : PromoteDataSource by dataSource