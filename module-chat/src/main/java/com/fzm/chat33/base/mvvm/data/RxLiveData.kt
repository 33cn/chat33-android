package com.fzm.chat33.base.mvvm.data

import androidx.annotation.MainThread
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.fuzamei.componentservice.app.Loading
import io.reactivex.disposables.Disposable
import org.reactivestreams.Subscription

/**
 * @author zhengjy
 * @since 2019/03/04
 * Description:用于存放RxJava进行的网络请求的相关数据
 */
class RxLiveData<T>(val loading: SingleLiveEvent<Loading>? = null) : MutableLiveData<T>() {

    private var mDisposable: Disposable? = null

    private var mSubscription: Subscription? = null

    fun setDisposable(disposable: Disposable?) {
        this.mDisposable = disposable
    }

    fun setSubscription(subscription: Subscription?) {
        this.mSubscription = subscription
    }

    @MainThread
    override fun observe(owner: LifecycleOwner, observer: Observer<in T>) {
        super.observe(owner, Observer { t ->
            // 如果loading不为空则关闭loading框
            loading?.value = Loading(false)
            observer.onChanged(t)
        })
    }

    override fun onInactive() {
        if (mDisposable?.isDisposed != false) {
            mDisposable?.dispose()
        }
        mSubscription?.cancel()
    }
}