package com.fzm.chat33.ait;

/**
 * @author zhengjy
 * @since 2019/08/19
 * Description:
 */
public interface AitTextChangeListener {

    void onTextAdd(String content, int start, int length);

    void onTextDelete(int start, int length);
}

