package com.fzm.chat33.net;

import com.fuzamei.common.net.AbstractRetrofitProvider;
import com.fuzamei.componentservice.config.AppConfig;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * @author zhengjy
 * @since 2018/11/15
 * Description:
 */
public class AppRetrofitProvider extends AbstractRetrofitProvider {

    private AppRetrofitProvider() {
        urlManager.putDomain(AppConfig.BASE_URL_NAME, AppConfig.CHAT_BASE_URL);
        Gson gson = new GsonBuilder()
                .setDateFormat("yyyy-MM-dd")
                .setLenient()
                .create();
        retrofit = retrofit.newBuilder()
                .client(
                        urlManager.with(((OkHttpClient) retrofit.callFactory()).newBuilder())
                                .addInterceptor(new AppInterceptor())
                                .connectTimeout(AppConfig.DEFAULT_TIMEOUT, TimeUnit.SECONDS)
                                .readTimeout(AppConfig.DEFAULT_TIMEOUT, TimeUnit.SECONDS)
                                .writeTimeout(AppConfig.DEFAULT_TIMEOUT, TimeUnit.SECONDS)
                                .build()
                )
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();

    }

    @Override
    protected String baseUrl() {
        return AppConfig.CHAT_BASE_URL;
    }

    public static Retrofit getRetrofit() {
        return AppRetrofitProviderHolder.appRetrofitProvider.retrofit;
    }

    private static class AppRetrofitProviderHolder {
        private static AppRetrofitProvider appRetrofitProvider = new AppRetrofitProvider();
    }
}
