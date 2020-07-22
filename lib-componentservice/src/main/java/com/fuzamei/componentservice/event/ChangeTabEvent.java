package com.fuzamei.componentservice.event;

import java.io.Serializable;

/**
 * @author zhengjy
 * @since 2018/11/06
 * Description:切换主页面的tab
 */
public class ChangeTabEvent implements Serializable {

    // 一级tab，底部
    public int tab;
    // 二级tab，顶部
    public int subTab;

    public ChangeTabEvent(int tab, int subTab) {
        this.tab = tab;
        this.subTab = subTab;
    }
}
