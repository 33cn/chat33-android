package com.fuzamei.common.base.mvvm

import androidx.lifecycle.*
import kotlinx.coroutines.CoroutineScope
import kotlin.coroutines.CoroutineContext

/**
 * @author zhengjy
 * @since 2019/08/09
 * Description:使用[viewModelScope]在ViewModel销毁时取消所有相应的任务
 */
open class LifecycleViewModel : ViewModel(), CoroutineScope {

    override val coroutineContext: CoroutineContext
        get() = viewModelScope.coroutineContext
}