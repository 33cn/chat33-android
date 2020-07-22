package com.fzm.chat33.core.db.bean;

import androidx.room.Ignore;
import android.text.TextUtils;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * @author zhengjy
 * @since 2018/10/18
 * Description:
 */
public class SenderInfo implements Serializable {
    @Ignore
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

    @Ignore
    @Expose
    public String position;

    public String getDisplayName() {
        return !TextUtils.isEmpty(remark) ? remark : nickname;
    }
}
