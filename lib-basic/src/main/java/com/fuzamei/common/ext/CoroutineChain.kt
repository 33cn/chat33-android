package com.fuzamei.common.ext

import android.util.Log
import com.fuzamei.common.net.Result
import com.fuzamei.common.net.rxjava.ApiException
import com.fuzamei.common.utils.ShowUtils
import com.google.gson.JsonSyntaxException
import io.reactivex.exceptions.CompositeException
import kotlinx.coroutines.*
import retrofit2.HttpException
import java.io.IOException
import java.lang.ClassCastException
import java.net.MalformedURLException
import java.security.cert.CertificateException
import javax.net.ssl.SSLHandshakeException

/**
 * @author zhengjy
 * @since 2019/08/09
 * Description:
 */
/**
 * 请求开始前在主线程执行一些操作
 *
 * @param start 开始时执行的操作
 */
fun CoroutineScope.start(start: (() -> Unit)): CoroutineScope {
    start()
    return this
}

/**
 * 在IO线程执行耗时请求
 *
 * @param loader            耗时操作
 */
fun <T> CoroutineScope.request(loader: suspend () -> T): Pair<CoroutineScope, Deferred<T>> {
    val result = async(Dispatchers.IO) { loader() }
    return Pair(this, result)
}

/**
 * 请求返回后主线程回调
 *
 * @param onSuccess     callback for onSuccess
 * @param onError       callback for onError
 * @param onComplete    callback for onComplete
 */
fun <T> Pair<CoroutineScope, Deferred<Result<T>>>.result(onSuccess: (T) -> Unit, onError: ((ApiException) -> Unit)? = null, onComplete: (() -> Unit)? = null): Job {
    return first.launch(Dispatchers.Main) {
        try {
            val result = this@result.second.await()
            if (result.isSucceed()) {
                onSuccess(result.data())
            } else {
                processError(onError, result.error())
            }
        } catch (e: Exception) {
            if (e is CancellationException) {
                Log.e("CoroutineChain", "Exception: ${e.message}")
            } else {
                processError(onError, handleException(e))
            }
        } finally {
            onComplete?.invoke()
        }
    }
}

fun <T> Pair<CoroutineScope, Deferred<Result<T>>>.result(onSuccess: (T) -> Unit): Job {
    return result(onSuccess, null, null)
}

/**
 * 获取原始的ChatResult<T>结果
 *
 * @param onResult      callback for onResult
 * @param onComplete    callback for onComplete
 */
fun <T> Pair<CoroutineScope, Deferred<Result<T>>>.rawResult(onResult: (Result<T>) -> Unit, onComplete: (() -> Unit)? = null): Job {
    return first.launch(Dispatchers.Main) {
        try {
            onResult(this@rawResult.second.await())
        } catch (e: Exception) {
            if (e is CancellationException) {
                Log.e("CoroutineChain", "Exception: ${e.message}")
            } else {
                onResult(Result.Error(handleException(e)))
            }
        } finally {
            onComplete?.invoke()
        }
    }
}

fun <T> Pair<CoroutineScope, Deferred<Result<T>>>.rawResult(onResult: (Result<T>) -> Unit): Job {
    return rawResult(onResult, null)
}

private fun processError(onError: ((ApiException) -> Unit)? = null, e: ApiException) {
    // 临时加入判断条件
    if (e.errorCode != -1              // 忽略错误
            && e.errorCode != -4001    // 红包已领完
            && e.errorCode != -4009    // 红包已领取
            && e.errorCode != -4013    // 红包已过期
            && e.errorCode != -2030    // 帐号被管理员封禁
    ) {
        ShowUtils.showToast(e.message)
    }
    onError?.invoke(e)
}

fun handleException(t: Exception?): ApiException {
    return if (t == null) {
        ApiException(0)
    } else if (t is CertificateException || t is SSLHandshakeException) {
        ApiException(1)
    } else if (t is MalformedURLException) {
        ApiException(2)
    } else if (t is HttpException) {
        ApiException(3)
    } else if (t is JsonSyntaxException) {
        ApiException(5)
    } else if (t is IOException || t is CompositeException) {
        ApiException(6)
    } else if (t is ClassCastException) {
        ApiException(8)
    } else if (t is ApiException) {
        t
    } else {
        ApiException(t)
    }
}