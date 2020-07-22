package com.fuzamei.common.net.rxjava;

import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;

import java.util.concurrent.TimeUnit;

import io.reactivex.Flowable;
import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by ljn on 2018/3/30.
 * Explain
 */

public class RxJavaUtil {
    private static final long DEFAULT_INTERVAL = 2000L;
    private static final long DEFAULT_DELAY = 0L;

    public static <T> Observer<T> toSubscribe(Observable<T> o, Observer<T> s) {
        if(o != null && s != null) {
            return o.subscribeOn(Schedulers.io())
                    .unsubscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeWith(s);
        } else {
            return null;
        }
    }

    @Deprecated
    public static <T> Observer<T> toSubscribeInterval(Observable<T> o, Observer<T> s) {
        return toSubscribeInterval(o, s, DEFAULT_INTERVAL);
    }

    @Deprecated
    public static <T> Observer<T> toSubscribeInterval(final Observable<T> o, Observer<T> s, long interval) {
        if(o != null && s != null) {
            return Observable
                    .interval(0, interval, TimeUnit.MILLISECONDS)
                    .flatMap(new Function<Long, ObservableSource<T>>() {
                        @Override
                        public ObservableSource<T> apply(Long aLong) {
                            return o;
                        }
                    })
                    .subscribeOn(Schedulers.io())
                    .unsubscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeWith(s);
        } else {
            return null;
        }
    }

    public static <T> Subscriber<T> toSubscribe(Flowable<T> o, Subscriber<T> s) {
        if (o == null || s == null) {
            return null;
        }
        return o.subscribeOn(Schedulers.io())
                .unsubscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(s);
    }

    public static <T> Subscriber<T> toSubscribeInterval(Flowable<T> o, Subscriber<T> s) {
        return toSubscribeInterval(o, s, DEFAULT_DELAY, DEFAULT_INTERVAL);
    }

    public static <T> Subscriber<T> toSubscribeInterval(final Flowable<T> o, final Subscriber<T> s, long delay, long interval) {
        if (o == null || s == null) {
            return null;
        }
        return Flowable
                .interval(delay, interval, TimeUnit.MILLISECONDS)
                .onBackpressureDrop()
                .subscribeOn(Schedulers.io())
                .unsubscribeOn(Schedulers.io())
                .flatMap(new Function<Long, Publisher<T>>() {
                    @Override
                    public Publisher<T> apply(Long aLong) {
                        return o;
                    }
                })
                .timeout(15 * 1000, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(s);
    }
}
