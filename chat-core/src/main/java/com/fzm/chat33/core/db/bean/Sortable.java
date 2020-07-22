package com.fzm.chat33.core.db.bean;

public interface Sortable {

    /**
     * 获取排序用的首字，如徐，王
     *
     * @return
     */
    String getFirstChar();

    /**
     * 获取排序用的首字母，如徐(X)，王(W)
     *
     * @return
     */
    String getFirstLetter();

    /**
     * 获取全拼，如张三(ZHANGSAN)
     *
     * @return
     */
    String getLetters();

    /**
     * 优先级，优先级高的直接排在最前面
     *
     * @return
     */
    int priority();
}
