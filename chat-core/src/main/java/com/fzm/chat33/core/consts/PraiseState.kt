package com.fzm.chat33.core.consts

/**
 * @author zhengjy
 * @since 2019/11/21
 * Description:消息打赏状态
 */
object PraiseState {
    /**
     * 未点赞未打赏
     */
    const val NONE = 0
    /**
     * 点赞
     */
    const val LIKE = 1
    /**
     * 打赏
     */
    const val REWARD = 2
    /**
     * 点赞且打赏
     */
    const val LIKE_AND_REWARD = 3
}

/**
 * 消息打赏操作
 */
object PraiseAction {
    /**
     * 点赞
     */
    const val ACTION_LIKE = "like"
    /**
     * 取消点赞
     */
    @Deprecated("删除了取消点赞功能")
    const val ACTION_CANCEL_LIKE = "cancel_like"
    /**
     * 打赏
     */
    const val ACTION_REWARD = "reward"
}