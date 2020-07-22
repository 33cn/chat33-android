package com.fzm.chat33.core.db.bean;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * @author zhengjy
 * @since 2018/10/18
 * Description:
 */
public class ReceiverInfo implements Serializable {
    @Expose
    public String id;

    @Expose
    public String avatar;

    @Expose
    @SerializedName(value = "nickname", alternate = {"name"})
    public String nickname;

    @Expose
    public String remark;

    @Expose
    public String uid;

    @Expose
    public int userLevel;

    @Expose
    public String position;
}
