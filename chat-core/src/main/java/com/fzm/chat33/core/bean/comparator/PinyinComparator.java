package com.fzm.chat33.core.bean.comparator;

import com.fzm.chat33.core.db.bean.Sortable;

import java.util.Comparator;

public class PinyinComparator implements Comparator<Sortable> {

    public int compare(Sortable o1, Sortable o2) {
        if (o1.priority() == o2.priority()) {
            if (o2.getFirstLetter().equals("#")) {
                return -1;
            } else if (o1.getFirstLetter().equals("#")) {
                return 1;
            } else {
                if (o1.getFirstLetter().equals(o2.getFirstLetter())) {
                    if (o1.getFirstChar().codePointAt(0) == o2.getFirstChar().codePointAt(0)) {
                        String p1 = o1.getLetters();
                        String p2 = o2.getLetters();
                        for (int i = 0; ; i++) {
                            if (p1.length() <= i && p2.length() <= i) {
                                return 0;
                            }
                            if (p1.length() <= i) {
                                return -1;
                            }
                            if (p2.length() <= i) {
                                return 1;
                            }
                            if (p1.charAt(i) == p2.charAt(i)) {
                                continue;
                            }
                            return p1.substring(i, i + 1).compareTo(p2.substring(i, i + 1));
                        }
                    } else {
                        return o1.getFirstChar().codePointAt(0) - o2.getFirstChar().codePointAt(0);
                    }
                } else {
                    return o1.getFirstLetter().compareTo(o2.getFirstLetter());
                }
            }
        } else if (o1.priority() > o2.priority()) {
            return -1;
        } else {
            return 1;
        }
    }
}
