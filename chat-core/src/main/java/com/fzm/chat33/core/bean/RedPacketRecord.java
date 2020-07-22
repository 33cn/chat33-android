package com.fzm.chat33.core.bean;

import com.fzm.chat33.core.response.RedPacketInfoResponse;

import java.io.Serializable;
import java.util.List;

/**
 * @author zhengjy
 * @since 2019/03/18
 * Description:
 */
public class RedPacketRecord implements Serializable {

    /**
     * 红包数
     */
    public int count;
    /**
     * 总额
     */
    public double sum;
    /**
     * 币种代号
     */
    public int coinId;
    /**
     * 币种名称
     */
    public String coinName;
    /**
     * 红包信息
     */
    public List<RedPacketInfoResponse> redPackets;
}
