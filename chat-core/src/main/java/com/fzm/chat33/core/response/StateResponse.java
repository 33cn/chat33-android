package com.fzm.chat33.core.response;

import com.google.gson.annotations.SerializedName;

/**
 * @author zhengjy
 * @since 2019/01/08
 * Description:用于返回结构体只有一个int数据表示状态的接口
 */
public class StateResponse extends BaseResponse {
    @SerializedName(value = "state", alternate = {"failsNumber", "IsSetPayPwd"})
    public int state;
}
