package com.fzm.chat33.core.bean.comparator;

import com.fzm.chat33.core.db.bean.BriefChatLog;

import java.util.Comparator;

/**
 * @author zhengjy
 * @since 2019/01/07
 * Description:
 */
public class DateComparator implements Comparator<BriefChatLog> {

    @Override
    public int compare(BriefChatLog o1, BriefChatLog o2) {
        if (o1.datetime > o2.datetime) {
            return 1;
        } else if (o1.datetime < o2.datetime) {
            return -1;
        } else {
            return 0;
        }
    }
}
