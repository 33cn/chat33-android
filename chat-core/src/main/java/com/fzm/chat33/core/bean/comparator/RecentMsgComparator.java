package com.fzm.chat33.core.bean.comparator;

import com.fzm.chat33.core.db.bean.RecentMessageBean;

import java.util.Comparator;

/**
 * @author zhengjy
 * @since 2018/10/31
 * Description:最近消息列表排序
 */
public class RecentMsgComparator implements Comparator<RecentMessageBean> {

    @Override
    public int compare(RecentMessageBean o1, RecentMessageBean o2) {
        int sticky1 = o1.isDeleted() || o1.getStickyTop() == 0 ? 2 : o1.getStickyTop();
        int sticky2 = o2.isDeleted() || o2.getStickyTop() == 0 ? 2 : o2.getStickyTop();
        // 根据置顶排序
        if (sticky1 < sticky2) {
            return -1;
        } else if (sticky1 > sticky2) {
            return 1;
        } else {
            // 根据最后消息时间排序
            if (o1.getDatetime() > o2.getDatetime()) {
                return -1;
            } else if (o1.getDatetime() < o2.getDatetime()) {
                return 1;
            } else {
                return 0;
            }
        }
    }
}
