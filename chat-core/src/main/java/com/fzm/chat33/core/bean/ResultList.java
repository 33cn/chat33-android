package com.fzm.chat33.core.bean;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;

/**
 * @author zhengjy
 * @since 2019/07/11
 * Description:返回结果String数组
 */
public class ResultList implements Serializable {

    @SerializedName(value = "rooms")
    public List<String> list;
}
