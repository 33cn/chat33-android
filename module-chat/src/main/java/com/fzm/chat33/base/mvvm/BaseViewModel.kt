package com.fzm.chat33.base.mvvm

import androidx.lifecycle.ViewModel
import com.fzm.chat33.base.mvvm.data.SingleLiveEvent
import com.fuzamei.componentservice.app.Loading

/**
 * @author zhengjy
 * @since 2019/03/04
 * Description:
 */
open class BaseViewModel: ViewModel() {
    /**
     * 是否显示loading，以及是否可取消
     */
    open val loading: SingleLiveEvent<Loading> = SingleLiveEvent()
}