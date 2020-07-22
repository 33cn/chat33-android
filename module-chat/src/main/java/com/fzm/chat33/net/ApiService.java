package com.fzm.chat33.net;

import com.fuzamei.common.net.rxjava.HttpResult;
import com.fzm.chat33.bean.UpdateApkInfo;
import com.fzm.chat33.core.bean.InviteBean;

import java.util.Map;

import io.reactivex.Observable;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;

/**
 * @author zhengjy
 * @since 2018/10/17
 * Description:
 */
public interface ApiService {

    /**
     * 获取应用更新信息
     *
     */
    @POST("chat/version")
    Observable<HttpResult<UpdateApkInfo>> checkUpdate(@Body Map<String, Object> map);

    /**
     * 找币获取邀请码接口
     *
     */
    @Deprecated
    @GET("chat33/getInviteCode")
    Observable<HttpResult<InviteBean>> inviteCode();

}
