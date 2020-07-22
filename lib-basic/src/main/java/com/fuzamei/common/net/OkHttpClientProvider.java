package com.fuzamei.common.net;

import com.fuzamei.commonlib.BuildConfig;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;

/**
 * Created by Mark on 2018/5/26.
 * Explain:
 */
public class OkHttpClientProvider {

    private static final long READ_TIME_OUT = 20_000;
    private static final long CONNECT_TIME_OUT = 20_000;

    public static OkHttpClient.Builder provideOkHttpClientBuilder() {
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        if (BuildConfig.DEBUG) {
            HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
            loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
            builder.addInterceptor(loggingInterceptor);
        }
        return builder.connectTimeout(CONNECT_TIME_OUT, TimeUnit.MILLISECONDS)
                .readTimeout(READ_TIME_OUT, TimeUnit.MILLISECONDS);
    }
}
