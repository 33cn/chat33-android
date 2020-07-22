package com.fuzamei.common.net.rxjava;

import io.reactivex.functions.Function;


public class HttpResponseFunc<T> implements Function<HttpResponse<T>, T> {

    private int normalCode;

    public HttpResponseFunc() {
        this(0);
    }

    public HttpResponseFunc(int normalCode) {
        this.normalCode = normalCode;
    }

    @Override
    public T apply(HttpResponse<T> httpResponse) {
        if (httpResponse.getCode() != normalCode) {
            throw new ApiException(httpResponse.getCode(), httpResponse.getMessage());
        }
        return httpResponse.getData();
    }
}
