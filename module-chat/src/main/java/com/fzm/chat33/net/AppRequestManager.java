package com.fzm.chat33.net;

import com.fuzamei.common.net.rxjava.HttpResult;
import com.fzm.chat33.bean.UpdateApkInfo;

import java.util.HashMap;
import java.util.Map;

import io.reactivex.Observable;

/**
 * @author zhengjy
 * @since 2018/10/17
 * Description:
 */
public enum AppRequestManager {

    /**
     * 单例
     */
    INS;

    static class Holder {
        private static ApiService apiService = AppRetrofitProvider.getRetrofit().create(ApiService.class);
    }

    private ApiService getAPIService() {
        return Holder.apiService;
    }

    public Observable<HttpResult<UpdateApkInfo>> checkUpdate(int version) {
        Map<String, Object> map = new HashMap<>();
        map.put("nowVersionCode", version);
        return getAPIService().checkUpdate(map);
    }
}
