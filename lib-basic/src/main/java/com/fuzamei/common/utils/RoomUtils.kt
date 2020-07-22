package com.fuzamei.common.utils

import android.annotation.SuppressLint

import com.fuzamei.common.executor.AppExecutors

import io.reactivex.Flowable
import io.reactivex.Maybe
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.functions.Consumer
import io.reactivex.schedulers.Schedulers

/**
 * @author zhengjy
 * @since 2018/10/27
 * Description:
 */
@SuppressLint("CheckResult")
class RoomUtils {

    companion object {

        @JvmStatic
        fun run(runnable: Runnable): Disposable? {
            return try {
                Schedulers.from(AppExecutors.databaseThreadPool()).createWorker().schedule(runnable)
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }

        @JvmStatic
        fun <T> subscribe(maybe: Maybe<T>, onSuccess: Consumer<T>) {
            maybe.subscribeOn(Schedulers.from(AppExecutors.databaseThreadPool()))
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(onSuccess, Consumer { })
        }

        @JvmStatic
        fun <T> subscribe(maybe: Maybe<T>, onSuccess: Consumer<T>, onError: Consumer<Throwable>) {
            maybe.subscribeOn(Schedulers.from(AppExecutors.databaseThreadPool()))
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(onSuccess, onError)
        }

        @JvmStatic
        fun <T> subscribe(flowable: Flowable<T>, onSuccess: Consumer<T>): Disposable {
            return flowable.subscribeOn(Schedulers.from(AppExecutors.databaseThreadPool()))
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(onSuccess, Consumer { })
        }

        @JvmStatic
        fun <T> subscribe(flowable: Flowable<T>, onSuccess: Consumer<T>, onError: Consumer<Throwable>): Disposable {
            return flowable.subscribeOn(Schedulers.from(AppExecutors.databaseThreadPool()))
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(onSuccess, onError)
        }

        @JvmStatic
        fun <T> subscribeSync(flowable: Flowable<T>, onSuccess: Consumer<T>): Disposable {
            return flowable.subscribe(onSuccess, Consumer { })
        }

        @JvmStatic
        fun <T> subscribe(single: Single<T>, onSuccess: Consumer<T>) {
            single.subscribeOn(Schedulers.from(AppExecutors.databaseThreadPool()))
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(onSuccess, Consumer { })
        }

        @JvmStatic
        fun <T> subscribe(single: Single<T>, onSuccess: Consumer<T>, onError: Consumer<Throwable>) {
            single.subscribeOn(Schedulers.from(AppExecutors.databaseThreadPool()))
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(onSuccess, onError)
        }
    }
}

@SuppressLint("CheckResult")
fun <T> Maybe<T>.run(onSuccess: Consumer<T>) {
    subscribeOn(Schedulers.from(AppExecutors.databaseThreadPool()))
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(onSuccess, Consumer { })
}

@SuppressLint("CheckResult")
fun <T> Maybe<T>.run(onSuccess: Consumer<T>, onError: Consumer<Throwable>) {
    subscribeOn(Schedulers.from(AppExecutors.databaseThreadPool()))
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(onSuccess, onError)
}

@SuppressLint("CheckResult")
fun <T> Flowable<T>.run(onSuccess: Consumer<T>): Disposable {
    return subscribeOn(Schedulers.from(AppExecutors.databaseThreadPool()))
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(onSuccess, Consumer { })
}

@SuppressLint("CheckResult")
fun <T> Flowable<T>.run(onSuccess: Consumer<T>, onError: Consumer<Throwable>): Disposable {
    return subscribeOn(Schedulers.from(AppExecutors.databaseThreadPool()))
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(onSuccess, onError)
}

@SuppressLint("CheckResult")
fun <T> Single<T>.run(onSuccess: Consumer<T>) {
    subscribeOn(Schedulers.from(AppExecutors.databaseThreadPool()))
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(onSuccess, Consumer { })
}

@SuppressLint("CheckResult")
fun <T> Single<T>.run(onSuccess: Consumer<T>, onError: Consumer<Throwable>) {
    subscribeOn(Schedulers.from(AppExecutors.databaseThreadPool()))
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(onSuccess, onError)
}