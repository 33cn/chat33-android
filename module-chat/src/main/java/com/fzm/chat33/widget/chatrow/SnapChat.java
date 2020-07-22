package com.fzm.chat33.widget.chatrow;

/**
 * @author zhengjy
 * @since 2018/12/13
 * Description:阅后即焚消息
 */
interface SnapChat {

    /**
     * 隐藏消息内容
     */
    void hideContent();

    /**
     * 展示消息内容
     */
    void showContent();

    /**
     * 开始倒计时
     */
    void startCount();

    /**
     * 销毁消息内容
     */
    void destroyContent(Object object);

    /**
     * 计算显示截至时间
     */
    long calculateRemainTime();
}
