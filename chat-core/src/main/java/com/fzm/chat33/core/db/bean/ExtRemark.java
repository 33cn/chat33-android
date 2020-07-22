package com.fzm.chat33.core.db.bean;

import com.fzm.chat33.core.bean.RemarkPhone;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;

/**
 * @author zhengjy
 * @since 2019/02/14
 * Description:好友信息中的额外备注信息
 */
public class ExtRemark implements Serializable {

    @SerializedName("telephones")
    public List<RemarkPhone> phones;
    @SerializedName("pictures")
    public List<String> images;
    public String description;
    public String encrypt;
}
