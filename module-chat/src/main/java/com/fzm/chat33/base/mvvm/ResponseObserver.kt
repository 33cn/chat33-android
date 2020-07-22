package com.fzm.chat33.base.mvvm

import androidx.lifecycle.Observer
import com.fuzamei.common.net.rxjava.ApiException
import com.fuzamei.common.utils.ShowUtils
import com.fzm.chat33.R
import com.fzm.chat33.core.Chat33

/**
 * @author zhengjy
 * @since 2019/03/04
 * Description:http请求返回结果的观察类，判断请求是否成功
 */
abstract class ResponseObserver<T> : Observer<ResponseWrapper<T>> {

    override fun onChanged(t: ResponseWrapper<T>?) {
        val data = t?.mData
        if (data == null) {
            val e = t?.mError ?: ApiException(-1, Chat33.getContext().getString(R.string.basic_error_unknown1))
            // 用到了和界面相关的Toast，Context使用了Application，但还是感觉不太好
            ShowUtils.showToast(Chat33.getContext(), e.message)
            onFail(e)
        } else {
            onSuccess(data)
        }
    }

    protected abstract fun onSuccess(data: T)

    protected abstract fun onFail(t: Throwable)
}