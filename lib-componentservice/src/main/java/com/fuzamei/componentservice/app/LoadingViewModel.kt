package com.fuzamei.componentservice.app

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.fuzamei.common.base.mvvm.LifecycleViewModel

/**
 * @author zhengjy
 * @since 2019/08/15
 * Description:带加载框的ViewModel
 */
open class LoadingViewModel : LifecycleViewModel() {

    private val _loading: MutableLiveData<Loading> by lazy { MutableLiveData<Loading>() }
    val loading: LiveData<Loading>
        get() = _loading

    fun loading() {
        _loading.value = Loading()
    }

    fun loading(cancelable: Boolean) {
        _loading.value = Loading(true, cancelable)
    }

    fun dismiss() {
        _loading.value = Loading(false)
    }
}