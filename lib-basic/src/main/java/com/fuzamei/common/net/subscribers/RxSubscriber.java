package com.fuzamei.common.net.subscribers;

import android.app.Activity;
import androidx.annotation.NonNull;
import android.text.TextUtils;

import com.fuzamei.common.net.rxjava.ApiException;
import com.fuzamei.common.utils.NetworkUtils;
import com.fuzamei.common.utils.ShowUtils;
import com.google.gson.JsonSyntaxException;

import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import java.io.InterruptedIOException;
import java.lang.ref.WeakReference;
import java.net.MalformedURLException;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.security.cert.CertificateException;
import java.util.concurrent.TimeoutException;

import javax.net.ssl.SSLHandshakeException;

import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import io.reactivex.exceptions.CompositeException;
import retrofit2.HttpException;

/**
 * @author zhengjy
 * @since 2018/07/27
 * Description:
 */
public class RxSubscriber<T> implements Subscriber<T>, Observer<T>, Cancelable {

    private Subscription subscription;
    private Disposable disposable;

    private OnSubscribeListener<T> listener;
    private WeakReference<Loadable> loadable;
    // 是否显示加载窗口
    private boolean showLoading;
    // 是否可以取消加载窗口
    private boolean cancelable;
    // 加载窗口显示的动画
    @Deprecated
    private int resId;
    private T place;

    public RxSubscriber(@NonNull OnSubscribeListener<T> listener) {
        this(listener, null, false, true, 0);
    }

    public RxSubscriber(@NonNull OnSubscribeListener<T> listener, Loadable loadable) {
        this(listener, loadable, false, true, 0);
    }

    public RxSubscriber(@NonNull OnSubscribeListener<T> listener, Loadable loadable, boolean showLoading) {
        this(listener, loadable, showLoading, true, 0);
    }

    public RxSubscriber(@NonNull OnSubscribeListener<T> listener, Loadable loadable, boolean showLoading, boolean cancelable) {
        this(listener, loadable, showLoading, cancelable, 0);
    }

    public RxSubscriber(@NonNull OnSubscribeListener<T> listener, Loadable loadable, int resId) {
        this(listener, loadable, true, true, resId);
    }

    public RxSubscriber(@NonNull OnSubscribeListener<T> listener, Loadable loadable, boolean showLoading, boolean cancelable, int resId) {
        this.listener = listener;
        this.loadable = new WeakReference<>(loadable);
        this.showLoading = showLoading;
        this.cancelable = cancelable;
        this.resId = resId;
    }

    protected void showLoading() {
        if (loadable != null && loadable.get() != null && showLoading) {
            loadable.get().loading(cancelable);
        }
    }

    protected void hideLoading() {
        if (loadable != null && loadable.get() != null && showLoading) {
            loadable.get().dismiss();
        }
    }

    @Override
    public void cancel() {
        hideLoading();
        if (subscription != null) {
            subscription.cancel();
            subscription = null;
        }
        if (disposable != null && !disposable.isDisposed()) {
            disposable.dispose();
            disposable = null;
        }
    }

    @Override
    public void onSubscribe(Subscription s) {
        this.subscription = s;
        if (!NetworkUtils.isConnected()) {
            onError(new ApiException(7));
            return;
        }
        subscription.request(1);
        showLoading();
    }

    @Override
    public void onSubscribe(Disposable d) {
        this.disposable = d;
        showLoading();
    }

    @Override
    public void onNext(T t) {
        if (loadable != null && loadable.get() != null && loadable.get() instanceof Activity) {
            if (((Activity) loadable.get()).isFinishing()) {
                return;
            }
        }
        hideLoading();
        listener.onSuccess(t);
        if (subscription != null) {
            subscription.request(1);
        }
    }

    @Override
    public void onError(Throwable t) {
        hideLoading();
        ApiException e;
        if (t == null) {
            e = new ApiException(0);
        } else if (t instanceof CertificateException || t instanceof SSLHandshakeException) {
            e = new ApiException(1);
        } else if (t instanceof MalformedURLException) {
            e = new ApiException(2);
        } else if (t instanceof HttpException) {
            e = new ApiException(3);
        } else if (t instanceof InterruptedIOException || t instanceof SocketException || t instanceof TimeoutException || t instanceof UnknownHostException) {
            e = new ApiException(4);
        } else if (t instanceof NullPointerException && t.getMessage().contains("The mapper function returned a null value")) {
            // 表示该接口成功但并无返回值   "data": null
//            e = new ApiException(0:, "数据为空");
            listener.onSuccess(place);
            return;
        } else if (t instanceof JsonSyntaxException) {
            e = new ApiException(5);
        } else if (t instanceof CompositeException) {
            // RxJava合并请求中有请求失败了
            e = new ApiException(6);
        } else if (t instanceof ApiException ) {
            e = (ApiException) t;
        } else {
            e = new ApiException(t);
        }
        if (loadable != null && loadable.get() != null && !TextUtils.isEmpty(e.getMessage())) {
            // 临时加入判断条件
            if (e.getErrorCode() != -1              // 忽略错误
                    && e.getErrorCode() != -4001    // 红包已领完
                    && e.getErrorCode() != -4009    // 红包已领取
                    && e.getErrorCode() != -4013    // 红包已过期
                    && e.getErrorCode() != -2030    // 帐号被管理员封禁
            ) {
                ShowUtils.showToast(e.getMessage());
            }
        }
        listener.onError(e);
    }

    @Override
    public void onComplete() {

    }
}
