package com.fzm.chat33.core.db.bean;

/**
 * @author zhengjy
 * @since 2018/11/05
 * Description:联系人接口，包含好友和群
 */
public interface Contact extends Sortable {

    /**
     * 获取联系人id
     */
    String getId();

    /**
     * 获取联系人头像
     */
    String getAvatar();

    /**
     * 获取联系人昵称
     */
    String getDisplayName();

    /**
     * 联系人类型：群组、好友
     */
    int channelType();

    /**
     * 联系人唯一key
     */
    String getKey();

    /**
     * 联系人托管账户地址（公钥）
     */
    String getDepositAddress();

    /**
     * 联系人认证信息（非实名认证）
     */
    String getIdentificationInfo();

    /**
     * 是否开启免打扰
     */
    boolean isNoDisturb();

    /**
     * 是否置顶
     */
    boolean isStickyTop();
}
