package com.fuzamei.common.net;

import com.fuzamei.common.retrofiturlmanager.RetrofitUrlManager;
import com.google.gson.GsonBuilder;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * @author Mark
 * @since 2018/8/28
 * Description:
 */
public abstract class AbstractRetrofitProvider {

    protected Retrofit retrofit;
    protected RetrofitUrlManager urlManager;
    protected OkHttpClient okHttpClient;

    public AbstractRetrofitProvider() {
        urlManager = RetrofitUrlManager.getUrlManager();
        okHttpClient =
                OkHttpClientProvider.provideOkHttpClientBuilder().build();
        this.retrofit = new Retrofit.Builder()
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create(getGsonBuilder().create()))//请求的结果转为实体类
                //适配RxJava2.0,RxJava1.x则为RxJavaCallAdapterFactory.create()
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .baseUrl(baseUrl())
                .build();

    }

    protected abstract String baseUrl();

    protected GsonBuilder getGsonBuilder() {
        return new GsonBuilder();
    }

}
