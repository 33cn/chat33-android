package com.fzm.chat33.base.mvvm

import com.fuzamei.common.net.rxjava.ApiException
import com.fzm.chat33.base.mvvm.data.RxLiveData
import com.fzm.chat33.base.mvvm.data.SingleLiveEvent
import com.fuzamei.componentservice.app.Loading
import com.google.gson.JsonSyntaxException
import io.reactivex.Observer
import io.reactivex.disposables.Disposable
import io.reactivex.exceptions.CompositeException
import org.reactivestreams.Subscriber
import org.reactivestreams.Subscription
import retrofit2.HttpException
import java.io.IOException
import java.net.MalformedURLException
import java.security.cert.CertificateException
import javax.net.ssl.SSLHandshakeException

/**
 * @author zhengjy
 * @since 2019/03/04
 * Description:http请求返回结果的包装类，包含数据或错误信息
 */
open class ResponseWrapper<T>(
        /**
         * 观察当前请求返回结果对象的LiveData对象
         */
        private val mRespLiveData: RxLiveData<ResponseWrapper<T>>,
        /**
         * 控制loading框的显示
         */
        private val mLoading: SingleLiveEvent<Loading>? = null
) : Observer<T>, Subscriber<T> {

    var mData: T? = null
        private set

    var mError: Throwable? = null
        private set

    override fun onSubscribe(s: Subscription?) {
        mRespLiveData.setSubscription(s)
        s?.request(1)
        // 如果mLoading不为空则显示loading框
        mLoading?.value = Loading()
    }

    override fun onSubscribe(d: Disposable) {
        mRespLiveData.setDisposable(d)
        // 如果mLoading不为空则显示loading框
        mLoading?.value = Loading()
    }

    override fun onNext(t: T) {
        mData = t
        mError = null
        mRespLiveData.value = this
    }

    override fun onError(t: Throwable) {
        mData = null
        mError = if (t is CertificateException || t is SSLHandshakeException) {
            ApiException(1)
        } else if (t is MalformedURLException) {
            ApiException(2)
        } else if (t is HttpException) {
            ApiException(3)
        } else if (t is JsonSyntaxException) {
            ApiException(5)
        } else if (t is IOException || t is CompositeException) {
            ApiException(6)
        } else if (t is ApiException) {
            t
        } else {
            ApiException(t)
        }
        mRespLiveData.value = this
    }

    override fun onComplete() {

    }
}