package com.fzm.chat33.core.bean.param;

import java.io.Serializable;
import java.util.List;

/**
 * @author zhengjy
 * @since 2018/10/24
 * Description:
 */
public class CreateGroupParam implements Serializable {

    public String roomName;
    public String roomAvatar;
    public List<String> users;
    public int encrypt;

    public CreateGroupParam(String roomName, List<String> users, int encrypt) {
        this.roomName = roomName;
        this.users = users;
        this.encrypt = encrypt;
    }

    public CreateGroupParam(String roomName, String roomAvatar, List<String> users) {
        this.roomName = roomName;
        this.roomAvatar = roomAvatar;
        this.users = users;
    }
}
