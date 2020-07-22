package com.fuzamei.componentservice.ext

import com.fuzamei.common.net.ContractResponse
import com.fuzamei.common.net.Result
import com.fuzamei.common.net.rxjava.ApiException
import com.fuzamei.common.net.rxjava.HttpResponse
import com.fuzamei.common.net.rxjava.HttpResult
import com.google.gson.JsonSyntaxException
import io.reactivex.exceptions.CompositeException
import retrofit2.HttpException
import java.io.IOException
import java.lang.ClassCastException
import java.net.MalformedURLException
import java.security.cert.CertificateException
import javax.net.ssl.SSLHandshakeException

/**
 * @author zhengjy
 * @since 2019/11/04
 * Description:网络请求相关的扩展函数
 */
suspend fun <T> apiCall(call: suspend () -> HttpResult<T>): Result<T> {
    return try {
        call().let {
            if (it.code == (if (it.error != null) 200 else 0)) {
                Result.Success(it.data)
            } else {
                Result.Error(handleException(ApiException(it.code, it.message)))
            }
        }
    } catch (e: Exception) {
        Result.Error(handleException(e))
    }
}

suspend fun <T> apiCall2(call: suspend () -> HttpResponse<T>): Result<T> {
    return try {
        call().let {
            if (it.code == 200) {
                Result.Success(it.data)
            } else {
                Result.Error(handleException(ApiException(it.code, it.message)))
            }
        }
    } catch (e: Exception) {
        Result.Error(handleException(e))
    }
}

suspend fun <T> apiCall3(call: suspend () -> ContractResponse<T>): Result<T> {
    return try {
        call().let {
            if (it.isSuccess()) {
                Result.Success(it.result)
            } else {
                Result.Error(handleException(ApiException(it.error)))
            }
        }
    } catch (e: Exception) {
        Result.Error(handleException(e))
    }
}

private fun handleException(t: Exception?): ApiException {
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