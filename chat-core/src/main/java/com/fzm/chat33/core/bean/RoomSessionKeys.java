package com.fzm.chat33.core.bean;

import com.fzm.chat33.core.response.MsgSocketResponse;

import java.io.Serializable;
import java.util.List;

/**
 * @author zhengjy
 * @since 2019/06/04
 * Description:
 */
public class RoomSessionKeys implements Serializable {

    public List<MsgSocketResponse> logs;
}
