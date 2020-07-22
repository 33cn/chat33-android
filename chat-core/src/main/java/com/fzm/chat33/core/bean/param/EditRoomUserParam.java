package com.fzm.chat33.core.bean.param;

import java.io.Serializable;
import java.util.List;

/**
 * @author zhengjy
 * @since 2018/10/26
 * Description:邀请入群获取踢出群 请求参数
 */
public class EditRoomUserParam implements Serializable {

    public String roomId;
    public List<String> users;

    public EditRoomUserParam(String roomId, List<String> users) {
        this.roomId = roomId;
        this.users = users;
    }
}
