package com.fzm.chat33.core.bean.param;

import com.fzm.chat33.core.bean.RemarkPhone;

import java.io.Serializable;
import java.util.List;

/**
 * @author zhengjy
 * @since 2019/01/04
 * Description:修改好友详细备注请求参数
 */
public class EditExtRemarkParam implements Serializable {

    /**
     * 好友id
     */
    public String id;
    /**
     * 备注
     */
    public String remark;
    /**
     * 联系号码
     */
    public List<RemarkPhone> telephones;
    /**
     * 描述
     */
    public String description;
    /**
     * 图片
     */
    public List<String> pictures;
    public String encrypt;

    public EditExtRemarkParam() {

    }

    public EditExtRemarkParam(String id, String remark, List<RemarkPhone> telephones, String description, List<String> pictures) {
        this.id = id;
        this.remark = remark;
        this.telephones = telephones;
        this.description = description;
        this.pictures = pictures;
    }
}
