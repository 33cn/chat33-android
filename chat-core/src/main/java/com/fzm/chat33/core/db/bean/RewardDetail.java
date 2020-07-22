package com.fzm.chat33.core.db.bean;

import com.fzm.chat33.core.consts.PraiseState;
import com.fzm.chat33.core.response.BaseResponse;

/**
 * @author zhengjy
 * @since 2019/11/21
 * Description:
 */
public class RewardDetail extends BaseResponse {

    /**
     * 消息点赞人数
     */
    public int like;
    /**
     * 消息打赏人数
     */
    public int reward;
    /**
     * 消息点赞状态
     */
    private int state;

    public void like() {
        this.state |= PraiseState.LIKE;
    }

    public void cancelLike() {
        this.state &= ~PraiseState.LIKE;
    }

    public void reward() {
        this.state |= PraiseState.REWARD;
    }

    public int getState() {
        return state;
    }

    public int getPraiseNum() {
        return like + reward;
    }

    /**
     * 用于判断自己的消息是否被人打赏
     *
     */
    public boolean beRewarded() {
        return reward > 0;
    }
}
