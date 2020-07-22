package com.fuzamei.common.net.rxjava;

import io.reactivex.functions.Function;


public class HttpResultFunc<T> implements Function<HttpResult<T>, T> {
    @Override
    public T apply(HttpResult<T> httpResult) {
        if (httpResult.getCode() != (httpResult.error != null ? 200 : 0)) {
            throw new ApiException(httpResult.getCode(), httpResult.getMessage());
        }
        return httpResult.getData();
    }
}
